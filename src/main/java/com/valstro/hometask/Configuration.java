package com.valstro.hometask;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration singleton that represents Spring context configuration. Configuration sits in 'app.properies' next to
 * jar.
 */
public class Configuration {

    private static Properties CFG = null;

    private Configuration() {}

    /**
     * Lazy initialization of application properties
     * @return application configuration properties
     */
    private static synchronized Properties getInstance() {
        if(CFG == null) {
            CFG = new Properties();
            try {
                CFG.load(new FileInputStream("app.properties"));
            } catch (IOException e) {
                System.out.println("WARNING: Application configuration file app.properties not found");
            }
        }
        return CFG;
    }

    /**
     * Get value from properties identified by the key
     * @param key points to a value in property file
     * @return value assigned to a key
     */
    private static String get(String key) {
        return getInstance().getProperty(key);
    }

    /**
     * Returns configuration parameter
     * @param key key pointing to the parameter
     * @param defVal default value if parameter is not defined
     * @return parameter value
     */
    public static String get(String key, String defVal) {
        String val = get(key);
        if(val == null || "".equals(val)) {
            return defVal;
        }
        return val;
    }

    /**
     * Returns configuration parameter
     * @param key key pointing to the parameter
     * @param defVal default value if parameter is not defined
     * @return parameter value
     */
    public static int get(String key, int defVal) {
        String val = get(key);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            // suppress
        }
        return defVal;
    }

    /**
     * Returns configuration parameter
     * @param key key pointing to the parameter
     * @param defVal default value if parameter is not defined
     * @return parameter value
     */
    public static boolean get(String key, boolean defVal) {
        String val = get(key);
        if(val == null || "".equals(val)) {
            return defVal;
        }
        return Boolean.parseBoolean(val);
    }

}
