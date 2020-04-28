package com.im.client;

import com.im.tutorial.GreeterGrpc;
import com.im.tutorial.Helloworld.HelloRequest;
import com.im.tutorial.Helloworld.HelloReply;
import com.im.utils.IPUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class RPCClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

        String localIP = IPUtil.getLocalIP();

        for (int i = 0; ; i++) {
            HelloRequest request = HelloRequest.newBuilder()
                    .setIp(localIP)
                    .setName("client-java")
                    .setNodeName("node-java")
                    .build();

            try {
                System.out.printf("[start] %s\nrequest:%s", new Date().toString(),
                        request.toString());
                HelloReply helloReply = stub.sayHello(request);
                System.out.printf("[end] %s\nreply:%s\n", new Date().toString(),
                        helloReply.toString());
            } catch (Exception e) {
                System.out.printf("err: %s\n", e.toString());
            }
            Thread.sleep(5000);
        }

//        channel.shutdown();
    }
}
