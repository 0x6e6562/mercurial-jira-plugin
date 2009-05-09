Atlassian JIRA Mercurial Plugin
--------------------------------

This was originally written by Matthew Doar, based on the atlassian subversion plugin
v.0.8.2 and the javasvn library v 0.8.8, with some help parsing
mercurial log output from the scm project.

This project seemed to get abandoned but a few people submitted various patches
which never got applied back to the mainline.

We've taken the old source from the Atlassian Subversion repository, put them
on github, applied the patches and have fixed some stuff in the process of doing
so.


What is it?
-----------
This plugin adds a 'Mercurial Commits' tab to JIRA issues, containing
commit logs associated with the issue.

For example, if your commit message is: "This fixes JRA-52 and JRA-54" -
the commit would be displayed in a tab when viewing JRA-52 and JRA-54.

Also, if you prefer to follow a branch-per-bug policy, if you name your branches
after an issue in JIRA, any commits will automatically be associated with that issue.

Building
--------

1. Ensure that you have maven 2 installed and functioning appropriately.
2. Install the following restricted jars:
  - http://java.sun.com/products/javamail/downloads/index.html
  - http://java.sun.com/products/javabeans/glasgow/jaf.html
3. Run mvn package in the plugin checkout directory.


Quick Install Instructions
--------------------------


1. Copy into JIRA's WEB-INF/lib (removing any existing older versions):
- lib/atlassian-mercurial-plugin-$VERSION.jar

2. Edit for your installation:
- mercurial-jira-plugin.properties

and use hg clone to create copies of the repositories on your local file system.

The properties are a bit cryptic and could be simplified. Basically, hg.root.N key
points to your hgweb installation and the hg.clonedir.N points to the directory
that contains your local clone. This is probably a bit confusing and should probably
get refactored.

3. Copy into JIRA's WEB-INF/classes
- mercurial-jira-plugin.properties

4. If you are upgrading from an older version of the plugin, please
   delete the Mercurial index directory
($jira's_index_dir}/plugins/atlassian-mercurial-revisions/). The plugin will recreate it when its service first runs.

5. Restart JIRA 
If you are using JIRA Standalone you can copy the files under the
atlassian-jira sub-directory. That is, the jar files into
atlassian-jira/WEB-INF/lib and the .properties file into
atlassian-jira/WEB-INF/classes.

If using the WAR distribution, create the directory
edit-webapp/WEB-INF/lib and copy the jar files into it. Then copy the
.properties file into edit-webapp/WEB-INF/classes and rebuild and
redeploy the JIRA war file.

(note: the first time the service runs it will take a while to index
all of your existing issues - be patient)


Scheduling
----------

You need to add a service to JIRA for the indexer to start indexing your repositories.

To configure Mercurial service's schedule to check for new commits:

- go to the "Administration" tab
- click on the "System" menu item
- click on "Services"
- in the "Add Services" box, enter "com.xensource.jira.plugin.ext.mercurial.revisions.RevisionIndexService"
  as the class, name the service anything you want
- update the delay (in minutes) to whatever you want.


Restarting
----------

If you want to reindex all of your commits or anything goes astray, simply:
- stop the JIRA server
- delete ${jira's_index_dir}/plugins/atlassian-mercurial-revisions
- start JIRA


More
----

This is this old support information, I'm not too sure whether you'll get a response. I'd try to use
the issue system on github first.

Detailed installation and usage instructions?

    http://confluence.atlassian.com/display/JIRAEXT

Suggestions, bug reports or feature requests?

    http://jira.atlassian.com/browse/HG

Support?

    mailto:bug-admin@xensource.com
