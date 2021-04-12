package com.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

public class HttpServer {
    private final int port;
    public HttpServer(int port){
        this.port = port;
    }

    public static void main(String[] args) throws Exception{
        int port = Integer.parseInt("8080");
        new HttpServer(port).start();
    }

    private void start() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //ServerBootstrap 是一个用于设置服务器的引导类
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(group)
                    .channel(NioSctpServerChannel.class) // 使用NioServerSocketChannel类，用于实例化新的通道以接受传入连接
                    .localAddress(new InetSocketAddress(port)) //设置服务器监听端口号
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception{
//                            ch.pipeline().addLast(new HttpServerHandler());//添加请求处理
                        }
                    });
            //绑定到端口和启动服务器
            ChannelFuture f = sb.bind().sync();
            System.out.println(HttpServer.class.getName() +
                    "started adn listening for connection on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}
