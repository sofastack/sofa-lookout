package com.alipay.sofa.lookout.ark.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Add following configs that start with "&lt;appName&gt;." to Spring Context:
 * <ol>
 * <li>properties specified by system property "lookoutall.config-file"</li>
 * <li>System Properties</li>
 * <li>Process Environment</li>
 * </ol>
 * TODO loading properties supporting spring profiles
 *
 * @author xzchaoo
 * @date 2019/1/2
 */
public class SofaArkEmbedAppInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Set<String> APP_NAME_SET = ConcurrentHashMap.newKeySet();
    private final        String      appName;

    public SofaArkEmbedAppInitializer(String appName) {
        this.appName = appName;
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        if (!APP_NAME_SET.add(appName)) {
            throw new IllegalStateException("same appName " + appName + " can only be used once!");
        }
        ConfigurableEnvironment cenv = ctx.getEnvironment();
        MutablePropertySources mps = cenv.getPropertySources();

        MapPropertySource lookoutallSubView = getLookoutAllSubView();
        if (lookoutallSubView != null) {
            mps.addFirst(lookoutallSubView);
        }

        String prefix = appName + ".";
        MapPropertySource env = new MapPropertySource("sofaark-environment", EnvUtils.getEnvSubView(prefix));
        mps.addFirst(env);

        MapPropertySource sd = new MapPropertySource("sofaark-systemProperties", EnvUtils.getSystemPropertySubView(prefix));
        mps.addFirst(sd);
    }

    private MapPropertySource getLookoutAllSubView() {
        String lookoutAllConfigFile = System.getProperty("lookoutall.config-file");
        if (lookoutAllConfigFile != null) {
            Properties properties = new Properties();
            try ( FileInputStream fis = new FileInputStream(lookoutAllConfigFile) ) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("fail to load lookoutall config file " + lookoutAllConfigFile, e);
            }
            String prefix = appName + ".";
            Map<String, Object> subView = EnvUtils.getPropertySubView(prefix, properties);
            return new MapPropertySource("lookoutall-" + appName, subView);
        }
        return null;
    }
}
