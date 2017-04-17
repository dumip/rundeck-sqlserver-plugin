package ro.dpa.rundeck.plugins.params;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to parse config params received from the executeStep method invoked
 * by Rundeck during job execution
 *
 * Created by dumitru.pascu on 3/26/2017.
 */
public class ParamUtils {

    private static final String MULTIPLE_PARAMS_SEPARATOR = "\n";
    private static final String EQUAL_SEPARATOR = "=";

    /**
     * Returns a single string value from the config Map provided by the Rundeck executeStep method.
     * @param key
     * @param options
     * @return
     */
    public static String getStringValue(String key, Map<String, Object> options) {
        return options.containsKey(key) ? options.get(key).toString() : null;
    }

    /**
     * Returns a single int value from the config Map provided by the Rundeck executeStep method.
     * @param key
     * @param options
     * @return
     */
    public static int getIntValue(String key, Map<String, Object> options) {
        return getIntValue(key, options, 0);
    }

    /**
     * Returns a single int value from the config Map provided by the Rundeck executeStep method.
     *
     * @param key
     * @param options
     * @param defaultValue
     * @return
     */
    public static int getIntValue(String key, Map<String, Object> options, int defaultValue) {
        String val = ParamUtils.getStringValue(key, options);
        if (val == null) {
            return defaultValue;
        }

        return Integer.parseInt(val);
    }


    /**
     * Returns a Map of parameters received from the executeStep method. The params should be provided in the following
     * format: param1=value1<b>\n</b>param2=value2...
     *
     * @param key The key from the options map
     * @param options The options map (all config params provided by Rundeck)
     * @return
     */
    public static Map<String, String> getMapValues(String key, Map<String, Object> options) {
        Map<String, String> params = new HashMap<>();

        String val = ParamUtils.getStringValue(key, options);

        if (val == null) {
            return params;
        }

        String[] pairs = val.split(MULTIPLE_PARAMS_SEPARATOR);

        for (String pair : pairs) {
            String[] nameAndValue = pair.split(EQUAL_SEPARATOR);
            if (nameAndValue.length != 2) {
                throw new IllegalArgumentException("Incorrect format for pair = " + pair + " from the " +
                    key + " parameter");
            }

            params.put(nameAndValue[0], nameAndValue[1]);
        }

        return params;
    }
}
