package com.im.server;


import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class RPCServer {
    public static Server buildServer(int port) throws IOException, InterruptedException {
        return ServerBuilder
                .forPort(port)
                .addService(new GreeterServiceImpl()).build();
    }

    public static Server buildAndStartServer(int port) throws IOException, InterruptedException {
        //        server.awaitTermination();
        return ServerBuilder
                .forPort(port)
                .addService(new GreeterServiceImpl()).build()
                .start();
    }
}
