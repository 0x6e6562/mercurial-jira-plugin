package com.xensource.jira.plugin.ext.mercurial.linkrenderer;

import com.atlassian.core.util.StringUtils;
import com.xensource.jira.plugin.ext.mercurial.MercurialConstants;
import com.xensource.jira.plugin.ext.mercurial.MercurialManager;
import com.xensource.jira.plugin.ext.mercurial.ViewLinkFormat;
import org.apache.log4j.Logger;
import com.xensource.hg.core.io.HGLogEntry;
import com.xensource.hg.core.io.HGLogEntryPath;

/**
 * A link renderer implementation which lets the user specify the
 * format in the properties file, to accommodate various formats
 * (ViewCVS, Fisheye, etc) out there.
 *
 * @author Chenggong Lu
 * @author Jeff Turner
 */
public class LinkFormatRenderer implements MercurialLinkRenderer
{
    private static Logger log = Logger.getLogger(LinkFormatRenderer.class);
    private String pathLinkFormat;
    private String fileReplacedFormat;
    private String fileAddedFormat;
    private String fileModifiedFormat;
    private String fileDeletedFormat;
    private String changesetFormat;

    public LinkFormatRenderer(MercurialManager mercurialManager)
    {

        ViewLinkFormat linkFormat = mercurialManager.getViewLinkFormat();
        if (linkFormat != null)
        {
            if (linkFormat.changesetFormat != null
                    && linkFormat.changesetFormat.trim().length() != 0)
            {
                changesetFormat = linkFormat.changesetFormat;
            }

            if (linkFormat.fileAddedFormat != null
                    && linkFormat.fileAddedFormat.trim().length() != 0)
            {
                fileAddedFormat = linkFormat.fileAddedFormat;
            }

            if (linkFormat.fileModifiedFormat != null
                    && linkFormat.fileModifiedFormat.trim().length() != 0)
            {
                fileModifiedFormat = linkFormat.fileModifiedFormat;
            }

            if (linkFormat.fileReplacedFormat != null
                    && linkFormat.fileReplacedFormat.trim().length() != 0)
            {
                fileReplacedFormat = linkFormat.fileReplacedFormat;
            }

            if (linkFormat.fileDeletedFormat != null
                    && linkFormat.fileDeletedFormat.trim().length() != 0)
            {
                fileDeletedFormat = linkFormat.fileDeletedFormat;
            }

        }
        else
        {
            log.warn("viewLinkFormat is null");
        }
    }

    public String getRevisionLink(HGLogEntry revision)
    {
        return getRevisionLink(revision.getFullRevision(), revision.getShortRevision());
    }

    public String getChangePathLink(HGLogEntry logEntry, HGLogEntryPath logEntryPath)
    {
        char changeType = logEntryPath.getType();
        String path = logEntryPath.getPath();
        String revision = logEntry.getFullRevision();

        if (changeType == MercurialConstants.MODIFICATION)
        {
            return linkPath(fileModifiedFormat, path, revision);
        }
        else if (changeType == MercurialConstants.ADDED)
        {
            return linkPath(fileAddedFormat, path, revision);
        }
        else if (changeType == MercurialConstants.REPLACED)
        {
            return linkPath(fileReplacedFormat, path, revision);
        }
        else if (changeType == MercurialConstants.DELETED)
        {
            return linkPath(fileDeletedFormat, path, revision);
        }
        else
        {
            return linkPath(fileReplacedFormat, path, revision);
        }
    }

    protected String getRevisionLink(String fullRevision, long shortRevision)
    {
        if (changesetFormat != null)
        {
            try
            {
                String href = StringUtils.replaceAll(changesetFormat, "${rev}", "" + fullRevision);
                return "<a href=\"" + href + "\">#" + shortRevision 
                       + ":" + fullRevision + "</a>";
            }
            catch (Exception ex)
            {
                log.error("format error: " + ex.getMessage(), ex);
            }
        }
        return "#" + shortRevision + ":" + fullRevision;

    }

    private String linkPath(final String format, String path, String revision)
    {
        if (format != null)
        {
            if (path != null && path.length() > 0 && path.charAt(0) != '/')
            {
                path = "/" + path;
            }

            try
            {
                String href = format;
                if (path != null)
                {
                    href = StringUtils.replaceAll(href, "${path}", path);
                }
                href = StringUtils.replaceAll(href, "${rev}", "" + revision);
		// TODO convert revision to a number and subtract 1
		String prev_revision = revision;
                href = StringUtils.replaceAll(href, "${rev-1}", "" + prev_revision);

                return "<a href=\"" + href + "\">" + path + "</a>";
            }
            catch (Exception ex)
            {
                log.error("format error: " + ex.getMessage(), ex);
            }
        }
        return path;
    }
}
