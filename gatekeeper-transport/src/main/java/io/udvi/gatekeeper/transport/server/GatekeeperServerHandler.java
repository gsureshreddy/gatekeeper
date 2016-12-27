package io.udvi.gatekeeper.transport.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by sureshreddy on 27/12/16.
 */
@Data
@Slf4j
public class GatekeeperServerHandler extends ChannelDuplexHandler {

    private final GatekeeperServer gatekeeperServer;

    private boolean isKeepAlive;

    public GatekeeperServerHandler(GatekeeperServer gatekeeperServer) {
        super();
        this.gatekeeperServer = gatekeeperServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Reading messages");
        }

        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            if(isKeepAlive) {
                ctx.writeAndFlush(getDefaultResponse(request, "OK"));
            } else {
                ctx.write(getDefaultResponse(request, "OK"));
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public FullHttpResponse getDefaultResponse(FullHttpRequest request, String content) {
        this.isKeepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, OK, Unpooled.copiedBuffer(content.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (isKeepAlive) {
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return response;
    }
}
