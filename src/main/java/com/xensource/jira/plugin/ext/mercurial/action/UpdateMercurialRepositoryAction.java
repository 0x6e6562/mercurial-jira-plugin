package com.xensource.jira.plugin.ext.mercurial.action;

import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;

public class UpdateMercurialRepositoryAction extends AddMercurialRepositoryAction {
	private long repoId = -1;

	public UpdateMercurialRepositoryAction(MercurialRepositoryManager repoManager) {
		super(repoManager);
	}

	public String doDefault() {
		if (ERROR.equals(super.doDefault()))
			return ERROR;

        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }


        if (repoId == -1) {
			addErrorMessage(getText("mercurial.repository.id.missing"));
			return ERROR;
		}

		// Retrieve the cvs repository
//		final MercurialManager repository = getMultipleRepoManager().getRepository(repoId);
//		if (repository == null) {
//			addErrorMessage(getText("mercurial.repository.does.not.exist", Long.toString(repoId)));
//			return ERROR;
//		}
//
//		this.setDisplayName(repository.getDisplayName());
//		this.setRoot(repository.getRoot());
//		if (repository.getViewLinkFormat() != null) {
//            this.setWebLinkType(repository.getViewLinkFormat().getType());
//            this.setChangesetFormat(repository.getViewLinkFormat().getChangesetFormat());
//			this.setViewFormat(repository.getViewLinkFormat().getViewFormat());
//			this.setFileAddedFormat(repository.getViewLinkFormat().getFileAddedFormat());
//			this.setFileDeletedFormat(repository.getViewLinkFormat().getFileDeletedFormat());
//			this.setFileModifiedFormat(repository.getViewLinkFormat().getFileModifiedFormat());
//			this.setFileReplacedFormat(repository.getViewLinkFormat().getFileReplacedFormat());
//		}
//		this.setUsername(repository.getUsername());
//		this.setPassword(repository.getPassword());
//		this.setPrivateKeyFile(repository.getPrivateKeyFile());
//		this.setRevisionCacheSize(new Integer(repository.getRevisioningCacheSize()));
//		this.setRevisionIndexing(new Boolean(repository.isRevisionIndexing()));

		return INPUT;
	}

	public String doExecute() {
		if (!hasPermissions()) {
			addErrorMessage(getText("mercurial.admin.privilege.required"));
			return ERROR;
		}

		if (repoId == -1) {
			return getRedirect("ViewMercurialRepositories.jspa");
		}

//		MercurialManager mercurialManager = getMultipleRepoManager().updateRepository(repoId, this);
//		if (!mercurialManager.isActive()) {
//			repoId = mercurialManager.getId();
//			addErrorMessage(mercurialManager.getInactiveMessage());
//			addErrorMessage(getText("admin.errors.occured.when.updating"));
//			return ERROR;
//		}
		return getRedirect("ViewMercurialRepositories.jspa");
	}

	public long getRepoId() {
		return repoId;
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

}
