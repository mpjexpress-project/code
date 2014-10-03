
package runtime.starter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class IOYarnThread extends Thread {

  Socket clientSock = null;
  BufferedReader in = null;

  public IOYarnThread(Socket clientSock){
    this.clientSock = clientSock;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(
                    new InputStreamReader(clientSock.getInputStream()));
     
      String message = in.readLine();
      while (!(message.endsWith("EXIT"))) {
        if(!message.startsWith("@Ping#"))
          System.out.println(message);
        message = in.readLine();
      }
    }
    catch (Exception exp) {
      exp.printStackTrace();
    }
  }
}                         
