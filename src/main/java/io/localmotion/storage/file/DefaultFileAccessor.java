package io.localmotion.storage.file;

import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Requires(notEnv="aws")
public class DefaultFileAccessor implements FileAccessor {

    @Override
    public boolean fileExists(String location, String path, String name) {
        return getFile(location, path, name).exists();
    }

    public List<String> readFile(String location, String path, String name) {
        List<String> result = new ArrayList<>();

        FileInputStream inputStream = null;
        try {
            try {
                inputStream = new FileInputStream(getFile(location, path, name));
                result = readLinesFromStream(inputStream);

            } finally {
                // To ensure that the network connection doesn't remain open, close any open input streams.
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch(IOException e) {
             e.printStackTrace();
        }

         return result;
    }


    private List<String> readLinesFromStream(InputStream input) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }
        return result;
    }

    public void writeFile(String location, String path, String name, String content) {
        FileWriter writer = null;
        try {
            try {
                writer = new FileWriter(getFile(location, path, name));
                writer.write(content);

            } finally {
                // To ensure that the network connection doesn't remain open, close any open input streams.
                if (writer!= null) {
                    writer.close();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String location, String path, String name) {
        getFile(location, path, name).delete();
    }

    private File getFile(String location, String path, String name) {
        File dir = path != null && !path.equals("") ? new File(location, path) : new File(location);
        return new File(dir, name);
    }
}

