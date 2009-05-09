package com.xensource.jira.plugin.ext.mercurial;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.xensource.jira.plugin.ext.mercurial.revisions.RevisionIndexService;
import com.xensource.jira.plugin.ext.mercurial.revisions.RevisionIndexer;
import com.atlassian.jira.project.version.VersionManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.File;

/**
 * This is a wrapper class for many MercurialManagers.
 * Configured via mercurial-jira-plugin.properties.
 *
 * @see MercurialManager
 * @author Dylan Etkin
 */
public class MercurialRepositoryManagerImpl implements MercurialRepositoryManager
{
    private static Logger log = Logger.getLogger(MercurialRepositoryManagerImpl.class);

    private Map repositoryMap;
    private RevisionIndexer revisionIndexer;

    public MercurialRepositoryManagerImpl(ApplicationProperties applicationProperties, VersionManager versionManager)
    {
        setupEnvironment(applicationProperties, versionManager);
    }

    private void setupEnvironment(ApplicationProperties applicationProperties, VersionManager versionManager)
    {
        Properties props = new Properties(System.getProperties());
        repositoryMap = new HashMap();

        try
        {
            // First, try loading from the properties file.
            props.load(ClassLoaderUtils.getResourceAsStream("mercurial-jira-plugin.properties", MercurialRepositoryManagerImpl.class));

            List properties = getPluginProperties(props);

            boolean anyRevisionIndexing = false;
            for (Iterator it = properties.iterator(); it.hasNext();)
            {
                MercurialProperties property = (MercurialProperties) it.next();
                try
                {
                    MercurialManager mercurialInstance = new MercurialManagerImpl(applicationProperties, property);
		    if (mercurialInstance == null) {
			log.error("Failed to construct a MercurialManager for the repository:" + property);
		    }
                    repositoryMap.put(mercurialInstance.getRepository().getRepositoryUUID(), mercurialInstance);

                    // Now setup revision indexing if they want it
                    if (property.revisionIndexing != null && property.revisionIndexing.booleanValue())
                    {
                        anyRevisionIndexing = true;
                    }
                }
                catch (InfrastructureException inf)
                {
		    log.error("Error initializing a repository: " +  inf);
                }

            }

            if (!anyRevisionIndexing) // they might have removed the property - let's check there is no service anyway
            {
                RevisionIndexService.remove();
            }
            else
            {
                // create revision indexer once we know we have succeed initializing our repositories
                revisionIndexer = new RevisionIndexer(this, applicationProperties, versionManager);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            log.error("Could not load properties from mercurial-jira-plugin.properties", t);
            throw new InfrastructureException("Could not load properties from mercurial-jira-plugin.properties", t);
        }
    }

    public boolean isIndexingRevisions()
    {
        return revisionIndexer != null;
    }

    public RevisionIndexer getRevisionIndexer()
    {
        return revisionIndexer;
    }

    public Collection getRepositoryList()
    {
        return repositoryMap.values();
    }

    public MercurialManager getRepository(String repoUUID)
    {
        MercurialManager subManager = (MercurialManager) repositoryMap.get(repoUUID);
        if (subManager != null)
        {
            return subManager;
        }
        else
        {
            return null;
        }
    }

    private List getPluginProperties(Properties allProps) throws InfrastructureException
    {
        List propertyList = new ArrayList();
        MercurialProperties prop = null;
        int i = 1;
        do
        {
            prop = getPluginProperty(i, allProps);
            i++;
            if (prop != null)
            {
		log.info("Added property: " + prop);
                propertyList.add(prop);
            }
        }
        while (prop != null);

	propertyList = getRepoDirs(propertyList, allProps);

        return propertyList;
    }

    /**
     * Add all the repositories in the named directories.
     */
    private List getRepoDirs(List propertyList, Properties props)
    {
        int i = 1;
        do
        {
	    String indexStr = "." + Integer.toString(i);
	    i++;
	    if (props.containsKey(MercurialRepositoryManager.PLUGIN_REPODIR_KEY + indexStr))
	    {
		String repoDir = props.getProperty(MercurialRepositoryManager.PLUGIN_REPODIR_KEY + indexStr);
		String rootUrl = props.getProperty(MercurialRepositoryManager.PLUGIN_ROOTURL_KEY + indexStr);
		Boolean updateRepo = false;
		if (props.containsKey(MercurialRepositoryManager.PLUGIN_UPDATE_REPO_KEY + indexStr))
		{
		    updateRepo = new Boolean("true".equalsIgnoreCase(props.getProperty(MercurialRepositoryManager.PLUGIN_UPDATE_REPO_KEY + indexStr)));
		}
		log.debug("Scanning " + repoDir + " for hg repositories");

		// Find all the repositories in the given repodir
		File dir = new File(repoDir);
		String[] repositories = dir.list();
		if (repositories == null) {
		    log.error("Either " + repoDir + " does not exist or is not a directory");
		    continue;
		} else {
		    for (int j=0; j < repositories.length; j++) {
			String repoName = repositories[j];
			// TODO reject files that aren't repositories
			String rootStr = rootUrl + "/" + repoName;
			String displayName = repoName;
			// Make the displayed name more informational
			int lastIndex = repoDir.lastIndexOf('/');
			if (lastIndex != -1) {
			    displayName = repoDir.substring(lastIndex+1, repoDir.length()) + "/" + displayName;
			}
			Boolean revisionIndexing = new Boolean("true");
			Integer revisionCacheSize = 10000;
			String cloneDir = repoDir;
			
			String changesetFormat = rootStr + "?cs=${rev}";
			String fileModifiedFormat = rootStr + "?fd=${rev};file=${path}";
			ViewLinkFormat viewLinkFormat = new ViewLinkFormat(changesetFormat, null, fileModifiedFormat, null, null, null, null);
			
			MercurialProperties prop = new MercurialProperties(rootStr, displayName, null, null, viewLinkFormat, revisionIndexing, revisionCacheSize, cloneDir, updateRepo);
			log.info("Added repository: " + prop);
			propertyList.add(prop);
		    }
		}
	    } else {
		break;
	    }
        }
        while (true);

	return propertyList;
    }

    private MercurialProperties getPluginProperty(int index, Properties props)
    {
        String indexStr = "." + Integer.toString(index);

        if (props.containsKey(MercurialRepositoryManager.PLUGIN_ROOT_KEY + indexStr))
        {
            String rootStr = props.getProperty(MercurialRepositoryManager.PLUGIN_ROOT_KEY + indexStr);
            String displayName = props.getProperty(MercurialRepositoryManager.PLUGIN_REPOSITORY_NAME + indexStr);
            String cloneDir = props.getProperty(MercurialRepositoryManager.PLUGIN_CLONEDIR_KEY + indexStr);
            
            String changesetFormat = props.getProperty(MercurialRepositoryManager.PLUGIN_LINKFORMAT_CHANGESET + indexStr);
            String fileModifiedFormat = props.getProperty(MercurialRepositoryManager.PLUGIN_LINKFORMAT_FILE_MODIFIED + indexStr);

            Boolean revisionIndexing = null;
            if (props.containsKey(MercurialRepositoryManager.PLUGIN_REVISION_INDEXING_KEY))
            {
                revisionIndexing = new Boolean("true".equalsIgnoreCase(props.getProperty(MercurialRepositoryManager.PLUGIN_REVISION_INDEXING_KEY)));
            }
            Integer revisionCacheSize = null;
            if (props.containsKey(MercurialRepositoryManager.PLUGIN_REVISION_CACHE_SIZE_KEY))
            {
                revisionCacheSize = new Integer(props.getProperty(MercurialRepositoryManager.PLUGIN_REVISION_CACHE_SIZE_KEY));
            }
            
            ViewLinkFormat viewLinkFormat = new ViewLinkFormat(changesetFormat, null, fileModifiedFormat, null, null, null, null);
            
            Boolean updateRepo = false;
            if (props.containsKey(MercurialRepositoryManager.PLUGIN_UPDATE_REPO_KEY + indexStr))
            {
                updateRepo = new Boolean("true".equalsIgnoreCase(props.getProperty(MercurialRepositoryManager.PLUGIN_UPDATE_REPO_KEY + indexStr)));
            }

            MercurialProperties prop =  new MercurialProperties(rootStr, displayName, null, null, viewLinkFormat, revisionIndexing, revisionCacheSize, cloneDir, updateRepo);
            log.debug("XXX: Added repository: " + prop);
            return prop;
        }
        else
        {
            log.debug("As expected, no " + MercurialRepositoryManager.PLUGIN_ROOT_KEY + indexStr + " specified in mercurial-jira-plugin.properties");
            return null;
        }
    }

    public void start() throws Exception
    {
        if (isIndexingRevisions())
        {
            getRevisionIndexer().start();
        }
    }
}
