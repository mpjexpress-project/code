package runtime.starter;

import java.io.*;
import java.lang.reflect.*;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.*;
import java.net.*;


  public class MPJYarnWrapper {

    public Socket clientSock = null;
    int processes = 0;
    String className = null;
    Class c = null;
    String deviceName = null;
    String rank = null;
    String[] nargs = null;
    String hostName = null;
    String serverName = null;
    int serverPort = 0;
    private String WRAPPER_INFO = null;
    String portInfo;

    public void run(String[] args){
      serverName = args[0];
      serverPort = Integer.parseInt(args[1]);
      deviceName = args[2];
      className = args[3];
      portInfo ="#Number of Processes;"+args[5]+
                                  ";#Protocol Switch Limit;"+args[4]+";";

      rank =args[6];
      try{
        InetAddress localaddr = InetAddress.getLocalHost();
        hostName = localaddr.getHostName();
        clientSock = new Socket(serverName, serverPort);
      }
      catch(UnknownHostException exp){
        System.out.println("Unknown Host Exception, Host not found");
        exp.printStackTrace();
      }catch(IOException exp){
        exp.printStackTrace(); 
      }
     
      // connect MPJYarnWrapper to MPJYarnClient
      yarnClientConnect(findPort(), findPort(),clientSock);
      
      // Redirecting Output Stream 
      try{
        System.setOut(new PrintStream(clientSock.getOutputStream())); 
      }
      catch(IOException e){
       e.printStackTrace();
      }
      try{
        c = Class.forName(className);
      }
      catch(ClassNotFoundException exp){
        exp.printStackTrace();
      }
      
      try {
        System.out.println("["+hostName+"]: Starting process <"+rank+">");

        String [] arvs;
        int numArgs=Integer.parseInt(args[7]);

        arvs = new String[3+numArgs];

        arvs[0] = rank;
        arvs[1] = portInfo.concat(WRAPPER_INFO);
        arvs[2] = deviceName;
	  
        for(int i=0; i < numArgs; i++){
          arvs[3+i]=args[8+i];
        }
 
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
        System.err.println("["+hostName+"-MPJYarnWrapper.java]:Multi-threaded"+
                                  " starter: exception" + ioe.getMessage());
        ioe.printStackTrace();
      }

    }

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
        System.err.println("[MPJYarnWrapper.java]:"+hostName+"-"+
               selectedPort+"]Port already in use. Checking for a new port..");
        continue;
      }

      try {
        sock.close();
      }
      catch (IOException e){
        System.err.println("["+hostName+":MPJYarnWrapper.java]: IOException"+
                        " encountered in closing sockets: "+e.getMessage());
        e.printStackTrace();
        }
      break;
    }

    return selectedPort;
  }

 private void yarnClientConnect(int wport, int rport, Socket clientSock){

    try {
      DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
      DataInputStream in = new DataInputStream(clientSock.getInputStream());

      out.writeInt(wport);
      out.flush();
      out.writeInt(rport);
      out.flush();
      out.writeInt(Integer.parseInt(rank));
      out.flush();

      int len = in.readInt();
      byte[] dataFrame = new byte[len];
      in.readFully(dataFrame);
      WRAPPER_INFO = new String(dataFrame, "UTF-8");
      
     // clientSock.close();
   }
   catch (IOException e){
     e.printStackTrace();
   }
  }



    public static void main(String args[]) throws Exception {
      MPJYarnWrapper wrapper = new MPJYarnWrapper();
      wrapper.run(args); 
    }
  }
                                                                             
