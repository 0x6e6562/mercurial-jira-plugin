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

package com.xensource.hg.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author TMate Software Ltd.
 *
 */
public class TimeUtil {
	
    private static final DateFormat ISO8601_FORMAT_OUT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'");

    // E.g Fri Jul 21 00:37:12 2006
    // Note: DateFormat doesn't support numerical timezones
    private static final DateFormat ISO8601_FORMAT_IN = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    static {
        ISO8601_FORMAT_IN.setTimeZone(TimeZone.getTimeZone("PST"));
        ISO8601_FORMAT_OUT.setTimeZone(TimeZone.getTimeZone("PST"));
    }
    
    public static final void formatDate(Date date, StringBuffer buffer) {
    	ISO8601_FORMAT_OUT.format(date, buffer, new FieldPosition(0));
    }

    public static final String formatDate(Date date) {
        if (date == null || date.getTime() == 0) {
            return null;
        }
    	return ISO8601_FORMAT_OUT.format(date);
    }
    
    public static final Date parseDate(String str, String timezone) {
	if (timezone != null) {
	    ISO8601_FORMAT_IN.setTimeZone(TimeZone.getTimeZone(timezone));
	}
    	if (str == null) {
    		return new Date(0);
    	}
    	try {
	    return ISO8601_FORMAT_IN.parse(str);
	} catch (Throwable e) {
	}
	return new Date(0);
    }
    
    public static final String toHumanDate(String str) {
        if (str == null) {
            return "";
        }
        str = str.replace('T', ' ');
        str = str.substring(0, 19) + 'Z';
        return str;
    }
}
