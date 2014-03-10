/*
 The MIT License

 Copyright (c) 2013 - 2013
   1. High Performance Computing Group, 
   School of Electrical Engineering and Computer Science (SEECS), 
   National University of Sciences and Technology (NUST)
   2. Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter (2013 - 2013)
   

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * File         : MPJProcessTicket.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : Oct 10, 2013
 * Revision     : $
 * Updated      : Nov 05, 2013 
 */

package runtime.processinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import runtime.constants.RTConstants;
import runtime.utils.io.IOHelper;
import runtime.utils.xml.XML;
import runtime.utils.xml.XMLList;
import runtime.utils.xml.XmlUtil;

public class MPJProcessTicket {

	private UUID ticketID;
	private String classPath;
	private int processCount;
	private int startingRank;
	private ArrayList<String> jvmArgs;
	private String workingDirectory;
	private String mainClass;
	private boolean zippedSource;
	private String sourceCode;
	private String deviceName;
	private String confFileContents;
	private ArrayList<String> appArgs;
	private String userID;
	/*  Hybrid Device */
	private int networkProcessCount;
	private int totalProcessCount;
	private String networkDevice;
	/* Debugger & Profiler */
	private boolean isDebug;
	private boolean isProfiler;
	private int debugPort;

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public int getProcessCount() {
		return processCount;
	}

	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}

	public int getStartingRank() {
		return startingRank;
	}

	public void setStartingRank(int startingRank) {
		this.startingRank = startingRank;
	}
	
	public ArrayList<String> getJvmArgs() {
		return jvmArgs;
	}

	public void setJvmArgs(ArrayList<String> jvmArgs) {
		this.jvmArgs = jvmArgs;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getConfFileContents() {
		return confFileContents;
	}

	public void setConfFileContents(String confFileContents) {
		this.confFileContents = confFileContents;
	}

	public ArrayList<String> getAppArgs() {
		return appArgs;
	}

	public void setAppArgs(ArrayList<String> appArguments) {
		this.appArgs = appArguments;
	}


	
	
	public UUID getTicketID() {
		return ticketID;
	}

	public void setTicketID(UUID ticketID) {
		this.ticketID = ticketID;
	}

	public boolean isZippedSource() {
		return zippedSource;
	}

	public void setZippedSource(boolean zippedSource) {
		this.zippedSource = zippedSource;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	
	public int getNetworkProcessCount() {
		return networkProcessCount;
	}

	public void setNetworkProcessCount(int networkProcessCount) {
		this.networkProcessCount = networkProcessCount;
	}

	public int getTotalProcessCount() {
		return totalProcessCount;
	}

	public void setTotalProcessCount(int totalProcessCount) {
		this.totalProcessCount = totalProcessCount;
	}

	public String getNetworkDevice() {
		return networkDevice;
	}

	public void setNetworkDevice(String networkDevice) {
		this.networkDevice = networkDevice;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public boolean isProfiler() {
		return isProfiler;
	}

	public void setProfiler(boolean isProfiler) {
		this.isProfiler = isProfiler;
	}

	public int getDebugPort() {
		return debugPort;
	}

	public void setDebugPort(int debugPort) {
		this.debugPort = debugPort;
	}

	public MPJProcessTicket()
	{
		this.classPath = "";
		this.processCount = 0;
		this.startingRank = 0;
		this.jvmArgs = new ArrayList<String>();
		this.workingDirectory = "";
		this.mainClass = "";
		this.deviceName = "";
		this.confFileContents = "";
		this.appArgs = new ArrayList<String>();
	
		zippedSource = false;
		ticketID = UUID.randomUUID();
		sourceCode = "";
		userID = "";
		totalProcessCount = -1;
		networkProcessCount = -1;
		networkDevice = "niodev";
		isDebug = false;
		isProfiler = false;
		debugPort = 24500;
	}




	public MPJProcessTicket(UUID ticketID, String classPath, int processCount,
			int startingRank, ArrayList<String> jvmArgs,
			String workingDirectory, String mainClass, boolean zippedCode,
			String codeFolder, String deviceName, String confFileContents,
			ArrayList<String> appArgs, int clientPort, String clientHostAddress,String userID, int nioProcessCount, int totalProcessCount,String networkDevice,boolean isDebug, boolean isProfiler,int debugPort) {
		super();
		this.ticketID = ticketID;
		this.classPath = classPath;
		this.processCount = processCount;
		this.startingRank = startingRank;
		this.jvmArgs = jvmArgs;
		this.workingDirectory = workingDirectory;
		this.mainClass = mainClass;
		this.zippedSource = zippedCode;
		this.sourceCode = codeFolder;
		this.deviceName = deviceName;
		this.confFileContents = confFileContents;
		this.appArgs = appArgs;
		this.totalProcessCount = totalProcessCount;
		this.networkProcessCount = nioProcessCount;
		this.networkDevice = networkDevice;
		this.isDebug = isDebug;
		this.isProfiler = isProfiler;
		this.debugPort = debugPort;
	}

	public XML ToXML() {
		XML processInfoXML = new XML(
				XmlUtil.getTag(RTConstants.MPJ_PROCESS_INFO));
		

		XML ticketIDXML = new XML(XmlUtil.getTag(RTConstants.TICKET_ID));	
		ticketIDXML.setText(this.ticketID.toString());	
		processInfoXML.appendChild(ticketIDXML);

		XML classPathXML = new XML(XmlUtil.getTag(RTConstants.CLASS_PATH));
		classPathXML.setText(this.classPath);	
		processInfoXML.appendChild(classPathXML);		

		XML processCountXML = new XML(XmlUtil.getTag(RTConstants.PROCESS_COUNT));	
		processCountXML.setText(Integer.toString(this.processCount));	
		processInfoXML.appendChild(processCountXML);

		XML startingRankXML = new XML(XmlUtil.getTag(RTConstants.STARTING_RANK));			
		startingRankXML.setText(Integer.toString(this.startingRank));
		processInfoXML.appendChild(startingRankXML);

		XML jvmArgsXML = new XML(XmlUtil.getTag(RTConstants.JVM_ARGS));	
		for ( String argument : this.jvmArgs)
		{
			XML argumentXML = new XML(XmlUtil.getTag(RTConstants.ARGUMENT));				
			argumentXML.setText(argument);
			jvmArgsXML.appendChild(argumentXML);
		}
		processInfoXML.appendChild(jvmArgsXML);

		XML workingDirectoryXML = new XML(
				XmlUtil.getTag(RTConstants.WORKING_DIRECTORY));				
		workingDirectoryXML.setText(this.workingDirectory);
		processInfoXML.appendChild(workingDirectoryXML);
		

		XML zippedCodeXML = new XML(XmlUtil.getTag(RTConstants.ZIPPED_SOURCE));			
		zippedCodeXML.setText(Boolean.toString(this.zippedSource));
		processInfoXML.appendChild(zippedCodeXML);

		XML codeFolderXML = new XML(XmlUtil.getTag(RTConstants.SOURCE_CODE));		
		codeFolderXML.setText(this.sourceCode);
		processInfoXML.appendChild(codeFolderXML);

		XML mainClassXML = new XML(XmlUtil.getTag(RTConstants.MAIN_CLASS));			
		mainClassXML.setText(this.mainClass);
		processInfoXML.appendChild(mainClassXML);

		XML deviceNameXML = new XML(XmlUtil.getTag(RTConstants.DEVICE_NAME));		
		deviceNameXML.setText(this.deviceName);
		processInfoXML.appendChild(deviceNameXML);

		XML confFileContentsXML = new XML(
				XmlUtil.getTag(RTConstants.CONF_FILE_CONTENTS));	
		confFileContentsXML.setText(this.confFileContents);
		processInfoXML.appendChild(confFileContentsXML);

		XML appArgsXML = new XML(XmlUtil.getTag(RTConstants.APP_ARGS));		
		for ( String argument : this.appArgs)
		{
			XML argumentXML = new XML(XmlUtil.getTag(RTConstants.ARGUMENT));				
			argumentXML.setText(argument);
			appArgsXML.appendChild(argumentXML);
		}
		processInfoXML.appendChild(appArgsXML);	
		
		XML userIDXML = new XML(XmlUtil.getTag(RTConstants.USER_ID));	
		userIDXML.setText(this.userID);
		processInfoXML.appendChild(userIDXML);
		
		
		XML nioProcessCountXML = new XML(XmlUtil.getTag(RTConstants.NETWORK_PROCESS_COUNT));	
		nioProcessCountXML.setText(Integer.toString(this.networkProcessCount));
		processInfoXML.appendChild(nioProcessCountXML);
				
		XML totalProcessCountXML = new XML(XmlUtil.getTag(RTConstants.TOTAL_PROCESS_COUNT));	
		totalProcessCountXML.setText(Integer.toString(this.totalProcessCount));
		processInfoXML.appendChild(totalProcessCountXML);
		
		XML networkDeviceXML = new XML(XmlUtil.getTag(RTConstants.NETWORK_DEVICE));	
		networkDeviceXML.setText(this.networkDevice);
		processInfoXML.appendChild(networkDeviceXML);
		
		XML debugXML = new XML(XmlUtil.getTag(RTConstants.DEBUG));	
		debugXML.setText(Boolean.toString(this.isDebug));
		processInfoXML.appendChild(debugXML);		
		
		XML debugPortXML = new XML(XmlUtil.getTag(RTConstants.DEBUG_PORT));	
		debugPortXML.setText(Integer.toString(this.debugPort));
		processInfoXML.appendChild(debugPortXML);
		
		XML profilerXML = new XML(XmlUtil.getTag(RTConstants.PROFILER));	
		profilerXML.setText(Boolean.toString(this.isProfiler));
		processInfoXML.appendChild(profilerXML);	
		
		return processInfoXML;

	}

	public void FromXML(String xmlString) {
		if (xmlString != null) {
			XML processInfoXml = new XML(xmlString);
			
			XML ticketIDXML = processInfoXml.getChild(RTConstants.TICKET_ID);
			this.ticketID = UUID.fromString(ticketIDXML.getText());


			XML classPathXML = processInfoXml.getChild(RTConstants.CLASS_PATH);
			this.classPath = classPathXML.getText();

			XML processCountXML = processInfoXml
					.getChild(RTConstants.PROCESS_COUNT);
			this.processCount = Integer.parseInt(processCountXML.getText());

			XML startingRankXML = processInfoXml
					.getChild(RTConstants.STARTING_RANK);
			this.startingRank = Integer.parseInt(startingRankXML.getText());

			XML jvmArgsXML = processInfoXml.getChild(RTConstants.JVM_ARGS);
			if(jvmArgsXML != null)
			{
				XMLList arguments = jvmArgsXML.getChildren(RTConstants.ARGUMENT);
				for(XML argumentXML : arguments )
				{
					this.jvmArgs.add( argumentXML.getText());
				}
			}
			
			XML workingDirectoryXML = processInfoXml
					.getChild(RTConstants.WORKING_DIRECTORY);
			this.workingDirectory = workingDirectoryXML.getText();
			
			XML zippedCodeXML = processInfoXml.getChild(RTConstants.ZIPPED_SOURCE);
			this.zippedSource = Boolean.parseBoolean(zippedCodeXML.getText());
			
			XML codeFolderXML = processInfoXml.getChild(RTConstants.SOURCE_CODE);
			this.sourceCode = codeFolderXML.getText();
			
			XML mainClassXML = processInfoXml.getChild(RTConstants.MAIN_CLASS);
			this.mainClass = mainClassXML.getText();

			XML deviceNameXML = processInfoXml
					.getChild(RTConstants.DEVICE_NAME);
			this.deviceName = deviceNameXML.getText();

			XML confFileContentsXML = processInfoXml
					.getChild(RTConstants.CONF_FILE_CONTENTS);
			this.confFileContents = confFileContentsXML.getText();

			XML appArgsXML = processInfoXml.getChild(RTConstants.APP_ARGS);			
			if(appArgsXML != null)
			{
				XMLList arguments = appArgsXML.getChildren(RTConstants.ARGUMENT);
				for(XML argumentXML : arguments )
				{
					this.appArgs.add( argumentXML.getText());
				}
			}	
			
			XML userIDXML = processInfoXml.getChild(RTConstants.USER_ID);
			this.userID = userIDXML.getText();
			
			
			XML nioProcessCountXML = processInfoXml.getChild(RTConstants.NETWORK_PROCESS_COUNT);
			this.networkProcessCount = Integer.parseInt(nioProcessCountXML.getText());			
			
			XML totalProcessCountXML = processInfoXml.getChild(RTConstants.TOTAL_PROCESS_COUNT);
			this.totalProcessCount = Integer.parseInt(totalProcessCountXML.getText());
			
			XML networkDeviceXML = processInfoXml.getChild(RTConstants.NETWORK_DEVICE);
			this.networkDevice = networkDeviceXML.getText();
			
			XML debugXML = processInfoXml.getChild(RTConstants.DEBUG);
			this.isDebug = Boolean.parseBoolean(debugXML.getText());
			
			XML debugPortXML = processInfoXml.getChild(RTConstants.DEBUG_PORT);
			this.debugPort = Integer.parseInt(debugPortXML.getText());

			XML profilerXML = processInfoXml.getChild(RTConstants.PROFILER);
			this.isProfiler = Boolean.parseBoolean(profilerXML.getText());
		}

	}

	public static void main(String args[]) {
		ArrayList<String> jvmArgs = new ArrayList<String>();
		jvmArgs.add("arg0");
		jvmArgs.add("arg1");
		ArrayList<String> appArgs = new ArrayList<String>();
		appArgs.add("arg0");
		appArgs.add("arg1");
		MPJProcessTicket ticket = new MPJProcessTicket(UUID.randomUUID(),"ClassPath", 10, 1,
				jvmArgs, "workingDirectory", "mainClass",false,"", "deviceName",
				"confFileContents", appArgs, 1000, "localhost","Khurram",5,10,"niodev",false,false,24500);
		IOHelper.writeCharacterFile("D:\\Test.xml", ticket.ToXML().toXmlString());
		String xmlString = IOHelper.readCharacterFile("D:\\Test.xml");
		MPJProcessTicket ticket2 = new MPJProcessTicket();
		ticket2.FromXML(xmlString);
		IOHelper.writeCharacterFile("D:\\Test2.xml", ticket2.ToXML().toXmlString());
	}

}
