package com.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioSelectorServer {
    public static void main(String[] args) throws IOException {
        //创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));
        serverSocketChannel.configureBlocking(false);

        //打开selector处理channel，即创建epoll
        Selector selector = Selector.open();//创建多路复用器
        //把serverSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功");

        while (true) {
            //阻塞等待需要处理的事件发生
            selector.select();
            //获取selector中注册的全部事件的SelectionKey实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            //遍历selectionKey对事件进行处理
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //如果是OP_ACCEPT事件，则进行连接获取和事件注册
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = server.accept();
                    socketChannel.configureBlocking(false);
                    //这里只注册了读事件，如果需要给客户端发送数据可以注册写事件
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    System.out.println("客户端连接成功");
                } else if (selectionKey.isReadable()) {
                    //如果是读事件，则进行读取和打印
                    ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    SocketChannel socketChannel = server.accept();
                    int lenth = socketChannel.read(byteBuffer);
                    if (lenth > 0){
                        System.out.println("接收到客户端消息：" + new String(byteBuffer.array(), 0, lenth));
                    } else if (lenth == -1) {
                        System.out.println("客户端断开连接");
                        socketChannel.close();
                    }

                }
                //从事件集合中删除本次处理过的key，防止下次select重复处理
                iterator.remove();
            }
        }

    }
}
