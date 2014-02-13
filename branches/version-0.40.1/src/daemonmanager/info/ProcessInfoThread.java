package daemonmanager.info;

import java.util.ArrayList;

import daemonmanager.constants.PMMessages;
import daemonmanager.pmutils.PMThread;
import daemonmanager.pmutils.ProcessUtil;

public class ProcessInfoThread extends PMThread {
	
	private String host = "localhost";

	public ProcessInfoThread(String machineName) {
		host = machineName;
	}

	public void run() {
		getJavaProcesses();
	}
	
	public void getJavaProcesses() 
	{
		String userName  = System.getProperty("user.name");
		ArrayList<String> consoleMessages = ProcessUtil.getJavaProcesses(host);	
		int messageCount = 0;
		for (String message : consoleMessages) 
		{
			if(message.toLowerCase().indexOf("jps") < 0)
			{
				System.out.println("[" +userName +" @ "+ host + "] "+ message);
				messageCount++;
			}
		}
		if(messageCount == 0)
		{
			System.out.println("[" +userName +" @ "+ host + "] "+ PMMessages.NO_JAVA_PROCESS_RUNNING);
		}
	}


}
