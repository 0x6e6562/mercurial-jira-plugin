/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 16, 2004
 * Time: 1:57:17 PM
 */
package com.xensource.jira.plugin.ext.mercurial.issuetabpanels.changes;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.JiraEntityUtils;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import com.xensource.hg.core.io.HGLogEntry;

import java.util.*;

public class MercurialRevisionsTabPanel extends AbstractIssueTabPanel
{
    private static Logger log = Logger.getLogger(MercurialRevisionsTabPanel.class);

    protected final MercurialRepositoryManager mercurialRepositoryManager;
    private PermissionManager permissionManager;

    public MercurialRevisionsTabPanel(MercurialRepositoryManager mercurialRepositoryManager, PermissionManager permissionManager)
    {
        this.mercurialRepositoryManager = mercurialRepositoryManager;
        this.permissionManager = permissionManager;
    }

    public List getActions(Issue issue, User remoteUser)
    {
        try
        {
            Map logEntries = mercurialRepositoryManager.getRevisionIndexer().getLogEntriesByRepository(issue);

            // This is a bit of a hack to get the error message across
            if (logEntries == null)
            {

                GenericMessageAction action = new GenericMessageAction(descriptor.getI18nBean().getText("no.index.error.message"));
                return EasyList.build(action);
            }
            else if (logEntries.size() == 0)
            {
                GenericMessageAction action = new GenericMessageAction(descriptor.getI18nBean().getText("no.log.entries.message"));
                return EasyList.build(action);
            }
            else
            {
                List actions = new ArrayList(logEntries.size());
                for (Iterator iterator = logEntries.keySet().iterator(); iterator.hasNext();)
                {
                    String repoUUID = (String) iterator.next();
                    for (Iterator iterator1 = ((List) logEntries.get(repoUUID)).iterator(); iterator1.hasNext();)
                    {
                        HGLogEntry logEntry = (HGLogEntry) iterator1.next();
                        actions.add(new MercurialRevisionAction(logEntry, mercurialRepositoryManager, descriptor, repoUUID));
                    }
                }
                Collections.sort(actions, IssueActionComparator.COMPARATOR);
                return actions;
            }
        }
        catch (Throwable t)
        {
            log.error("Error retrieving actions for : " + issue.getKey(), t);
        }

        return Collections.EMPTY_LIST;
    }

    public boolean showPanel(Issue issue, User remoteUser)
    {
        return mercurialRepositoryManager.isIndexingRevisions() &&
                permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, issue, remoteUser);
    }
}
