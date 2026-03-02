class TokenBucket {

    private final int maxTokens;
    private final double refillRatePerMillis;

    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, long refillPeriodMillis) {
        this.maxTokens = maxTokens;
        this.refillRatePerMillis =
                (double) maxTokens / refillPeriodMillis;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {

        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {

        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTime;

        double tokensToAdd = timeElapsed * refillRatePerMillis;

        if (tokensToAdd > 0) {
            tokens = Math.min(maxTokens, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    public synchronized int getRemainingTokens() {
        refill();
        return (int) tokens;
    }
}