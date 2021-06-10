package server.handlers;

import common.FileMove;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Paths;

import static common.command.Command.UPDATE;


//получение файлов от клиента
public class FileInputHandler extends SimpleChannelInboundHandler<FileMove> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileMove fileMove) throws Exception {
        if (!Files.exists(Paths.get(fileMove.getPath(), fileMove.getName()))) {
            Files.createFile(Paths.get(fileMove.getPath(), fileMove.getName()));
            Files.write(Paths.get(fileMove.getPath(), fileMove.getName()), fileMove.getBytes());
            channelHandlerContext.writeAndFlush(UPDATE);
        }
    }
}