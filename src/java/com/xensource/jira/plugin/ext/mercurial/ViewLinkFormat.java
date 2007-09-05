package com.xensource.jira.plugin.ext.mercurial;

/**
 * Struct holding the linkformat.* link format parameters for a repository.
 *
 * @author Chenggong Lu
 */
public class ViewLinkFormat
{
    public String changesetFormat;
    public String revisionedPathLinkFormat;
    public String fileAddedFormat;
    public String revisionedPathDelLinkFormat;
    public String copyFromFormat;
    public String fileModifiedFormat;
    public String fileReplacedFormat;
    public String fileDeletedFormat;

    public ViewLinkFormat(String changesetFormat, String fileAddedFormat, String fileModifiedFormat, String fileReplacedFormat, String fileDeletedFormat, String pathLinkFormat)
    {
        this.copyFromFormat = pathLinkFormat;
        this.fileAddedFormat = fileAddedFormat;
        this.fileModifiedFormat = fileModifiedFormat;
        this.fileReplacedFormat = fileReplacedFormat;
        this.fileDeletedFormat = fileDeletedFormat;
        this.changesetFormat = changesetFormat;
    }

	public void fillFormatFromOther(ViewLinkFormat other)
	{
        if (other != null)
        {
            if (this.copyFromFormat == null) this.copyFromFormat = other.copyFromFormat;
            if (this.fileAddedFormat == null) this.fileAddedFormat = other.fileAddedFormat;
            if (this.fileModifiedFormat == null) this.fileModifiedFormat = other.fileModifiedFormat;
            if (this.fileReplacedFormat == null) this.fileReplacedFormat = other.fileReplacedFormat;
            if (this.fileDeletedFormat == null) this.fileDeletedFormat = other.fileDeletedFormat;
            if (this.changesetFormat == null) this.changesetFormat = other.changesetFormat;
        }

    }

	public String toString() {
		return "changesetFormat: " + changesetFormat
		    + " modifiedFormat: " + fileModifiedFormat;
	}
}
