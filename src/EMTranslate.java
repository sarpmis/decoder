import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/** Given English and Foreign sentence files,
 * prints out the most likely word to word translations using EM alignment algorithm
 * @author Sarp + Dylan
 */
public class EMTranslate {
    private HashMap<String, HashMap<String, Double>> table;
    private HashMap<String, HashMap<String, Double>> countPairs;
    private HashMap<String, Double> count;

    private static final double INIT_PROB = 0.01;

    EMTranslate() {
    }
    /**
     * @param engFile the path to english sentences
     * @param forFile the path to foreign sentences
     * @param iterations number of iterations
     */
    public HashMap runEM(String engFile, String forFile, int iterations) {
        table = new HashMap<>(); //stores p(f|e)
        countPairs = new HashMap<>();
        count = new HashMap<>();

        try {
            Scanner engScan = new Scanner(new File(engFile));
            Scanner forScan = new Scanner(new File(forFile));

            List<String[]> engSents = new ArrayList<>();
            List<String[]> fornSents = new ArrayList<>();

            // initial step
            while(engScan.hasNextLine() && forScan.hasNextLine()) {
                String eng = "NULL " + engScan.nextLine(); // add NULL word here
                String forn = forScan.nextLine();

                String[] engLst = eng.split("\\s+");
                String[] fornLst = forn.split("\\s+");

                engSents.add(engLst);
                fornSents.add(fornLst);

                // initialize each entry in table with the initial probability
                for (String engWord : engLst) {
                    if(!table.containsKey(engWord)) {
                        table.put(engWord, new HashMap<>());
                        countPairs.put(engWord, new HashMap<>());
                    }
                    for (String fornWord : fornLst) {
                        if(!table.get(engWord).containsKey(fornWord)) {
                            table.get(engWord).put(fornWord, INIT_PROB);
                        }
                    }
                }
            }

            //run EM Algorithm for iterations
            for(int i = 0; i < iterations; i++) {
                for(int k = 0; k < engSents.size(); k++) {
                    String[] engLst = engSents.get(k);
                    String[] fornLst = fornSents.get(k);

                    // memoize denominators for efficiency
                    HashMap<String, Double> sums = new HashMap<>();
                    HashSet<String> seen = new HashSet<>();

                    for(String engWord : engLst) {
                        seen.add(engWord);
                    }

                    for(String engWord : engLst) {
                        for(String fornWord : fornLst) {
                            double fToE;
                            double numerator = table.get(engWord).get(fornWord);

                            if(sums.containsKey(fornWord)) {
                                fToE = numerator / sums.get(fornWord);
                            } else {
                                double sum = 0;
                                for(String s : seen) {
                                    sum += table.get(s).get(fornWord);
                                }
                                sums.put(fornWord, sum);
                                fToE = numerator / sum;
                            }

                            countPairs.get(engWord).merge(fornWord, fToE, Double::sum);
                            count.merge(engWord, fToE, Double::sum);
                        }
                    }
                }

                // normalize
                for(String e : countPairs.keySet()) {
                    HashMap<String, Double> fs = countPairs.get(e);
                    for(String f : fs.keySet()) {
                        table.get(e).put(f, fs.get(f) / count.get(e));
                    }
                }

                // reset counts
                count = new HashMap<>();
                for(Map.Entry e : countPairs.entrySet()) {
                    e.setValue(new HashMap<>());
                }
            }

            return table;

        } catch(FileNotFoundException e) {
            System.out.println("file no foundy");
        }
        return table;
    }

    /**
     * Prints the probabilities in alphabetical order
     * @param threshold the minimum probability to print
     */
    public void printProbabilities(double threshold) {
        List<String> sortedEng = table.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        for(String engWord : sortedEng) {
            HashMap<String, Double> fs = table.get(engWord);

            // sort words alphabetically
            List<String> sortedForn = fs.keySet()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            for(String fornWord : sortedForn) {
                double prob = fs.get(fornWord);

                // print only if probability is above threshold
                if(prob > threshold) {
                    System.out.println(engWord + "\t" + fornWord + "\t" + prob);
                }
            }
        }
    }

    /**
     * Prints 25 random entries from the table
     * @param threshold the minimum probability to print
     */
    public void printRandom(double threshold) {
        System.out.println("\nPrinting 25 random entries!\n");
        Random r = new Random();
        Object[] engs = table.keySet().toArray();

        int count = 0;
        while(count < 25) {
            // pick random english word
            String eng = (String)engs[r.nextInt(engs.length)];

            // pick random foreign word that it maps to
            Object[] forns = table.get(eng).keySet().toArray();
            String forn = (String) forns[r.nextInt(forns.length)];

            // if val > threshold print
            double val = table.get(eng).get(forn);
            if(val > threshold) {
                System.out.println(eng + "\t" + forn + "\t" + val);
                count++;
            }
        }
    }
    public void printToFile(double threshold, String fileName){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File (fileName)));
            List<String> sortedEng = table.keySet()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            for(String engWord : sortedEng) {
                HashMap<String, Double> fs = table.get(engWord);

                // sort words alphabetically
                List<String> sortedForn = fs.keySet()
                        .stream()
                        .sorted()
                        .collect(Collectors.toList());

                for(String fornWord : sortedForn) {
                    double prob = fs.get(fornWord);

                    // print only if probability is above threshold
                    if(prob > threshold) {
                        //System.out.println(engWord + "\t" + fornWord + "\t" + prob);
                        writer.write(engWord + "\t" + fornWord + "\t" + prob+"\n");
                    }
                }
            }
        }catch(IOException e){

        }

    }

    public static void main(String[] args) {
        EMTranslate em = new EMTranslate();
        em.runEM("data/Span10k.txt", "data/En10k.txt", 10);
        em.printToFile(.01, "data/emSpanToEng.txt");
    }
}
