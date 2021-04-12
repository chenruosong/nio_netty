package com.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NioServer {
    //保存客户端连接
    private static List<SocketChannel> channelList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        //创建NIO的ServerSocketChannel，与 BIO 的ServerSocket类似
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));

        //设置serverSocketChannel为非阻塞，如果不设置默认为阻塞
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务器启动成功");

        while (true) {
            //非阻塞模式accept方法不会阻塞
            //NIO的非阻塞是由操作系统内部实现的，底层调用了linux内核的accept函数
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null){
                System.out.println("连接成功");
                //设置该连接为非阻塞
                socketChannel.configureBlocking(false);
                //保存该连接到list
                channelList.add(socketChannel);
            }
            //遍历所有客户端连接，进行数据读取
            //如果连接数过多，每次循环都有大量无效遍历，优化：没有写数据的连接不在此进行遍历
            Iterator<SocketChannel> iterator = channelList.iterator();
            while (iterator.hasNext()){
                SocketChannel sc = iterator.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                //非阻塞模式的sc read方法不会阻塞
                int len = sc.read(byteBuffer);
                if (len>0){
                    System.out.println("接收到客户端消息:"+new String(byteBuffer.array(),0,len));
                } else if (len == -1) {
                    //客户端断开连接，从list中移除
                    iterator.remove();
                    System.out.println("客户端断开连接");
                }
            }
        }
    }
}
