package com.io;

import java.io.IOException;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",9000);
        //send to server
        socket.getOutputStream().write("hello nio server".getBytes());
        socket.getOutputStream().flush();
        byte[] bytes = new byte[1024];
        //阻塞
        int read = socket.getInputStream().read(bytes);
        if (read != -1){
            System.out.println("accept from server:"+new String(bytes,0,read));
        }
        socket.close();
    }
}
