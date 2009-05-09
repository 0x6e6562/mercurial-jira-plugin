/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 1, 2004
 * Time: 4:58:40 PM
 */
package com.xensource.jira.plugin.ext.mercurial.revisions;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.xensource.jira.plugin.ext.mercurial.MercurialManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.util.LuceneUtils;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.xensource.hg.core.io.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RevisionIndexer
{
    private static Logger log = Logger.getLogger(RevisionIndexer.class);

    private final MercurialRepositoryManager mercurialRepositoryManager;
    private final ApplicationProperties applicationProperties;
    private final VersionManager versionManager;
    private Hashtable latestIndexedRevisionTbl;
    private static final String REVISIONS_INDEX_DIRECTORY = "plugins" + System.getProperty("file.separator") + "atlassian-mercurial-revisions";
    private String indexPath = null;
    private static final String FIELD_REVISIONNUMBER = "revision";
    private static final Term START_REVISION = new Term(FIELD_REVISIONNUMBER, "");
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_ISSUEKEY = "key";
    private static final String FIELD_REPOSITORY = "repository";
    private static final String PATH_TO_SEGMENTS = System.getProperty("file.separator") + "segments";
    public static final StandardAnalyzer ANALYZER = new StandardAnalyzer();

    public RevisionIndexer(MercurialRepositoryManager mercurialRepositoryManager, ApplicationProperties applicationProperties, VersionManager versionManager)
    {
        this.mercurialRepositoryManager = mercurialRepositoryManager;
        this.applicationProperties = applicationProperties;
        this.versionManager = versionManager;
        initializeLatestIndexedRevisionCache();
    }

    public void start()
    {
        try
        {
	    log.debug("RevisionIndexer: start() entered");
            createIndexIfNeeded();
            RevisionIndexService.install(); // ensure the changes index service is installed
        }
        catch (Throwable t)
        {
            log.error("Could not load properties from mercurial-jira-plugin.properties", t);
            throw new InfrastructureException("Could not load properties from mercurial-jira-plugin.properties", t);
        }

    }

    /**
     * This method will scan for the index directory, if it can resolve the path and the directory does not
     * exist then it will create the index.
     *
     * @return true if the index exists, false otherwise
     */
    private boolean createIndexIfNeeded()
    {
	if (log.isDebugEnabled())
            log.debug("RevisionIndexer.createIndexIfNeeded()");

        boolean indexExists = indexDirectoryExists();
        if (getIndexPath() != null && !indexExists)
        {
            try
            {
                LuceneUtils.getIndexWriter(getIndexPath(), true, ANALYZER).close();
                initializeLatestIndexedRevisionCache();
                return true;
            }
            catch (Exception e)
            {
                log.error("Could not create the index directory for the mercurial plugin.", e);
                return false;
            }
        }
        else
        {
            return indexExists;
        }
    }

    private void initializeLatestIndexedRevisionCache()
    {
        Collection repositories = mercurialRepositoryManager.getRepositoryList();
        Iterator repoIter = repositories.iterator();
        final Long NOT_INDEXED = new Long(-1);
        latestIndexedRevisionTbl = new Hashtable();
        while (repoIter.hasNext())
        {
            HGRepository currentRepo = ((MercurialManager) repoIter.next()).getRepository();
            latestIndexedRevisionTbl.put(currentRepo.getRepositoryUUID(), NOT_INDEXED);
        }
        if (log.isDebugEnabled())
        {
            log.debug("Repository list size = " + repositories.size());
        }
    }

    private boolean indexDirectoryExists()
    {
        try
        {
            // check if the directory exists
            File file = new File(getIndexPath());

            return file.exists();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public String getIndexPath()
    {
        if (indexPath == null)
        {
            String rootIndexPath = applicationProperties.getString(APKeys.JIRA_PATH_INDEX);
            if (rootIndexPath != null)
            {
                indexPath = rootIndexPath + System.getProperty("file.separator") + REVISIONS_INDEX_DIRECTORY;
            }
            else
            {
                log.warn("At the moment the root index path of jira is not set, so we can not form an index path for the mercurial plugin.");
            }
        }

        return indexPath;
    }

    /**
     * This method updates the index, creating it if it does not already exist.
     */
    public void updateIndex() throws IndexException, IOException, HGException {
        if (createIndexIfNeeded()) {
            Collection repositories = mercurialRepositoryManager.getRepositoryList();
            Iterator repoIter = repositories.iterator();

            // temp log comment
            if (log.isDebugEnabled()) {
                log.debug("repos size = " + repositories.size());
            }

            while (repoIter.hasNext()) {
                HGRepository currentRepo = ((MercurialManager) repoIter.next()).getRepository();
                String repoId = currentRepo.getRepositoryUUID();
                long latestIndexedRevision = -1;

                if (latestIndexedRevisionTbl.get(repoId) != null) {
                    latestIndexedRevision = ((Long) latestIndexedRevisionTbl.get(repoId)).longValue();
                } else {
                    // no latestIndexedRevision, no need to update? This probably means
                    // that the repository have been removed from the file system
                    log.warn("Did not update index because null value in hash table for " + repoId);
                    continue;
                }

                log.info("Updating revision index for repository=" + repoId);

                if (latestIndexedRevision < 0)
                    latestIndexedRevision = updateLastRevisionIndexed(repoId);

                log.info("Latest indexed revision for repository=" + repoId + " is : " + latestIndexedRevision);

                long latestRevision = currentRepo.getLatestRevision();

                log.info("Latest revision in repository=" + repoId + "  is : " + latestRevision);

                if (latestRevision > 0 && latestRevision <= latestIndexedRevision) {
                    log.info("Have all the commits for repository=" + repoId + " - doing nothing.");
                    continue;
                }

                long retrieveStart = latestIndexedRevision + 1;
                if (retrieveStart < 0)
                    retrieveStart = 0;
                // Deal with empty repositories
                if (latestIndexedRevision == 0) {
                    retrieveStart = 0;
                }

                log.info("Retrieving revisions to index (between " + retrieveStart + " and " + latestRevision + ") for repository=" + repoId);

                final Collection logEntries = new ArrayList();

                currentRepo.log(new String[]{""}, retrieveStart, latestRevision, true, true, new ISVNLogEntryHandler() {
                    public void handleLogEntry(HGLogEntry logEntry) {
                        if (log.isDebugEnabled())
                            log.debug("Retrieved #" + logEntry.getShortRevision() + " : " + logEntry.getMessage());

                        if (isInteresting(logEntry)) {
                            logEntries.add(logEntry);
                        }
                    }
                });
                log.info("Retrieved " + logEntries.size() + " relevant revisions to index (between " + retrieveStart + " and " + latestRevision + ") from repository=" + repoId);

                IndexWriter writer = LuceneUtils.getIndexWriter(getIndexPath(), false, ANALYZER);

                try {

                    final IndexReader reader = LuceneUtils.getIndexReader(getIndexPath());

                    try {
                        for (Iterator iterator = logEntries.iterator(); iterator.hasNext();) {
                            HGLogEntry logEntry = (HGLogEntry) iterator.next();
                            // TODO This call to isInteresting looks redundantw
                            if (isInteresting(logEntry)) {
                                if (!hasDocument(repoId, logEntry.getShortRevision(), reader)) {
                                    Document doc = getDocument(repoId, logEntry);
                                    log.info("Indexing repository=" + repoId + ", revision: " + logEntry.getShortRevision());
                                    writer.addDocument(doc);
                                }
                            }
                        }
                    }
                    finally {
                        reader.close();
                    }
                    // The original plugin only recorded the last revision
                    // that contained an entry worth indexing. This leads to
                    // lots of repeated work.
                    latestIndexedRevision = latestRevision;
                    // update the in-memory cache SVN-71
                    latestIndexedRevisionTbl.put(repoId, new Long(latestRevision));
                    log.info("Finished indexing repository=" + repoId + ", latestIndexedRevision=" + latestIndexedRevision);
                }
                finally {
                    writer.close();
                }

            }  // while
        }
    }

    private boolean isInteresting(HGLogEntry logEntry) {
      if (TextUtils.stringSet(logEntry.getMessage()) && JiraKeyUtils.isKeyInString(logEntry.getMessage()))
        return true;

      if (TextUtils.stringSet(logEntry.getBranchName()) &&  JiraKeyUtils.isKeyInString(logEntry.getBranchName()))
        return true;

      return false;
    }

    /**
     * Work out whether a given change, for the specified repository, is already in the index or not.
     */
    private boolean hasDocument(String repoId, long revisionNumber, IndexReader reader) throws IOException
    {
        IndexSearcher searcher = new IndexSearcher(reader);
        try
        {
            TermQuery repoQuery = new TermQuery(new Term(FIELD_REPOSITORY, repoId));
            TermQuery revQuery = new TermQuery(new Term(FIELD_REVISIONNUMBER, Long.toString(revisionNumber)));
            BooleanQuery repoAndRevQuery = new BooleanQuery();

            final boolean REQUIRED = true;
            final boolean NOT_PROHIBITED = false;
            repoAndRevQuery.add(repoQuery, BooleanClause.Occur.MUST);
            repoAndRevQuery.add(revQuery, BooleanClause.Occur.MUST);

            Hits hits = searcher.search(repoAndRevQuery);

            if (hits.length() == 1)
            {
                return true;
            }
            else if (hits.length() == 0)
            {
                return false;
            }
            else
            {
                log.error("Found MORE than one document for revision: " + revisionNumber + ", repository=" + repoId);
                return true;
            }
        }
        finally
        {
            searcher.close();
        }
    }

    private long updateLastRevisionIndexed(String repoId) throws IndexException, IOException
    {
        if (log.isDebugEnabled())
            log.debug("Updating last revision indexed.");

        // find all log entries that have already been indexed for the specified repository
        // (i.e. all logs that have been associated with issues in JIRA)
        long latestIndexedRevision = ((Long) latestIndexedRevisionTbl.get(repoId)).longValue();

        final IndexReader reader = LuceneUtils.getIndexReader(getIndexPath());
        IndexSearcher searcher = new IndexSearcher(reader);

        try
        {
            Hits hits = searcher.search(new TermQuery(new Term(FIELD_REPOSITORY, repoId)));
            List logEntries = new ArrayList(hits.length());

            for (int i = 0; i < hits.length(); i++)
            {
                Document doc = hits.doc(i);
                final long revision = Long.parseLong(doc.get(FIELD_REVISIONNUMBER));
                if (revision > latestIndexedRevision)
                    latestIndexedRevision = revision;

            }
            log.debug("latestIndRev for " + repoId + " = " + latestIndexedRevision);
            latestIndexedRevisionTbl.put(repoId, new Long(latestIndexedRevision));
        }
        finally
        {
            reader.close();
        }

        return latestIndexedRevision;
    }

    private Document getDocument(String repoId, HGLogEntry logEntry)
    {
        Document doc = new Document();

        // revision information
        doc.add(new Field(FIELD_MESSAGE, logEntry.getMessage(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        if (logEntry.getAuthor() != null)
            doc.add(new Field(FIELD_AUTHOR, logEntry.getAuthor(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        doc.add(new Field(FIELD_REPOSITORY, repoId, Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_REVISIONNUMBER, Long.toString(logEntry.getShortRevision()), Field.Store.YES, Field.Index.UN_TOKENIZED));

        if (logEntry.getDate() != null)
            doc.add(new Field(FIELD_DATE, logEntry.getDate().toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        // relevant issue keys
        List logEntryKeys = JiraKeyUtils.getIssueKeysFromString(logEntry.getMessage());
        List branchKeys = JiraKeyUtils.getIssueKeysFromString(logEntry.getBranchName());

        for (Iterator iterator = logEntryKeys.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            doc.add(new Field(FIELD_ISSUEKEY, key, Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
        for (Iterator iterator = branchKeys.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            doc.add(new Field(FIELD_ISSUEKEY, key, Field.Store.YES, Field.Index.UN_TOKENIZED));
        }

        return doc;
    }

    /**
     * This method will return the log entries collected from mercurial categorized by the repository
     * it came from. NOTE: a null map will be returned if the indexes for this plugin have not yet
     * been initialized.
     *
     * @param issue the issue to get entries for.
     * @return A map with key of repository display name and entry of a collections of Logs. Null
     *         if the repository has not yet been initialized.
     */
    public Map getLogEntriesByRepository(Issue issue) throws IndexException, IOException
    {
        if (log.isDebugEnabled())
            log.debug("Retrieving revisions for : " + issue.getKey());

        if (!indexDirectoryExists())
        {
            log.warn("The indexes for the mercurial plugin have not yet been created.");
            return null;
        }
        else
        {
            String key = issue.getKey();

            final IndexReader reader = LuceneUtils.getIndexReader(getIndexPath());
            IndexSearcher searcher = new IndexSearcher(reader);

            try
            {
                Hits hits = searcher.search(new TermQuery(new Term(FIELD_ISSUEKEY, key)));
                Map logEntries = new HashMap(hits.length());

                for (int i = 0; i < hits.length(); i++)
                {
                    Document doc = hits.doc(i);
                    final long revision = Long.parseLong(doc.get(FIELD_REVISIONNUMBER));
                    final String repositoryId = doc.get(FIELD_REPOSITORY);
                    MercurialManager repo = mercurialRepositoryManager.getRepository(repositoryId);
                    HGLogEntry logEntry = repo.getLogEntry(revision);
                    if (logEntry == null)
                    {
                        log.error("Could not find log message for revision: " + revision);
                    }
                    else
                    {
                        // Look for list of map entries for repository
                        List entries = (List) logEntries.get(repositoryId);
                        if (entries == null)
                        {
                            entries = new ArrayList();
                            logEntries.put(repositoryId, entries);
                        }
                        entries.add(logEntry);
                    }
                }

                for (Iterator iterator = logEntries.values().iterator(); iterator.hasNext();)
                {
                    ArrayList entries = (ArrayList) iterator.next();
                    Collections.sort(entries, new Comparator()

                    {
                        public int compare(Object o, Object o1)
                        {
                            long r = ((HGLogEntry) o).getShortRevision();
                            long r1 = ((HGLogEntry) o1).getShortRevision();
                            if (r == r1)
                                return 0;
                            else if (r > r1)
                                return -1;
                            else
                                return 1;
                        }
                    });

                }

                return logEntries;
            }
            finally
            {
                searcher.close();
                reader.close();
            }
        }
    }
    /**
     * This method returns the log entries collected from mercurial categorized by the repository
     * it came from. NOTE: a null map will be returned if the indexes for this plugin have not yet
     * been initialized.
     *
     * This method uses the Version Manager to look up all issues affected by and fixed by the supplied {@link Version}.
     * The Lucene index is then used to look up all the commits for all the issues.
     *
     * @param version the version to get entries for.
     * @param numberOfEntries How many entries to fetch.
     * @return A map with key of repository display name and entry of a collections of Logs. <code>null</code>
     *         if the repository has not yet been initialized.
     * @throws com.atlassian.jira.issue.index.IndexException if the Lucene index reader cannot be retrieved
     * @throws java.io.IOException if reading the Lucene index fails
     */
    public Map getLogEntriesByVersion(Version version, int numberOfEntries) throws IndexException, IOException
    {
        if (version == null || numberOfEntries < 0) {
            throw new IllegalArgumentException("getLogEntriesByVersion(" + version + ")");
        }
        if (log.isDebugEnabled())
        {
            log.debug("getLogEntriesByVersion(" + version + ", " + numberOfEntries + ")");
        }

        if (!indexDirectoryExists())
        {
            log.warn("getLogEntriesByVersion() The indexes for the subversion plugin have not yet been created.");
            return null;
        }

        // Find all isuses affected by and fixed by any of the versions:
        Collection issues = new HashSet();

        try {
            issues.addAll(versionManager.getFixIssues(version));
            issues.addAll(versionManager.getAffectsIssues(version));
        } catch (GenericEntityException e) {
            log.error("getLogEntriesByVersion() Caught exception while looking up issues related to version " + version.getName() + "!", e);
            // Keep going. We may have got some issues stored.
        }

        // Construct a query with all the issue keys. Make sure to increase the maximum number of clauses if needed.
        int maxClauses = BooleanQuery.getMaxClauseCount();
        if (issues.size() > maxClauses) {
            BooleanQuery.setMaxClauseCount(issues.size());
        }
        BooleanQuery query = new BooleanQuery();

        for (Iterator iterator = issues.iterator(); iterator.hasNext();) {
            GenericValue issue = (GenericValue) iterator.next();
            String key = issue.getString(FIELD_ISSUEKEY);
            TermQuery termQuery = new TermQuery(new Term(FIELD_ISSUEKEY, key));
            query.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        final IndexReader reader = LuceneUtils.getIndexReader(getIndexPath());
        IndexSearcher searcher = new IndexSearcher(reader);
        Map logEntries;

        try{
            // Run the query and sort by date in descending order
            Sort sort = new Sort(FIELD_DATE, true);
            Hits hits = searcher.search(query, sort);

            if (hits == null) {
                log.info("getLogEntriesByVersion() No matches -- returning null.");
                return null;
            }

            logEntries = new HashMap(hits.length());
            for (int i = 0; i < hits.length() && i < numberOfEntries; i++)
            {
                Document doc = hits.doc(i);
                String repositoryId = doc.get(FIELD_REPOSITORY);//repositoryId is UUID + location
                MercurialManager manager = mercurialRepositoryManager.getRepository(repositoryId);
                long revision = Long.parseLong(doc.get(FIELD_REVISIONNUMBER));
                HGLogEntry logEntry = manager.getLogEntry(revision);
                if (logEntry == null)
                {
                    log.error("getLogEntriesByVersion() Could not find log message for revision: " + Long.parseLong(doc.get(FIELD_REVISIONNUMBER)));
                }
                else
                {
                    // Add the entry to a list of map entries for the repository
                    List entries = (List) logEntries.get(doc.get(FIELD_REPOSITORY));
                    if (entries == null)
                    {
                        entries = new ArrayList();
                        logEntries.put(doc.get(FIELD_REPOSITORY), entries);
                    }
                    entries.add(logEntry);
                }
            }
        } finally {
            searcher.close();
            reader.close();
            BooleanQuery.setMaxClauseCount(maxClauses);
        }

        return logEntries;
    }


}
