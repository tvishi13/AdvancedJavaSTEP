import java.util.*;
import java.util.concurrent.*;

public class DNSCache {

    private final int MAX_SIZE = 5;

    // LRU Cache using LinkedHashMap
    private LinkedHashMap<String, DNSEntry> cache;

    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;

    public DNSCache() {

        cache = new LinkedHashMap<>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_SIZE;
            }
        };

        startCleanupTask();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            totalLookupTime += (System.nanoTime() - startTime);
            System.out.println("Cache HIT");
            return entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
            System.out.println("Cache EXPIRED");
        }

        misses++;
        System.out.println("Cache MISS → Querying upstream...");

        String ip = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(domain, ip, 5)); // 5s TTL

        totalLookupTime += (System.nanoTime() - startTime);

        return ip;
    }

    // Simulate upstream DNS
    private String queryUpstreamDNS(String domain) {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return "172.217.14." + new Random().nextInt(255);
    }

    // Background cleanup
    private void startCleanupTask() {

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Cache statistics
    public void getCacheStats() {

        double hitRate = (hits + misses == 0)
                ? 0
                : (hits * 100.0) / (hits + misses);

        double avgLookupMs = totalLookupTime / 1_000_000.0 / (hits + misses);

        System.out.println("Hit Rate: " + hitRate + "%");
        System.out.println("Average Lookup Time: " + avgLookupMs + " ms");
    }

    public static void main(String[] args) throws InterruptedException {

        DNSCache cache = new DNSCache();

        System.out.println(cache.resolve("google.com"));
        System.out.println(cache.resolve("google.com"));

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(cache.resolve("google.com"));

        cache.getCacheStats();
    }
}