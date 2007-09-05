/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 16, 2004
 * Time: 2:00:52 PM
 */
package com.xensource.jira.plugin.ext.mercurial.issuetabpanels.changes;

import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.xensource.jira.plugin.ext.mercurial.MercurialConstants;
import com.xensource.jira.plugin.ext.mercurial.linkrenderer.MercurialLinkRenderer;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.xensource.hg.core.io.HGLogEntry;
import com.xensource.hg.core.io.HGLogEntryPath;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Date;

/**
 * One item in the 'Mercurial Commits' tab.
 */
public class MercurialRevisionAction extends AbstractIssueAction
{

    private final HGLogEntry revision;

    private final String repoUUID;
    protected final IssueTabPanelModuleDescriptor descriptor;
    protected MercurialRepositoryManager mercurialRepositoryManager;
    protected Timestamp timePerformed;

    public MercurialRevisionAction(HGLogEntry logEntry, MercurialRepositoryManager mercurialRepositoryManager, IssueTabPanelModuleDescriptor descriptor, String repoUUID)
    {
        super(descriptor);
        this.mercurialRepositoryManager = mercurialRepositoryManager;
        this.descriptor = descriptor;
        this.revision = logEntry;
        this.timePerformed = new Timestamp(revision.getDate().getTime());
        this.repoUUID = repoUUID;
    }

    protected void populateVelocityParams(Map params)
    {
        params.put("mercurial", this);
    }

    public MercurialLinkRenderer getLinkRenderer()
    {
        return mercurialRepositoryManager.getRepository(repoUUID).getLinkRenderer();
    }

    public String getRepositoryDisplayName()
    {
        return mercurialRepositoryManager.getRepository(repoUUID).getDisplayName();
    }

    public Date getTimePerformed()
    {
        return timePerformed;
    }

    public String getRepoUUID()
    {
        return repoUUID;
    }

    public String getUsername()
    {
        return revision.getAuthor();
    }

    public HGLogEntry getRevision()
    {
        return revision;
    }

    public boolean isAdded(HGLogEntryPath logEntryPath)
    {
        return MercurialConstants.ADDED == logEntryPath.getType();
    }

    public boolean isModified(HGLogEntryPath logEntryPath)
    {
        return MercurialConstants.MODIFICATION == logEntryPath.getType();
    }

    public boolean isReplaced(HGLogEntryPath logEntryPath)
    {
        return MercurialConstants.REPLACED == logEntryPath.getType();
    }

    public boolean isDeleted(HGLogEntryPath logEntryPath)
    {
        return MercurialConstants.DELETED == logEntryPath.getType();
    }
}
