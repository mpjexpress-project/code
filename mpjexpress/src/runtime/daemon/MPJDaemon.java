/*
 The MIT License

 Copyright (c) 2005 - 2011
   1. Distributed Systems Group, University of Portsmouth (2005)
   2. Aamir Shafi (2005 - 2011)
   3. Bryan Carpenter (2005 - 2011)
   4. Mark Baker (2005 - 2011)

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
 * File         : MPJDaemon.java 
 * Author       : Aamir Shafi, Bryan Carpenter, Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : Sun Dec 12 12:22:15 BST 2004
 * Revision     : $Revision: 1.28 $
 * Updated      : $Date: 2013/11/05 17:24:47 $
 */

package runtime.daemon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerRepository;

import runtime.common.MPJRuntimeException;

public class MPJDaemon {

  private int D_SER_PORT = 10000;
  static final boolean DEBUG = false;
  static Logger logger = null;
  private String mpjHomeDir = null;
  public volatile static ConcurrentHashMap<Socket, ProcessLauncher> servSockets;
  ConnectionManager connectionManager;
  PortManagerThread pManager;

  public MPJDaemon(String args[]) throws Exception {

    System.out.println("MPJ Daemon started");
    InetAddress localaddr = InetAddress.getLocalHost();
    String hostName = localaddr.getHostName();
    servSockets = new ConcurrentHashMap<Socket, ProcessLauncher>();
    Map<String, String> map = System.getenv();
    mpjHomeDir = map.get("MPJ_HOME");
    createLogger(mpjHomeDir, hostName);
    if (DEBUG && logger.isDebugEnabled()) {
      logger.debug("mpjHomeDir " + mpjHomeDir);
    }
    if (args.length == 1) {

      if (DEBUG && logger.isDebugEnabled()) {
	logger.debug(" args[0] " + args[0]);
	logger.debug("setting daemon port to" + args[0]);
      }

      D_SER_PORT = new Integer(args[0]).intValue();

    } else {
      throw new MPJRuntimeException("Usage: java MPJDaemon daemonServerPort");
    }
    pManager = new PortManagerThread();
    pManager.start();

    connectionManager = new ConnectionManager();
    connectionManager.start();
    serverSocketInit();

  }

  private void createLogger(String homeDir, String hostName)
      throws MPJRuntimeException {

    if (logger == null) {

      DailyRollingFileAppender fileAppender = null;

      try {
	fileAppender = new DailyRollingFileAppender(new PatternLayout(
	    " %-5p %c %x - %m\n"), homeDir + "/logs/daemon-" + hostName
	    + ".log", "yyyy-MM-dd-a");

	Logger rootLogger = Logger.getRootLogger();
	rootLogger.addAppender(fileAppender);
	LoggerRepository rep = rootLogger.getLoggerRepository();
	rootLogger.setLevel((Level) Level.ALL);
	logger = Logger.getLogger("mpjdaemon");
      }
      catch (Exception e) {
	throw new MPJRuntimeException(e);
      }
    }
  }

  private void serverSocketInit() {

    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(D_SER_PORT);
      do {
	Socket servSock = serverSocket.accept();
	if (DEBUG && logger.isDebugEnabled()) {
	  logger.debug("Accepted connection");
	}
	ProcessLauncher pLaunch = new ProcessLauncher(servSock);
	servSockets.put(servSock, pLaunch);
	pLaunch.start();
      } while (true);
    }
    catch (IOException ioEx) {
      System.out.println("Unable to attach to port!");
      System.exit(1);
    }
    if (!serverSocket.isClosed())
      try {
	serverSocket.close();
      }
      catch (IOException e) {
	e.printStackTrace();
      }
    if (pManager != null) {
      pManager.isRun = false;
    }
    if (connectionManager != null) {
      connectionManager.isRun = false;
    }

  }

  public static void main(String args[]) {
    try {
		
      MPJDaemon dae = new MPJDaemon(args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
