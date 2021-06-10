package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import server.handlers.MessageHandler;
import server.handlers.FileInputHandler;

import static common.command.Command.PORT;

public class ServerService {
    public ServerService() {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ObjectDecoder(100 * 1024 * 1024,
                                            ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MessageHandler(),
                                    new FileInputHandler()
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(PORT).sync();
            System.out.println("Сервер запустился");
            future.channel().closeFuture().sync();
            System.out.println("Сервер отключен");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
