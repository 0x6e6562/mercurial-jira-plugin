package com.xensource.jira.plugin.ext.mercurial;



public class MercurialProperties implements HgProperties
{
    public String root;
    public String displayName;
    public String username;
    public String password;
    public ViewLinkFormat viewLinkFormat;
    public String viewCvsUrl;
    public Boolean revisionIndexing;
    public Integer revisioningCacheSize;
    public String cloneDir;
    public Boolean updateRepo;
    public String executable;

    public MercurialProperties(String root, String displayName, String username, String password, ViewLinkFormat viewLinkFormat, Boolean revisionIndexing, Integer revisioningCacheSize, String cloneDir, Boolean updateRepo, String executable) {
        this.root = root;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.viewLinkFormat = viewLinkFormat;
        this.revisionIndexing = revisionIndexing;
        this.revisioningCacheSize = revisioningCacheSize;
        this.cloneDir = cloneDir;
        this.updateRepo = updateRepo;
        this.executable = executable;
    }

    public void fillPropertiesFromOther(MercurialProperties other) {
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
        if (this.updateRepo == null) {
            this.updateRepo = other.updateRepo;
        }
    }

    public String toString() {
        return "root " + root + " displayName " + displayName + " revisioningIndex: " + revisionIndexing + " revisioningCacheSize: " + revisioningCacheSize + " cloneDir: " + cloneDir + " updateRepo " + updateRepo;
    }
}
