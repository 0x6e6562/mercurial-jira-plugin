package com.xensource.jira.plugin.ext.mercurial;

public class WebLinkType {
    
    private final String key;
    private final String name;
    private final ViewLinkFormat viewLinkFormat;

    public WebLinkType(String key, String name, String viewFormat, String changeset, String fileAdded,
                       String fileModified, String fileReplaced, String fileDeleted)
    {
        this.key = key;
        this.name = name;
        viewLinkFormat = new ViewLinkFormat(key, changeset, fileAdded, fileModified, fileReplaced, fileDeleted, viewFormat);
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getChangesetFormat()
    {
        return viewLinkFormat.getChangesetFormat();
    }

    public String getFileAddedFormat()
    {
        return viewLinkFormat.getFileAddedFormat();
    }

    public String getViewFormat()
    {
        return viewLinkFormat.getViewFormat();
    }

    public String getFileModifiedFormat()
    {
        return viewLinkFormat.getFileModifiedFormat();
    }

    public String getFileReplacedFormat()
    {
        return viewLinkFormat.getFileReplacedFormat();
    }

    public String getFileDeletedFormat()
    {
        return viewLinkFormat.getFileDeletedFormat();
    }
}
