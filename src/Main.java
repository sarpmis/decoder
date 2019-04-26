import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world");

        LMBigram bimod = new LMBigram(0.01);
        bimod.train("data/sentences");

        LModel model = bimod;
        ArrayList<String> lst = new ArrayList(Arrays.asList(new String[]{"hi", "my", "name", "is", "victor"}));
        System.out.println(model.logProb(lst));
    }
}
