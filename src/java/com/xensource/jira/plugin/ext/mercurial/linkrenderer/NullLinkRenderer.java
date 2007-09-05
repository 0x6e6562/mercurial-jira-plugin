/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 30, 2004
 * Time: 1:47:18 PM
 */
package com.xensource.jira.plugin.ext.mercurial.linkrenderer;

import com.xensource.hg.core.io.HGLogEntry;
import com.xensource.hg.core.io.HGLogEntryPath;

/**
 * Used when the user does not specify any web links - just return
 * String values, no links.
 */
public class NullLinkRenderer implements MercurialLinkRenderer
{
    public String getRevisionLink(HGLogEntry revision)
    {
        return "" + revision.getShortRevision();
    }

    public String getChangePathLink(HGLogEntry revision, HGLogEntryPath logEntryPath)
    {
        return logEntryPath.getPath();
    }
}