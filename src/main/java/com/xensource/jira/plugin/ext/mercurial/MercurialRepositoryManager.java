package com.xensource.jira.plugin.ext.mercurial;

import com.atlassian.jira.extension.Startable;
import com.xensource.jira.plugin.ext.mercurial.revisions.RevisionIndexer;

import java.util.Collection;

/**
 * Main component of the Mercurial plugin.
 */
public interface MercurialRepositoryManager extends Startable
{
    public static final String PLUGIN_ROOT_KEY = "hg.root";
    public static final String PLUGIN_REPOSITORY_NAME = "hg.display.name";

    public static final String PLUGIN_LINKFORMAT_CHANGESET = "linkformat.changeset";
    public static final String PLUGIN_LINKFORMAT_FILE_MODIFIED = "linkformat.file.modified";

    public static final String PLUGIN_REVISION_INDEXING_KEY = "revision.indexing";
    public static final String PLUGIN_REVISION_CACHE_SIZE_KEY = "revision.cache.size";
    public static final String PLUGIN_CLONEDIR_KEY = "hg.clonedir";
    public static final String PLUGIN_UPDATE_REPO_KEY = "hg.updaterepo";
    public static final String PLUGIN_REPODIR_KEY = "hg.repodir";
    public static final String PLUGIN_ROOTURL_KEY = "hg.repodir.url";

    boolean isIndexingRevisions();

    RevisionIndexer getRevisionIndexer();

    Collection getRepositoryList();

    MercurialManager getRepository(String repoUUID);
}
