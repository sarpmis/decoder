import sun.applet.resources.MsgAppletViewer;

import java.io.*;
import java.util.*;

public class TMSimple implements TModel {

    Map<String,Map<String,Double>> translations;

    public TMSimple() {

        translations = new HashMap<>();

        // Read from file
        try {
            Scanner sc = new Scanner(new File("data/spanToEng.txt"));
            Scanner sc2 = new Scanner(new File("data/spanishCommon.txt"));
            while (sc.hasNext() || sc2.hasNext()) {

                String line;
                if (sc.hasNext()) line = sc.nextLine();
                else line = sc2.nextLine();

                String[] words = line.split("\\s");
                if (words.length == 2) {
                    String frWord = words[0];
                    String enWord = words[1];

                    // Ensure keys
                    if (!translations.containsKey(frWord)) translations.put(frWord, new HashMap<>());

                    translations.get(frWord).put(enWord, 1.0);
                }
            }
        } catch (IOException e) {
            // Print something
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
