import java.util.*;

public class PlagiarismDetector {

    // n-gram → documents containing it
    private HashMap<String, Set<String>> invertedIndex = new HashMap<>();

    // document → total n-grams count
    private HashMap<String, Integer> documentSizeMap = new HashMap<>();

    private final int N = 5;  // 5-gram

    // Add document to database
    public void indexDocument(String docId, String content) {

        List<String> ngrams = generateNGrams(content);

        documentSizeMap.put(docId, ngrams.size());

        for (String ngram : ngrams) {
            invertedIndex
                    .computeIfAbsent(ngram, k -> new HashSet<>())
                    .add(docId);
        }
    }

    // Analyze new document
    public void analyzeDocument(String docId, String content) {

        List<String> ngrams = generateNGrams(content);

        Map<String, Integer> matchCounts = new HashMap<>();

        for (String ngram : ngrams) {

            if (invertedIndex.containsKey(ngram)) {

                for (String matchedDoc : invertedIndex.get(ngram)) {
                    matchCounts.merge(matchedDoc, 1, Integer::sum);
                }
            }
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (String matchedDoc : matchCounts.keySet()) {

            int matches = matchCounts.get(matchedDoc);
            int total = documentSizeMap.get(matchedDoc);

            double similarity =
                    (matches * 100.0) / total;

            System.out.println("Found " + matches +
                    " matching n-grams with \"" + matchedDoc + "\"");
            System.out.println("Similarity: " +
                    String.format("%.2f", similarity) + "%");

            if (similarity > 60) {
                System.out.println("⚠ PLAGIARISM DETECTED");
            }
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {

        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {

            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }

            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        detector.indexDocument("essay_089.txt",
                "Artificial intelligence is transforming education worldwide");

        detector.indexDocument("essay_092.txt",
                "Machine learning and artificial intelligence are transforming education worldwide rapidly");

        detector.analyzeDocument("essay_123.txt",
                "Artificial intelligence is transforming education worldwide rapidly");
    }
}
