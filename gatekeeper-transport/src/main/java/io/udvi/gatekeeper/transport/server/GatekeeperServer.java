package io.udvi.gatekeeper.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by sureshreddy on 27/12/16.
 */
@Data
@Slf4j
public class GatekeeperServer {

    private final ServerBootstrap serverBootstrap;

    private final NioEventLoopGroup bossGroup;

    private final NioEventLoopGroup workerGroup;

    private String bindAddress = "0.0.0.0";

    private int bindPort = 8090;

    public GatekeeperServer() {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class);
    }

    public GatekeeperServer bind(String bindAddress, int bindPort) {
        this.bindAddress = bindAddress;
        this.bindPort = bindPort;
        return this;
    }

    public void start() throws Exception {
        final GatekeeperServer gatekeeperServer = this;
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel channel) {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.addLast(new HttpServerCodec());
                channelPipeline.addLast(new HttpObjectAggregator(20971520));
                channelPipeline.addLast(new HttpContentCompressor());
                channelPipeline.addLast(new GatekeeperServerHandler(gatekeeperServer));
            }
        });

        Channel ch = this.serverBootstrap.bind(this.bindAddress, this.bindPort).sync().channel();

        System.err.println("Open your web browser and navigate to " +
                ("http://127.0.0.1:" + this.bindPort + '/'));
        ch.closeFuture().sync();
    }

    public static void main (String a[]) {
        try {
            new GatekeeperServer().bind("0.0.0.0", 8091).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
