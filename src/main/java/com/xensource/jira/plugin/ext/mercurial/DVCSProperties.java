package com.xensource.jira.plugin.ext.mercurial;

/**
 * Not quite sure what this should represent yet........
 * ... it seems to get used when listing repos in the view.
 */
public interface DVCSProperties {

    String getRoot();
    String getWebLink();
    ViewLinkFormat getViewLinkFormat();
    String getId();
    
}
