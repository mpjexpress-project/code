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
 * File         : IOMessagesThread.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : Oct 28, 2013
 * Revision     : $
 * Updated      : Nov 05, 2013 
 */

package runtime.starter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class IOMessagesThread extends Thread {

  Socket clientSock;
  int wport;
  int rport;
  int rank;
  String runtimeType;
  int DEBUG_PORT;
 

  public IOMessagesThread(Socket clientSock, int DEBUG_PORT,
					     String runtimeType) {
    this.clientSock = clientSock;
    this.DEBUG_PORT= DEBUG_PORT;
    this.runtimeType = runtimeType;
  }
  public void setSock(Socket clientSock){
    this.clientSock = clientSock;
  }

  @Override
  public void run() {
    serverSocketInit();
  }

  private void serverSocketInit() {
    Scanner input = null;
    PrintWriter output = null;
    try {
      input = new Scanner(clientSock.getInputStream());
      output = new PrintWriter(clientSock.getOutputStream(), true);
      String message = input.nextLine();
      while (!(message.endsWith("EXIT"))) {
        if (message.equals("Sending Rank and Ports")){
          //get write port
          wport = Integer.parseInt(input.nextLine());
          //get read port
          rport = Integer.parseInt(input.nextLine());
          //get rank
          rank  = Integer.parseInt(input.nextLine());

          //Implement logger and print container information

          String info= ";" + clientSock.getInetAddress().getHostAddress() +
                        "@" + rport + "@" + wport + "@" + rank +
                        "@" + DEBUG_PORT;
          
          if(runtimeType.equals("YARN")){
            System.out.println("Connection Established: "+
                              clientSock.getInetAddress().getHostAddress()+
                             "\nRead Port "+rport+
                             "\nWrite Port "+wport+
                             "\nRank "+rank+"\n");


            MPJYarnClient.broadCast(rank,info);
          }
          else{
            MPJRun.broadCast(rank,info);
          }        
        }
	else if(!message.startsWith("@Ping#"))
          System.out.println(message);
	message = input.nextLine();
      }

    }
    catch (Exception cce) {
      cce.printStackTrace();
      //System.exit(0);
    }
    finally {
      try {
        //System.out.println("Closing Socket "+clientSock.getInetAddress().
	//						getHostAddress());
	clientSock.close();
	input.close();
	output.close();
      }
      catch (IOException ioEx) {
        ioEx.printStackTrace();
        //System.exit(1);
      }
    }
  }

}
