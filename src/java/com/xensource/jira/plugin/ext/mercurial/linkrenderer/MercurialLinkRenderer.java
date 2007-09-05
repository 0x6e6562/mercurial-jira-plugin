/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 30, 2004
 * Time: 1:44:30 PM
 */
package com.xensource.jira.plugin.ext.mercurial.linkrenderer;

import com.xensource.hg.core.io.HGLogEntry;
import com.xensource.hg.core.io.HGLogEntryPath;

public interface MercurialLinkRenderer
{
    String getRevisionLink(HGLogEntry revision);

    String getChangePathLink(HGLogEntry revision, HGLogEntryPath changePath);

}