import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    private final String SPANISH = "data/final/SpanClean";
    private final String TRANS = "data/final/trans/noSwap";
    private final String REAL = "data/final/EnClean.txt";

    private final double LM_DISCOUNT = 0.01;

    private List<String> readSents(String fileName) {

        List<String> sents = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File(fileName));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!(line.charAt(0) == '#')) {
                    sents.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading input sentences.");
        }

        return sents;
    }

    private void writeSentsToFile(List<String> sents, String fileName) {
        try {
            FileWriter writer = new FileWriter(new File(fileName));
            for (String sent : sents) {
                writer.write(sent + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing translations to file");
        }
    }

    public Main () {
        LModel lmodel = new KYLM("1mil_wiki_3gram");

        TModel tmodel = new TMSimple();

        BeamSearch searcher = new BeamSearch(tmodel, lmodel);

        List<String> sents = readSents(SPANISH);

        List<String> outputs = new ArrayList<>();

        for (String sent : sents) {

            System.out.println("Translate sentence: " + sent);
            String[] sentWords = sent.split("\\s");
            ArrayList<String> sentList = new ArrayList(Arrays.asList(sentWords));
            List<String> translation = searcher.runSearch(sentList);

            StringJoiner sj = new StringJoiner(" ");
            for (String s : translation) {
                sj.add(s);
            }
            String outputSent = sj.toString();
            outputs.add(outputSent);
        }

        writeSentsToFile(outputs, TRANS);

        double bleuScore = new BLEU().runBLEU(TRANS, REAL);
        System.out.println("Bleu score: " + bleuScore);
    }

    public static void main(String[] args) {
        new Main();
    }
}
