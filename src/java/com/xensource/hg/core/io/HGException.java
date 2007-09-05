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

import java.util.Collection;

/**
 * @author Alexander Kitaev
 */
public class HGException extends Exception {

    private static final long serialVersionUID = 1661853897041563030L;
    
    private HGError[] myErrors;

    public HGException() {
    }
    public HGException(String message) {
        super(message);
    }
    public HGException(String message, Throwable cause) {
        super(message, cause);
    }
    public HGException(Throwable cause) {
        super(cause);
    }
    
    public HGException(HGError[] errors) {
        this("", errors);
        
    }
    public HGException(String message, HGError[] errors) {
        super(message);
        myErrors = errors;
        
    }

    public HGException(HGError error) {
        this(new HGError[] {error});
    }

    public HGException(String message, Collection errors) {
        super(message);
        myErrors = (HGError[]) errors.toArray(new HGError[errors.size()]);
    }
    
    public HGError[] getErrors() {
        return myErrors;
    }
    
    public String getMessage() {
        if (myErrors == null || myErrors.length == 0) {
            return super.getMessage();
        }
        StringBuffer sb  = new StringBuffer();
        sb.append(super.getMessage());
        for(int i = 0; i < myErrors.length; i++) {
            sb.append("\n");
            sb.append(myErrors[i].getMessage());            
        }
        return sb.toString();
    }
}
