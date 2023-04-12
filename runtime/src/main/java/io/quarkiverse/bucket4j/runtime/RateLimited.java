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

    enum SegmentationMode {
        UNSEGMENTED,
        SEGMENT_BY_IP
    }

    String DEFAULT_KEY = "io.quarkiverse.bucket4j.runtime.RateLimited<DEFAULT>";

    @Nonbinding
    String limitsKey() default DEFAULT_KEY;

    @Nonbinding
    Class<? extends IdentityKeyResolver> identityResolver() default ConstantResolver.class;

}
