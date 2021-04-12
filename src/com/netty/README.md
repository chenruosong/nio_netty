# Netty模型

---

## NioEventLoopGroup
主要管理 NioEventLoop 的生命周期，可以理解为一个线程池，内部维护了一组线程，每个线程 (NioEventLoop)负责处理多个 Channel 上的事件，而一个 Channel 只对应于一个线程。
1. Netty 抽象出两组线程池 BossGroup 和 WorkerGroup , BossGroup 处理连接请求，workerGroup负责网络读写、完成真正的和客户端业务处理；
2. BossGroup 和 WorkerGroup 都是 NioEventLoopGroup
3. NioEventLoopGroup 相当于一个[事件循环线程组],这个组中含有多个[事件循环线程]，每一个事件循环线程是NioEventLoop
4. 每个 NioEventLoop 都有一个selector，用于监听注册在其上的socketChannel的网络通讯。
5. 每个Boss NioEventLoop线程内部执行循环的步骤：
    - 处理accept事件，与client建立连接，生成NioSocketChannel
    - 将NioSocketChannel注册到某个Worker NioEventLoop 上的 selector
    - 处理任务队列的任务，即runAllTasks。
6. 每个Worker NioEventLoop线程内部循环执行步骤：
    - 轮询注册到自己selector上的所有NioSocketChannel的read，write事件
    - 处理 I/O 事件，在对应的channel处理业务
    - runAllTasks处理任务队列TaskQueue的任务，一些耗时的业务处理一般可以放在TaskQeueu中处理，这样不影响数据在pipeline中的流动处理
7. 每个worker NioEventLoop处理NioSocketChannel业务时，会使用pipeline，管道中维护了很多handler处理器用来处理channel中的数据
8. NioEventLoop 中维护了一个线程和任务队列，支持异步提交执行任务，线程启动时会调用 NioEventLoop 的 run 方 法，执行 I/O 任务和非 I/O 任务:
   I/O任务，即 selectionKey 中 ready 的事件，如 accept、connect、read、write 等，由 processSelectedKeys 方法触发。
   非IO任务，添加到 taskQueue 中的任务，如 register0、bind0 等任务，由 runAllTasks 方法触发。

## Bootstrap、ServerBootstrap
启动类。一个Netty应用通常由一个Bootstrap开始主要作用是配置整个Netty程序,串联各个组件。

## Future、ChannelFuture
Netty中所有IO操作都是异步的，不能立刻得知消息是否被正确处理。Futrue可以注册一个监听，当操作执行成功或失败时监听会自动触发注册的监听事件

## Selector
Netty 基于 selector 对象实现IO多路复用，通过selector一个线程可以监听多个连接的channel的事件。
当向一个selector中注册channel后，selector内部的机制就可以自动不断地查询（select）这些注册的Channel是否有已就绪的IO事件。

## Channel
Netty 网络通信的组建，能够用于执行网络IO操作，Channel为用户提供：
1. 当前网络连接的通道的状态（是否打开，是否连接）
2. 网络连接的配置参数（buffer大小）
3. 提供异步的网络IO操作（建立连接、读写、绑定端口),异步调用意味着任何IO操作都将立即返回，并且不保证在调用结束时所请求的IO操作都已完成。
4. 调用立即返回一个ChannelFutrue实例，通过注册监听器到ChannelFutrue上，可以在IO操作成功/失败/取消时回调通知调用方。
5. 支持关联IO操作与对应的处理程序。不同协议、不同阻塞类型的连接都有不同的channel类型与之对应。
```text
1. NioSocketChannel，异步的客户端TCP Socket连接
2. NioServerSocketChannel, 异步的服务器端 TCP Socket 连接。
3. NioDatagramChannel, 异步的 UDP 连接。
4. NioSctpChannel, 异步的客户端 Sctp 连接。
5. NioSctpServerChannel, 异步的服务端 Sctp 连接。
```

## ChannelHandler
ChannelHandler 是一个接口，处理 I/O 事件或拦截 I/O 操作，并将其转发到其 ChannelPipeline(业务处理链)中的 下一个处理程序。
ChannelHandler 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它的子类:
   - ChannelInboundHandler 用于处理入栈 I/O 事件。
   - ChannelOutboundHandler 用于处理出栈 I/O 操作。
   
或者使用以下适配器：
   - ChannelInboundHandlerAdapter 用于处理入栈 I/O 事件。
   - ChannelOutboundHandlerAdapter 用于处理出栈 I/O 操作。
   
## ChannelHandlerContext
保存 Channel 相关的所有上下文信息，上下文对象，含有通道channel、管道pipeline,同时关联一个 ChannelHandler 对象。

## ChannelPipeline
保存 ChannelHandler 的 List，用于处理或拦截 Channel 的入站事件和出站操作。
ChannelPipeline 实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 Channel 中各 个的 ChannelHandler 如何相互交互。
在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应，它们的组成关系如下:


[channelPipeline](https://qiniuwork.huasheng.xiaochang.com/crs/04F425E9-1D26-4317-91A1-8F038B6AB8E6.png)


一个 Channel 包含了一个 ChannelPipeline，而 ChannelPipeline 中又维护了一个由 ChannelHandlerContext 组 成的双向链表，
并且每个 ChannelHandlerContext 中又关联着一个 ChannelHandler。 read事件(入站事件)和write事件(出站事件)在一个双向链表中，
入站事件会从链表 head 往后传递到最后一个入站的 handler，
出站事件会从链表 tail 往前传递到最前一个出站的 handler，
两种类型的 handler 互不干扰。

SocketChannel.pipeline().addLast()方法操作链表指针，将channelHandler加入到链表的尾部。

## ByteBuf
从结构上来说，ByteBuf 由一串字节数组构成。即相当于
```
byte[] bytes = new byte[1024];
```

ByteBuf 提供了两个索引，一个用于读取数据，一个用于写入数据。这两个索引通过在字节数组中移动，来定 位需要读或者写信息的位置。
当从 ByteBuf 读取时，它的 readerIndex 将会根据读取的字节数递增。
同样，当写 ByteBuf 时，它的 writerIndex 也会根据写入的字节数进行递增。
