package net.vyhub.VyHubMinecraft.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


public class Cache<T> {
    private String filename;
    private Type  type;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Cache(String name, Type type) {
        this.filename = String.format("plugins/VyHub/%s.json", name);
        this.type = type;
    }

    private String toJson(T data) {
        return gson.toJson(data);
    }

    private String readFile() {
        try {
            return Files.readString(Paths.get(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public void save(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        try (FileWriter file = new FileWriter(filename)) {
            file.write(toJson(data));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public T load() {
        String data = readFile();

        if (data == null) {
            return null;
        }

        return gson.fromJson(data, type);
    }
}
