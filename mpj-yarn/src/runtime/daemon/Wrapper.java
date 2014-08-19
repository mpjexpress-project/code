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
  
  String serverName = null;
  int serverPort = 0;
  private String WRAPPER_INFO = null;

  public Wrapper(ThreadGroup group, String name) {
    super(group, name);
  }

  /**
   * Executes MPJ program in a new JVM. This method is invoked in main
   * method of this class, which is started by MPJDaemon. This method 
   * can start multiple threads in a JVM. This will also parse configuration
   * file.
   * 
   * @param args
   *          Arguments to this method. args[0] is configFileName 'String',
   *          args[1] is number of processes, args[2] is deviceName, args[3] is
   *          hostname of MPJRun.java server, args[4] is the port number of
   *          MPJRun.java server, args[5] is rank, args[6] is className
   */
  public void execute(String args[]) throws Exception {

    InetAddress localaddr = InetAddress.getLocalHost();
    hostName = localaddr.getHostName();
    //System.out.println("FK[wrapper.java]:running on " + hostName);

    configFileName = args[0];
    processes = (new Integer(args[1])).intValue();
    deviceName = args[2];
    serverName = args[3];
    serverPort = Integer.parseInt(args[4]);
    rank = args[5];
    className = args[6];

    int tmp = mpjrunConnect(findPort(), findPort());

    /* This code segment is used to append the received mpjdev.conf 
     * contents into the already existing mpjdev.conf file
     */
    /*StringTokenizer conf_file = new StringTokenizer(WRAPPER_INFO, ";");
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(configFileName, true);
    }
    catch (FileNotFoundException e) {
      System.err.println("["+hostName+"-Wrapper.java]: mpjdev.conf not found..");
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
      System.err.println("["+hostName+"-Wrapper.java]: Error closing mpjdev.conf");
      e.printStackTrace();
    }*/

    /* This code sengment appends the received port numbers into
     * the argument string of device Init code.
     */
    String tmpx = configFileName.concat(WRAPPER_INFO);
    

    nargs = new String[(args.length - 7)];
    System.arraycopy(args, 7, nargs, 0, nargs.length);

    c = Class.forName(className);

    try {
      System.out.println("["+hostName+"]: Starting process <"+rank+">");

      String arvs[] = new String[nargs.length + 3];

      arvs[0] = rank;
      arvs[1] = configFileName;
      arvs[2] = deviceName;

      System.out.println("New string: " + tmpx);

      for (int i = 0; i < nargs.length; i++) {
	arvs[i + 3] = nargs[i];
      }

      /* FK -> Tmp code to read MPJDEV.conf */
/*      BufferedReader in = new BufferedReader(new FileReader(arvs[1]));
      String line;
      while( (line = in.readLine()) != null )
        System.out.println(line);
      in.close();
*/
      /* Temporarily commented until argument parsing can be sorted */
      
      Method m = c.getMethod("main", new Class[] { arvs.getClass() });
      m.setAccessible(true);
      int mods = m.getModifiers();
      if (m.getReturnType() != void.class || !Modifier.isStatic(mods)
	  || !Modifier.isPublic(mods)) {
	throw new NoSuchMethodException("main");
      }
      
      m.invoke(null, new Object[] { arvs });
      
      System.out.println("["+hostName+"]: Process <"+rank+"> completed");
    }
    catch (Exception ioe) {
      System.err.println("["+hostName+"-Wrapper.java]: Multi-threaded starter: exception" + ioe.getMessage());
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
   * Returns an avaiable port on the system.
   * <p>
   * This method searches for a random port between 25,000 and 40,000.
   * It opens up a server socket on this port to confirm availability
   * and then passes it back. If the port is not available, another 
   * random port is generated.
   *
   * @param selectedPort Integer value of the available port
   */

  private int findPort(){
    int minPort = 25000;
    int maxPort = 40000;
    int selectedPort;
    ServerSocket sock = null;
    DatagramSocket dataSock = null;

    /* The loop generates a random port number, opens a socket on 
     * the generated port
     */

    while(true){
      Random rand = new Random();
      selectedPort = (rand.nextInt((maxPort - minPort) + 1) + minPort);
 
      try {
        sock = new ServerSocket(selectedPort);
        sock.setReuseAddress(true);
      }
      catch (IOException e) {
        System.err.println("[Wrapper.java]:"+hostName+"-"+selectedPort+"]Port already in use. Checking for a new port..");
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
    Socket clientSock = null;

    try {
      clientSock = new Socket(serverName, serverPort);
      DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
      DataInputStream in = new DataInputStream(clientSock.getInputStream());

      out.writeInt(wport);
      out.flush();
      out.writeInt(rport);
      out.flush();
      out.writeInt(Integer.parseInt(rank));
      out.flush();

      //int r = in.readInt();
      //rank = Integer.toString(r);

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
