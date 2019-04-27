import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LMBigram implements LModel {
    public static final String START_TOKEN = "<s>";
    public static final String END_TOKEN = "</s>";
    public static final String UNKNOWN_TOKEN = "<UNK>";

    private HashMap<String, HashMap<String, Double>> bigrams;
    private HashMap<String, Integer> unigrams;
    private HashMap<String, Integer> counts; // #of times a word appears as the first word of a bigram
    private Set<String> vocab;
    private int totalWords;
    private double discount;
    private HashMap<String, Double> alphas;

    public LMBigram(double discount) {
        this.discount = discount;
        unigrams = new HashMap<>();
        bigrams = new HashMap<>();
        counts = new HashMap<>();
        totalWords = 0;
    }

    public void train(String filename) {
        try {
            // Vocabulary set
            vocab = new HashSet<>();
            vocab.addAll(Arrays.asList(new String[]{UNKNOWN_TOKEN, START_TOKEN, END_TOKEN}));
            HashSet<String> seen = new HashSet<>();
            seen.add(START_TOKEN);
            seen.add(END_TOKEN);

            Scanner sc = new Scanner(new File(filename));
            String line;
            String[] words;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                line = START_TOKEN + " " + line + " " + END_TOKEN;
                words = line.split(" ");

                // Replace first unseen words with unknown token
                for (int j = 0; j < words.length; j++) {
                    if (!seen.contains(words[j])) {
                        seen.add(words[j]);
                        words[j] = UNKNOWN_TOKEN;
                    } else {
                        vocab.add(words[j]);
                    }
                }

                // Count the unigrams and bigrams
                for (int i = 0; i < words.length; i++) {
                    totalWords++;
                    unigrams.merge(words[i], 1, Integer::sum);
                    if (i + 1 < words.length) {
                        counts.merge(words[i], 1, Integer::sum);
                        // Add bigram
                        if (bigrams.containsKey(words[i])) {
                            bigrams.get(words[i]).merge(words[i + 1], 1.0, Double::sum);
                        } else {
                            bigrams.put(words[i], new HashMap<>());
                            bigrams.get(words[i]).put(words[i + 1], 1.0);
                        }
                    }
                }
            }

            // Calculate reserved mass for each word
            alphas = new HashMap<>();
            for(String word : vocab) {
                if(bigrams.containsKey(word)) {
                    double reservedMass = (bigrams.get(word).size() * discount) / counts.get(word);
                    double sum = bigrams.get(word).keySet().stream()
                            .mapToDouble(a -> unigrams.get(a))
                            .reduce(0.0, Double::sum) / totalWords;
                    alphas.put(word, reservedMass / (1.0-sum));
                } else { // when using start/end tokens only the end token should be here
                    alphas.put(word, 0.0);
                }
            }
        } catch(FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }

    @Override
    public double logProb(List<String> sentWords) {
        double sum = 0.0;

        // Make a copy so we don't mess with sentWords
        List<String> words = new ArrayList<>(sentWords);

        // Add start end tokens
        words.add(0, START_TOKEN);
        words.add(END_TOKEN);

        for(int i = 0; i < words.size(); i++) {
            if(i+1 < words.size()) {
                sum += Math.log10(getBigramProb(words.get(i), words.get(i+1)));
            }
        }
        return sum;
    }

    public double getBigramProb(String first, String second) {
        if(!vocab.contains(first)) first = UNKNOWN_TOKEN;
        if(!vocab.contains(second)) second = UNKNOWN_TOKEN;

        // Seen bigram
        if(bigrams.get(first).containsKey(second))
            return (bigrams.get(first).get(second) - discount) / counts.get(first);
            // Seen first word as the first word of a bigram but haven't seen the bigram
        else {
            return (alphas.get(first) * unigrams.get(second) / totalWords);
        }
    }
}
