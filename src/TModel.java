import java.util.*;

public interface TModel {

    // Gets all likely translations of a foreign word
    public Map<String, Double> getTranslations(String frWord);
}
