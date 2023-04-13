package io.quarkiverse.bucket4j.runtime;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RateLimitExceptionMapper implements ExceptionMapper<RateLimitException> {

    @Override
    public Response toResponse(RateLimitException e) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .build();
    }
}
