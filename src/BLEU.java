import java.util.*;
import java.io.*;

public class BLEU {
    private int r = 0;
    private int c = 0;
    private static final int N =4;
    public BLEU(){

    }

    public static void main(String args[]){
        System.out.println("Hello world");
        BLEU b =  new BLEU();
        System.out.println(b.runBLEU("mtTest1.txt","rtTest1.txt"));

    }
    public double runBLEU(String MTFile, String RTFile){
        double sum=0;
        for(int n =1; n<=N;n++){
            double pn = this.getPn(MTFile,RTFile,n);
            if(pn >0){
                sum+= Math.log(pn/((double)N));
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

                r+=rt.length;
                c+=mt.length;
            }

        }catch(IOException e){

        }
        //System.out.println("Numerator "+numerator+ " Denominator "+denominator);
        return ((double) numerator)/((double)denominator);
    }

}
