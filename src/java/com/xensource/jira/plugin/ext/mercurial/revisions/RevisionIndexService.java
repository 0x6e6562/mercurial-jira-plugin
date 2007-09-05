/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 1, 2004
 * Time: 5:06:44 PM
 */
package com.xensource.jira.plugin.ext.mercurial.revisions;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionNames;
import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.ServiceManager;
import org.ofbiz.core.util.UtilMisc;
import webwork.dispatcher.ActionResult;

public class RevisionIndexService extends AbstractService
{
    public static final String REVISION_INDEX_SERVICE_NAME = "Mercurial Revision Indexing Service";
    public static final long REVISION_INDEX_SERVICE_DELAY = 60 * 60 * 1000L;

    public void run()
    {
        try
        {
            MercurialRepositoryManager mercurialRepositoryManager = getMercurialRepositoryManager();

            if (mercurialRepositoryManager.getRevisionIndexer() != null)
                mercurialRepositoryManager.getRevisionIndexer().updateIndex();
            else
                log.warn("Tried to index changes but MercurialManager has no revision indexer?");
        }
        catch (Throwable t)
        {
            log.error("Error indexing changes: " + t, t);
        }
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("HGREVISIONSERVICE", "services/plugins/mercurial/revisionindexservice.xml", null);
    }

    public static void install() throws Exception
    {
        if (getServiceManager().getServiceWithName(REVISION_INDEX_SERVICE_NAME) != null)
        {
            getServiceManager().addService(REVISION_INDEX_SERVICE_NAME,
                RevisionIndexService.class.getName(),
                REVISION_INDEX_SERVICE_DELAY);
        }
    }

    public static void remove() throws Exception
    {
        if (getServiceManager().getServiceWithName(REVISION_INDEX_SERVICE_NAME) != null)
        {
            getServiceManager().removeServiceByName(REVISION_INDEX_SERVICE_NAME);
        }
    }

    private static ServiceManager getServiceManager()
    {
        return (ServiceManager) ComponentManager.getInstance().getContainer().getComponentInstance(ServiceManager.class);
    }

    private MercurialRepositoryManager getMercurialRepositoryManager()
    {
        return (MercurialRepositoryManager) ComponentManager.getInstance().getContainer().getComponentInstance(MercurialRepositoryManager.class);
    }

    public boolean isUnique()
    {
        return true;
    }

    public boolean isInternal()
    {
        return true;
    }

    public String getDescription()
    {
        return "This service indexes Mercurial revisions.";
    }
}
