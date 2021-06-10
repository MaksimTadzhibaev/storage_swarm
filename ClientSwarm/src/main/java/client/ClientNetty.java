package client;

import client.interfaces.AnswerFromServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import static common.command.Command.*;

// клиент на Netty
public class ClientNetty {
    private SocketChannel channel;
    private AnswerFromServer answer;

    public ClientNetty(AnswerFromServer answer) {
        this.answer = answer;
        Thread t1 = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                                     @Override
                                     protected void initChannel(SocketChannel socketChannel) throws Exception {
                                         channel = socketChannel;
                                         socketChannel.pipeline().addLast(
                                                 new ObjectEncoder(),
                                                 new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                                 new ClientMsgHandler(answer)
                                         );
                                     }
                                 }
                        );
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                System.out.println("Клиент подключился");
                future.channel().closeFuture().sync();
                System.out.println("Клиент отключился");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                worker.shutdownGracefully();
            }
        });
        t1.setDaemon(true);
        t1.start();
    }

    //отправка комманд на сервер
    public void sendMessage(Object msg) {
        channel.writeAndFlush(msg);
    }
}
