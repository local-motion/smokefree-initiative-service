package io.localmotion.storage.file;

import java.util.List;

public interface FileAccessor {
    public default String readFileToString(String location, String name) {
        List<String> lines = readFile(location, name);
        StringBuilder sb = new StringBuilder();
        for (String i : lines)
            sb.append(i);
        return sb.toString();
    }

    public boolean fileExists(String location, String name);
    public List<String> readFile(String location, String name);
    public void writeFile(String location, String name, String content);
    public void deleteFile(String location, String name);
}
