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
 * File         : ProcessLauncher.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : Oct 10, 2013
 * Revision     : $
 * Updated      : Nov 05, 2013 
 */

package runtime.daemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Logger;

import runtime.portmanager.PortManager;
import runtime.processinfo.MPJProcessTicket;

public class ProcessLauncher extends Thread {


	boolean DEBUG = true;
	private Process p[] = null;
	private Socket sockClient = null;
	private Socket sockserver = null;
	private Logger logger = MPJDaemon.logger;

	public ProcessLauncher( Socket sockServer) {

		this.sockserver = sockServer;
	}

	@Override
	public void run() {
		ExecuteJob();
	}

	private void ExecuteJob() {

		System.out.println("Job Started");
		MPJProcessTicket pTicket = new MPJProcessTicket();

		try {

			String ticketString = getStringFromInputStream(sockserver
					.getInputStream());
			logger.debug(ticketString);
			if (ticketString != "")
				pTicket.FromXML(ticketString);
			
		} catch (IOException e3) {
			e3.printStackTrace();
			return;
		}	
		try {
			sockClient = new Socket(pTicket.getClientHostAddress(),
					pTicket.getClientPort());
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		int JvmProcessCount = 0;
		if (pTicket.getDeviceName().equals("niodev")) {
			JvmProcessCount = pTicket.getProcessCount();
		} else if (pTicket.getDeviceName().equals("hybdev")) {
			JvmProcessCount = 1;
		}
	
		OutputHandler[] outputThreads = new OutputHandler[JvmProcessCount];
		p = new Process[JvmProcessCount];
		ProcessArgumentsManager  argManager = new ProcessArgumentsManager(pTicket);
		String [] arguments = argManager.GetArguments(pTicket);
		

		for (int j = 0; j < JvmProcessCount; j++) {		
			if (pTicket.getDeviceName().equals("niodev")) 
			{
				String rank = new String("" + (pTicket.getStartingRank() + j));
				arguments[argManager.getRankArgumentIndex()] = rank;
				if(pTicket.isProfiler())
					arguments[arguments.length + 1] = "-tau:node=" + rank;			
			}
			if(pTicket.isDebug())
			{
				arguments[arguments.length + 1] = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + (pTicket.getDebugPort() + j * 2);
			}
			
			if (MPJDaemon.DEBUG && logger.isDebugEnabled()) {
				String args = "";
				for (int i = 0; i < arguments.length; i++) {
					args += arguments[i] + " ";		
				}
				logger.debug(args);
			}		
		
			ProcessBuilder pb = new ProcessBuilder(arguments);
			Map<String, String> map = System.getenv();
			String mpjHomeDir = map.get("MPJ_HOME");
			pb.directory(new File(mpjHomeDir));
			pb.redirectErrorStream(true);;

			if (DEBUG && logger.isDebugEnabled()) {
				logger.debug("starting the process ");
			}
			logger.debug("starting the process ");

			try {
				p[j] = pb.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * Step 4: Start a new thread to handle output from this
			 * particular JVM. FIXME: Now this seems like a good amount of
			 * overhead. If we start 4 JVMs on a quad-core CPU, we also
			 * start 4 additional threads to handle I/O. Is it possible to
			 * get rid of this overhead?
			 */
			outputThreads[j] = new OutputHandler(p[j], sockClient);
			outputThreads[j].start();

			if (DEBUG && logger.isDebugEnabled()) {
				logger.debug("started the process ");
			}
		} // end for loop.

		

		// Wait for the I/O threads to finish. They finish when
		// their corresponding JVMs finish.
		for (int j = 0; j < JvmProcessCount; j++) {
			try {
				outputThreads[j].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (DEBUG && logger.isDebugEnabled()) {
			logger.debug("Stopping the output");
		}

		OutputStream outToServer = null;
		try {
			outToServer = sockClient.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			out.write("EXIT".getBytes(), 0, "EXIT".getBytes().length);
			System.out.println("Job finished");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (!sockClient.isClosed())
				try {
					outToServer.close();
					sockClient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		// Its important to kill all JVMs that we started ...
		synchronized (p) {
			for (int i = 0; i < JvmProcessCount; i++)
				p[i].destroy();

		}
		for (Integer port : argManager.getProcessesPorts()) {
			PortManager.usedPorts.remove(port);
		}
		try {
			if (!sockserver.isClosed())
				sockserver.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (DEBUG && logger.isDebugEnabled()) {
			logger.debug("\n\n ** .. execution ends .. ** \n\n");
		}

	}

	private String getStringFromInputStream(InputStream is) {

		DataInputStream in = new DataInputStream(is);
		int len = 0;
		try {
			len = in.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Ticket length  " + len);
		byte[] xml = new byte[len];
		try {
			
			in.readFully(xml, 0, len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(xml);
	}
}
