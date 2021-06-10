package client;

import client.interfaces.AnswerFromServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static common.command.Command.*;

public class ClientMsgHandler extends SimpleChannelInboundHandler<Object> {
    AnswerFromServer answer;

    //получение и обработка сообщений от сервера
    public ClientMsgHandler(AnswerFromServer answer) {
        this.answer = answer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (o.equals(END))
            channelHandlerContext.close();

        if (answer != null)
            answer.call(o);
    }
}
