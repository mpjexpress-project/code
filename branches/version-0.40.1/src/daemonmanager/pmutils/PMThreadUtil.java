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
 * File         : PMThreadUtil.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : January 30, 2013 6:00:57 PM 2013
 * Revision     : $
 * Updated      : $
 */

package daemonmanager.pmutils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import daemonmanager.boot.BootThread;
import daemonmanager.cleanup.CleanUpThread;
import daemonmanager.constants.PMConstants;
import daemonmanager.halt.HaltThread;
import daemonmanager.info.ProcessInfoThread;

import daemonmanager.pmutils.CLOptions;
import daemonmanager.pmutils.IOUtil;
import daemonmanager.pmutils.PMThread;
import daemonmanager.status.StatusThread;

public class PMThreadUtil 
{
	public static ExecutorService getThreadExecutor(int nThreads)
	{
		return  Executors.newFixedThreadPool(nThreads);
	}
	
	public static void ExecuteThreads(ArrayList<Thread> threads,int nThreads)
	{
		
			ExecutorService tpes = getThreadExecutor( nThreads);
			for (Thread thread : threads) 
			{					
				tpes.execute(thread);
			}		
			tpes.shutdown();
			try {
				
				tpes.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		
		
	}
	public static void ExecuteCommand(CLOptions options)
	{
		String type = options.getCmdType();			
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ArrayList<String> machinesList = new ArrayList<String>();	
		if(options.getMachineList().size() > 0)
			machinesList = options.getMachineList();
		else
			machinesList = IOUtil.readMachineFile(options.getMachineFilePath());
		if(machinesList!=null && machinesList.size() > 0) {
		for (String host : machinesList)
		{
			PMThread thread = null;
			if(type.equals(PMConstants.BOOT))
			{
				thread =   new BootThread(host,options.getPort());		
			}
			else if(type.equals(PMConstants.HALT))
			{
				thread =   new HaltThread(host);		
			}
			else if(type.equals(PMConstants.CLEAN))
			{
				thread =   new CleanUpThread(host);		
			}
			else if(type.equals(PMConstants.STATUS))
			{
				thread =   new StatusThread(host);		
			}
			else if(type.equals(PMConstants.INFO))
			{
				thread =   new ProcessInfoThread(host);		
			}
			if(thread != null)
			{
				if(options.isbThreading())
					threads.add(thread);
				else 
					thread.run();
			}
				
		}
		if(options.isbThreading())
			ExecuteThreads(threads,options.getThreadCount());
		}
	}
	
	
}
