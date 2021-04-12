package com.io.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9000);
        while (true) {
            System.out.println("Waiting for connection...");
            //阻塞方法
            Socket clientSocket = serverSocket.accept();
            System.out.println("accept");
            handler(clientSocket);
        }
    }

    private static void handler(Socket clientSocket) throws IOException {
        byte[] bytes = new byte[1024];
        System.out.println("准备read");
        //接收客户端发来的数据，阻塞方法，如果客户端没有发送数据则阻塞
        int read = clientSocket.getInputStream().read(bytes);
        System.out.println("read完毕");
        if (read != -1) {
            System.out.println("read:"+new String(bytes,0,read));
        }
        clientSocket.getOutputStream().write("helloclient".getBytes());
        clientSocket.getOutputStream().flush();
    }
}
