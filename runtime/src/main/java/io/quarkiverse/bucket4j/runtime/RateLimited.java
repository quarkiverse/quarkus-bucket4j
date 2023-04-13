package io.quarkiverse.bucket4j.runtime;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityKeyResolver;

@InterceptorBinding
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface RateLimited {

    String DEFAULT_KEY = "io.quarkiverse.bucket4j.runtime.RateLimited<DEFAULT>";

    /**
     * This is the configuration key that hold the limits for this endpoint
     * If two methods share the same key, their buckets will be shared.
     * This mean that for a given identity key, the number of allowed requests
     * is shared for all the methods with the same limitsKey
     */
    @Nonbinding
    String limitsKey() default DEFAULT_KEY;

    /**
     * This is the resolver for the segmentation key.
     * there are two provided strategies you can use:
     * ConstantResolver, disable the segmentation completely
     * IpResolver, segment by source Ip address
     * Or you can implement a custom resolver, which must be a CDI bean
     */
    @Nonbinding
    Class<? extends IdentityKeyResolver> identityResolver() default ConstantResolver.class;

}
