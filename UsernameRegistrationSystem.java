import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameRegistrationSystem {

    // Stores username → userId
    private ConcurrentHashMap<String, Long> usernameMap = new ConcurrentHashMap<>();

    // Stores username → attempt count
    private ConcurrentHashMap<String, Integer> attemptFrequency = new ConcurrentHashMap<>();

    public UsernameRegistrationSystem() {
        // Sample existing users
        usernameMap.put("john_doe", 101L);
        usernameMap.put("admin", 1L);
        usernameMap.put("testuser", 202L);
    }

    // O(1) availability check
    public boolean checkAvailability(String username) {

        attemptFrequency.merge(username, 1, Integer::sum);

        return !usernameMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        if (checkAvailability(username)) {
            suggestions.add(username);
            return suggestions;
        }

        // Append numbers
        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace underscore with dot
        if (username.contains("_")) {
            String modified = username.replace("_", ".");
            if (!usernameMap.containsKey(modified)) {
                suggestions.add(modified);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static void main(String[] args) {

        UsernameRegistrationSystem system = new UsernameRegistrationSystem();

        System.out.println(system.checkAvailability("john_doe"));   // false
        System.out.println(system.checkAvailability("jane_smith")); // true

        System.out.println(system.suggestAlternatives("john_doe"));

        System.out.println("Most attempted: " + system.getMostAttempted());
    }
}