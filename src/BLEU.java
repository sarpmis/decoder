import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class BLEU {
    private int r = 0; //r word count
    private int c = 0; //c word count
    private static final int N =4;
    public BLEU(){

    }

    public static void main(String args[]){
        BLEU b =  new BLEU();
        //System.out.println("BLEU score "+b.runBLEU("mtTest1.txt","rtTest1.txt"));
        System.out.println("lols");
        System.out.println("reference translation "+b.runBLEU("en30Final.txt","en30Final.txt"));

        System.out.println("gTrans "+b.runBLEU("gTransFinal.txt","en30Final.txt"));
        System.out.println("base "+b.runBLEU("base.txt","en30Final.txt"));
        System.out.println("LMMoreImportant "+b.runBLEU("LMMoreImportant.txt","en30Final.txt"));
        System.out.println("LowSwap "+b.runBLEU("LowSwap.txt","en30Final.txt"));
        System.out.println("wordForWord "+b.runBLEU("wordForWord.txt","en30Final.txt"));
        System.out.println("lmEvenMoreImportant "+b.runBLEU("lmEvenMoreImportant.txt","en30Final.txt"));
        System.out.println("lmMediumImportant "+b.runBLEU("LMMediumImportance.txt","en30Final.txt"));
        System.out.println("no swap "+b.runBLEU("noSwap.txt","en30Final.txt"));
        System.out.println("swap penalty .5 "+b.runBLEU("swapPenaltyPoint5.txt","en30Final.txt"));
        System.out.println("swap penalty .1 "+b.runBLEU("swapPenaltyPoint1.txt","en30Final.txt"));


        b.cleanFile("gTrans.txt", "gTransFinal.txt");


    }
    public double runBLEU(String MTFile, String RTFile){
        r=0;
        c=0;
        double sum=0;
        this.getLengths(MTFile, RTFile);
        //System.out.println("Reference translation corpus length "+r+" Machine translation corpus length "+c);
        for(int n =1; n<=N;n++){
            double pn = this.getPn(MTFile,RTFile,n);
           // System.out.println("Reference translation corpus length "+r+" Machine translation corpus length "+c);

           // System.out.println(n+" "+pn);
            if(pn >0){
                sum+= Math.log(pn)/((double)N);
            }
        }

        double BP = 1;
        if(c<=r){
            BP= Math.exp(1-((double)r/(double)c ));
        }
        return BP*Math.exp(sum);
    }

    public double getPn(String MTFile, String RTFile, int n){
        int numerator =0;
        int denominator =0;

        try{
            Scanner scanMT = new Scanner(new File(MTFile));
            Scanner scanRT =  new Scanner(new File(RTFile));
            while(scanRT.hasNext() && scanMT.hasNext()){
                //alt enter
                //get n gram counts for reference translation
                String[] rt = scanRT.nextLine().toLowerCase().replaceAll("[^(a-z| )]","").split("\\s");
                HashMap<String, Integer> rtCount = new HashMap<>();
                for(int i=0;i+n<=rt.length;i++){
                    String w = rt[i];
                    for(int j=i+1;j<i+n;j++){
                        w= w+" "+rt[j];
                    }
                    rtCount.merge(w, 1, Integer::sum);
                }

                //get n gram counts for machine translation
                String[] mt = scanMT.nextLine().toLowerCase().replaceAll("[^(a-z| )]","").split("\\s");
                HashMap<String, Integer> mtCount = new HashMap<>();
                for(int i=0;i+n<=mt.length;i++){
                    String w = mt[i];
                    for(int j=i+1;j<i+n;j++){
                        w= w+" "+mt[j];
                    }
                    mtCount.merge(w, 1, Integer::sum);
                }
                //get intersection
                mtCount.keySet().retainAll(rtCount.keySet());
                //add clipped count to numerator
                for(String w:mtCount.keySet()){
                    if(rtCount.get(w)<mtCount.get(w)){
                        numerator+= rtCount.get(w);
                    }else{
                        numerator+= mtCount.get(w);
                    }
                }
                denominator+= mt.length-n+1;
            }

        }catch(IOException e){

        }
        //System.out.println("Numerator "+numerator+ " Denominator "+denominator);
        return ((double) numerator)/((double)denominator);
    }

    public void getLengths(String MTFile, String RTFile){
        try{
            Scanner scanMT = new Scanner(new File(MTFile));
            Scanner scanRT =  new Scanner(new File(RTFile));
            while(scanRT.hasNext() && scanMT.hasNext()){
                String[] rt = scanRT.nextLine().toLowerCase().replaceAll("[^(a-z| )]","").split("\\s");
                String[] mt = scanMT.nextLine().toLowerCase().replaceAll("[^(a-z| )]","").split("\\s");

                r+=rt.length;
                c+=mt.length;
            }

        }catch(IOException e){

        }
    }

    public void cleanFile(String inFile, String outFile){
        try {

            Scanner engScan = new Scanner(new File(inFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFile)));

            while(engScan.hasNext()){
                String eng ="";
                char[] engChars = (engScan.nextLine()).toLowerCase().toCharArray();
                for(char c:engChars) {
                    if(Character.isLetter(c)|Character.isWhitespace(c)|c=='.'| c==','| c==';'|c ==':'|c== '!'|c=='?'| c=='\''| c== '"'|c=='('| c==')'|c=='-') {
                        eng =eng+c;
                    }else {
                        eng =eng+" ";
                    }
                }
                writer.write(eng.toLowerCase()+"\n");
            }
            writer.close();
        } catch (IOException e) {

        }




    }

}
