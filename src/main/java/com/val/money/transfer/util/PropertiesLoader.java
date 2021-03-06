package com.val.money.transfer.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    /**
     * Load properties by file name
     * @param fileName load properties by file name
     * @return loaded properties from file
     *
     * @throws IOException exception during file reading
     */
    public static Properties load(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = PropertiesLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if(stream == null) {
                throw new FileNotFoundException("Specified properties file can't be found: " + fileName);
            }
            properties.load(stream);
        }
        return properties;
    }
}
