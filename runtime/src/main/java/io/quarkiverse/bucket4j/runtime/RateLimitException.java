package io.quarkiverse.bucket4j.runtime;

public class RateLimitException extends RuntimeException {

    private final long waitTimeInMilliSeconds;

    RateLimitException(long waitTimeInMilliSeconds) {
        this.waitTimeInMilliSeconds = waitTimeInMilliSeconds;
    }

    public long getWaitTimeInMilliSeconds() {
        return waitTimeInMilliSeconds;
    }

    /**
     * Does not fill in the stack trace for this exception
     * for performance reasons.
     *
     * @return this instance
     * @see java.lang.Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
