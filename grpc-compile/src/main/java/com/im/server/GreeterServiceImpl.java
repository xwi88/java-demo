package com.im.server;


import com.im.tutorial.GreeterGrpc;
import com.im.tutorial.Helloworld.HelloRequest;
import com.im.tutorial.Helloworld.HelloReply;
import com.im.utils.IPUtil;
import io.grpc.stub.StreamObserver;

import java.util.Date;

public class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String requestIP = request.getIp();
        String requestName = request.getName();
        String requestNodeName = request.getNodeName();

        System.out.printf("[%s] requestIP:%s,requestName:%s,requestNodeName:%s\n",
                new Date().toString(),
                requestIP, requestName, requestNodeName);

        String localIP = IPUtil.getLocalIP();
        HelloReply helloReply = HelloReply.newBuilder()
                .setIp(localIP)
                .setNodeName("server-java")
                .setMessage(new Date().toString())
                .build();

        responseObserver.onNext(helloReply);
        responseObserver.onCompleted();

    }
}
