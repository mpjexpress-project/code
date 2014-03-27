package runtime.daemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionManager extends Thread {
	public volatile boolean isRun = true;
	public ConnectionManager() {
		// TODO Auto-generated constructor stub
	}
	public void run() {
			
		while(isRun)
		{
			for (Socket sock : MPJDaemon.servSockets.keySet())
			{				
				OutputStream outToServer = null;
				try {
					String line = "@Ping#\n";
					outToServer = sock.getOutputStream();
					DataOutputStream out = new DataOutputStream(outToServer);
					out.write(line.getBytes(), 0, line.getBytes().length);
		
				} catch (Exception e) {
					System.out.println("Client Disconnected");					
					MPJDaemon.servSockets.get(sock).killProcesses();	
					MPJDaemon.servSockets.remove(sock);
				}			
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Exiting connection manager thread");	
	}
}
