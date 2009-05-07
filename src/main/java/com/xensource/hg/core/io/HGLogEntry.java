/*
 * ====================================================================
 * Copyright (c) 2004 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://tmate.org/svn/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

package com.xensource.hg.core.io;

import java.util.Date;
import java.util.Map;

/**
 * @author Alexander Kitaev
 */
public class HGLogEntry {
    
    private long myShortRevision;
    private String myFullRevision;
    private String myAuthor;
    private Date myDate;
    private String myMessage;
    private Map myChangedPaths;
    
    public HGLogEntry(Map changedPaths, long shortRevision, String fullRevision, String author, Date date, String message) {
        myShortRevision = shortRevision;
        myFullRevision = fullRevision;
        myAuthor = author;
        myDate = date;
        myMessage = message;
        myChangedPaths = changedPaths;
    }
    
    public Map getChangedPaths() {
        return myChangedPaths;
    }
    
    public String getAuthor() {
        return myAuthor;
    }
    public Date getDate() {
        return myDate;
    }
    public String getMessage() {
        return myMessage;
    }
    public long getShortRevision() {
        return myShortRevision;
    }
    public String getFullRevision() {
        return myFullRevision;
    }
}
