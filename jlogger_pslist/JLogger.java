/*
** Description:
**
** JLogger
**
** A class that calls any external command and logs the output
** to a log file via the log4j framework. Using log4j gives us 
** the ability to control the maximum log file size, gives us 
** log file rotation, and variable log file format, etc, all 
** for free.
**
** JeremyC 14-11-2019
*/

import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JLogger {
    static String strVersion = "JLogger 1.01";
	static Logger logger = Logger.getLogger(JLogger.class);
	static Boolean bDebug = false;
	
	// Default command to run. This is expected to be overridden
	// by using the "-c" input command-line option.
	static String[] strDumpCmd = { "pslist.exe", "/accepteula" };

	private static void printUsage() {
		String errString = "\n" + strVersion + 										   "\n" +
				"Usage:																	\n" +
				"java JLogger [-r num] [-d secs] -s dump_dir -c command	[args]		  	\n" +
				"Where:																	\n" +
				"   -v            - Display verbose output.								\n" +
				"   -r num        - Number of consecutive dumps (default 1).			\n" +
				"   -d secs       - Delay between consecutive dumps (default 60 secs).	\n" +
				"   -s dump_dir   - Directory containing the jlogger.properties	log4j   \n" +
				"                   config file and also the JLogger runtime files.     \n" +
				"                   The output log files will also appear here.		    \n" +
				"   -c command    - The command and arguments to execute. Any remaining	\n" +
				"                   values are considered arguments to the command.     \n" +
				"																	    \n" +                        
				"Example:																\n" +
				"java JLogger -r 2 -d 60 -s \"C:\\temp\\jlogger_1\" -c pslist /accepteula	\n";
		System.err.println(errString);
	}

	public static void main(String[] args) {
		int nTotalDumps = 1;	// Number of consecutive dumps.
		int nDelaySecs = 60;	// Delay between consecutive dumps.
		String strDumpDir = null;

		if (args.length < 2) {
			printUsage();
			System.exit(-1);
		}

		for (int i=0; i<args.length; i++) {
			if (args.length == 1 && (args[0].equals("-h") || args[0].equals("/?"))) {
				printUsage();
				System.exit(0);
			}

			if (args[i].equals("-v")) {
				bDebug = true;
			}

			if (args[i].equals("-r")) {
				// Repeat n times
				if (args.length < i+2) {
					System.err.println("ERROR: No repeat value specified");
					System.exit(-2);
				}
				nTotalDumps = Integer.parseInt(args[i+1]);
				if (bDebug) System.out.println("-r: nTotalDumps=" + nTotalDumps);
			}
	
			if (args[i].equals("-d")) {
				// Delay before next repeat dump
				if (args.length < i+2) {
					System.err.println("ERROR: No delay value specified");
					System.exit(-2);
				}
				nDelaySecs = Integer.parseInt(args[i+1]);
				if (bDebug) System.out.println("-r: nDelaySecs=" + nDelaySecs);
			}
			
			if (args[i].equals("-s")) {
				// Runtime and output directory
				if (args.length < i+2) {
					System.err.println("ERROR: No dump directory specified");
					System.exit(-2);
				}
				strDumpDir = args[i+1].trim();
				if (bDebug) System.out.println("-s: strDumpDir=" + strDumpDir);
			}
			
			if (args[i].equals("-c")) {
				// Command to execute, including arguments.
				if (args.length < i+2) {
					System.err.println("ERROR: No command specified");
					System.exit(-2);
				}
				// Grab the remainder of the arguments as the command and arguments to execute.
				int nArgs = args.length - i - 1;
				strDumpCmd = new String[nArgs];
				System.arraycopy(args, i + 1, strDumpCmd, 0, nArgs);
				if (bDebug) System.out.println("-c: strDumpCmd=" + String.join(" ", strDumpCmd));
				break;	// When we see the "-c" option, the remaining args are considered the args to the command.
			}
		}

		// Check mandatory arguments.
		if (strDumpDir == null) {
			System.err.println("ERROR: No dump directory specified");
			System.exit(-2);
		}

		// Read our Log4j properties file.
		String propertiesFile = strDumpDir + File.separator + "jlogger.properties"; 
		File f = new File(propertiesFile);
		if (!f.exists()){
			System.err.println("Error: No such file: " + propertiesFile);
			System.exit(-3);
		}
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propertiesFile));
		}
		catch (Exception e) 
		{
			System.err.println("Error loading property file: " + propertiesFile);
			System.exit(-3);
		}
		
		// Set the full path to the output log file location.
		String strLogFileName = props.getProperty("log4j.appender.A2.File");
		String strLogFilePath = strDumpDir + File.separator + strLogFileName;
		props.setProperty("log4j.appender.A2.File", strLogFilePath);
		
		// Set the log4j properties back.
		PropertyConfigurator.configure(props);
		
		// Build the external command to run. 
		String[] strCmd = strDumpCmd;
		
		// Run the external command n times.
		for (int i=1; i<=nTotalDumps; i++) {
			runCmd(strCmd, i, nTotalDumps);
			if (i < nTotalDumps) {
				// More dumps to come, so add delay.
				try {
					Log("");
					Log("Sleeping for " + nDelaySecs + " seconds before next execution ...");
					Log("");
					Thread.sleep(nDelaySecs * 1000);
				}
				catch (InterruptedException e) {
					System.err.println( "awakened prematurely" );
				}
			}
		}
	}

	private static String getTimeNow() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private static void Log(String s) {
		logger.info(s);
		System.out.println(s);
	}

	private static void printPrologue(String strCmd, int nDumpNum, int nTotalDumps) {
		Log("");
		//Log("#########################################");
		//Log(getTimeNow());
		Log("### DUMP START:");
		Log(strCmd);
		//Log("Dump number " + nDumpNum + " of " + nTotalDumps + " dumps.");
		//Log("#########################################");
	}
	private static void printEpilogue(String strCmd, int nDumpNum, int nTotalDumps) {
		//Log("#########################################");
		//Log(getTimeNow());
		Log("### DUMP END");
		//Log("Dump number " + nDumpNum + " of " + nTotalDumps + " dumps.");
		//Log("#########################################");
	}

	private static void runCmd(String[] strCmd, int nDumpNum, int nTotalDumps) {
		printPrologue(String.join(" ", strCmd), nDumpNum, nTotalDumps);

		if (bDebug) {
			System.out.println("runCmd: strCmd=\"" + String.join(" ", strCmd) + "\", nDumpNum=" + nDumpNum + ", nTotalDumps=" + nTotalDumps);
		}
		
		Process p = null;
		BufferedReader bri = null;
		BufferedReader bre = null;
		try {	 
			String line;
			p = Runtime.getRuntime().exec(strCmd);

			/*
			 14-11-2019 JeremyC: For some (yet) unknown reason, I can't 
			 reliably capture both stderr and stdout. Only capture stdout
			 for now.
				
			// Log stderr
			bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = bre.readLine()) != null) {
				logger.error(line);
			}
			*/

			// Log stdout
			bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = bri.readLine()) != null) {
				logger.info(line);
			}
			
			if (p != null) 
				p.waitFor();
			if (bre != null) 
				bre.close();
			if (bri != null) 
				bri.close();
		} 
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		printEpilogue(String.join(" ", strCmd), nDumpNum, nTotalDumps);
	}
}
