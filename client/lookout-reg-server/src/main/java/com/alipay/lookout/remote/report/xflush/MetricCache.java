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
package com.alipay.lookout.remote.report.xflush;

import com.alipay.lookout.api.Clock;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/12
 */
public class MetricCache {
    private final long  rate;
    private Slot[]      slots;
    private final Clock clock;

    @SuppressWarnings("unchecked")
    public MetricCache(Clock clock, long rate, int slotCount) {
        Preconditions.checkArgument(rate > 0, "rate must greater than 0");
        Preconditions.checkArgument(slotCount > 0, "slotCount must greater than 0");

        this.clock = clock;
        this.rate = rate;
        this.slots = new Slot[slotCount];
        for (int i = 0; i < slotCount; ++i) {
            this.slots[i] = new Slot();
        }
    }

    /**
     * 如果存在原始的cache, 那么尽量保留原始origin里的数据
     *
     * @param origin
     * @param rate
     * @param slotCount
     */
    public MetricCache(MetricCache origin, long rate, int slotCount) {
        Preconditions.checkNotNull(origin);
        Preconditions.checkArgument(rate > 0, "rate must greater than 0");
        Preconditions.checkArgument(slotCount > 0, "slotCount must greater than 0");

        this.clock = origin.clock;
        this.rate = rate;
        this.slots = new Slot[slotCount];

        // 尽量保存原有的正在使用的slot, 这样可以减少数据丢失
        int i = 0;
        for (Slot slot : origin.slots) {
            if (slot.getCursor() > 0) {
                this.slots[i++] = slot;
                if (i >= slotCount) {
                    break;
                }
            }
        }
        for (int j = i; j < slotCount; ++j) {
            this.slots[j] = new Slot();
        }
    }

    public List<Slot> getNextData(Set<Long> successCursors) {
        List<Slot> result = new ArrayList<Slot>();
        synchronized (this) {
            for (Slot slot : slots) {
                if (slot.getCursor() > 0) {
                    if (successCursors != null && successCursors.contains(slot.getCursor())) {
                        // 清理掉已经完成的slot
                        slot.clear();
                    } else {
                        // 其余slot加入结果
                        result.add(slot);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 找出一个可用的位置, 第一个available=true 或者 所有slot里, cursor最小的那个
     *
     * @return
     */
    private int findAvailableSlot() {
        int oldestSlotIndex = -1;
        for (int i = 0; i < slots.length; i++) {
            Slot slot = slots[i];
            if (slot.getCursor() < 0) {
                return i;
            } else if (oldestSlotIndex == -1
                       || slot.getCursor() < slots[oldestSlotIndex].getCursor()) {
                oldestSlotIndex = i;
            }
        }
        return oldestSlotIndex;
    }

    public synchronized void clear() {
        for (Slot slot : slots) {
            slot.clear();
        }
    }

    public synchronized void add(List<MetricDto> data) {
        long cursor = getCurrentCursor();
        int index = findAvailableSlot();
        slots[index] = new Slot(cursor, data);
    }

    private long getCurrentCursor() {
        // 将当前时间对齐的结果作为cursor
        return clock.wallTime() / rate * rate;
    }
}
