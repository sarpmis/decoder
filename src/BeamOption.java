import java.util.*;

public class BeamOption implements Comparable<BeamOption> {

    // >1: word probs more important, <1: lm probs more important
    private final double TM_TO_LM_RATIO = 30.0;
    private final int MAX_SWAP_DIST = 0;
    private final double SWAP_PENALTY = 0.2;

    private List<String> words;
    private double wordProbs;
    private double penalty;

    private LModel lmodel;

    public BeamOption(LModel lmodel) {
        words = new LinkedList<>();
        wordProbs = 0;
        penalty = 0;
        this.lmodel = lmodel;
    }

    public BeamOption(LModel lmodel, BeamOption oldOption) {
        words = (LinkedList<String>) ((LinkedList<String>)oldOption.getWords()).clone();
        wordProbs = oldOption.getWordProbs();
        penalty = oldOption.getPenalty();
        this.lmodel = lmodel;
    }

    public List<BeamOption> getAllExtensions(String w, double p) {

        List<BeamOption> newOpts = new LinkedList<>();

        int maxDist = Math.min(words.size(), MAX_SWAP_DIST);

        for (int swapDist = 0; swapDist <= maxDist; swapDist += 1) {
            int pos = words.size() - swapDist;
            BeamOption newOpt = new BeamOption(lmodel, this);
            newOpt.addWord(w, p, pos);
            newOpt.addPenalty(computePenalty(swapDist));
            newOpts.add(newOpt);
        }

        return newOpts;

    }

    private double computePenalty (int swapDist) {
        return SWAP_PENALTY * Math.pow(1.5, -1.0 * swapDist) - 1.0;
    }

    public void addPenalty(double p) {
        penalty += p;
    }

    public double getPenalty() {
        return penalty;
    }

    public void addWord(String w, double p, int position) {
        words.add(position, w);
        wordProbs += p;
    }

    public List<String> getWords() {
        return words;
    }

    public Double getScore() {
        double lmProb = lmodel.logProb(words);
        lmProb = 0; // TODO remove
        return (wordProbs * TM_TO_LM_RATIO + lmProb) / (TM_TO_LM_RATIO + 1.0) + penalty;
    }

    public double getWordProbs() {
        return wordProbs;
    }

    @Override
    public int compareTo(BeamOption other) {
        return getScore().compareTo(other.getScore());
    }
}
