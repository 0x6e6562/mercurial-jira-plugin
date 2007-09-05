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

import java.net.MalformedURLException;
import java.net.URL;

import com.xensource.hg.util.PathUtil;

/**
 * @author Alexander Kitaev
 */
public class HGRepositoryLocation {

    private String myPath;
    private final String myHost;
    private final int myPort;
    private final String myProtocol;
    private String myAsString;

    public static boolean equals(HGRepositoryLocation location1, HGRepositoryLocation location2) {
        if (location1 == null || location2 == null) {
            return location1 == location2;
        } 
        return location1.toString().equals(location2.toString());
    }

    public static HGRepositoryLocation parseURL(String location) throws HGException {
        if (location == null) {
            return null;
        }
        int index = location.indexOf(':');
        if (index < 0) {
            throw new HGException("malformed url: " + location);
        }
        String protocol = location.substring(0, index);
        location = "http" + location.substring(protocol.length());
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            throw new HGException("malformed url " + location);
        }
        if (url != null) {
            String host = url.getHost();
            int port = url.getPort();
            if (port < 0) {
                if ("http".equals(protocol)) {
                    port = 80;
                } else if ("https".equals(protocol)) {
                    port = 443;
                }
            }
            String path = url.getPath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            return new HGRepositoryLocation(protocol, host, port, path);
        }
        throw new HGException("malformed url " + location);
    }

    public HGRepositoryLocation(String protocol, String host, int port, String path) {
        myHost = host;
        myProtocol = protocol;
        myPort = port;
        myPath = path;
        myPath = PathUtil.removeTrailingSlash(myPath);
        myPath = PathUtil.encode(myPath);
    }

    public String getProtocol() {
        return myProtocol;
    }

    public String getHost() {
        return myHost;
    }

    public String getPath() {
        return myPath;
    }

    public int getPort() {
        return myPort;
    }

    public String toString() {
        if (myAsString != null) {
            return myAsString;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(myProtocol);
        sb.append("://");
        sb.append(PathUtil.encode(myHost));
        if (myPort != getDefaultPort(myProtocol)) {
            sb.append(':');
            sb.append(myPort);
        }
        sb.append(myPath);
        myAsString = sb.toString();
        return sb.toString();
    }
    
    public String toCanonicalForm() {
        StringBuffer sb = new StringBuffer();
        sb.append(myProtocol);
        sb.append("://");
        sb.append(PathUtil.encode(myHost));
        sb.append(':');
        sb.append(myPort);
        sb.append(myPath);
        return sb.toString();
    }

    private static int getDefaultPort(String protocol) {
        if ("http".equals(protocol)) {
            return 80;
        } else if ("https".equals(protocol)) {
            return 443;
        }
        return -1;
    }
    
    /** 
     * Return the string after the last / in the URL
     */
    public String getLast() {
	int lastIndex = myPath.lastIndexOf('/');
	if (lastIndex == -1) {
	    return myPath;
	} else {
	    return myPath.substring(lastIndex+1, myPath.length());
	}
    }

    public boolean equals(Object o) {
    	if (o == this) {
    		return true;
    	}
    	if (o == null || o.getClass() != HGRepositoryLocation.class) {
    		return false;
    	}
    	return toCanonicalForm().equals(((HGRepositoryLocation) o).toCanonicalForm());
    }
    
    public int hashCode() {
    	return toCanonicalForm().hashCode();
    }
}
