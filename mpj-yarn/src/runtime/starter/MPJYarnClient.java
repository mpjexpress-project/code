package runtime.starter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
  
  public MPJYarnClient(String[] args){
  
    //Set Number of containers..
    n = Integer.parseInt(args[0]);

  }
 
  public void run() throws Exception {  
    
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
    
      amContainer.setCommands(
          Collections.singletonList(
              "$JAVA_HOME/bin/java" +
              " -Xmx256M" +
              " runtime.starter.ApplicationMaster " +
              " " + String.valueOf(n) +
              " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" +
              " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"
          )
      );

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
    ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
    
    appContext.setApplicationName("MPJ-YARN"); 
    appContext.setAMContainerSpec(amContainer);
    appContext.setResource(capability);
    appContext.setQueue("default"); // queue
    
    // Submit application
    ApplicationId appId = appContext.getApplicationId();
    System.out.println("\nSubmitting application " + appId+"\n");
    yarnClient.submitApplication(appContext);

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
      client.run();  
  }

}
