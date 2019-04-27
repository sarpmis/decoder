import java.util.*;

public class BeamOption implements Comparable<BeamOption> {
    private List<String> words;
    private double wordProbs;
    private double penalty;

    private LModel lmodel;

    public BeamOption(LModel lmodel) {
        words = new LinkedList<String>();
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

    public void addPenalty(double p) {
        penalty += p;
    }

    public double getPenalty() {
        return penalty;
    }

    public void addWord(String w, double p) {
        words.add(w);
        wordProbs += p;
    }

    public List<String> getWords() {
        return words;
    }

    public Double getScore() {
        double lmProb = lmodel.logProb(words);
        return wordProbs + lmProb + penalty;
    }

    public double getWordProbs() {
        return wordProbs;
    }

    @Override
    public int compareTo(BeamOption other) {
        return getScore().compareTo(other.getScore());
    }
}
