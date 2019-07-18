package io.localmotion.storage.file;

import java.util.List;

public interface FileAccessor {
    public default String readFileToString(String location, String name) {
        return readFileToString(location, "", name);
    }
    public default String readFileToString(String location, String path, String name) {
        List<String> lines = readFile(location, path, name);
        StringBuilder sb = new StringBuilder();
        for (String i : lines)
            sb.append(i);
        return sb.toString();
    }

    public default boolean fileExists(String location, String name) {
        return fileExists(location, "", name);
    }
    public boolean fileExists(String location, String path, String name);

    public default List<String> readFile(String location, String name) {
        return readFile(location, "", name);
    }
    public List<String> readFile(String location, String path, String name);

    public default void writeFile(String location, String name, String content) {
        writeFile(location, "", name, content);
    }
    public void writeFile(String location, String path, String name, String content);

    public default void deleteFile(String location, String name) {
        deleteFile(location, "", name);
    }
    public void deleteFile(String location, String path, String name);
}
