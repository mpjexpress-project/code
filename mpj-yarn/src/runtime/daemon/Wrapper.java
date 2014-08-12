/*
 The MIT License

 Copyright (c) 2005 - 2010
   1. Distributed Systems Group, University of Portsmouth (2005)
   2. Aamir Shafi (2005 - 2010)
   3. Bryan Carpenter (2005 - 2010)
   4. Mark Baker (2005 - 2010)

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
 * File         : Wrapper.java 
 * Author       : Aamir Shafi, Bryan Carpenter
 * Created      : Sun Dec 12 12:22:15 BST 2004
 * Revision     : $Revision: 1.19 $
 * Updated      : $Date: Wed Mar 31 15:22:37 PKT 2010$
 */

package runtime.daemon;

import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.apache.log4j.*;

public class Wrapper extends Thread {

  String configFileName = null;
  int processes = 0;
  String className = null;
  Class c = null;
  String deviceName = null;
  String rank = null;
  String[] nargs = null;
  String hostName = null;
  String args[] = null;

  //FK>> Variables to communicate with MPJRun.java
  String serverIP = null;
  int serverPort = 0;
  private String WRAPPER_INFO = null;

  public Wrapper(ThreadGroup group, String name) {
    super(group, name);
  }

  /**
   * Executes MPJ program in a new JVM. This method is invoked in main method of
   * this class, which is started by MPJDaemon. This method can start multiple
   * threads in a JVM. This will also parse configuration file.
   * 
   * @param args
   *          Arguments to this method. args[0] is configFileName 'String',
   *          args[1] is number of processes, args[2] is deviceName, args[3] is
   *          rank, args[4] is className
   */
  public void execute(String args[]) throws Exception {

    InetAddress localaddr = InetAddress.getLocalHost();
    hostName = localaddr.getHostName();
    // #FK - Prepping for port search and allocation
    //System.out.println("FK[wrapper.java]:running on " + hostName);

    configFileName = args[0];
    processes = (new Integer(args[1])).intValue();
    deviceName = args[2];
    rank = args[3];
    className = args[4];

    // #FK - Checking for arguments
    //System.out.println("FK>> I will read "+configFileName+", np is "+processes+",device to be used is "+deviceName+", rank would be "+rank+", and class:"+className);
    int tmp1 = findPort();
    int tmp2 = findPort();
    System.out.println("["+hostName+"]:Port comm status = "+ mpjrunConnect(tmp1,tmp2));

    StringTokenizer conf_file = new StringTokenizer(WRAPPER_INFO, ";");
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(configFileName, true);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    PrintStream cout = new PrintStream(out);
    
    while(conf_file.hasMoreTokens()) {
      String token = conf_file.nextToken();
      cout.println(token);
    }
    cout.close();
    try {
      out.close();
    }
    catch (IOException e){
    }

    nargs = new String[(args.length - 5)];
    System.arraycopy(args, 5, nargs, 0, nargs.length);

    c = Class.forName(className);

    try {
      System.out.println("Starting process <" + rank + "> on <" + hostName
	  + ">");

      String arvs[] = new String[nargs.length + 3];

      arvs[0] = rank;
      arvs[1] = configFileName;
      arvs[2] = deviceName;

      for (int i = 0; i < nargs.length; i++) {
	arvs[i + 3] = nargs[i];
      }

      /* FK -> Tmp code to read MPJDEV.conf */
      /*BufferedReader in = new BufferedReader(new FileReader(arvs[1]));
      String line;
      while( (line = in.readLine()) != null )
        System.out.println(line);
      in.close();*/

      Method m = c.getMethod("main", new Class[] { arvs.getClass() });
      m.setAccessible(true);
      int mods = m.getModifiers();
      if (m.getReturnType() != void.class || !Modifier.isStatic(mods)
	  || !Modifier.isPublic(mods)) {
	throw new NoSuchMethodException("main");
      }
      
      System.out.println("#FK> Going to invoke method");
      m.invoke(null, new Object[] { arvs });

      System.out.println("Stopping process <" + rank + "> on <" + hostName
	  + ">");
    }
    catch (Exception ioe) {
      System.out
	  .println("multi-threaded starter: exception" + ioe.getMessage());
      ioe.printStackTrace();
    }

  }

  public void run() {
    try {
      execute(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
  * #FK
  * input: none
  * output: integer
  * description: function to scan and allocate free ports
  *
  **/
  private int findPort(){
    //System.out.println("["+hostName+"]#FK> Generating ports numbers..");
    int minPort = 25000;
    int maxPort = 40000;
    int selectedPort;
    ServerSocket sock = null;
    DatagramSocket dataSock = null;

    while(true){
      Random rand = new Random();
      selectedPort = (rand.nextInt((maxPort - minPort) + 1) + minPort);
      //System.out.println("#FK> Port generated:"+selectedPort+"]. Checking availability..");
 
      try {
        sock = new ServerSocket(selectedPort);
        sock.setReuseAddress(true);
      }
      catch (IOException e) {
        System.err.println("["+hostName+":"+selectedPort+"]Port already in use. Checking for new port..");
        continue;
      }
      
      try {
        sock.close();
      }
      catch (IOException e){
        System.err.println("["+hostName+":Wrapper.java]: IOException encountered in closing sockets: "+e.getMessage());
        e.printStackTrace();
        }
      break;
    }

    //System.out.println("#FK> Port successfully generated..");
    return selectedPort;
  }

  /**
  * #FK
  * input: Two integers
  * output: Integer (0 for success, 1 for error)
  * description: function to send selected ports to MPJRun.java
  *
  **/
  private int mpjrunConnect(int wport, int rport){
    //System.out.println("#FK>[Wrapper.java]:I am going to send ports!");
    Socket clientSock = null;

    try {
      clientSock = new Socket("barq.seecs.edu.pk", 40003);
      DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
      DataInputStream in = new DataInputStream(clientSock.getInputStream());

      out.writeInt(wport);
      out.flush();
      out.writeInt(rport);
      out.flush();

      int len = in.readInt();
      byte[] dataFrame = new byte[len];
      in.readFully(dataFrame);
      WRAPPER_INFO = new String(dataFrame, "UTF-8");
      //System.out.println("I received: " + WRAPPER_INFO);
      
      clientSock.close();
   }
   catch (IOException e){
   }

    return 1;
  }

  public static void main(String args[]) throws Exception {
    ThreadGroup group = new ThreadGroup("MPI" + args[3]);
    Wrapper wrap = new Wrapper(group, args[3]);
    wrap.args = args;
    wrap.start();
    wrap.join();
  }
}
