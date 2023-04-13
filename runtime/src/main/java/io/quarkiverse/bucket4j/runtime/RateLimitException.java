package io.quarkiverse.bucket4j.runtime;

public class RateLimitException extends RuntimeException {

    private final long waitTimeInNanoSeconds;

    RateLimitException(long waitTimeInNanoSeconds) {
        this.waitTimeInNanoSeconds = waitTimeInNanoSeconds;
    }

    public long getWaitTimeInNanoSeconds() {
        return waitTimeInNanoSeconds;
    }
}
