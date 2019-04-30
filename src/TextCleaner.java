import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextCleaner {

    private static final String NUM_TOKEN = "%NUM%";
    private static final int MIN_LINE_SIZE_TO_CONSIDER = 50, MIN_LINE_SIZE_TO_PRINT = 10;

    /**
     * Cleans text. Removes all words that aren't just letters or punctuation. Replaces numbers
     * with NUM_TOKEN. Handles wrapped sentences so there is one sentence per line.
     * @param inputFile file to clean
     * @param outputFile file to write output
     * @throws IOException
     */
    public static void clean(String inputFile, String outputFile) throws IOException {
        Scanner sc = new Scanner(new File(inputFile));

        FileWriter writer = new FileWriter(new File(outputFile));

        int totalLines = 0, emptyLines = 0, totalSents = 0;
        long totalChars = 0;

        // for sentences that wrap to the next line
        String wrappedSent = null;

        while(sc.hasNextLine()) {
            totalLines++;
            String line = sc.nextLine();

            // skip empty lines
            if(line.isEmpty() || line.startsWith("-") || line.length() < MIN_LINE_SIZE_TO_CONSIDER) {
                emptyLines++;
                continue;
            }

            // remove everything in parentheses
            line = line.replaceAll("\\(.*\\)", " ");

            // remove all quotation marks
            line = line.replaceAll("\"", " ");

            // put space before/after each punctuation
            line = line.replaceAll("([.,!?;:])", " $1 ");

            // remove extra spaces
            line = line.replaceAll("\\s+", " ");


            // word level cleanup
            String[] words = line.split(" ");
            List<String> lst = new ArrayList<>();

            String punctuation = ".,!?;:'";
            for(String w : words) {
                char[] cs = w.toCharArray();
                boolean invalid = false, number = false;

                // check if word contains any non-letter or non-punctuation
                for(char c : cs) {
                    totalChars++;
                    if(!Character.isLetter(c) && !punctuation.contains("" + c)) {
                        invalid = true;
                        if(Character.isDigit(c)) {
                            number = true;
                        }
                    }
                }

                if(number) {
                    lst.add(NUM_TOKEN);
                } else if(!invalid) {
                    lst.add(w.toLowerCase());
                }
            }

            line = lst.stream().reduce("", (a,b) -> a + " " + b);

            // there is more than one sentence per line and sometimes they wrap
            // make it so that there is only one sentence per line and make wrapped sentences a single line
            line = line.trim();

            Pattern p = Pattern.compile("[.?!]");
            Matcher m = p.matcher(line);

            // extract the left-most sentence
            while(m.find()) {
                int pos = m.start();
//                System.out.println("POS = " + pos);
                String sent = line.substring(0, pos + 1);

                // if a sentence wrapped from the last line add it to the start of this sentence
                if(wrappedSent != null) {
                    sent = wrappedSent + " " +  sent;
                    wrappedSent = null;
                }

                sent = sent.trim();
                sent = sent.replaceAll("\\s+", " "); // remove extra spaces again

                // write the sentence to file
                if(sent.length() > MIN_LINE_SIZE_TO_PRINT) {
                    totalSents++;
                    writer.write(sent + "\n");
                }

                line = line.substring(pos + 1, line.length());
                m = p.matcher(line);
            }

            if(!line.isEmpty()) {
                wrappedSent = line;
            }
        }

        writer.close();

        System.out.println("Empty lines = " + emptyLines);
        System.out.println("Total lines = " + totalLines);
        System.out.println("Total sents = " + totalSents);
        System.out.println("Sents per line = " + ((double) totalSents)/totalLines);
        System.out.println("Total chars = " + totalChars);
    }
}
