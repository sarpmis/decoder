import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private final double LM_DISCOUNT = 0.01;

    public Main () {
        LModel lmodel = new KYLM("1mil_wiki_3gram");

        TModel tmodel = new TMSimple();

        BeamSearch searcher = new BeamSearch(tmodel, lmodel);

        ArrayList<String> sents = new ArrayList<>();

        // This sent doesn't translate well because tuve, sexo and esa don't have translations in EM
//        sents.add("yo prometo a todos que no tuve sexo con esa mujer .");
//        // This sent shows that we need EM - no doesn't map to "doesn't"
//        sents.add("ella no habla con nadie .");
//        sents.add("nosotros escribimos la carta esta mañana .");
//        sents.add("ponerse el sombrero .");
//        sents.add("me gustaría que vinieran .");
//        sents.add("yo voy al hospital .");
//        sents.add("yo hablo con él .");
//        sents.add("yo soy muy inteligente .");
        sents.add("la presencia humana en méxico se remonta a %NUM% años antes del presente .");
        sents.add("es una planta herbácea anual , erecta o trepadora , de tallo pubescente o glabrescente cuando adulta .");
        sents.add("las flores se disponen en racimos usualmente axilares , más cortos que las hojas .");
        sents.add("méxico es el undécimo país más poblado del mundo , con una población estimada en más de %NUM% millones de personas en %NUM% .");
        sents.add("destaca su gran arquitectura monumental , y sus ofrendas hechas de jade .");

        for (String sent : sents) {

            System.out.println("Translate sentence: " + sent);
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
