package com.im.server;


import com.google.common.base.Charsets;
import com.im.utils.IPUtil;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DemoServer {
    private static final Logger logger = Logger.getLogger(DemoServer.class.getName());
    private static final HashSet<String> ENDPOINTS = new HashSet<String>() {
        {
            add("http://127.0.0.1:2379");
//            add("http://10.14.41.51:2379");
//            add("http://10.14.41.52:2379");
//            add("http://10.14.41.53:2379");
        }
    };
    private static final String SCHEME = "services";
    private static final String SERVICE_NAME = "demand:engine/v1/";
    private static final long TTL = 5L;

    private int port;
    private Server server;
    private Client etcdClient;

    private DemoServer(int port) {
        this.port = port;
    }

    private void start() throws IOException, ExecutionException, InterruptedException {
        server = RPCServer.buildAndStartServer(port);
        logger.info("Server started on port:" + port);
        String localIP = IPUtil.getLocalIP();
        String scheme = "http";
        final URI uri = URI.create(scheme + "://" + localIP + ":" + port);
        this.etcdClient = Client.builder()
                .endpoints(ENDPOINTS.stream().map(URI::create).collect(Collectors.toList()))
                .build();
        long leaseId = etcdClient.getLeaseClient().grant(TTL).get().getID();
        // etcd store like: /services/<SERVICE_NAME>/<value>
        String serviceKV = "/" + SCHEME + "/" + SERVICE_NAME + uri.toASCIIString();
        System.out.println("etcd key: " + serviceKV);
        etcdClient.getKVClient().put(
                ByteSequence.from(serviceKV, Charsets.US_ASCII),
                ByteSequence.from(uri.toASCIIString(), Charsets.US_ASCII),
//                ByteSequence.from(Long.toString(leaseId), Charsets.US_ASCII),
                PutOption.newBuilder().withLeaseId(leaseId).build());
        etcdClient.getLeaseClient().keepAlive(leaseId, new EtcdServiceRegisterer());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down server on port: " + port);
            DemoServer.this.stop();
        }));
    }

    private void stop() {
        etcdClient.close();
        server.shutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        int port = 8080;
        System.out.println(Arrays.asList(args).toString());
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        final DemoServer demoServer = new DemoServer(port);
        demoServer.start();
        demoServer.blockUntilShutdown();
    }

    static class EtcdServiceRegisterer implements StreamObserver<LeaseKeepAliveResponse> {

        @Override
        public void onNext(LeaseKeepAliveResponse value) {
            logger.info("got renewal for lease: " + value.getID());
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onCompleted() {
        }
    }


}
