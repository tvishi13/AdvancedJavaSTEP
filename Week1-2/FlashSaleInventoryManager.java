import java.util.concurrent.*;
import java.util.*;

public class FlashSaleInventoryManager {

    // productId → stock count
    private ConcurrentHashMap<String, AtomicInteger> stockMap;

    // productId → waiting list (FIFO)
    private ConcurrentHashMap<String, Queue<Long>> waitingListMap;

    public FlashSaleInventoryManager() {

        // Pre-size to reduce resizing under load
        stockMap = new ConcurrentHashMap<>(16, 0.75f);
        waitingListMap = new ConcurrentHashMap<>();

        // Example product with 100 units
        stockMap.put("IPHONE15_256GB", new AtomicInteger(100));
        waitingListMap.put("IPHONE15_256GB", new ConcurrentLinkedQueue<>());
    }

    // O(1) Stock Check
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    // Purchase Operation (Thread-Safe)
    public String purchaseItem(String productId, long userId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();

            if (currentStock <= 0) {
                waitingListMap.get(productId).add(userId);
                return "Added to waiting list, position #" +
                        waitingListMap.get(productId).size();
            }

            // Atomic decrement using compareAndSet
            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    public static void main(String[] args) {

        FlashSaleInventoryManager manager =
                new FlashSaleInventoryManager();

        System.out.println(
                "Stock: " +
                manager.checkStock("IPHONE15_256GB")
        );

        System.out.println(
                manager.purchaseItem("IPHONE15_256GB", 12345)
        );

        System.out.println(
                manager.purchaseItem("IPHONE15_256GB", 67890)
        );
    }
}
