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
package com.alipay.sofa.lookout.gateway.core.queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 *
 * @author zhangzhuo
 * @version $Id: Writable.java, v 0.1 2018年10月23日 下午2:43 zhangzhuo Exp $
 */
public class MappedFilePersistentQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappedFilePersistentQueue.class);

    private static final int DEFAULT_PAGE_SIZE = 1024 * 1024 * 16;

    private static final float DEFAULT_MAX_QUEUE_SIZE_PERCENT = 0.8f;

    private int pageSize;

    public static final int MSG_SIZE_LIMIT = 4 * 1024 * 1024;

    private static final long DEFAULT_MAX_QUEUE_TIME = 24 * 3600 * 1000;

    private long maxQueueTime = DEFAULT_MAX_QUEUE_TIME;

    private RandomAccessFile readDataFile; //random access file for data

    private RandomAccessFile writeDataFile; //random access file for data

    private FileChannel readDataChannel; // channel for read data

    private FileChannel writeDataChannel; // channel for write data

    private MappedByteBuffer readMbb; // buffer used to read;

    private MappedByteBuffer writeMbb; // buffer used to write

    private File currentReadingFile;

    private File currentWritingFile;

    private final int headerLength = 5;

    private final byte READ = (byte) 0;

    private final byte EOF = (byte) 2;

    private final byte WRITTEN = (byte) 3;

    // 1 byte for status of the message, 4 bytes length of the payload
    private final ByteBuffer header = ByteBuffer.allocate(headerLength);

    private final int endingLength = 5;

    private long readFileIndex = 0; // read index file

    private long writeFileIndex = 0; // write index file

    private long maxFileCount;

    private static final String DATA_FILE_SUFFIX = ".dat";

    private String fileNamePrefix; // persistence file name

    private File queueDir; // queue directory

    private long size;

    private boolean shutdown = false;

    public synchronized long getSize() {
        return size;
    }

    public MappedFilePersistentQueue(String dir, String qName) throws IOException {
        this(dir, qName, DEFAULT_PAGE_SIZE);
    }

    /**
     * A memory mapped persistence queue.
     *
     * @param dir,     directory for queue data
     * @param qName,   the name of the persistent queue
     * @param pageSize page size in bytes
     * @throws IOException
     */
    public MappedFilePersistentQueue(String dir, String qName, int pageSize) throws IOException {
        if (StringUtils.isBlank(dir)) {
            throw new IllegalArgumentException("dir is empty");
        }
        if (StringUtils.isBlank(qName)) {
            throw new IllegalArgumentException("name is empty");
        }
        String qDir = dir;
        if (!qDir.endsWith("/")) {
            qDir += File.separator;
        }
        qDir += qName;
        queueDir = new File(qDir);
        if (!queueDir.exists()) {
            queueDir.mkdirs();
        }
        this.fileNamePrefix = queueDir.getPath();
        if (!this.fileNamePrefix.endsWith("/")) {
            this.fileNamePrefix += File.separator;
        }
        this.fileNamePrefix += "data";
        this.pageSize = pageSize;
        long space = queueDir.getTotalSpace();
        maxFileCount = (long) (space * DEFAULT_MAX_QUEUE_SIZE_PERCENT) / pageSize;
        LOGGER.info("create queue with pageSize:{},maxFileCount:{}", pageSize, maxFileCount);
        this.init();
    }

    public void setMaxFileCount(long maxFileCount) {
        this.maxFileCount = maxFileCount;
    }

    private void init() throws IOException {
        initFiles();

        this.initReadBuffer();
        this.initWriteBuffer();

        new Thread(new FileCleanTask()).start();
    }

    private TreeMap<Long, File> files = new TreeMap<Long, File>();

    private BlockingQueue<File> toDeleteFiles = new LinkedBlockingQueue<>();

    private void initFiles() {
        File[] queueFiles = queueDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(DATA_FILE_SUFFIX);
            }
        });
        if (queueFiles != null) {
            for (File queueFile : queueFiles) {
                if (!deleteOldFile(queueFile)) {
                    String fileName = queueFile.getName();
                    int beginIndex = fileName.lastIndexOf('-') + 1;
                    int endIndex = fileName.lastIndexOf(DATA_FILE_SUFFIX);
                    String sIndex = fileName.substring(beginIndex, endIndex);
                    long index = Long.parseLong(sIndex);
                    files.put(index, queueFile);
                }
            }
        }
        //  write to latest file.
        if (!files.isEmpty()) {
            size = (files.size() - 1) * pageSize;
            writeFileIndex = files.lastKey();
            files.remove(writeFileIndex);
        }
        // read second latest file.
        if (!files.isEmpty()) {
            readFileIndex = files.lastKey();
            files.remove(readFileIndex);
        } else {
            readFileIndex = writeFileIndex;
        }
        LOGGER.info("==InitFiles== Writer index: {}, read index: {}", writeFileIndex, readFileIndex);
    }

    private boolean deleteOldFile(File file) {
        try {
            if (System.currentTimeMillis() - file.lastModified() > maxQueueTime) {
                file.delete();
                return true;
            }
        } catch (Throwable t) {
            LOGGER.error("delete file error", t);
        }
        return false;
    }

    //clean files before timestamp
    public synchronized void clean(long timestamp) {
        Iterator<Map.Entry<Long, File>> iterator = files.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, File> entry = iterator.next();
            File file = entry.getValue();
            if (file.lastModified() < timestamp) {
                iterator.remove();
                toDeleteFiles.add(file);
            }
        }
    }

    private void initReadBuffer() throws IOException {
        currentReadingFile = new File(this.fileNamePrefix + "-" + readFileIndex + DATA_FILE_SUFFIX);
        readDataFile = new RandomAccessFile(currentReadingFile, "rw");
        readDataChannel = readDataFile.getChannel();
        readMbb = readDataChannel.map(READ_WRITE, 0, pageSize); // create the read buffer with readPosition 0 initially
        int position = readMbb.position();
        byte state = readMbb.get(); // first byte to see whether the message is already read or not
        int length = readMbb.getInt(); // next four bytes to see the data length

        while (state == READ && length > 0) { // message is non active means, its read, so skipping it
            size -= headerLength + length;
            readMbb.position(position + headerLength + length); // skipping the read bytes
            position = readMbb.position();
            state = readMbb.get();
            length = readMbb.getInt();
        }
        readMbb.position(position);
    }

    private void initWriteBuffer() throws IOException {
        currentWritingFile = new File(this.fileNamePrefix + "-" + writeFileIndex + DATA_FILE_SUFFIX);
        writeDataFile = new RandomAccessFile(currentWritingFile, "rw");
        writeDataChannel = writeDataFile.getChannel();

        // start the write buffer with writePosition 0 initially
        writeMbb = writeDataChannel.map(READ_WRITE, 0, pageSize);
        int position = writeMbb.position();
        byte active = writeMbb.get();
        int length = writeMbb.getInt();
        while (length > 0) { // message is there, so skip it, keep doing until u get the end
            if (active == WRITTEN || active == READ) {
                size += headerLength + length;
            }
            writeMbb.position(position + headerLength + length);
            position = writeMbb.position();
            active = writeMbb.get();
            length = writeMbb.getInt();
        }
        writeMbb.position(position);
    }

    public boolean produce(byte[] bytes) {
        return produce(bytes, 0, bytes.length);
    }

    public synchronized boolean produce(byte[] bytes, int offset, int length) {
        try {
            if (shutdown) {
                LOGGER.error("Produce fail! Queue is already shutdown");
                return false;
            }

            if (length == 0) {
                LOGGER.error("Issue in dumping the object with zero byte into persistent queue");
                return false;
            }
            if (length > MSG_SIZE_LIMIT) {
                LOGGER.error("Issue in dumping the object into persistent queue, object size " + length +
                        " exceeds limit " + MSG_SIZE_LIMIT );
                return false;
            }
            //prepare the header
            prepareHeader(length);

            // check weather current buffer is enuf, otherwise we need to change the buffer
            if (writeMbb.remaining() < headerLength + length + endingLength) {
                writeMbb.put(EOF); // the end
                //                writeMbb.force();
                unMap(writeMbb);
                closeResource(writeDataChannel);
                closeResource(writeDataFile);
                if (writeFileIndex != readFileIndex) {
                    files.put(writeFileIndex, currentWritingFile);
                    if (files.size() + 2 > maxFileCount) {
                        removeOldestFile();
                    }
                }
                writeFileIndex++;
                currentWritingFile = new File(this.fileNamePrefix + "-" + writeFileIndex + DATA_FILE_SUFFIX);
                writeDataFile = new RandomAccessFile(currentWritingFile, "rw");
                writeDataChannel = writeDataFile.getChannel();

                // start the write buffer with writePosition 0 initially
                writeMbb = writeDataChannel.map(READ_WRITE, 0, pageSize);
            }
            writeMbb.put(header); // write header
            writeMbb.put(bytes, offset, length);
            size += headerLength + length;
            return true;
        } catch (Throwable e) {
            LOGGER.error("Issue in dumping the object into persistent ", e);
            return false;
        }
    }

    public synchronized boolean produce(Writable writable) {
        try {
            if (shutdown) {
                LOGGER.error("Produce fail! Queue is already shutdown");
                return false;
            }
            int length = writable.getLength();
            if (length == 0) {
                LOGGER.error("Issue in dumping the object with zero byte into persistent queue");
                return false;
            }
            if (length > MSG_SIZE_LIMIT) {
                LOGGER.error("Issue in dumping the object into persistent queue, object size " + length +
                        " exceeds limit " + MSG_SIZE_LIMIT );
                return false;
            }
            //prepare the header
            prepareHeader(length);

            // check weather current buffer is enuf, otherwise we need to change the buffer
            if (writeMbb.remaining() < headerLength + length + endingLength) {
                writeMbb.put(EOF); // the end
                //                writeMbb.force();
                unMap(writeMbb);
                closeResource(writeDataChannel);
                closeResource(writeDataFile);
                if (writeFileIndex != readFileIndex) {
                    files.put(writeFileIndex, currentWritingFile);
                    if (files.size() + 2 > maxFileCount) {
                        removeOldestFile();
                    }
                }
                writeFileIndex++;
                currentWritingFile = new File(this.fileNamePrefix + "-" + writeFileIndex + DATA_FILE_SUFFIX);
                writeDataFile = new RandomAccessFile(currentWritingFile, "rw");
                writeDataChannel = writeDataFile.getChannel();

                // start the write buffer with writePosition 0 initially
                writeMbb = writeDataChannel.map(READ_WRITE, 0, pageSize);
            }
            writeMbb.put(header); // write header
            writable.writeTo(writeMbb);
            size += headerLength + length;
            return true;
        } catch (Throwable e) {
            LOGGER.error("Issue in dumping the object into persistent ", e);
            return false;
        }
    }

    private void removeOldestFile() {
        Long key = files.firstKey();
        File file = files.remove(key);
        file.delete();
    }

    public synchronized int consume(byte[] buffer) {
        try {
            if (shutdown) {
                LOGGER.error("Consume fail! Queue is already shutdown");
                return 0;
            }
            int currentPosition = readMbb.position();
            byte state = readMbb.get();
            int length;
            // end of the file
            if (state == EOF) {
                finishCurrentFile();
                setCurrentReadingFile();
                if (currentReadingFile == null) {
                    return 0;
                }
                currentPosition = readMbb.position();
                state = readMbb.get();
                length = readMbb.getInt();
                while (state == READ && length > 0) { // skip read message
                    readMbb.position(currentPosition + headerLength + length); // skipping the read bytes
                    currentPosition = readMbb.position();
                    state = readMbb.get();
                    length = readMbb.getInt();
                }
            } else {
                length = readMbb.getInt();
            }
            // state is wrong or length is wrong
            if (state != WRITTEN || length <= 0) {
                readMbb.position(currentPosition);
                if (writeFileIndex != readFileIndex) {
                    LOGGER.error("error queue file:{},position:{},state:{},length", currentReadingFile.getName(), currentPosition, state,
                            length);
                    setCurrentReadingFile();
                }
                return 0; // the queue is empty
            }
            if (state == WRITTEN) {
                if (length <= buffer.length) {
                    readMbb.get(buffer, 0, length);
                    readMbb.put(currentPosition, READ); // making it not active (deleted)
                    size -= headerLength + length;
                    return length;
                } else {
                    LOGGER.error("message too large:{}", length);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("error reading the persistent queue", e);
        }
        return 0;
    }

    public synchronized byte[] consume() {
        try {
            if (shutdown) {
                LOGGER.warn("Consume fail! Queue is already shutdown");
                return null;
            }
            int currentPosition = readMbb.position();
            byte state = readMbb.get();
            int length;
            // end of the file
            if (state == EOF) {
                finishCurrentFile();
                setCurrentReadingFile();
                if (currentReadingFile == null) {
                    return null;
                }
                currentPosition = readMbb.position();
                state = readMbb.get();
                length = readMbb.getInt();
                while (state == READ && length > 0) { // skip read messages
                    size -= headerLength + length;
                    readMbb.position(currentPosition + headerLength + length); // skipping the read bytes
                    currentPosition = readMbb.position();
                    state = readMbb.get();
                    length = readMbb.getInt();
                }
            } else {
                length = readMbb.getInt();
            }
            // state is wrong or length is wrong
            if (state != WRITTEN || length <= 0) {
                readMbb.position(currentPosition);
                if (writeFileIndex != readFileIndex) {
                    LOGGER.error("error queue file:{},position:{},state:{},length", currentReadingFile.getName(), currentPosition, state,
                            length);
                    setCurrentReadingFile();
                }
                return null; // the queue is empty
            }
            if (state == WRITTEN) {
                byte[] bytes = new byte[length];
                readMbb.get(bytes);
                readMbb.put(currentPosition, READ); // making it not state (deleted)
                size -= headerLength + length;
                return bytes;
            }
        } catch (Throwable e) {
            LOGGER.error("Issue in reading the persistent queue : ", e);
        }
        return null;
    }

    private void finishCurrentFile() {
        unMap(readMbb);
        closeResource(readDataChannel);
        closeResource(readDataFile);
        toDeleteFiles.add(currentReadingFile);
        currentReadingFile = null;
    }

    private void setCurrentReadingFile() throws IOException {
        if (readFileIndex == writeFileIndex) {
            return;
        }
        if (!files.isEmpty()) {
            readFileIndex = files.lastKey();
            files.remove(readFileIndex);
        } else {
            readFileIndex = writeFileIndex;
        }
        currentReadingFile = new File(this.fileNamePrefix + "-" + readFileIndex + DATA_FILE_SUFFIX);
        readDataFile = new RandomAccessFile(currentReadingFile, "rw");
        readDataChannel = readDataFile.getChannel();
        readMbb = readDataChannel.map(READ_WRITE, 0, pageSize);
    }

    private void prepareHeader(int length) {
        header.clear();
        header.put(WRITTEN);
        header.putInt(length);
        header.flip();
    }

    private static void unMap(MappedByteBuffer buffer) {
        if (buffer == null) { return; }
        sun.misc.Cleaner cleaner = ((DirectBuffer) buffer).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    private static void closeResource(Closeable c) {
        try {
            if (c != null) { c.close(); }
        } catch (Exception ignore) {            /* Do Nothing */
        }
    }

    /**
     * file size in bytes
     */
    public long getBackFileSize() {
        File[] queueFiles = queueDir.listFiles((dir, name) -> name.endsWith(DATA_FILE_SUFFIX));
        long sum = 0;
        if (queueFiles != null) {
            for (File queueFile : queueFiles) {
                sum += queueFile.length();
            }
        }
        return sum;
    }

    public boolean isEmpty() {
        return this.readFileIndex == this.writeFileIndex && readMbb.position() == writeMbb.position();
    }

    public synchronized void shutdown() {
        shutdown = true;

        if (writeMbb != null) {
            writeMbb.force();
            unMap(writeMbb);
        }
        if (readMbb != null) {
            readMbb.force();
            unMap(readMbb);
        }

        closeResource(readDataChannel);
        closeResource(readDataFile);
        closeResource(writeDataChannel);
        closeResource(writeDataFile);
    }

    /**
     * Periodically delete old used file which are not in current read/write window.
     *
     * @author yangbo
     */
    class FileCleanTask implements Runnable {

        public void run() {
            while (true) {
                File file = null;
                try {
                    file = toDeleteFiles.take();
                } catch (InterruptedException e) {
                }
                file.delete();
            }
        }
    }

}
