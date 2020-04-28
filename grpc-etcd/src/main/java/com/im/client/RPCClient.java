package com.im.client;

import com.im.discovery.EtcdNameResolverProvider;
import com.im.tutorial.GreeterGrpc;
import com.im.tutorial.Helloworld.HelloRequest;
import com.im.tutorial.Helloworld.HelloReply;
import com.im.utils.IPUtil;
import com.im.utils.ProcessUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RPCClient {
    private static final Logger logger = Logger.getLogger(RPCClient.class.getName());
    private static final String defaultLoadBalancingPolicy = "round_robin";
    private static final String ENDPOINT = "http://127.0.0.1:2379";
    private static final String TARGET = "demand:engine";

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public RPCClient() {
        List<URI> endpoints = new ArrayList<>();
        endpoints.add(URI.create(ENDPOINT));
        this.channel = ManagedChannelBuilder.forTarget(TARGET)
                .nameResolverFactory(EtcdNameResolverProvider.forEndpoints(endpoints))
                .defaultLoadBalancingPolicy(defaultLoadBalancingPolicy)
                .usePlaintext()
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sayHello() {
        logger.info("trying to sayHello");
        String localIP = IPUtil.getLocalIP();
        int pid = ProcessUtil.getProcess();
        HelloRequest request = HelloRequest.newBuilder()
                .setNodeName(String.format("client-java [pid=%s]", pid))
                .setIp(localIP)
                .setName(String.format("world %s", Calendar.getInstance().get(Calendar.SECOND)))
                .build();
        HelloReply response;
        logger.info(String.format("[start] %s request:%s", new Date().toString(),
                request.toString()));
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info(String.format("[end] %s reply:%s", new Date().toString(),
                response.toString()));
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        RPCClient client = new RPCClient();
        try {
            while (true) {
                client.sayHello();
                Thread.sleep(1000L);
            }
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
}
