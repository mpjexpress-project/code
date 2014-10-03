
package runtime.starter;

import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;



public class MPJAppMaster {

  Configuration conf = new YarnConfiguration();
  String mpjHomeDir;
  int rank = 0;
 
  public void run(String[] args) throws Exception {
  
    Map<String, String> map = System.getenv();
    mpjHomeDir = map.get("MPJ_HOME");
    
    if (mpjHomeDir == null) {
      throw new Exception("[MPJRun.java]:MPJ_HOME "+
						  "environment not found..");
    }
   

    // number of container to be launched
    final int n = Integer.valueOf(args[0]);
    System.out.println("Number of containers to Launch : "+n);

    // Uploading YarnWrapper.jar to HDFS
    // FIXME: I will workout some way to upload jars in local Filesystem
    
    FileSystem fs = FileSystem.get(conf);
    Path source = new Path(mpjHomeDir+"/lib/mpjYarnWrapper.jar");
    String pathSuffix = "/mpjYarnWrapper.jar";
    Path dest = new Path(fs.getHomeDirectory(), pathSuffix);
    fs.copyFromLocalFile(false, true, source, dest);
    FileStatus destStatus = fs.getFileStatus(dest);
   
    Path sourceUserClass = new Path(args[5]+"/"+args[4]+".class");
    String suffixUserClass = "/"+args[4]+".class";
    Path destUserClass = new Path(fs.getHomeDirectory(),suffixUserClass);
    fs.copyFromLocalFile(false,true,sourceUserClass,destUserClass);
    FileStatus destStatusClass = fs.getFileStatus(destUserClass); 

    // Initialize AM <--> RM communication protocol
    AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
    rmClient.init(conf);
    rmClient.start();

    // Initialize AM <--> NM communication protocol
    NMClient nmClient = NMClient.createNMClient();
    nmClient.init(conf);
    nmClient.start();

    // Register with ResourceManager
    System.out.println("registerApplicationMaster 0");
    rmClient.registerApplicationMaster("", 0, "");
    	   
    // Priority for containers - priorities are intra-application
    Priority priority = Records.newRecord(Priority.class);
    priority.setPriority(0);

    // Resource requirements for containers
    Resource capability = Records.newRecord(Resource.class);
    capability.setMemory(128);
    capability.setVirtualCores(1);

    // Make container requests to ResourceManager
    for (int i = 0; i < n; ++i) {
      ContainerRequest containerAsk =
        		new ContainerRequest(capability, null, null, priority);
      
      System.out.println("Making container request " + i);
      rmClient.addContainerRequest(containerAsk);
    }	
  
    Map<String,LocalResource> localResources = 
			 	new HashMap <String,LocalResource> ();
    // Creating Local Resource for Wrapper   
    LocalResource wrapperJar = Records.newRecord(LocalResource.class);

    wrapperJar.setResource(ConverterUtils.getYarnUrlFromPath(dest));
    wrapperJar.setSize(destStatus.getLen());
    wrapperJar.setTimestamp(destStatus.getModificationTime());
    wrapperJar.setType(LocalResourceType.ARCHIVE);
    wrapperJar.setVisibility(LocalResourceVisibility.APPLICATION);
 
    // Creating Local Resource for UserClass
    LocalResource userClass = Records.newRecord(LocalResource.class);
  
    userClass.setResource(ConverterUtils.getYarnUrlFromPath(destUserClass));
    userClass.setSize(destStatusClass.getLen());
    userClass.setTimestamp(destStatusClass.getModificationTime());
    userClass.setType(LocalResourceType.FILE);
    userClass.setVisibility(LocalResourceVisibility.APPLICATION);

    localResources.put("mpjYarnWrapper.jar", wrapperJar);
    localResources.put(args[4]+".class", userClass);


    // Obtain allocated containers and launch 
    int allocatedContainers = 0;
    int completedContainers = 0;
    	
    while (allocatedContainers < n) {
		
      AllocateResponse response = rmClient.allocate(0);
      		
      for (Container container : response.getAllocatedContainers()) {
        	   
        ++allocatedContainers;
	System.out.println("Container at: " +container.getNodeHttpAddress()+
        			              		      " Allocated !");

       	ContainerLaunchContext ctx = 
			      Records.newRecord(ContainerLaunchContext.class);
        
        List <String> commands = new ArrayList<String>();
       
        commands.add(" $JAVA_HOME/bin/java");
        commands.add(" -Xmx128M");
        commands.add(" -cp " +"."
            + File.pathSeparator + "" + mpjHomeDir + "/lib/loader1.jar"
            + File.pathSeparator + "" + mpjHomeDir + "/lib/mpj.jar"
            + File.pathSeparator + "" + mpjHomeDir + "/lib/log4j-1.2.11.jar"
            + File.pathSeparator + "" + mpjHomeDir + "/lib/mpjYarnWrapper.jar"
            );
        commands.add(" runtime.starter.MPJYarnWrapper");
        commands.add(" " + args[1]);  // server name
        commands.add(" " + args[2]);  // server port
        commands.add(" " + args[3]);  // device name
        commands.add(" " + args[4]);  // class name
        commands.add(" " + args[6]);  // protocol switch limit
        commands.add(" " + args[0]);  // no. of containers 
        commands.add(" " + Integer.toString(rank++)); // rank
        commands.add(" " + args[7]); //num of Args
        
        int numArgs = Integer.parseInt(args[7]);
        if( numArgs > 0){
          for(int i = 0; i < numArgs; i++){
            commands.add(" "+ args[8+i]);
          }
        }
        commands.add(" 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + 
                                                                  "/stdout");
        commands.add(" 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + 
                                                                  "/stderr");
       	
        ctx.setCommands(commands);	
	System.out.println("Launching container " + allocatedContainers);

	// Set local resource for containers
	ctx.setLocalResources(
	        // Collections.singletonMap("mpjYarnWrapper.jar", wrapperJar)
		localResources);
      
       	// Set environment for container
       	Map<String, String> containerEnv = new HashMap<String, String>();
       	setupEnv(containerEnv);
       	ctx.setEnvironment(containerEnv);

        // Time to start the container
	nmClient.startContainer(container, ctx);
      }	
      
      for (ContainerStatus status : response.getCompletedContainersStatuses()) {
        ++completedContainers;
        System.out.println("Completed container " + completedContainers);
      }

      Thread.sleep(100);

    } // end While

    // Now wait for containers to complete
    while (completedContainers < n) {
      AllocateResponse response = rmClient.allocate(completedContainers/n);

      for (ContainerStatus status : response.getCompletedContainersStatuses()){
        ++completedContainers;
        System.out.println("Completed container " + completedContainers);
      }
	
      Thread.sleep(100);
    }

    // Un-register with ResourceManager 
    rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, 
								     "", "");
  
  } //end run()

  private void setupEnv(Map<String, String> containerEnv) {
    for (String c : conf.getStrings(
      YarnConfiguration.YARN_APPLICATION_CLASSPATH,
      YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
                    
      Apps.addToEnvironment(containerEnv, 
				Environment.CLASSPATH.name(), c.trim());
                    
    }

    Apps.addToEnvironment(containerEnv,
                          Environment.CLASSPATH.name(),
                          Environment.PWD.$() + File.separator + "*");
  } //end setupEnv()

  

  public static void main(String[] args) throws Exception {
    MPJAppMaster am =new MPJAppMaster();
    am.run(args);
  }

}//end class
