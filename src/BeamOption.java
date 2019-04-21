import java.util.*;

public class BeamOption implements Comparable<BeamOption> {
    private List<String> words;
    private double wordProbs;
    private double penalty;
    private double langModelProb;

    public BeamOption() {
        words = new LinkedList<String>();
        wordProbs = 0;
        penalty = 0;
        langModelProb = 0;
    }

    public BeamOption(BeamOption oldOption) {
        words = (LinkedList<String>) ((LinkedList<String>)oldOption.getWords()).clone();
        wordProbs = oldOption.getWordProbs();
        penalty = oldOption.getPenalty();
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
        langModelProb = 0; // TODO Update to actually calculate using language model
    }

    public List<String> getWords() {
        return words;
    }

    public Double getScore() {
        return wordProbs + penalty;
    }

    public double getWordProbs() {
        return wordProbs;
    }

    @Override
    public int compareTo(BeamOption other) {
        return getScore().compareTo(other.getScore());
    }
}
