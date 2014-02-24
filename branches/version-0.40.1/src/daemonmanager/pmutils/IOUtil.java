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
 * File         : IOUtil.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : January 30, 2013 6:00:57 PM 2013
 * Revision     : $
 * Updated      : $
 */
package daemonmanager.pmutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import daemonmanager.constants.PMConstants;
import daemonmanager.pmutils.IOUtil;

public class IOUtil {

	public static ArrayList<String> readMPJFile(String path) {

		ArrayList<String> machineList = new ArrayList<String>();
		FileInputStream stream = null;
		InputStreamReader streamReader = null;
		BufferedReader bufferedReader = null;

		try {
			stream = new FileInputStream(path);
			streamReader = new InputStreamReader(stream, "UTF-8");
			bufferedReader = new BufferedReader(streamReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line=line.trim();
				if (line != "" && !machineList.contains(line))	
				{
//					 InetAddress address = InetAddress.getByName(line);
//				     String machineIP = address.getHostAddress();
//				     machineList.add(machineIP);
					 machineList.add(line);
				}
			}

		} catch (Exception exp) {

			exp.printStackTrace();
			return null;

		} finally {
			try {
				bufferedReader.close();
				stream.close();

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return machineList;
	}

	public static ArrayList<String> readMachineFile(String path) {
		return readMPJFile(path);
	}

	public static String getConfigValue(String property) {
		for (String configLine : readWrapperConfigFile()) {
			if (configLine.indexOf(property) > -1) {
				String[] tokens = configLine.split("=");
				if (tokens.length > 1)
					return tokens[1];
			}
		}
		return "";
	}

	public static ArrayList<String> readWrapperConfigFile() {
		String path = getWrapperConfPath();
		return readMPJFile(path);
	}

	public static String getWrapperConfPath() {
		return getMPJHomeDir() + PMConstants.CONF + File.separator
				+ PMConstants.WRAPPER_CONF;

	}

	public static String getMPJHomeDir() {
		String mpjHomeDir = "";
		Map<String, String> map = System.getenv();
		mpjHomeDir = map.get("MPJ_HOME");
		if (mpjHomeDir != "") {
			if (!mpjHomeDir.endsWith("/"))
				mpjHomeDir = mpjHomeDir + File.separator;
		}
		return mpjHomeDir;
	}

	public static String getWrapperLogPath() {
		return getMPJHomeDir() + PMConstants.LOGS + File.separator
				+ PMConstants.WRAPPER_LOG;
	}

	public static String getJarPath(String jarName) {
		return getMPJHomeDir() + PMConstants.LIB + File.separator + jarName
				+ PMConstants.EXT_JAR;
	}

	public static String getMachineFilePath(String fileName) {
		return IOUtil.getMPJHomeDir() + fileName;
	}

	public static String getMachineFilePath() {
		return IOUtil.getMPJHomeDir() + PMConstants.MACHINES;
	}

	public static boolean IsBusy(final InetAddress remote, int port) {
		try {
			Socket s = new Socket(remote, port);
			s.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String FormatMessage(String host, String message) {
		return "[" + host + "] " + message;
	}

	
	
	

}
