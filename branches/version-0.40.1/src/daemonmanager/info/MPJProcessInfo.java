package daemonmanager.info;

import daemonmanager.pmutils.CLOptions;
import daemonmanager.pmutils.PMThreadUtil;

public class MPJProcessInfo {

	public static void main(String[] args) {
		CLOptions options  = new CLOptions();
		options.parseCommandLineArgs(args);		
		MPJProcessInfo info = new MPJProcessInfo();
		info.getJavaProcessInfo(options);		
	}
	public void getJavaProcessInfo(CLOptions options) 
	{
		PMThreadUtil.ExecuteCommand(options);		
	}
}
