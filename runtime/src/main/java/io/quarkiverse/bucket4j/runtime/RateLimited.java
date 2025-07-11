package io.quarkiverse.bucket4j.runtime;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.bucket4j.runtime.resolver.ConstantResolver;
import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;

@InterceptorBinding
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Repeatable(RateLimited.List.class)
public @interface RateLimited {

    String DEFAULT_BUCKET = "io.quarkiverse.bucket4j.runtime.RateLimited<DEFAULT>";

    /**
     * This is the bucket identity for this method.
     * A configuration key that hold the limits for this bucket must exist
     * If multiple methods share the same bucket, the number of permitted uses
     * is shared among all them
     */
    @Nonbinding
    String bucket() default DEFAULT_BUCKET;

    /**
     * Identity resolver allow to segment the population.
     * Each resolved identity key will have its own quota.
     * There are two provided strategies you can use:
     * ConstantResolver, disable the segmentation completely
     * IpResolver, segment by source Ip address
     * Or you can implement a custom resolver, which must be a valid CDI bean
     */
    @Nonbinding
    Class<? extends IdentityResolver> identityResolver() default ConstantResolver.class;

    @Target({ METHOD, TYPE })
    @Retention(RUNTIME)
    @interface List {
        RateLimited[] value();
    }
}
