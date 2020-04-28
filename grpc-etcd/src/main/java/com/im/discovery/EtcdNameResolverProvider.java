package com.im.discovery;

import com.google.common.base.Preconditions;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

public class EtcdNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "services";
    private static final int PRIORITY = 6;

    private final List<URI> endpoints;

    private EtcdNameResolverProvider(List<URI> endpoints) {
        this.endpoints = Preconditions.checkNotNull(endpoints, "endpoints");
        Preconditions.checkArgument(!endpoints.isEmpty(), "at least 1 endpoint required");
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return PRIORITY;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        if (SCHEME.equals(targetUri.getScheme())) {
            String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
            Preconditions.checkArgument(targetPath.startsWith("/"),
                    "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
            return new EtcdNameResolver(
                    endpoints,
                    targetPath.substring(1));
        } else {
            return null;
        }
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    public static EtcdNameResolverProvider forEndpoints(List<URI> endpoints) {
        return new EtcdNameResolverProvider(endpoints);
    }
}
