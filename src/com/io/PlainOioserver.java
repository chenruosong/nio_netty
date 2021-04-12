package com.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * 1.ServerSocket 创建并监听端口的连接请求
 * 2.accept() 调用阻塞，直到一个连接被建立了。返回一个新的 Socket 用来处理 客户端和服务端的交互
 * 3.流被创建用于处理 socket 的输入和输出数据。BufferedReader 读取从字符输入流里面的本文。PrintWriter 打印格式化展示的对象读到本文输出流
 * 4.处理循环开始 readLine() 阻塞，读取字符串直到最后是换行或者输入终止。
 * 5.如果客户端发送的是“Done”处理循环退出
 * 6.执行方法处理请求，返回服务器的响应
 * 7.响应发回客户端
 * 8.处理循环继续
 */
public class PlainOioserver {
    public void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);     //1.创建并监听端口的连接请求
        try {
            for (;;) {
                final Socket clientSocket = socket.accept();    //2.调用阻塞，直到一个连接被建立了。返回一个新的 Socket 用来处理 客户端和服务端的交互
                System.out.println("Accepted connection from " + clientSocket);

                new Thread(new Runnable() {                        //3
                    /**
                     * 为了实现多个并行的客户端我们需要分配一个新的 Thread 给每个新的客户端 socket。
                     * 但考虑使用这种方法来支持大量的同步，长连接。在任何时间点多线程可能处于休眠状态，等待输入或输出数据。
                     * 这很容易使得资源的大量浪费，对性能产生负面影响。当然，有一种替代方案。
                     */
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
                            out.println("Hi!\r\n");                            //4
                            clientSocket.close();                //5

                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        }
                    }
                }).start();                                        //6
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
