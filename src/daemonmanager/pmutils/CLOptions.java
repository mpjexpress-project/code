/*
 The MIT License

 Copyright (c) 2013 - 2013
   1. High Performance Computing Group, 
   School of Electrical Engineering and Computer Science (SEECS), 
   National University of Sciences and Technology (NUST)
   2. Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter (2013 - 2013)
   

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * File         : CLOptions.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : January 30, 2013 6:00:57 PM 2013
 * Revision     : $
 * Updated      : $
 */

package daemonmanager.pmutils;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import daemonmanager.constants.PMConstants;
import daemonmanager.constants.PMMessages;

import daemonmanager.pmutils.CLOptions;
import daemonmanager.pmutils.IOUtil;

public class CLOptions {

	private ArrayList<String> machineList;
	private int threadCount;
	private boolean bThreading;
	private String cmdType;
	private String userCmd;
	private String machineFilePath;
	private String port;

	public CLOptions(ArrayList<String> machineList, int threadCount,
			boolean bThreading, String cmdType, String userCmd,String port) {
		super();
		this.machineList = machineList;
		this.threadCount = threadCount;
		this.bThreading = bThreading;
		this.cmdType = cmdType;
		this.userCmd = userCmd;
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public CLOptions() {
		super();
		this.machineList = new ArrayList<String>();
		this.threadCount = 20;
		this.bThreading = true;
		this.cmdType = PMConstants.STATUS;
		this.userCmd = "";
		this.machineFilePath = "";
		this.port = PMConstants.DAEMON_PORT_NUMBER;
		this.machineFilePath = IOUtil.getMachineFilePath();
	}

	public ArrayList<String> getMachineList() {
		return machineList;
	}

	public void setMachineList(ArrayList<String> machineList) {
		this.machineList = machineList;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int nThreads) {
		this.threadCount = nThreads;
	}

	public boolean isbThreading() {
		return bThreading;
	}

	public void setbThreading(boolean bThreading) {
		this.bThreading = bThreading;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public String getUserCmd() {
		return userCmd;
	}

	public void setUserCmd(String userCmd) {
		this.userCmd = userCmd;
	}

	public String getMachineFilePath() {
		return machineFilePath;
	}

	public void setMachineFilePath(String machineFilePath) {
		this.machineFilePath = machineFilePath;
	}

	public void PrintOptions() {
		System.out.println("Command Type: " + this.cmdType);
		System.out.println("Machine File Path: " + this.machineFilePath);
		for (String hostname : this.machineList)
			System.out.println("Host Name: " + hostname);
		System.out.println("No of Threads: " + this.threadCount);
		System.out.println("Threading On/Off: " + this.bThreading);

	}
	public void PrintHelp() {
		System.out.println(FormatCMDoptionMessages("",PMConstants.HELP,PMMessages.CMD_OPT_HELP ));
		System.out.println(FormatCMDoptionMessages(PMConstants.BOOT_OPT , PMConstants.BOOT ,PMMessages.CMD_OPT_BOOT ));
		System.out.println(FormatCMDoptionMessages(PMConstants.HALT_OPT, PMConstants.HALT ,PMMessages.CMD_OPT_HALT ));
		System.out.println(FormatCMDoptionMessages(PMConstants.CLEAN_OPT , PMConstants.CLEAN ,PMMessages.CMD_OPT_CLEAN ));
		System.out.println(FormatCMDoptionMessages(PMConstants.STATUS_OPT , PMConstants.STATUS ,PMMessages.CMD_OPT_STATUS ));
		System.out.println(FormatCMDoptionMessages(PMConstants.INFO_OPT , PMConstants.INFO ,PMMessages.CMD_OPT_INFO ));
		
		System.out.println(FormatCMDoptionMessages(PMConstants.MACHINE_FILE_OPT , PMConstants.MACHINE_FILE ,PMMessages.CMD_OPT_MACHINE_FILE ));
		System.out.println(FormatCMDoptionMessages(PMConstants.HOSTS_OPT , PMConstants.HOSTS ,PMMessages.CMD_OPT_HOSTS ));
		System.out.println(FormatCMDoptionMessages(PMConstants.THREAD_COUNT_OPT , PMConstants.THREAD_COUNT ,PMMessages.CMD_OPT_THREAD_COUNT ));
		System.out.println(FormatCMDoptionMessages(PMConstants.THREADED_OPT , PMConstants.THREADED ,PMMessages.CMD_OPT_THREADED ));
		System.out.println(FormatCMDoptionMessages(PMConstants.PORT_OPT , PMConstants.PORT ,PMMessages.CMD_OPT_PORT ));
		System.out.println("");
		

	}
	public String FormatCMDoptionMessages(String shortOption,String longOption,String description)	
	{
		String message ="";
		
		String option ="- " ;
		if(shortOption == "") 
			option = option+ longOption + ": ";
		else
			option+= shortOption +"|"+ longOption + ": ";
		
		for(int i=option.length();i<=15;i++)
			option+= " ";
		message = option + description;	
		return message;
	}
	
	@SuppressWarnings("static-access")
	public  void parseCommandLineArgs(String[] args) {		
		
		CommandLineParser parser = new PosixParser();

		Options options = new Options();
		options.addOption(new Option(PMConstants.HELP_OPT, PMConstants.HELP,
				false, PMMessages.CMD_OPT_HELP));
		options.addOption(new Option(PMConstants.BOOT_OPT, PMConstants.BOOT,
				false, PMMessages.CMD_OPT_BOOT));
		options.addOption(new Option(PMConstants.HALT_OPT, PMConstants.HALT,
				false, PMMessages.CMD_OPT_HALT));
		options.addOption(new Option(PMConstants.CLEAN_OPT, PMConstants.CLEAN,
				false, PMMessages.CMD_OPT_CLEAN));
		options.addOption(new Option(PMConstants.STATUS_OPT,
				PMConstants.STATUS, false, PMMessages.CMD_OPT_STATUS));
		options.addOption(new Option(PMConstants.INFO_OPT,
				PMConstants.INFO, false, PMMessages.CMD_OPT_INFO));
		options.addOption(new Option(PMConstants.MACHINE_FILE_OPT,
				PMConstants.MACHINE_FILE, true, PMMessages.CMD_OPT_MACHINE_FILE));
		options.addOption(OptionBuilder.withLongOpt(PMConstants.HOSTS)
				.hasArgs().withDescription(PMMessages.CMD_OPT_HOSTS)
				.withValueSeparator(' ').create(PMConstants.HOSTS_OPT));
		options.addOption(new Option(PMConstants.THREAD_COUNT_OPT,
				PMConstants.THREAD_COUNT, true, PMMessages.CMD_OPT_THREAD_COUNT));
		options.addOption(new Option(PMConstants.THREADED_OPT,
				PMConstants.THREADED, true, PMMessages.CMD_OPT_THREADED));
		options.addOption(new Option(PMConstants.PORT_OPT, PMConstants.PORT,
				true, PMMessages.CMD_OPT_PORT));

		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption(PMConstants.HELP))
				this.setCmdType(PMConstants.HELP);
			else if (line.hasOption(PMConstants.BOOT))
				this.setCmdType(PMConstants.BOOT);
			else if (line.hasOption(PMConstants.HALT))
				this.setCmdType(PMConstants.HALT);
			else if (line.hasOption(PMConstants.STATUS))
				this.setCmdType(PMConstants.STATUS);
			else if (line.hasOption(PMConstants.INFO))
				this.setCmdType(PMConstants.INFO);
			else if (line.hasOption(PMConstants.CLEAN))
				this.setCmdType(PMConstants.CLEAN);

			if (line.hasOption(PMConstants.HOSTS)) {
				String[] hosts = line.getOptionValues(PMConstants.HOSTS);
				for (String host : hosts)
					this.getMachineList().add(host);
			}
			else if (line.hasOption(PMConstants.MACHINE_FILE)) {
				String machineFilePath = line
						.getOptionValue(PMConstants.MACHINE_FILE);
				this.setMachineFilePath(machineFilePath);
			}
			

			if (line.hasOption(PMConstants.THREAD_COUNT)) {
				int nThreads = Integer.parseInt(line
						.getOptionValue(PMConstants.THREAD_COUNT));
				this.setThreadCount(nThreads);
			}
			if (line.hasOption(PMConstants.THREADED)) {
				boolean bThread = Boolean.parseBoolean(line
						.getOptionValue(PMConstants.THREADED));
				this.setbThreading(bThread);
			}
			if (line.hasOption(PMConstants.PORT)) {
				Integer port = Integer.parseInt(line.getOptionValue(PMConstants.PORT));
				this.setPort(port.toString());
			}
			

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	public static void main(String[] args) {
		CLOptions options  = new CLOptions();
		options.parseCommandLineArgs(args);
		options.PrintOptions();
	}


}
