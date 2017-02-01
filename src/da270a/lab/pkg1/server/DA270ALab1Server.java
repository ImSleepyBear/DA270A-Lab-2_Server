/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package da270a.lab.pkg1.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Mohini
 */
public class DA270ALab1Server {

    private static final int PORT = 8000;
    private ServerSocket serverSocket;
    
    private String command = " ";
    String directory;
    File curDir;// = new File(directory);
    
    String files = "";
    File[] filesList;
    
    File transferFile;

    public static void main(String[] args) {

        int port = PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        new DA270ALab1Server(port);
    }

    public DA270ALab1Server(int port) {

        directory = System.getProperty("user.dir");
        curDir = new File(directory);
        filesList = curDir.listFiles();
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error in creation of the server socket");
            System.exit(0);
        }

        while (true) {
            try {

                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection: " + socket);

                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    command = inputFromClient.readUTF();

                    if (command.equalsIgnoreCase("pwd")) {
                        
                        System.out.println("retrieved command: pwd");
                        outputToClient.writeUTF("status ok \ncurrent directory: " + directory + "\n");
                        
                    } else if (command.equalsIgnoreCase("printfiles")) {
                        
                        System.out.println("retrieved command: printfiles");
                        
                        for (File f : filesList) {
                            
                            if (f.isDirectory()) {
                                System.out.println("directory: " + f.getName());
                                files = files + "directory: " + f.getName() + "\n";
                            }
                            if (f.isFile()) {
                                System.out.println("file: " + f.getName());
                                files = files + "file: " + f.getName() + "\n";
                            }
                        }
                        
                        outputToClient.writeUTF(files);
                        outputToClient.flush();
                        
                    } else if (command.equalsIgnoreCase("download")) {
                        
                        byte[] myByteArray = new byte[1024*16];
                        
                        transferFile = new File("s.txt");
                        FileInputStream fin = new FileInputStream(transferFile);
                        
                        while(fin.available() > 0){
                            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                            bos.write(myByteArray, 0, fin.read(myByteArray));
                        }
                        
                        
                        System.out.println("File transferred");
                    }
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

}
