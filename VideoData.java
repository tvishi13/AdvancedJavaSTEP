import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId) {
        this.videoId = videoId;
        this.content = "VideoContent_" + videoId;
    }
}

public class MultiLevelCache {

    private static final int L1_CAPACITY = 10000;
    private static final int L2_CAPACITY = 100000;

    // L1 - Memory
    private LinkedHashMap<String, VideoData> l1Cache =
            new LinkedHashMap<>(16, 0.75f, true) {
                protected boolean removeEldestEntry(
                        Map.Entry<String, VideoData> eldest) {
                    return size() > L1_CAPACITY;
                }
            };

    // L2 - SSD
    private LinkedHashMap<String, VideoData> l2Cache =
            new LinkedHashMap<>(16, 0.75f, true) {
                protected boolean removeEldestEntry(
                        Map.Entry<String, VideoData> eldest) {
                    return size() > L2_CAPACITY;
                }
            };

    // Access counters
    private Map<String, Integer> accessCount = new HashMap<>();

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;

    // Get video
    public synchronized VideoData getVideo(String videoId) {

        long start = System.nanoTime();

        // L1
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            simulateDelay(0.5);
            System.out.println("L1 HIT");
            return l1Cache.get(videoId);
        }

        System.out.println("L1 MISS");

        // L2
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            simulateDelay(5);
            System.out.println("L2 HIT → Promoting to L1");

            VideoData video = l2Cache.get(videoId);
            l1Cache.put(videoId, video);
            incrementAccess(videoId);
            return video;
        }

        System.out.println("L2 MISS");

        // L3 Database
        l3Hits++;
        simulateDelay(150);
        System.out.println("L3 HIT (Database)");

        VideoData video = new VideoData(videoId);

        // Add to L2 initially
        l2Cache.put(videoId, video);
        incrementAccess(videoId);

        return video;
    }

    private void incrementAccess(String videoId) {
        accessCount.merge(videoId, 1, Integer::sum);

        // Promote if frequently accessed
        if (accessCount.get(videoId) > 5) {
            if (l2Cache.containsKey(videoId)) {
                l1Cache.put(videoId, l2Cache.get(videoId));
            }
        }
    }

    private void simulateDelay(double millis) {
        try {
            Thread.sleep((long) millis);
        } catch (InterruptedException e) {}
    }

    public void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        accessCount.remove(videoId);
        System.out.println("Cache invalidated for " + videoId);
    }

    public void getStatistics() {

        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\n===== CACHE STATS =====");
        System.out.println("L1 Hit Rate: " +
                (l1Hits * 100.0 / total) + "%");
        System.out.println("L2 Hit Rate: " +
                (l2Hits * 100.0 / total) + "%");
        System.out.println("L3 Hit Rate: " +
                (l3Hits * 100.0 / total) + "%");
        System.out.println("=======================");
    }

    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}