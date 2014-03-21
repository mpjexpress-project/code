package daemonmanager.halt;

import java.io.IOException;
import java.util.ArrayList;

import daemonmanager.boot.WinBoot;
import daemonmanager.constants.PMMessages;
import daemonmanager.pmutils.IOUtil;
import daemonmanager.pmutils.ProcessUtil;

public class WinHalt {

	private String host = "localhost";	
	
	public void haltMPJExpress() 
	{
		ProcessBuilder pb = new ProcessBuilder();
		String pid = ProcessUtil.getMPJProcessID(host,pb);	
		if(pid != "")
		{
			String[] command = { "taskkill","/f", "/pid", pid, };
			ArrayList<String> consoleMessages = ProcessUtil.runProcess(command);
			for (String message : consoleMessages) {
				if (message.indexOf(PMMessages.UNKNOWN_HOST) > 0)
					System.out.println(IOUtil.FormatMessage(host, PMMessages.HOST_INACESSABLE ));						
			}
			pid = ProcessUtil.getMPJProcessID(host);
			if(pid == "")
				System.out.println(IOUtil.FormatMessage(host, PMMessages.MPJDAEMON_STOPPED ));		
		}
		else 
			System.out.println(IOUtil.FormatMessage(host, PMMessages.MPJDAEMON_NOT_AVIALBLE ));		
	}
	

}
