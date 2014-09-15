package runtime.starter;

import java.io.*;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.*;

import java.util.Collections;
import java.util.HashMap;


import org.apache.hadoop.conf.Configuration; //apache configuration

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileContext;

import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.URL;

import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;

import org.apache.hadoop.yarn.conf.YarnConfiguration; //YARN configuration

import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;


public class MPJYarnClient {

  //conf fetches information from yarn-site.xml and yarn-default.xml.
  Configuration conf = new YarnConfiguration();
  String mpjHomeDir;

  //Number of conatiners
  final int n;
  private int SERVER_PORT = 0;
  private int DEBUG_PORT = 0;
  private String WRAPPER_INFO = "#Peer Information";


  
  public MPJYarnClient(String[] args){
  
    //Set Number of containers..
    n = Integer.parseInt(args[0]);
    DEBUG_PORT = Integer.parseInt(args[7]);
    SERVER_PORT = Integer.parseInt(args[2]);
  }
 
  public void run(String[] args) throws Exception {  
    
      Map<String, String> map = System.getenv(); 
   	
      try{
           mpjHomeDir = map.get("MPJ_HOME");
           
           if (mpjHomeDir == null) {
              throw new Exception("[MPJRun.java]:MPJ_HOME environment found..");
           }
      }
      catch (Exception exc) {
           System.out.println("[MPJRun.java]:" + exc.getMessage());
           exc.printStackTrace();
           return;
      }

      // Copy the application master jar to HDFS
      // Create a local resource to point to the destination jar path
      FileSystem fs = FileSystem.get(conf);
      Path source = new Path(mpjHomeDir+"/lib/AM.jar");
	
      String pathSuffix = "/AM.jar";
      Path dest = new Path(fs.getHomeDirectory(), pathSuffix);
      fs.copyFromLocalFile(false, true, source, dest);
      FileStatus destStatus = fs.getFileStatus(dest);
    
      YarnConfiguration conf = new YarnConfiguration();
      YarnClient yarnClient = YarnClient.createYarnClient();
      yarnClient.init(conf);
      yarnClient.start();

      // Create application via yarnClient
      YarnClientApplication app = yarnClient.createApplication();

      // Set up the container launch context for the application master
      ContainerLaunchContext amContainer = 
              Records.newRecord(ContainerLaunchContext.class);

      List <String> commands= new ArrayList<String>();
      commands.add("$JAVA_HOME/bin/java");
      commands.add(" -Xmx256M");
      commands.add(" runtime.starter.ApplicationMaster"); 
      commands.add(" "+String.valueOf(n));
      commands.add(" "+args[1]); //server name
      commands.add(" "+args[2]); //server port
      commands.add(" "+args[3]); //device name
      commands.add(" "+args[4]); //class name
      commands.add(" "+args[5]); //wdir
      commands.add(" "+args[6]); //class path
      commands.add(" "+args[8]); //protocol switch limit
      commands.add(" "+args[9]); //num of Args
      
      int numArgs = Integer.parseInt(args[9]);
      if(numArgs > 0){      
        for(int i=0; i < numArgs; i++){
          commands.add(" "+args[10+i]); 
        }
      }
     
      commands.add(" 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + 
								"/stdout");
      commands.add(" 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + 
								"/stderr"); 

      amContainer.setCommands(commands); //set commands

    // Setup local Resource for ApplicationMaster
    LocalResource appMasterJar = Records.newRecord(LocalResource.class);

    appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(dest));
    appMasterJar.setSize(destStatus.getLen());
    appMasterJar.setTimestamp(destStatus.getModificationTime());
    appMasterJar.setType(LocalResourceType.ARCHIVE);
    appMasterJar.setVisibility(LocalResourceVisibility.APPLICATION); 
    
    amContainer.setLocalResources(
          Collections.singletonMap("AM.jar", appMasterJar));
    
    // Setup CLASSPATH for ApplicationMaster
    // Setting up the environment
    Map<String, String> appMasterEnv = new HashMap<String, String>();
    setupAppMasterEnv(appMasterEnv);
    amContainer.setEnvironment(appMasterEnv);

    // Set up resource type requirements for ApplicationMaster
    Resource capability = Records.newRecord(Resource.class);
    capability.setMemory(256);
    capability.setVirtualCores(1);

    // Finally, set-up ApplicationSubmissionContext for the application
    ApplicationSubmissionContext appContext = 
                                  app.getApplicationSubmissionContext();
    
    appContext.setApplicationName("MPJ-YARN"); 
    appContext.setAMContainerSpec(amContainer);
    appContext.setResource(capability);
    appContext.setQueue("default"); // queue
    
    // Submit application
    ApplicationId appId = appContext.getApplicationId();
    System.out.println("\nSubmitting application " + appId+"\n");
    yarnClient.submitApplication(appContext);
    
    int rank = 0;
    int wport = 0;
    int rport = 0;
    ServerSocket servSock = null;
    Socket sock = null;
    Vector<Socket> socketList;
    socketList = new Vector<Socket>();
    byte[] dataFrame = null;
    String[] peers = new String[n];

    System.out.println("Creating server socket at HOST "+
                        args[1]+" PORT "+
                        args[2]+" \n Waiting for "+
                        n +" processes to connect");
    

    // Creating a server socket for incoming connections
    try {
      servSock = new ServerSocket(SERVER_PORT);
    }
    catch (Exception e) {
      System.err.println(" Error opening server port..");
      e.printStackTrace();
    }

    // Loop to read port numbers from Wrapper.java processes
    // and to create WRAPPER_INFO (containing all IPs and ports)
    for(int i = n; i > 0; i--){
      try{
        sock = servSock.accept();
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        DataInputStream in = new DataInputStream(sock.getInputStream());

        wport = in.readInt();
        rport = in.readInt();
        rank = in.readInt();
        System.out.println("\n Container "+
                           sock.getInetAddress().getHostAddress()+
			   "\n Read Port "+rport+
                           "\n Write Port "+wport+
                           "\n Rank "+rank);

        peers[rank] = ";" + sock.getInetAddress().getHostAddress() + 
				"@" + rport + "@" + wport + "@" + rank;


        socketList.add(sock);

        peers[rank] += "@" + (DEBUG_PORT);
      }
      catch (Exception e){
        System.err.println("Error accepting connection from peer socket..");
        e.printStackTrace();
      }
    }

    // Loop to sort contents of mpjdev.conf according to rank
    for(int i=0; i < n; i++) {
      WRAPPER_INFO += peers[i];
    }

    try {
      dataFrame = new byte[WRAPPER_INFO.getBytes("UTF-8").length];
      dataFrame = WRAPPER_INFO.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e){
    }
    int length = dataFrame.length;

    // Loop to broadcast WRAPPER_INFO to all Wrappers
    for(int i = n; i > 0; i--){
      try{
        sock = socketList.get(n- i);
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());

        out.writeInt(length);
        out.flush();
        out.write(dataFrame, 0, length);
        out.flush();

        sock.close();
      }
      catch (Exception e){
        System.err.println(" Error closing connection from peer socket..");
        e.printStackTrace();
      }
    }

    


    ApplicationReport appReport = yarnClient.getApplicationReport(appId);
    YarnApplicationState appState = appReport.getYarnApplicationState();
    while (appState != YarnApplicationState.FINISHED &&
           appState != YarnApplicationState.KILLED &&
           appState != YarnApplicationState.FAILED) {
      
                Thread.sleep(100);
                appReport = yarnClient.getApplicationReport(appId);
                appState = appReport.getYarnApplicationState();
    }

    System.out.println(
     " Application :" + appId + "\n" +
     " State :" + appState + "\n" +
     " Finish Time: " + appReport.getFinishTime());

  }

  private void setupAppMasterEnv(Map<String, String> appMasterEnv) {

   for (String c : conf.getStrings(
        YarnConfiguration.YARN_APPLICATION_CLASSPATH,
        YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
              Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
              c.trim());
    }

    Apps.addToEnvironment(appMasterEnv,
        Environment.CLASSPATH.name(),
        Environment.PWD.$() + File.separator + "*");
  }

  public static void main(String[] args) throws Exception {
      MPJYarnClient client = new MPJYarnClient(args);
      client.run(args);  
  }

}
