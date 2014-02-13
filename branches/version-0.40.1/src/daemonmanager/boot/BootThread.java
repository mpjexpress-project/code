package daemonmanager.boot;
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
 * File         : PMThread.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : January 30, 2013 6:00:57 PM 2013
 * Revision     : $
 * Updated      : $
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import daemonmanager.constants.PMMessages;
import daemonmanager.pmutils.IOUtil;
import daemonmanager.pmutils.PMThread;
import daemonmanager.pmutils.ProcessUtil;


public class BootThread extends PMThread {
	private String host = "localhost";
	private String port = "8888";
	ProcessBuilder pb = null;

	public BootThread(String machineName, String deamonPort) {
		host = machineName;
		port = deamonPort;
		
	}

	public void run() {
		try {
			bootNetWorkMachines();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void bootNetWorkMachines() throws IOException {

	if (validExecutionParams()) {
			try {
				String[] command = { "ssh", host, "nohup", "java", "-cp",
						IOUtil.getJarPath("daemon") + ":.",
						"runtime.daemon.MPJDaemon", port,
						">" + IOUtil.getWrapperLogPath(),
						"2>" + IOUtil.getWrapperLogPath(),

				};				
				ArrayList<String> consoleMessages = ProcessUtil.runProcess(command,false);
				String pid = ProcessUtil.getMPJProcessID(host);
				if (!pid.equals("") && Integer.parseInt(pid) > -1
						&& Integer.parseInt(pid) < 30000)
				{
					System.out.println(IOUtil.FormatMessage(host, PMMessages.MPJDAEMON_STARTED + pid));
				}
				else
				{
					for (String message : consoleMessages) 
						System.out.println(message);
				}			

			} catch (Exception ex) {
				System.out.print(ex.getMessage() + "\n" + ex.getStackTrace());
			}
		}

	}

	private boolean validExecutionParams() {

		
		String pid = ProcessUtil.getMPJProcessID(host, pb);
		if (!pid.equals("")) {
			System.out.println(IOUtil.FormatMessage(host, PMMessages.MPJDAEMON_ALREADY_RUNNING + pid));
			return false;
		}
		InetAddress address = null;
		try {

			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			System.out.println(e.getMessage());
			return false;
		}
		if (IOUtil.IsBusy(address, Integer.parseInt(port))) {
			System.out.println(IOUtil.FormatMessage(host, PMMessages.BUSY_PORT ));			
			return false;
		}

		return true;
	}

}