import com.sun.javafx.property.JavaBeanAccessHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private final double LM_DISCOUNT = 0.01;

    public Main () {

        LMBigram bimod = new LMBigram(LM_DISCOUNT);
        bimod.train("data/sentences");
        LModel lmodel = bimod;

        TModel tmodel = new TMSimple();

        BeamSearch searcher = new BeamSearch(tmodel, lmodel);

        ArrayList<String> sents = new ArrayList<>();

        // This sent doesn't translate well because tuve, sexo and esa don't have translations in EM
        sents.add("yo prometo a todos que no tuve sexo con esa mujer .");
        // This sent shows that we need EM - no doesn't map to "doesn't"
        sents.add("ella no habla con nadie");
        sents.add("nosotros escribimos la carta esta mañana .");
        sents.add("ponerse el sombrero .");
        sents.add("me gustaría que vinieran .");
        sents.add("yo voy al hospital .");
        sents.add("yo hablo con él");
        sents.add("yo soy muy inteligente");

        for (String sent : sents) {

            String[] sentWords = sent.split("\\s");
            ArrayList<String> sentList = new ArrayList(Arrays.asList(sentWords));
            List<String> translation = searcher.runSearch(sentList);
            for (String s : translation) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
