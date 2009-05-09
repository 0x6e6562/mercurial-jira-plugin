/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 30, 2004
 * Time: 8:13:56 AM
 */
package com.xensource.jira.plugin.ext.mercurial;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.xensource.jira.plugin.ext.mercurial.linkrenderer.NullLinkRenderer;
import com.xensource.jira.plugin.ext.mercurial.linkrenderer.MercurialLinkRenderer;
import com.xensource.jira.plugin.ext.mercurial.linkrenderer.LinkFormatRenderer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import com.xensource.hg.core.io.*;

import java.util.Map;

public class MercurialManagerImpl implements MercurialManager {
    private static Logger log = Logger.getLogger(MercurialManagerImpl.class);

    private final ApplicationProperties applicationProperties;
    private MercurialLinkRenderer linkRenderer;
    private String webLink;
    private ViewLinkFormat viewLinkFormat;
    private String displayName;
    private String cloneDir;
    private Boolean updateRepo;
    private Map logEntryCache;
    private HGRepository repository;

    public MercurialManagerImpl(ApplicationProperties applicationProperties, MercurialProperties props) {
        this.applicationProperties = applicationProperties;
        setupEnvironment(props);
    }

    private void setupEnvironment(MercurialProperties props) {

        displayName = props.displayName;
        cloneDir = props.cloneDir;
        updateRepo = props.updateRepo;

        // Now setup web link renderer
        linkRenderer = null;

        if (props.viewLinkFormat != null) {
            viewLinkFormat = props.viewLinkFormat;

            linkRenderer = new LinkFormatRenderer(this);
        }
        if (linkRenderer == null) {
            linkRenderer = new NullLinkRenderer();
        }

        // Now setup revision indexing if they want it
        if (props.revisionIndexing != null && props.revisionIndexing.booleanValue()) {
            // Setup the log message cache
            int cacheSize = 10000;

            if (props.revisioningCacheSize != null) {
                cacheSize = props.revisioningCacheSize.intValue();
            }

            logEntryCache = new LRUMap(cacheSize);

            try {
                HGRepositoryLocation location = HGRepositoryLocation.parseURL(props.root);


                repository = new HGRepository(props.executable, location, cloneDir, updateRepo);
                repository.testConnection();
            }
            catch (HGException e) {
                log.error("Connection to Mercurial repository " + props.root + " failed: " + e, e);
                throw new InfrastructureException("Connection to Mercurial repository " + props.root + " failed.", e);
            }
        }
    }

    public HGRepository getRepository() {
        return repository;
    }

    public String getDisplayName() {
        return (displayName == null) ? repository.getLocation().toString() : displayName;
    }

    /**
     * Make sure a single log message is cached.
     */
    private void ensureCached(HGLogEntry logEntry) {
        synchronized (logEntryCache) {
            logEntryCache.put(new Long(logEntry.getShortRevision()), logEntry);
        }
    }

    public HGLogEntry getLogEntry(long revision) {
        final HGLogEntry[] logEntry = new HGLogEntry[]{(HGLogEntry) logEntryCache.get(new Long(revision))};

        if (logEntry[0] == null) {
            try {
                if (log.isDebugEnabled())
                    log.debug("No cache - retrieving log message from " + getDisplayName() + " for revision: " + revision);

                // TODO This is a total hack and is necessary because there is a worse
                // hack in the log method of HgRepository that ignores revision number 1
                // Anyway I think all of this revision numbers is bullshit, for hg you
                // should probably use the cyrptographic id, because that is what a changeset
                // is actually identified with
                final long startRevision = (revision == 1) ? 0 : revision;

                repository.log(new String[]{""}, startRevision, revision, true, true, new ISVNLogEntryHandler() {
                    public void handleLogEntry(HGLogEntry entry) {
                        logEntry[0] = entry;
                        ensureCached(entry);
                    }
                });
            }
            catch (HGException e) {
                log.error("Error retrieving logs: " + e, e);
                throw new InfrastructureException(e);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Found cached log message for revision: " + revision);
        }
        return logEntry[0];
    }

    public boolean isWebLinking() {
        return webLink != null;
    }

    public ViewLinkFormat getViewLinkFormat() {
        return viewLinkFormat;
    }

    public String getWebLink() {
        return webLink;
    }

    public MercurialLinkRenderer getLinkRenderer() {
        return linkRenderer;
    }
}
