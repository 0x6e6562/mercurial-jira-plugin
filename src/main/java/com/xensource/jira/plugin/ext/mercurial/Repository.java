package com.xensource.jira.plugin.ext.mercurial;


/**
 * This is the metadata for a repo.
 */
public class Repository implements HgProperties {



    public String root;
    public String displayName;
    public String username;
    public String password;
    public ViewLinkFormat viewLinkFormat;
    public String viewCvsUrl;




    // TODO These paramaters look like global config
    public Boolean revisionIndexing;
    public Integer revisioningCacheSize;
    public String cloneDir;
    public String executable;

    public Repository(String root, String displayName, String username, String password,
                      ViewLinkFormat viewLinkFormat, Boolean revisionIndexing,
                      Integer revisioningCacheSize, String cloneDir, String executable) {
        this.root = root;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.viewLinkFormat = viewLinkFormat;
        this.revisionIndexing = revisionIndexing;
        this.revisioningCacheSize = revisioningCacheSize;
        this.cloneDir = cloneDir;
        this.executable = executable;
    }

    // TODO This looks like some braindead copy constructor
    public void fillPropertiesFromOther(Repository other) {
        if (this.username == null) {
            this.username = other.username;
        }
        if (this.password == null) {
            this.password = other.password;
        }
        if (this.revisionIndexing == null) {
            this.revisionIndexing = other.revisionIndexing;
        }
        if (this.revisioningCacheSize == null) {
            this.revisioningCacheSize = other.revisioningCacheSize;
        }
        if (this.cloneDir == null) {
            this.cloneDir = other.cloneDir;
        }
        if (this.viewLinkFormat == null)
        {
        	this.viewLinkFormat = new ViewLinkFormat(null, null, null, null, null, null, null);
        	this.viewLinkFormat.fillFormatFromOther(other.viewLinkFormat);
        }
    }

    public String toString() {
        return "root " + root + " displayName " + displayName + " revisioningIndex: " + revisionIndexing + " revisioningCacheSize: " + revisioningCacheSize + " cloneDir: " + cloneDir;
    }
}
