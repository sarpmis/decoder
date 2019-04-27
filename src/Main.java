import com.sun.javafx.property.JavaBeanAccessHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        LMBigram bimod = new LMBigram(0.01);
        bimod.train("data/sentences");
        LModel lmodel = bimod;

        TModel tmodel = new TMSimple();

        BeamSearch searcher = new BeamSearch(tmodel, lmodel);

        String sent = "yo prometo a todos que no tuve sexo con esa mujer .";
        String[] sentWords = sent.split("\\s");
        ArrayList<String> sentList = new ArrayList(Arrays.asList(sentWords));
        List<String> translation = searcher.runSearch(sentList);
        for (String s : translation) {
            System.out.print(s + " ");
        }
        System.out.println();

    }
}
