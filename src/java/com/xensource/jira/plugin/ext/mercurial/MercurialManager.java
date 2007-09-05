/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 30, 2004
 * Time: 8:14:04 AM
 */
package com.xensource.jira.plugin.ext.mercurial;

import com.xensource.jira.plugin.ext.mercurial.linkrenderer.MercurialLinkRenderer;
import com.xensource.jira.plugin.ext.mercurial.revisions.RevisionIndexer;
import com.xensource.hg.core.io.HGLogEntry;
import com.xensource.hg.core.io.HGRepository;

public interface MercurialManager
{
    boolean isWebLinking();

    String getWebLink();
    
    ViewLinkFormat getViewLinkFormat(); 

    MercurialLinkRenderer getLinkRenderer();

    HGLogEntry getLogEntry(long revision);

    HGRepository getRepository();

    String getDisplayName();
}