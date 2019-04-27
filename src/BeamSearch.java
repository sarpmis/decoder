import java.util.*;

public class BeamSearch {

    final int BEAM_SIZE = 10;

    private TModel tmodel;
    private LModel lmodel;

    /**
     * Creates a beam search with given
     * probability map.
     *
     * @param tmodel
     */
    public BeamSearch(TModel tmodel, LModel lmodel) {
        this.tmodel = tmodel;
        this.lmodel = lmodel;
    }

    /**
     * Given a foreign sentence, runs a beam
     * search and returns the best
     * translation found.
     *
     * @param sentence the list of words in the foreign sentence
     * @return List of words with best translation
     */
    public List<String> runSearch(List<String> sentence) {
        PriorityQueue<BeamOption> beamQueue = new PriorityQueue<>();
        beamQueue.add(new BeamOption(lmodel));

        // consider each foreign word and extend all options by one
        for (String word : sentence) {

            PriorityQueue<BeamOption> newQueue = new PriorityQueue<>();
            for (BeamOption option : beamQueue) {

                Map<String, Double> engWords = tmodel.getTranslations(word);

                for (String engWord : engWords.keySet()) {

                    BeamOption newOption = new BeamOption(lmodel, option);
                    newOption.addWord(engWord, engWords.get(engWord));
                    newQueue.add(newOption);
                }
            }

            // removing option to fit beam size
            beamQueue = newQueue;
            while (beamQueue.size() > BEAM_SIZE) {
                beamQueue.poll();
            }
        }

        // find best option
        while (beamQueue.size() > 1) {
            beamQueue.poll();
        }

        return beamQueue.poll().getWords();
    }

    // Test with a toy example to make sure that it works
    // Expected output: e1 e3 e6
    public static void main(String[] args) {
        Map<String, Map<String, Double>> transMap = new HashMap<>();

        Map<String, Double> f1Map = new HashMap<>();
        f1Map.put("e1", 0.7);
        f1Map.put("e2", 0.3);

        Map<String, Double> f2Map = new HashMap<>();
        f2Map.put("e3", 0.6);
        f2Map.put("e4", 0.5);

        Map<String, Double> f3Map = new HashMap<>();
        f3Map.put("e5", 0.2);
        f3Map.put("e6", 0.3);

        transMap.put("f1", f1Map);
        transMap.put("f2", f2Map);
        transMap.put("f3", f3Map);


        List<String> sent = new ArrayList<>();
        sent.add("f1");
        sent.add("f2");
        sent.add("f3");


//        BeamSearch bs = new BeamSearch(transMap);
//
//        List<String> translation = bs.runSearch(sent);
//        for (String s : translation) {
//            System.out.println(s);
//        }
    }
}
