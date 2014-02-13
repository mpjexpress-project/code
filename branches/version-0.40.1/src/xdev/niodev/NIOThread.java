package xdev.niodev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class NIOThread extends Thread {
	
	private  SocketChannel channel;
	private String machineName;
	private int port;
	
	public NIOThread( SocketChannel channel,String machineName,int port)
	{
		this.channel = channel;
		this.machineName = machineName;
		this.port = port;
	}
	public void run() {
		connectClientChannel();
	}
	public void connectClientChannel()
	{
		try {
			channel.connect(new InetSocketAddress(machineName,port));
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
