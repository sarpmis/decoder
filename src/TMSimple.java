import sun.applet.resources.MsgAppletViewer;

import java.io.*;
import java.util.*;

public class TMSimple implements TModel {

    private final int MAX_TRANSLATIONS = 3;

    // Input from EM file
    private Map<String,Map<String,Double>> emResults;

    // Input from dictionary
    private Map<String,Map<String,Double>> dictResults;

    // Final translations
    private Map<String,Map<String,Double>> translations;

    private void loadFileIntoMap(String fileName, Map<String,Map<String,Double>> map) {
        try {
            // Use EM model
            Scanner sc = new Scanner(new File(fileName));

            while (sc.hasNext()) {

                String line = sc.nextLine();

                String[] words = line.split("\\s");
                if (words.length >= 2) {
                    String frWord = words[0];
                    String enWord = words[1];

                    frWord = frWord.toLowerCase();
                    enWord = enWord.toLowerCase();

                    double prob = 1.0;
                    if (words.length == 3) {
                        prob = Double.valueOf(words[2]);
                    }

                    // Ensure keys
                    if (!map.containsKey(frWord)) map.put(frWord, new HashMap<>());

                    map.get(frWord).put(enWord, prob);
                }
            }
        } catch (IOException e) {
            // Print something
        }
    }

    private class WordAndProb implements Comparable<WordAndProb> {

        private String word;
        private Double prob;

        public WordAndProb (String word, Double prob) {
            this.word = word;
            this.prob = prob;
        }

        public String getWord() {
            return word;
        }

        public Double getProb() {
            return prob;
        }

        @Override
        public int compareTo(WordAndProb other) {
            return prob.compareTo(other.getProb());
        }
    }

    public TMSimple() {

        emResults = new HashMap<>();
        dictResults = new HashMap<>();
        translations = new HashMap<>();

        loadFileIntoMap("data/emSpanToEng.txt", emResults);
        loadFileIntoMap("data/spanToEng.txt", dictResults);
        loadFileIntoMap("data/spanishCommon.txt", dictResults);

        // Combine EM and dict
        for (String frWord : emResults.keySet()) {

            Map<String, Double> emEnWords = emResults.get(frWord);

            // If in dictionary, use EM probs
            if (dictResults.containsKey(frWord)) {

                Map<String, Double> dictEnWords = dictResults.get(frWord);
                for (String enWord : dictEnWords.keySet()) {

                    // Make sure EM actually has this particular translation
                    if (emEnWords.containsKey(enWord)) {
                        double prob = emEnWords.get(enWord);

                        // Ensure key
                        if (!translations.containsKey(frWord)) translations.put(frWord, new HashMap<>());

                        translations.get(frWord).put(enWord, prob);
                    }

                }

            }

            // If not in dictionary, get MAX_TRANSLATION best ones
            else {
                PriorityQueue<WordAndProb> pq = new PriorityQueue<>();
                for (String enWord : emEnWords.keySet()) {
                    double prob = emEnWords.get(enWord);
                    WordAndProb wap = new WordAndProb(enWord, -1 * prob);
                    pq.add(wap);
                }

                // Take off most likely words
                for (int i = 0; i < MAX_TRANSLATIONS && pq.size() > 0; i += 1) {
                    WordAndProb wap = pq.poll();
                    String word = wap.getWord();
                    double prob = wap.getProb() * -1;

                    // Ensure key
                    if (!translations.containsKey(frWord)) translations.put(frWord, new HashMap<>());

                    translations.get(frWord).put(word, prob);
                }
            }

        }

    }

    @Override
    public Map<String, Double> getTranslations(String frWord) {
        if (!translations.containsKey(frWord)) {
            Map<String, Double> ts = new HashMap<>();
            ts.put("NULL", 1.0);
            return ts;
        }
        else return translations.get(frWord);
    }

    public static void main (String[] args) {
        TMSimple ts = new TMSimple();
        Map<String, Double> translations = ts.getTranslations("yo");
        for (String s : translations.keySet()) {
            System.out.println(s + " " + translations.get(s));
        }
    }

}
