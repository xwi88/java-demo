package com.im.client;

import com.im.tutorial.GreeterGrpc;
import com.im.tutorial.Helloworld.HelloRequest;
import com.im.tutorial.Helloworld.HelloReply;
import com.im.utils.IPUtil;
import com.im.utils.ProcessUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Calendar;
import java.util.Date;


public class RPCClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

        String localIP = IPUtil.getLocalIP();
        int pid = ProcessUtil.getProcess();

        for (; ; ) {
            HelloRequest request = HelloRequest.newBuilder()
                    .setIp(localIP)
                    .setName(String.format("world %s", Calendar.getInstance().get(Calendar.SECOND)))
                    .setNodeName(String.format("client-java [pid=%s]", pid))
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
