package runtime.starter;

import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;

public class KillYarnApp extends Thread {

  private ApplicationId appId;
  private YarnClient appManager;

  public KillYarnApp(ApplicationId appId, YarnClient appManager) {
    this.appId = appId;
    this.appManager = appManager;
  }

  @Override
  public void run() {
    if (MPJYarnClient.isRunning) {
      try {
          appManager.killApplication(appId); 
          System.out.println("Application Killed Forcefully");

      } catch (Exception exp) {
        System.err.println("Error when Killing Application");
        exp.printStackTrace();
      }
    }
  }

}
