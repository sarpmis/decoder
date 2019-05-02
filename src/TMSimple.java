import sun.applet.resources.MsgAppletViewer;

import java.io.*;
import java.util.*;

public class TMSimple implements TModel {

    private final int MAX_TRANSLATIONS = 5;

    // Add more weight to items in dict
    private final double DICT_PREFERENCE = Math.log(2.0);


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
                    // Make it a log prob
                    prob = Math.log10(prob);

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

        loadFileIntoMap("data/emSpanToEng100kCleanedFinal.txt", emResults);
        loadFileIntoMap("data/spanToEng.txt", dictResults);
        loadFileIntoMap("data/spanishCommon.txt", dictResults);

        // Combine EM and dict
        for (String frWord : emResults.keySet()) {

            translations.put(frWord, new HashMap<>());

            Map<String, Double> emEnWords = emResults.get(frWord);

            // If in dictionary, use EM probs
            if (dictResults.containsKey(frWord)) {

                // Words in dict
                Map<String, Double> dictEnWords = dictResults.get(frWord);

                // Get the best prob of anything in EM
                double bestProb = -100000000.0;
                for (String s : emEnWords.keySet()) {
                    double curProb = emEnWords.get(s);
                    bestProb = Math.max(bestProb, curProb);
                }
                bestProb += DICT_PREFERENCE;
                if (bestProb < -100000) bestProb = 0.0;

                // For all dict words not in EM, use best prob
                for (String enWord : dictEnWords.keySet()) {
                    translations.get(frWord).put(enWord, bestProb);
                }
            }

            // Fill up with EM words until we have MAX_TRANSLATIONS
            int wordsLeft = MAX_TRANSLATIONS - translations.get(frWord).size();
            if (wordsLeft > 0) {
                PriorityQueue<WordAndProb> pq = new PriorityQueue<>();
                for (String enWord : emEnWords.keySet()) {
                    // Only add if not already added by dict
                    if (!translations.get(frWord).containsKey(enWord)) {
                        double prob = emEnWords.get(enWord);
                        WordAndProb wap = new WordAndProb(enWord, -1 * prob);
                        pq.add(wap);
                    }
                }

                // Take off most likely words
                for (int i = 0; i < wordsLeft && pq.size() > 0; i += 1) {
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
