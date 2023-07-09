package net.vyhub.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Cache<T> {
    private final String filename;
    private final Type type;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Cache(String name, Type type) {
        this.filename = String.format("plugins/VyHub/%s.json", name);
        this.type = type;
    }

    private String toJson(T data) {
        return gson.toJson(data);
    }

    private String readFile() {
        try {
            byte[] encodedBytes = Files.readAllBytes(Paths.get(filename));
            return new String(encodedBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public void save(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        File f = new File(filename);

        if (!f.exists()) {
            File parent = f.getParentFile();
            parent.mkdirs();
        }

        try (FileWriter file = new FileWriter(f)) {
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
