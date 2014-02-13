
package runtime.daemon;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

class OutputHandler extends Thread {

	Process p = null;
	Socket sock = null;

	public OutputHandler(Process p,Socket cSock) {
		this.p = p;
		sock = cSock;
	}

	public void run() {

		InputStream outp = p.getInputStream();
		String line = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(outp));

		if (MPJDaemon.DEBUG && MPJDaemon.logger.isDebugEnabled()) {
			MPJDaemon.logger.debug("outputting ...");
		}

		try {
			do {
				if (!line.equals("")) {
					line.trim();

					synchronized (this) {
						  line += "\n";
						  OutputStream outToServer = sock.getOutputStream();
					      DataOutputStream out =      new DataOutputStream(outToServer);     			
					      out.write(line.getBytes(),0,line.getBytes().length);
					      out.flush();
					}
				}
			} while ((line = reader.readLine()) != null);
			// && !kill_signal);
		} catch (Exception e) {
			if (MPJDaemon.DEBUG && MPJDaemon.logger.isDebugEnabled()) {
				MPJDaemon.logger.debug("outputHandler =>" + e.getMessage());
			}
			e.printStackTrace();
		}
	} // end run.
}