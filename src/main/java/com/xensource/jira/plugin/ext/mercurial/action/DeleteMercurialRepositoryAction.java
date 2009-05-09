package com.xensource.jira.plugin.ext.mercurial.action;

import com.xensource.jira.plugin.ext.mercurial.MercurialManager;
import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;

public class DeleteMercurialRepositoryAction extends MercurialActionSupport {
	private long repoId;
	private MercurialManager mercurialManager;

    public DeleteMercurialRepositoryAction(MercurialRepositoryManager manager) {
		super(manager);
	}

    public String getRepoId() {
		return Long.toString(repoId);
	}

	public void setRepoId(String repoId) {
		this.repoId = Long.parseLong(repoId);
	}

	public String doDefault() {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }

		//mercurialManager = getMultipleRepoManager().getRepository(repoId);

		return INPUT;
	}

	public String doExecute() {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }

		//getMultipleRepoManager().removeRepository(repoId);

		return getRedirect("ViewSubversionRepositories.jspa");
	}

	public MercurialManager getMercurialManager() {
		return mercurialManager;
	}
}
