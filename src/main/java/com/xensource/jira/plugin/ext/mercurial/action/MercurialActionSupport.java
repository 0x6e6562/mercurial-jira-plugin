package com.xensource.jira.plugin.ext.mercurial.action;

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.xensource.jira.plugin.ext.mercurial.MercurialRepositoryManager;
import com.xensource.jira.plugin.ext.mercurial.WebLinkType;
import com.xensource.jira.plugin.ext.mercurial.MercurialManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MercurialActionSupport extends JiraWebActionSupport {

	protected MercurialRepositoryManager repoManager;
	private List webLinkTypes;

    public MercurialActionSupport(MercurialRepositoryManager manager) {
		this.repoManager = manager;
	}

    // TODO Wanted to make this abstract, but I think the whole area needs
    // an overhaul
    protected MercurialManager getMercurialManager() {return null;}

	public boolean hasPermissions() {
		return isHasPermission(Permissions.ADMINISTER);
	}

	public String doDefault() {
		if (!hasPermissions())
        {
			return PERMISSION_VIOLATION_RESULT;
		}

		return INPUT;
	}

	public List getWebLinkTypes() throws IOException {
		if (webLinkTypes == null) {
			webLinkTypes = new ArrayList();
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("/weblinktypes.properties"));

			String[] types = properties.getProperty("types", "").split(" ");
			for (int i = 0; i < types.length; i++) {
				webLinkTypes.add(new WebLinkType(
								types[i],
								properties.getProperty(types[i] + ".name", types[i]),
								properties.getProperty(types[i] + ".view"),
								properties.getProperty(types[i] + ".changeset"),
								properties.getProperty(types[i] + ".file.added"),
								properties.getProperty(types[i] + ".file.modified"),
								properties.getProperty(types[i] + ".file.replaced"),
								properties.getProperty(types[i] + ".file.deleted")
				));
			}
		}
		return webLinkTypes;
	}
}
