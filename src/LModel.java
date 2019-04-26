import java.util.ArrayList;

/**
 * Interface for a language model, similar to the one
 * given in Assignment 2 by Dr. Dave
 */
public interface LModel {

    public double logProb(ArrayList<String> sentWords);
}
