package com.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class NettyByteBuf {
    public static void main(String[] args) {
        //创建byteBuf对象，该对象内部包含一个字节数组byte[10]
        //通过readerindex和writerIndex和capacity，将buffer分成三个区域
        //已经读取的区域:[0,readerindex)
        //可读取的区域:[readerindex,writerIndex)
        //可写的区域: [writerIndex,capacity)
        ByteBuf byteBuf = Unpooled.buffer(10);
        for (int i = 0;i<8;i++){
            byteBuf.writeByte(i);
        }
        int j = byteBuf.getByte(1);

        for (int i = 0;i<5;i++){
            byteBuf.readByte();
        }

        //用Unpooled工具类创建ByteBuf
        ByteBuf byteBuf2 = Unpooled.copiedBuffer("hello world!", CharsetUtil.UTF_8);

    }
}
