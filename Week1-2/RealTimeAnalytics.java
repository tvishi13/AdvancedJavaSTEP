import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class RealTimeAnalytics {

    private ConcurrentHashMap<String, LongAdder> pageViewCountMap =
            new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Set<String>> uniqueVisitorsMap =
            new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, LongAdder> sourceCountMap =
            new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public RealTimeAnalytics() {

        // Refresh dashboard every 5 seconds
        scheduler.scheduleAtFixedRate(this::printDashboard,
                5, 5, TimeUnit.SECONDS);
    }

    // Process incoming event
    public void processEvent(String url, String userId, String source) {

        // Total page views
        pageViewCountMap
                .computeIfAbsent(url, k -> new LongAdder())
                .increment();

        // Unique visitors
        uniqueVisitorsMap
                .computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        // Traffic source count
        sourceCountMap
                .computeIfAbsent(source, k -> new LongAdder())
                .increment();
    }

    // Compute Top 10 pages
    private List<Map.Entry<String, LongAdder>> getTopPages() {

        PriorityQueue<Map.Entry<String, LongAdder>> minHeap =
                new PriorityQueue<>(Comparator.comparingLong(e -> e.getValue().sum()));

        for (Map.Entry<String, LongAdder> entry : pageViewCountMap.entrySet()) {

            minHeap.offer(entry);

            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, LongAdder>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()));

        return result;
    }

    // Dashboard Output
    private void printDashboard() {

        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        List<Map.Entry<String, LongAdder>> topPages = getTopPages();

        int rank = 1;
        for (Map.Entry<String, LongAdder> entry : topPages) {

            String url = entry.getKey();
            long views = entry.getValue().sum();
            int unique = uniqueVisitorsMap.get(url).size();

            System.out.println(rank++ + ". " + url +
                    " - " + views + " views (" +
                    unique + " unique)");
        }

        System.out.println("\nTraffic Sources:");
        sourceCountMap.forEach((source, count) ->
                System.out.println(source + " → " + count.sum()));

        System.out.println("=================================");
    }

    public static void main(String[] args) throws InterruptedException {

        RealTimeAnalytics analytics = new RealTimeAnalytics();

        // Simulated streaming events
        for (int i = 0; i < 1000; i++) {
            analytics.processEvent(
                    "/article/breaking-news",
                    "user_" + i,
                    i % 2 == 0 ? "google" : "facebook"
            );
        }

        Thread.sleep(15000); // Let dashboard update 3 times
    }
}
