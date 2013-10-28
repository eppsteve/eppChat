package chatclient;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import java.lang.*;
import java.net.*;


 class FileTransferClient_socket {

    static final int PORT    = 5792;
    static final String HOST = "localhost";

    public static boolean send( String filename,String fname ) {

        try {
            System.out.print("Sending data...\n");
            Socket skt = new Socket(HOST, PORT);


            FileInputStream fis = new FileInputStream(filename);
            BufferedInputStream in = new BufferedInputStream(fis);
            BufferedOutputStream out = new BufferedOutputStream( skt.getOutputStream() );
            fname=fname+"}";
            byte[] name=fname.getBytes();
            for(int i=0;i<name.length;i++)
                 out.write(name[i]);

            int i;
            while ((i = in.read()) != -1) {
                out.write(i);

            }


            out.flush();
            out.close();
            in.close();
            skt.close();

            return true;
        }
        catch( Exception e ) {

            System.out.print("Error! It didn't work! " + e + "\n");

            return false;
        }
    }
}