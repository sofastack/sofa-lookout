/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.lookout.os.linux;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.os.CachedMetricsImporter;
import com.alipay.lookout.os.utils.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wuqin
 * @author kevin.luy@alipay.com
 * @version $Id: NetTrafficMetricsImporter.java, v 0.1 2017-03-18 下午6:33 wuqin Exp $$
 */
public class NetTrafficMetricsImporter extends CachedMetricsImporter {

    private final Logger          logger            = LookoutLoggerFactory
                                                        .getLogger(NetTrafficMetricsImporter.class);

    private static final String   DEFAULT_FILE_PATH = "/proc/net/dev";

    private static final String   SPLIT             = "\\s+";

    /**
     * See https://github.com/OpenTSDB/tcollector/blob/master/collectors/0/ifstat.py
     */
    private static final String   STR_PATTERN       = "\\s+"
                                                      + "("
                                                      + "eth\\d+|"
                                                      + "em\\d+_\\d+/\\d+|em\\d+_\\d+|em\\d+"
                                                      + "p\\d+p\\d+_\\d+/\\d+|p\\d+p\\d+_\\d+|p\\d+p\\d+|"
                                                      + "(?:"
                                                      // Start of 'predictable network interface names'
                                                      + "(?:en|sl|wl|ww)" + "(?:"
                                                      + "b\\d+|"
                                                      // # BCMA bus
                                                      + "c[0-9a-f]+|"
                                                      // # CCW bus group
                                                      + "o\\d+(?:d\\d+)?|"
                                                      // # On-board device
                                                      + "s\\d+(?:f\\d+)?(?:d\\d+)?|"
                                                      // # Hotplug slots
                                                      + "x[0-9a-f]+|"
                                                      // # Raw MAC address
                                                      + "p\\d+s\\d+(?:f\\d+)?(?:d\\d+)?|"
                                                      // # PCI geographic loc
                                                      + "p\\d+s\\d+(?:f\\d+)?(?:u\\d+)*(?:c\\d+)?(?:i\\d+)?"
                                                      // # USB
                                                      + ")" + ")" + "):(.*)";

    private static final Pattern  NET_PATTERN       = Pattern.compile(STR_PATTERN);

    private static final String[] FIELDS            = { "net.in.bytes", "net.in.packets",
            "net.in.errs", "net.in.dropped", "net.in.fifo.errs", "net.in.frame.errs",
            "net.in.compressed", "net.in.multicast", "net.out.bytes", "net.out.packets",
            "net.out.errs", "net.out.dropped", "net.out.fifo.errs", "net.out.collisions",
            "net.out.carrier.errs", "net.out.compressed" };

    private String                filePath;
    private Map<String, Long[]>   statByFace;

    public NetTrafficMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    public NetTrafficMetricsImporter(String filePath, long timeout, TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        this.statByFace = new HashMap<String, Long[]>();

        disable = !new File(filePath).exists();
        if (!disable)
            loadIfNessesary();
    }

    @Override
    protected void doRegister(Registry registry) {
        for (final Map.Entry<String, Long[]> entry : statByFace.entrySet()) {
            final String face = entry.getKey();
            Id id = registry.createId("os.net.stat." + face);
            MixinMetric mixin = registry.mixinMetric(id);

            for (int i = 0; i < entry.getValue().length; i++) {
                final int index = i;
                mixin.gauge(FIELDS[index], new Gauge<Long>() {
                    @Override
                    public Long value() {
                        loadIfNessesary();
                        return statByFace.get(face)[index];
                    }
                });
            }
        }
    }

    @Override
    protected void loadValues() {
        try {
            List<String> lines = FileUtils.readFileAsStringArray(filePath);
            for (String line : lines) {
                Matcher netMatcher = NET_PATTERN.matcher(line);
                if (netMatcher.matches()) {
                    String face = netMatcher.group(1);
                    if (statByFace.get(face) == null) {
                        statByFace.put(face, new Long[FIELDS.length]);
                    }

                    String[] stats = netMatcher.group(2).trim().split(SPLIT);
                    for (int i = 0; i < stats.length; i++) {
                        statByFace.get(face)[i] = Long.parseLong(stats[i]);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("warning,can't parse text at /proc/net/dev", e.getMessage());
        }
    }
}
