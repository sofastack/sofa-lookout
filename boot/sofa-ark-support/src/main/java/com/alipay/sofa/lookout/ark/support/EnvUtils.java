package com.alipay.sofa.lookout.ark.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author xzchaoo
 * @date 2018/12/10
 */
final class EnvUtils {
    private EnvUtils() {
    }

    /**
     * get a sub view that started with &lt;prefix&gt; from Environments
     *
     * @param prefix
     * @return
     */
    public static Map<String, Object> getEnvSubView(String prefix) {
        if (prefix == null) {
            return Collections.emptyMap();
        }
        Map<String, String> env = System.getenv();
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> e : env.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                map.put(e.getKey().substring(prefix.length()), e.getValue());
            }
        }
        return map;
    }

    /**
     * get a sub view that started with &lt;prefix&gt; from System Properties
     *
     * @param prefix
     * @return
     */
    public static Map<String, Object> getSystemPropertySubView(String prefix) {
        if (prefix == null) {
            return Collections.emptyMap();
        }
        return getPropertySubView(prefix, System.getProperties());
    }

    public static Map<String, Object> getPropertySubView(String prefix, Properties properties) {
        if (prefix == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = e.getKey().toString();
            if (key.startsWith(prefix)) {
                map.put(key.substring(prefix.length()), e.getValue().toString());
            }
        }
        return map;
    }
}
