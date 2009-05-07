package com.xensource.hg.util;

import java.io.*;
import java.util.ArrayList;
import java.lang.Runtime;
import java.lang.Process;
import org.apache.log4j.Logger;

public class ExecUtil {
    public ExecUtil() {
    }

/**
 * Call Runtime.exec() and capture stdout and stderr,
 */
    public ArrayList<String> exec(String cmd, String[] env_vars, File curr_dir, Logger log) throws IOException {
    ArrayList<String> result = new ArrayList<String>();
    
    try {
	if (curr_dir != null) {
	    log.info("In " + curr_dir.getAbsolutePath() + " running: " + cmd);
	} else {
	    log.info("Running: " + cmd);
	}
	Process p = Runtime.getRuntime().exec(cmd, env_vars, curr_dir);
	StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", null, log);
	StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", result, log);
	errorGobbler.start();
	outputGobbler.start();
	
	if (p.waitFor() != 0) {
	    // Wait for the threads to finish reading
	    outputGobbler.join();
	    errorGobbler.join();
	    // Close streams
	    p.getErrorStream().close();
	    p.getInputStream().close();
	    throw new IOException("Non-zero return value from command");
	}
	// Wait for the threads to finish reading
	outputGobbler.join();
	errorGobbler.join();
	// Close streams
	p.getErrorStream().close();
	p.getInputStream().close();
    } catch (InterruptedException ie) {
	log.info("command interrupted");
    }
    return result;
}

/**
 * Handle all of the output from an exec call.
 */
class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    ArrayList<String> lines;
    Logger log;

    StreamGobbler(InputStream is, String type, ArrayList<String> linesList, Logger logger)
    {
	this.is = is;
	this.type = type;
	this.lines = linesList;
	this.log = logger;
    }
    
    public void run()
    {
	try
	    {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line=null;
		while ( (line = br.readLine()) != null) {
		    if (type.equals("OUTPUT")) {
			lines.add(line);
		    } else {
			log.error(type + ": " + line);
		    }
		}
	    } catch (IOException ioe)
	    {
		ioe.printStackTrace();  
	    }
    }
}

}