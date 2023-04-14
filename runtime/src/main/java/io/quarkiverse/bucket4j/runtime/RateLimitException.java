package io.quarkiverse.bucket4j.runtime;

public class RateLimitException extends RuntimeException {

    private final long waitTimeInMilliSeconds;

    RateLimitException(long waitTimeInNanoSeconds) {
        this.waitTimeInMilliSeconds = waitTimeInNanoSeconds;
    }

    public long getWaitTimeInMilliSeconds() {
        return waitTimeInMilliSeconds;
    }
}
