package you.you.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import you.you.model.MenuItem;

public class RuleManager {

    private static final String RULES_FILE = "rules.json";
    private static final String ASSETS_DEFAULT = "default_rules.json";

    private static RuleManager instance;
    private final Context context;
    private final Gson gson;
    private RulesData cachedData;

    private static class RulesData {
        List<MenuItem> menuItems;
        List<String> fallbackOptions;
    }

    private RuleManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    public void invalidateCache() {
        cachedData = null;
    }

    public static synchronized RuleManager getInstance(Context context) {
        if (instance == null) {
            instance = new RuleManager(context);
        }
        return instance;
    }

    private RulesData loadData() {
        if (cachedData != null) {
            return cachedData;
        }

        File file = new File(context.getFilesDir(), RULES_FILE);

        if (!file.exists()) {
            copyDefaultRules();
        }

        try {
            InputStream is = context.openFileInput(RULES_FILE);
            InputStreamReader reader = new InputStreamReader(is);
            Type type = new TypeToken<RulesData>() {}.getType();
            cachedData = gson.fromJson(reader, type);
            reader.close();
            return cachedData;
        } catch (IOException e) {
            e.printStackTrace();
            return getDefaultRules();
        }
    }

    private void saveData(RulesData data) {
        cachedData = data;
        try {
            FileOutputStream fos = context.openFileOutput(RULES_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            gson.toJson(data, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDefaultRules() {
        try {
            InputStream is = context.getAssets().open(ASSETS_DEFAULT);
            FileOutputStream fos = context.openFileOutput(RULES_FILE, Context.MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RulesData getDefaultRules() {
        try {
            InputStream is = context.getAssets().open(ASSETS_DEFAULT);
            InputStreamReader reader = new InputStreamReader(is);
            Type type = new TypeToken<RulesData>() {}.getType();
            RulesData data = gson.fromJson(reader, type);
            reader.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            RulesData data = new RulesData();
            data.menuItems = new ArrayList<>();
            data.fallbackOptions = new ArrayList<>();
            return data;
        }
    }

    public List<MenuItem> getAllRules() {
        return new ArrayList<>(loadData().menuItems);
    }

    public void addRule(MenuItem item) {
        RulesData data = loadData();
        data.menuItems.add(item);
        saveData(data);
    }

    public void updateRule(int position, MenuItem item) {
        RulesData data = loadData();
        data.menuItems.set(position, item);
        saveData(data);
    }

    public void deleteRule(int position) {
        RulesData data = loadData();
        data.menuItems.remove(position);
        saveData(data);
    }

    public List<String> getFallbackOptions() {
        return new ArrayList<>(loadData().fallbackOptions);
    }

    public void saveFallbackOptions(List<String> options) {
        RulesData data = loadData();
        data.fallbackOptions = new ArrayList<>(options);
        saveData(data);
    }

    public void resetToDefault() {
        cachedData = null;
        copyDefaultRules();
    }

    public String exportRules() {
        return gson.toJson(loadData());
    }
}