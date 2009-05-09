package com.xensource.jira.plugin.ext.mercurial.action;

import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.xensource.jira.plugin.ext.mercurial.MercurialManager;

public class ActivateMercurialRepositoryAction extends MercurialActionSupport {
	private long repoId;
	private MercurialManager mercurialManager;

    public ActivateMercurialRepositoryAction(MercurialRepositoryManager manager) {
		super(manager);
	}

    public String getRepoId() {
		return Long.toString(repoId);
	}

	public void setRepoId(String repoId) {
		this.repoId = Long.parseLong(repoId);
	}

	public String doExecute() {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }

		mercurialManager = repoManager.getRepository(repoId + "");
//		mercurialManager.activate();
//		if (!mercurialManager.isActive()) {
//			addErrorMessage(getText("subversion.repository.activation.failed", mercurialManager.getInactiveMessage()));
//		}
		return SUCCESS;
	}

	public MercurialManager getMercurialManager() {
		return mercurialManager;
	}

}
