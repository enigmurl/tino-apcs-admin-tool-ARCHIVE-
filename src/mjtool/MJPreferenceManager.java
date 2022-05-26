package mjtool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class MJPreferenceManager {

    public static final String ASSIGNMENT_KEY = "assignment_name";
    public static final String EXPORT_KEY = "export_path";
    public static final String ROSTER_KEY = "roster_name";


    private static final String FILE = "mj_pref.json";

    private final JsonObject json;

    public MJPreferenceManager() {
        File f = new File(FILE);

        if(f.exists() && !f.isDirectory()) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8);

                StringBuilder str = new StringBuilder();
                while (reader.ready()) {
                    str.append((char) reader.read());
                }
                reader.close();

                json = new JsonParser().parse(str.toString()).getAsJsonObject();
            } catch (IOException | IllegalStateException e) {
                throw new RuntimeException("Preferences unavailable");
            }
        } else {
            json = new JsonObject();
        }

    }


    public void setKey(String key, String value) {
        json.remove(key);
        json.addProperty(key, value);

        try {
            File out = new File(FILE);
            FileWriter fw = new FileWriter(out);
            fw.append(json.toString());
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Preferences unavailable");
        }
    }

    public String getKey(String key) {
        JsonElement j = json.get(key);
        if (j == null) {
            return null;
        }
        return j.getAsString();
    }

    public String getOrDefault(String key, String def) {
        String ret = json.get(key).getAsString();
        if (ret == null) ret = def;
        return ret;
    }
}
