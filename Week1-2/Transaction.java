import java.util.*;

class Transaction {
    int id;
    double amount;
    String merchant;
    String account;
    long timestamp;

    public Transaction(int id, double amount,
                       String merchant, String account,
                       long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }

    public String toString() {
        return "id:" + id + " amount:" + amount;
    }
}

public class FinancialTransactionAnalyzer {

    // 1️⃣ Classic Two-Sum
    public static List<List<Transaction>> findTwoSum(
            List<Transaction> transactions, double target) {

        Map<Double, Transaction> map = new HashMap<>();
        List<List<Transaction>> result = new ArrayList<>();

        for (Transaction tx : transactions) {

            double complement = target - tx.amount;

            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement), tx));
            }

            map.put(tx.amount, tx);
        }

        return result;
    }

    // 2️⃣ Two-Sum with Time Window (1 hour)
    public static List<List<Transaction>> findTwoSumWithWindow(
            List<Transaction> transactions,
            double target,
            long windowMillis) {

        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        List<List<Transaction>> result = new ArrayList<>();
        int left = 0;
        Map<Double, Transaction> map = new HashMap<>();

        for (int right = 0; right < transactions.size(); right++) {

            while (transactions.get(right).timestamp -
                   transactions.get(left).timestamp > windowMillis) {

                map.remove(transactions.get(left).amount);
                left++;
            }

            double complement = target - transactions.get(right).amount;

            if (map.containsKey(complement)) {
                result.add(Arrays.asList(
                        map.get(complement),
                        transactions.get(right)));
            }

            map.put(transactions.get(right).amount,
                    transactions.get(right));
        }

        return result;
    }

    // 3️⃣ K-Sum (generalized)
    public static List<List<Transaction>> findKSum(
            List<Transaction> transactions,
            int k,
            double target) {

        transactions.sort(Comparator.comparingDouble(t -> t.amount));
        return kSumHelper(transactions, 0, k, target);
    }

    private static List<List<Transaction>> kSumHelper(
            List<Transaction> list,
            int start,
            int k,
            double target) {

        List<List<Transaction>> result = new ArrayList<>();

        if (k == 2) {
            Map<Double, Transaction> map = new HashMap<>();

            for (int i = start; i < list.size(); i++) {

                double complement = target - list.get(i).amount;

                if (map.containsKey(complement)) {
                    result.add(Arrays.asList(
                            map.get(complement),
                            list.get(i)));
                }

                map.put(list.get(i).amount, list.get(i));
            }

            return result;
        }

        for (int i = start; i < list.size(); i++) {

            for (List<Transaction> subset :
                    kSumHelper(list, i + 1,
                            k - 1,
                            target - list.get(i).amount)) {

                List<Transaction> combo = new ArrayList<>();
                combo.add(list.get(i));
                combo.addAll(subset);
                result.add(combo);
            }
        }

        return result;
    }

    // 4️⃣ Duplicate Detection
    public static Map<String, List<Transaction>> detectDuplicates(
            List<Transaction> transactions) {

        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction tx : transactions) {

            String key = tx.amount + "_" + tx.merchant;

            map.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(tx);
        }

        Map<String, List<Transaction>> duplicates = new HashMap<>();

        for (Map.Entry<String, List<Transaction>> entry :
                map.entrySet()) {

            Set<String> accounts = new HashSet<>();

            for (Transaction tx : entry.getValue()) {
                accounts.add(tx.account);
            }

            if (accounts.size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }

    // MAIN METHOD (Testing)
    public static void main(String[] args) {

        long now = System.currentTimeMillis();

        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", now),
                new Transaction(2, 300, "Store B", "acc2", now + 1000),
                new Transaction(3, 200, "Store C", "acc3", now + 2000),
                new Transaction(4, 500, "Store A", "acc2", now + 3000)
        );

        System.out.println("Two-Sum (500): " +
                findTwoSum(transactions, 500));

        System.out.println("Two-Sum with Window (500): " +
                findTwoSumWithWindow(transactions,
                        500, 3600_000));

        System.out.println("K-Sum (k=3, target=1000): " +
                findKSum(transactions, 3, 1000));

        System.out.println("Duplicates: " +
                detectDuplicates(transactions));
    }
}
