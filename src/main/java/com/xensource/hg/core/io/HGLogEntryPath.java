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

/**
 * @author Alexander Kitaev
 */
public class HGLogEntryPath {
    
    private String myPath;
    private char myType;
    
    public HGLogEntryPath(String path, char type) {
        myPath = path;
        myType = type;
    }
    public String getPath() {
        return myPath;
    }
    public char getType() {
        return myType;
    }
    protected void setPath(String path) {
    	myPath = path;
    }
}
