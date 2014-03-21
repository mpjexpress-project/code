package daemonmanager.boot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import daemonmanager.constants.PMConstants;
import daemonmanager.constants.PMMessages;
import daemonmanager.pmutils.CLOptions;
import daemonmanager.pmutils.IOUtil;
import daemonmanager.pmutils.ProcessUtil;

public class WinBoot {
	
	public WinBoot()
	{
	}
	private String host = "localhost";
	private String port = IOUtil.getConfigValue(PMConstants.CONF_PORT_KEY);
	
	public void startMPJExpress() throws IOException {

		if (validExecutionParams()) {
				try {
					String[] command = { "java", "-cp",
							IOUtil.getJarPath("daemon") + ";.",
							"runtime.daemon.MPJDaemon", port,
					};				
					ArrayList<String> consoleMessages = ProcessUtil.runProcess(command,false);
					String pid = ProcessUtil.getMPJProcessID(host);
					if (!pid.equals("") && Integer.parseInt(pid) > -1
							&& Integer.parseInt(pid) < 30000)
					{
						System.out.println(IOUtil.FormatMessage(host, PMMessages.MPJDAEMON_STARTED + pid));
					}
					else
					{
						for (String message : consoleMessages) 
							System.out.println(message);
					}			

				} catch (Exception ex) {
					System.out.print(ex.getMessage() + "\n" + ex.getStackTrace());
				}
			}

		}

		private boolean validExecutionParams() {		
		
			InetAddress address = null;
			try {
				address = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
				System.out.println(e.getMessage());
				return false;
			}
			if (IOUtil.IsBusy(address, Integer.parseInt(port))) {
				System.out.println(IOUtil.FormatMessage(host, PMMessages.BUSY_PORT ));			
				return false;
			}
			return true;
		}
		
}
