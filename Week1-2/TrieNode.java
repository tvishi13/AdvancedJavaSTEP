import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEnd;
    String query;
}

public class AutocompleteSystem {

    private TrieNode root = new TrieNode();
    private HashMap<String, Integer> frequencyMap = new HashMap<>();

    // Insert query into Trie
    public void insert(String query) {

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }

        node.isEnd = true;
        node.query = query;
    }

    // Update frequency
    public void updateFrequency(String query) {

        frequencyMap.merge(query, 1, Integer::sum);

        // If new query, insert into trie
        if (!frequencyMap.containsKey(query)) {
            insert(query);
        }
    }

    // Search top 10 suggestions
    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        List<String> results = new ArrayList<>();
        dfs(node, results);

        // Get top 10 by frequency
        PriorityQueue<String> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(frequencyMap::get));

        for (String query : results) {
            minHeap.offer(query);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<String> top = new ArrayList<>(minHeap);
        top.sort((a, b) -> frequencyMap.get(b) - frequencyMap.get(a));

        return top;
    }

    private void dfs(TrieNode node, List<String> results) {

        if (node.isEnd) {
            results.add(node.query);
        }

        for (TrieNode child : node.children.values()) {
            dfs(child, results);
        }
    }
}
