import kylm.model.ngram.NgramLM;
import kylm.model.ngram.reader.ArpaNgramReader;
import kylm.model.ngram.reader.NgramReader;
import kylm.model.ngram.reader.SerializedNgramReader;
import kylm.model.ngram.smoother.KNSmoother;
import kylm.model.ngram.smoother.NgramSmoother;
import kylm.model.ngram.writer.ArpaNgramWriter;
import kylm.model.ngram.writer.NgramWriter;
import kylm.model.ngram.writer.SerializedNgramWriter;
import kylm.reader.SentenceReader;
import kylm.reader.TextFileSentenceReader;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapper for KYLM
 */
public class KYLM implements LModel {
    private NgramLM lm;

    /**
     * Trains a new KYLM n-gram model, and optionally writes it on a file
     * @param n the n from n-gram
     * @param name name of the model, the written file will be named by this name
     * @param smoothUnigrams whether unigrams should be smoothed. currently smoothing is Kneser-Ney
     * @param unkCutOff minimum number of occurrences for a word to be considered <unk>
     * @param corpusFile the path to the corpus data
     * @param saveModel whether the model should be saved to a file in the models/ directory
     */
    public KYLM(int n, String name, boolean smoothUnigrams, int unkCutOff, String corpusFile, boolean saveModel) {
        int debug = 1; // 0 for no debug, 1 for debugging prints

        // create smoother
        NgramSmoother smoother = new KNSmoother();
        smoother.setDebugLevel(0);
        smoother.setSmoothUnigrams(smoothUnigrams);

        try {
            // create input corpus reader
            SentenceReader loader = new TextFileSentenceReader(corpusFile);

            // create model object and set fields
            lm = new NgramLM(n, smoother);
            lm.getSmoother().setCutoffs(null);
            lm.setDebug(debug);
            lm.setName(name);
            lm.setUnknownSymbol("<unk>");
            lm.setVocabFrequency(unkCutOff);
            lm.setStartSymbol("<s>");
            lm.setTerminalSymbol("</s>");

            System.err.println("Training model");
            // train the model
            lm.trainModel(loader);
            System.err.println("Training complete");

            if(saveModel) {
                // create writer
                NgramWriter writer = new SerializedNgramWriter();

                System.err.println("Started writing");
                long time = System.currentTimeMillis();

                // print the model
                BufferedOutputStream os = new BufferedOutputStream(
                        new FileOutputStream("models/" + name + ".model"), 16384
                );

                writer.write(lm, os);
                os.close();

                System.err.println("Done writing - "+(System.currentTimeMillis()-time)+" ms");
            }
        } catch(IOException e) {
            System.err.println("Input corpus file not found!");
            System.exit(1);
        }
    }

    /**
     * Reads a KYLM model from the models/ directory
     * @param name name of the model to read
     */
    public KYLM(String name) {
        System.err.println("Reading model");

        NgramReader nr =  new SerializedNgramReader();
        lm = null;
        try {
            lm = nr.read("models/" + name + ".model");
        } catch(IOException e) {
            System.err.println("Problem reading model from file models/"+ name +".model : "+e.getMessage());
            System.exit(1);
        }

        System.err.println("Done reading");
    }

    @Override
    public double logProb(List<String> sentWords) {
        String[] sent = sentWords.toArray(new String[0]);
        return lm.getSentenceProb(sent);
    }

    public static void main(String[] args) {
        // Example usage
        KYLM kylm = new KYLM(3, "full_wiki_3gram", true, 10, "data/wiki_clean.big", true); // create new model
        //KYLM kylm = new KYLM("sentences_3gram"); // read saved model
        LModel model = kylm;
        ArrayList<String> lst = new ArrayList(Arrays.asList(new String[]{"hi", "there", "my", "name", "is", "victor", "."}));
        System.out.println(model.logProb(lst));

        lst = new ArrayList(Arrays.asList(new String[]{"paris", "is", "the", "capital", "of", "france", "."}));
        System.out.println(model.logProb(lst));
    }

}
