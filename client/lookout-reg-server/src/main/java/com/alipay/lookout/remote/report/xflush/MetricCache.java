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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/12
 */
public class MetricCache {
    private final long bucketIntervalMills;
    private Slot[] slots;

    @SuppressWarnings("unchecked")
    public MetricCache(long slotIntervalMills, int slotCount) {
        if (slotIntervalMills <= 0 || slotCount <= 0) {
            throw new IllegalArgumentException();
        }
        // TODO check params, bucketIntervalMills 有一个最大值和最小值的限制
        this.bucketIntervalMills = slotIntervalMills;
        this.slots = new Slot[slotCount];
        for (int i = 0; i < slotCount; ++i) {
            this.slots[i] = new Slot();
        }
    }

    /**
     * 如果存在原始的cache, 那么尽量保留原始origin里的数据
     *
     * @param origin
     * @param slotIntervalMills
     * @param slotCount
     */
    public MetricCache(MetricCache origin, long slotIntervalMills, int slotCount) {
        if (slotIntervalMills <= 0 || slotCount <= 0) {
            throw new IllegalArgumentException();
        }
        this.bucketIntervalMills = slotIntervalMills;
        this.slots = new Slot[slotCount];

        // 尽量保存原有的正在使用的slot, 这样可以减少数据丢失
        int i = 0;
        for (Slot slot : origin.slots) {
            if (slot.using) {
                this.slots[i++] = slot;
                if (i == slotCount) {
                    break;
                }
            }
        }
        for (int j = i; j < slotCount; ++j) {
            this.slots[j] = new Slot();
        }
    }

    public synchronized List<SlotItem> getNextData(Set<Long> successCursors) {
        List<SlotItem> result = new ArrayList<SlotItem>();

        // 找到最后一个成功的cursor
        long maxSuccessCursor = 0;
        if (!successCursors.isEmpty()) {
            for (long x : successCursors) {
                maxSuccessCursor = Math.max(maxSuccessCursor, x);
            }
        }

        for (Slot slot : slots) {
            if (slot.using) {
                if (successCursors.contains(slot.cursor)) {
                    slot.clear();
                } else {
                    result.add(slot.toFoo());
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
    private Slot findAvailableSlot() {
        Slot oldestSlot = null;
        for (Slot slot : slots) {
            if (!slot.using) {
                return slot;
            } else if (oldestSlot == null || slot.cursor < oldestSlot.cursor) {
                oldestSlot = slot;
            }
        }
        return oldestSlot;
    }

    public synchronized void clear() {
        for (Slot slot : slots) {
            slot.clear();
        }
    }

    public synchronized void add(List<MetricDto> data) {
        long cursor = getCurrentCursor();
        Slot slot = findAvailableSlot();
        if (slot.using) {
            // force replace! a warn here
        } else {
            slot.using = true;
        }
        slot.cursor = cursor;
        slot.data = data;
        this.notifyAll();
    }

    public final long getCurrentCursor() {
        // 将当前时间对齐的结果作为cursor
        return System.currentTimeMillis() / bucketIntervalMills * bucketIntervalMills;
    }

    private static class Slot {
        long cursor = -1;
        List<MetricDto> data = null;
        boolean using = false;

        SlotItem toFoo() {
            return new SlotItem(cursor, data);
        }

        void clear() {
            cursor = -1;
            data = null;
            using = false;
        }
    }
}
