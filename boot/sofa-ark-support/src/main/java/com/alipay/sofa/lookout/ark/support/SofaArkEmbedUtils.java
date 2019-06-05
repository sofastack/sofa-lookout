package com.alipay.sofa.lookout.ark.support;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author xiangfeng.xzc
 * @date 2019/1/2
 */
public final class SofaArkEmbedUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SofaArkEmbedUtils.class);

    private static final String SOFA_ARK_CONFIGS = "sofaark.configs";

    private SofaArkEmbedUtils() {}

    private static Set<String> splitToSet(String str) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : str.split(",")) {
            set.add(s.trim());
        }
        return set;
    }

    /**
     * check whether specific app is enabled
     *
     * @param appName
     * @return
     */
    public static boolean isAppEnabled(String appName) {
        String whiteList = getProperty("sofaark.embed.apps.whitelist");
        if (!StringUtils.isEmpty(whiteList)) {
            return splitToSet(whiteList).contains(appName);
        }

        String blacklist = System.getProperty("sofaark.embed.apps.blacklist");
        if (!StringUtils.isEmpty(blacklist)) {
            return !splitToSet(blacklist).contains(appName);
        }
        return true;
    }

    private static String getProperty(String key) {
        return getProperty(key, null);
    }

    private static String getProperty(String key, String defaultValue) {
        String property = System.getProperty(key, System.getenv(key));
        return property != null ? property : defaultValue;
    }

    /**
     * enhance spring application builder with the ability to extract app related configs:
     * <ol>
     * <li>System Properties that start with "&lt;appName&gt;."</li>
     * <li>Environments that start with "&lt;appName&gt;."</li>
     * <li>Config Files Location which is specified by a system property "SOFA_ARK_CONFIGS" </li>
     * </ol>
     *
     * @param appName current app name
     * @param builder spring application builder
     */
    public static void enhance(String appName, SpringApplicationBuilder builder) {
        Properties properties = parseSofaArkSupportProperties(appName);
        LOGGER.info("{} use properties {}", appName, properties);
        builder.properties(properties);
        builder.initializers(new SofaArkEmbedAppInitializer(appName));
    }

    public static Properties parseSofaArkSupportProperties(String appName) {
        Properties properties = new Properties();
        String configsStr = System.getProperty(SOFA_ARK_CONFIGS);
        if (configsStr != null) {
            JSONObject configs = JSONObject.parseObject(configsStr);
            String configDirStr = configs.getString("configDir");
            // set CONFIG_ADDITIONAL_LOCATION_PROPERTY to ${configDir}/${appName}
            if (configDirStr != null) {
                Path configDir = Paths.get(configDirStr);
                String appConfigDir = configDir.resolve(appName).toAbsolutePath().toUri().toString();
                properties.setProperty(ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY, appConfigDir);
            }
        }
        String property = getProperty(appName + ".config-additional-location");
        if (property != null) {
            String oldValue = properties.getProperty(ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY);
            String newValue = oldValue != null ? (oldValue + "," + property) : property;
            properties.setProperty(ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY, newValue);
        }
        return properties;
    }

    public static boolean isSofaArkStarted() {
        return BootUtils.isSofaArkStarted();
    }
}
