package you.you.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SynonymManager {

    private static SynonymManager instance;
    private final Map<String, Set<String>> synonymMap;

    private SynonymManager(Context context) {
        synonymMap = new HashMap<>();
        loadSynonyms(context);
    }

    public static synchronized SynonymManager getInstance(Context context) {
        if (instance == null) {
            instance = new SynonymManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadSynonyms(Context context) {
        try {
            InputStream is = context.getAssets().open("synonyms.json");
            InputStreamReader reader = new InputStreamReader(is);
            Type type = new TypeToken<Map<String, String[]>>() {}.getType();
            Map<String, String[]> rawMap = new Gson().fromJson(reader, type);
            reader.close();

            for (Map.Entry<String, String[]> entry : rawMap.entrySet()) {
                Set<String> set = new HashSet<>();
                for (String s : entry.getValue()) {
                    set.add(s.toLowerCase());
                }
                synonymMap.put(entry.getKey().toLowerCase(), set);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> expand(String input) {
        String lowerInput = input.toLowerCase();
        Set<String> result = new HashSet<>();
        result.add(lowerInput);

        for (Map.Entry<String, Set<String>> entry : synonymMap.entrySet()) {
            String tag = entry.getKey();
            Set<String> synonyms = entry.getValue();
            if (lowerInput.contains(tag)) {
                result.add(tag);
                result.addAll(synonyms);
            }
            for (String syn : synonyms) {
                if (lowerInput.contains(syn)) {
                    result.add(tag);
                    result.addAll(synonyms);
                    break;
                }
            }
        }

        return result;
    }
}