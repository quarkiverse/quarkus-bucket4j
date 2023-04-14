package io.quarkiverse.bucket4j.runtime;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import io.quarkus.arc.DefaultBean;

public class DefaultProxyManagerProducer {

    @ConfigProperty(name = "quarkus.rate-limiter.max-size")
    int maxSize;

    @ConfigProperty(name = "quarkus.rate-limiter.keep-after-refill")
    Duration keepAfterRefill;

    @Produces
    @DefaultBean
    @ApplicationScoped
    ProxyManager<String> proxyManager() {
        Caffeine<String, RemoteBucketState> builder = (Caffeine) Caffeine.newBuilder().maximumSize(maxSize);
        return new CaffeineProxyManager<>(builder, keepAfterRefill);
    }
}
