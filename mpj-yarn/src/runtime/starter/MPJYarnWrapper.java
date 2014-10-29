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
    String portInfo;

    public void run(String[] args){
      serverName = args[0];
      serverPort = Integer.parseInt(args[1]);
      deviceName = args[2];
      className = args[3];
      portInfo ="#Number of Processes;"+args[5]+
                ";#Protocol Switch Limit;"+args[4]+
                ";#Server Name;"+serverName+";#Server Port;"+args[7];

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
     
      // Redirecting Output Stream 
      try{
        System.setOut(new PrintStream(clientSock.getOutputStream(),true)); 
        System.setErr(new PrintStream(clientSock.getOutputStream()));
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
        int numArgs=Integer.parseInt(args[8]);

        arvs = new String[3+numArgs];

        arvs[0] = rank;
        arvs[1] = portInfo;
        arvs[2] = deviceName;
	  
        for(int i=0; i < numArgs; i++){
          arvs[3+i]=args[9+i];
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
        System.out.println("EXIT");
        try{
          clientSock.close();
        }
	catch(IOException e){
          e.printStackTrace();
        }
      }
      catch (Exception ioe) {
        ioe.printStackTrace();
      }

    }


    public static void main(String args[]) throws Exception {
      MPJYarnWrapper wrapper = new MPJYarnWrapper();
      wrapper.run(args); 
    }
  }
                                                                             
