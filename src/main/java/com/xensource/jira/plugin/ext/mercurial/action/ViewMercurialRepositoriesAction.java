package com.xensource.jira.plugin.ext.mercurial.action;

import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;

import java.util.Collection;

/**
 * Manage 1 or more repositories
 */
public class ViewMercurialRepositoriesAction extends MercurialActionSupport
{

    public ViewMercurialRepositoriesAction(MercurialRepositoryManager manager)
    {
        super (manager);
    }

    public Collection getRepositories()
    {
        //return getMercurialManager().getRepositoryList();
        return null;
    }
}
