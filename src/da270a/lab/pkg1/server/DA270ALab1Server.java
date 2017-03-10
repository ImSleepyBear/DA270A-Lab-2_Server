/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package da270a.lab.pkg1.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Mohini
 */
public class DA270ALab1Server {

    private static final int PORT = 8000; //Deafult port
    private ServerSocket serverSocket = null;
    private boolean shutdownServer = false; // this variable designed to be changed from a diffrent thread 

    Socket socket;

    BufferedReader buffRead;

    BufferedOutputStream buffOutStream;
    PrintStream printStream;

    public static void main(String[] args) throws InterruptedException {

        int port = PORT;
        if (args.length > 0) {
//            System.out.println("port initialized");
            port = Integer.parseInt(args[0]);
        }
        new DA270ALab1Server(port);
    }

    public DA270ALab1Server(int port) throws InterruptedException {

        System.out.println("port initialized");
        
        File file = new File(".");

        // create a server socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error in the creation of the server socket." + e);
            System.exit(-1);
        }

        log("Server is Started on port : " + port);

        try {
            SERVER_CONN:
            while (true) { //wait for connection 
                socket = null;
                try {
                    // waiting for a connection, only one connection at a time
                    log("Server is waiting for a connection...");
                    socket = serverSocket.accept(); //code block here until connection

                    log("Got request from " + socket.getInetAddress());

                    buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    buffOutStream = new BufferedOutputStream(socket.getOutputStream()); //will be used to send files
                    printStream = new PrintStream(buffOutStream); //will be used to send messages

                    while (true) { //wait for input from client
                        String request = null;
                        socket.setSoTimeout(120000); //wait 120 sec max
                        try {
                            log("The Server is waiting for a command...");
                            long waitCommandTime = System.currentTimeMillis();

                            WAIT_COMMAND:
                            do {
                                Thread.sleep(10);
                                if (buffRead.ready()) {
                                    request = buffRead.readLine();
                                    waitCommandTime = System.currentTimeMillis();
                                    if (request.equals("ACK")) {
                                        log("got ACK");
                                        //ignore the response and take the next command
                                        continue WAIT_COMMAND;
                                    } else {
                                        break WAIT_COMMAND;
                                    }
                                }
                                ping();
                            } while (true);

                        } catch (Exception e) {
                            System.out.println(e);
                            sendLine("The connnection dropped because TimeOut, it's been over 120 sec with no command.");
                            continue SERVER_CONN; //wait for another connection 
                        }

                        //when request is null it mean the clinet socket closed
                        if (request == null) {
                            log("The connection closed from client's side.");
                            continue SERVER_CONN; //take next connection
                        }

                        if (request.equalsIgnoreCase("curdir")) {
                            log("got curdir");
                            sendLine("Current Folder \n" + file.getAbsolutePath());
                            sendLineTerminal();
                            printStream.flush();
                            
                        } else if (request.equalsIgnoreCase("list")) {
                            log("got list");
                            File[] files = file.listFiles();
                            sendLine("Total Files and Folders: " + files.length);
                            for (File f : files) {
                                if (f.isDirectory()) {
                                    sendLine("[" + f.getName() + "]");
                                } else {
                                    sendLine(f.getName() + "\t\t\t" + f.length() / 1024 + " KB");
                                }
                            }
                            sendLineTerminal();
                            printStream.flush();
                            
                        } else if (request.startsWith("get ")) {
                            log("got get");
                            String filename = request.substring(4).trim();
                            System.out.println(filename);
                            File f = new File("." + File.separator + filename);
                            if (f.exists()) {
                                if (f.isFile()) {
                                    sendLine("COPYING " + f.length());
                                    printStream.flush();

                                    FileInputStream fis = new FileInputStream(f);

                                    byte[] b;
                                    final int defBufferSize = 8192;
                                    if (f.length() < defBufferSize) {
                                        b = new byte[(int) f.length()];
                                    } else {
                                        b = new byte[defBufferSize]; //max of buffer
                                    }

                                    int r;
                                    while ((r = fis.read(b)) > 0) {
                                        buffOutStream.write(b, 0, r);
                                    }
                                    buffOutStream.flush();
                                    b = null;
                                } else { //it's a folder not file
                                    sendLine("It is not a filename.");
                                    sendLineTerminal();
                                    printStream.flush();
                                }
                            } else { //file does not exist
                                sendLine("The file does not exist.");
                                sendLineTerminal();
                                printStream.flush();
                            }

                        } else { // unknown command
                            log("Client sent an unknown command.");
                            sendLine("Unknown command:" + request);
                            sendLineTerminal();
                            printStream.flush();
                        }

                        Thread.sleep(10);
                        if (request.startsWith("ServerShutdown")) { //command needs more work
                            break SERVER_CONN;
                        }

                    } //waiting for a command loop 
                } catch (IOException e) {
                    System.out.println(e);

                }
            } //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop //waiting for a new connection loop

            //shutdown the server
            printStream.close();
            buffOutStream.close();
            buffRead.close();
            socket.close();
            printStream = null;
            buffOutStream = null;
            buffRead = null;
            socket = null;

        } catch (IOException e) {
            System.out.println("error");
            System.err.println(e);
        }

    }

    private void log(String msg)
    {
        System.out.println(msg);
    }

    long pingTimer;

    private void ping()
    {
        if (true) {
            return; //disable ping
        }
        final int interval = 2000;

        if (System.currentTimeMillis() - pingTimer > interval) {
            printStream.println("PING"); //send heart beat
            printStream.flush();
            log("send PING");
            pingTimer = System.currentTimeMillis();
        }

    }

    private void sendLine(String res)
    {
        printStream.println(res);
    }

    private void sendLineTerminal()
    {
        printStream.println();
    }
    
}
