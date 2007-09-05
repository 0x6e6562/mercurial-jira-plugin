
Atlassian JIRA Mercurial Plugin
--------------------------------

Version: 0.1

Written by Matthew Doar, based on the atlassian subversion plugin
v.0.8.2 and the javasvn library v 0.8.8, with some help parsing
mercurial log output from the scm project.


What is it?
-----------
This plugin adds a 'Mercurial Commits' tab to JIRA issues, containing
commit logs associated with the issue.

For example, if your commit message is: "This fixes JRA-52 and JRA-54" -
the commit would be displayed in a tab when viewing JRA-52 and JRA-54.


Quick Install Instructions
--------------------------


1. Copy into JIRA's WEB-INF/lib (removing any existing older versions):
- lib/atlassian-mercurial-plugin-0.1.jar

2. Edit for your installation:
- mercurial-jira-plugin.properties

and use hg clone to create copies of the repositories under /tmp/mercurial

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
You can also optionally configure Mercurial service's schedule to
check for new commits (defaults to 1 hour)

To do this, in JIRA:
- go to the "Administration" tab
- click on the "System" menu item
- click on "Services"
- edit the Mercurial Indexing Service
- update the period (in minutes) to whatever you want.


Restarting
----------
If you want to reindex all of your commits or anything goes astray, simply:
- stop the JIRA server
- delete ${jira's_index_dir}/plugins/atlassian-mercurial-revisions
- start JIRA


More
----
Detailed installation and usage instructions?

    http://confluence.atlassian.com/display/JIRAEXT

Suggestions, bug reports or feature requests?

    http://jira.atlassian.com/browse/HG

Support?

    mailto:bug-admin@xensource.com
