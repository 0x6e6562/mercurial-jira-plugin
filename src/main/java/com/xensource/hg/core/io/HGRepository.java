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

import com.xensource.hg.util.ExecUtil;
import com.xensource.hg.util.TimeUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthew Doar
 */
public class HGRepository {

    private String myRepositoryUUID = "not set";
    private String myRepositoryRoot = "not set";
    private HGRepositoryLocation myLocation;
    private String myCloneDir = "not set";
    private Boolean myUpdateRepo = false;

    private static Logger log = Logger.getLogger(HGRepository.class);
    private static final String DEFAULT_BRANCH = "default";

    //-------------------------------------------------------------------------

    // TODO Start trying to canonicalize these commands
    // It's a bit of hack and slay ATM
    // For example, these constants should not be responsible for putting
    // spaces into the command execution

    // This could be done a lot neater in Scala, but lets try to get the UI based
    // config up and running first

    final String HG_TIP;
    final String HG_CMD;
    final String HG_CLONE;
    final String HG_PULL_U;

    //-------------------------------------------------------------------------

    public HGRepository(String executable, HGRepositoryLocation location, String cloneDir, Boolean updateRepo) {
        myLocation = location;
        myRepositoryRoot = location.toCanonicalForm();
        myRepositoryUUID = myRepositoryRoot;
        myCloneDir = cloneDir;
        // TODO WTF is this updateRepo shit? Can't we just nuke it???
        myUpdateRepo = (updateRepo == null) ? false : updateRepo;
        log.debug("mercurial repository object: " + myRepositoryRoot + ", cloneDir: " + cloneDir);

        HG_TIP = executable + " tip";
        HG_CMD = executable + " ";
        HG_CLONE = executable + " clone ";
        HG_PULL_U = executable + " pull -u";
    }


    public HGRepositoryLocation getLocation() {
        return myLocation;
    }

    public String getRepositoryUUID() {
        return myRepositoryUUID;
    }

    public String getRepositoryRoot() {
        return myRepositoryRoot;
    }

    public String getCloneDir() {
        return myCloneDir;
    }

    public Boolean getUpdateRepo() {
        return myUpdateRepo;
    }

    /**
     * Run an hg command.
     * <p/>
     * For the latest revision: hg head
     * For logging:
     * If the local repository doesn't exist, clone it.
     * If it does exist, update it.
     * Then run hg -v log on it and return the result.
     */
    private ArrayList<String> hg_cmd(String cmd) throws HGException {
        ArrayList<String> linesList;
        String[] env_vars = new String[]{};
        File basedir = new File(myCloneDir);
        File hg_logdir = null;
        ExecUtil eu = new ExecUtil();

        try {
            hg_logdir = new File(basedir.getAbsolutePath() + "/" + myLocation.getLast());
            if (!myUpdateRepo) {
                if (cmd.equals("head")) {

                    linesList = eu.exec(HG_TIP, env_vars, hg_logdir, log);
                } else if (cmd.startsWith("log")) {
                    linesList = eu.exec(HG_CMD + cmd, env_vars, hg_logdir, log);
                } else {
                    throw new HGException("Unknown command: " + cmd);
                }
                return linesList;
            }

            if (!basedir.exists()) {
                log.info("Creating base directory for hg repositories: " + basedir);
                if (!basedir.mkdir()) {
                    throw new HGException("Unable to create directory " + basedir.getAbsolutePath());
                }
            }
            if (!hg_logdir.exists()) {
                log.info("Cloning " + myRepositoryRoot);
                linesList = eu.exec(HG_CLONE + myRepositoryRoot, env_vars, basedir, log);
            } else {
                if (cmd.equals("head")) {
                    // No need to repeat the hg pull for other
                    // commands in this case
                    // TODO debug why pull is shown called twice
                    log.info("cmd = " + cmd);
                    linesList = eu.exec(HG_PULL_U, env_vars, hg_logdir, log);
                }
            }

            if (cmd.equals("head")) {
                linesList = eu.exec(HG_TIP, env_vars, hg_logdir, log);
            } else if (cmd.startsWith("log")) {
                linesList = eu.exec(HG_CMD + cmd, env_vars, hg_logdir, log);
            } else {
                throw new HGException("Unknown command: " + cmd);
            }
        } catch (IOException ioe) {
            throw new HGException("Failed to find " + hg_logdir.getAbsolutePath() + ": " + ioe);
        }
        return linesList;
    }

    public long getLatestRevision() throws HGException {
        log.debug("Entered getLatestRevision");

        ArrayList<String> linesList = hg_cmd("head");
        String[] lines = linesList.toArray(new String[linesList.size()]);

        String cs_line = lines[0];
        if (cs_line.startsWith("changeset:   ")) {
            String latest_revision = cs_line.substring(13).split(":")[0];
            log.debug("latest revision = " + latest_revision);
            return Long.parseLong(latest_revision);
        } else {
            throw new HGException("Unexpected hg log entry: " + cs_line);
        }
    }

    /**
     * Run hg log and return the number of log entries found. Pass the actual
     * entries back using the handler.
     */
    public int log(String[] targetPaths, long startRevision,
                   long endRevision, boolean changedPath, boolean strictNode,
                   ISVNLogEntryHandler handler) throws HGException {
        log.debug("Entered log with startRevision " + startRevision +
                ", endRevision " + endRevision);

        if (startRevision == 1) {
            // Hack to deal with empty repositories
            return 0;
        }

        ArrayList<String> linesList = hg_cmd("log -v -r" + endRevision + ":" + startRevision);
        String[] lines = linesList.toArray(new String[linesList.size()]);

        int count = 0;

        String changesetShort = null;
        String changesetFull = null;
        String author = null;
        String branch = DEFAULT_BRANCH;
        Date date = null;
        String files = null;
        StringBuilder description = null;
        Map changedPathsMap = new HashMap();

        int i = 0;
        while (i < lines.length) {
            log.debug("Parsing line " + i + " : " + lines[i]);
            if (lines[i].equals("")) {
                // Ignore blank lines between entries
            } else if (lines[i].startsWith("changeset:   ")) {
                String changeset = lines[i].substring(13);
                String[] cs = changeset.split(":");
                changesetShort = cs[0];
                changesetFull = cs[1];
            } else if (lines[i].startsWith("user:        ")) {
                author = lines[i].substring(13).split("@")[0];
            } else if (lines[i].startsWith("branch:")) {
                branch = lines[i].substring(7).trim();
            } else if (lines[i].startsWith("files:       ")) {
                // The filenames appear on one line separated by spaces
                files = lines[i].substring(13);
                changedPathsMap.clear();
                String[] s = files.split(" ");
                for (int j = 0; j < s.length; j++) {
                    /*
                   Change type is one of (M)odified, (A)dded,
                   (D)eleted, or (R)eplaced but hg -v log doesn't show
                   this information
                 */
                    String filename = s[j];
                    changedPathsMap.put(filename, new HGLogEntryPath(filename, 'M'));
                }
            } else if (lines[i].startsWith("parent:      ")) {
                // Ignore.
            } else if (lines[i].startsWith("tag:         ")) {
                // Ignore.
            } else if (lines[i].startsWith("date:        ")) {
                String dateStr = lines[i].substring(13);
                String no_tz = dateStr.substring(0, 24);
                String timezone = dateStr.substring(25);
                date = TimeUtil.parseDate(no_tz, timezone);
            } else if (lines[i].equals("description:")) {
                description = new StringBuilder();
                i++;
                while (i < lines.length && !lines[i].startsWith("changeset:   ")) {
                    //log.debug("Adding description line " + i + ": " + lines[i]);
                    description.append(lines[i++]);
                    description.append("\n");
                }
                /* The description is the final field of an entry */
                /* It is perfectly reasonable for the files line to not be
                 * present, in the case of branch merges */
                if (changesetShort == null || changesetFull == null ||
                        author == null || date == null ||
                        description == null) {
                    throw new HGException("incomplete log record: changesetShort=" +
                            changesetShort + ", changesetFull=" + changesetFull + ", author=" + author + ", date=" + date + ", files=" + files + ", description " + description);
                }
                HGLogEntry dummyData = new HGLogEntry(changedPathsMap, Long.parseLong(changesetShort), changesetFull, author, date, branch, description.toString());
                handler.handleLogEntry(dummyData);
                count++;
                log.debug("Added a new changeset: " + changesetShort);
                i--;
                //log.debug("Current line: " + i);
                changesetShort = null;
                changesetFull = null;
                author = null;
                branch = DEFAULT_BRANCH;
                files = null;
                description = null;
            } else {
                throw new HGException("Unknown header on line " + (i + 1) + ": " + lines[i]);
            }
            i++;
        }
        return count;
    }

    public void testConnection() throws HGException {
        log.debug("Entered testConnection");
    }

    /**
     * This is here to make testing parsing logs easier.
     * <p/>
     * cd target/classes
     * java -cp .:/jira/atlassian-jira-enterprise-3.4.3-standalone/atlassian-jira/WEB-INF/lib/log4j-1.2.7.jar com.xensource.hg.core.io.HGRepository
     */
    public static void main(String[] args) {
        int count = 0;
        try {
            org.apache.log4j.BasicConfigurator.configure();
            HGRepository currentRepo = new HGRepository(args[0], HGRepositoryLocation.parseURL("http://hg.hq.xensource.com/mirror/carbon/carbon/empty.hg"), "/usr/groups/xen/HG/mirror/carbon/carbon", false);
            long latest = currentRepo.getLatestRevision();
            log.debug("Latest revision is: " + latest);

            count = currentRepo.log(new String[]{""}, 0, latest, true, true, new ISVNLogEntryHandler() {
                public void handleLogEntry(HGLogEntry logEntry) {
                    log.debug("Retrieved #" + logEntry.getShortRevision() + " : " + logEntry.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Exception: " + e);
            e.printStackTrace();
        }
        log.info("End of test, found " + count + " changesets");
    }

}
