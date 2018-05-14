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
package com.alipay.lookout.os.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wuqin
 * @version $Id: FileUtils.java, v 0.1 2017-03-18 下午5:17 wuqin Exp $$
 */
public class FileUtils {
    /**
     * Read the content of file
     *
     * @param path the path to file
     * @return the file content as string
     */
    public static String readFile(String path) throws IOException {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }

    /**
     * Read the content of file
     *
     * @param path the path to file
     * @return the file content as string list
     */
    public static List<String> readFileAsStringArray(String path) throws IOException {
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return lines;
    }

    /**
     * Read the last N lines of file
     *
     * @param path the path to file
     * @param numRead the number of line
     * @return
     */
    public static List<String> readLastNLine(String path, int numRead) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return null;
        }

        List<String> result = new ArrayList<String>();
        long count = 0;
        RandomAccessFile fileRead = null;
        try {
            fileRead = new RandomAccessFile(file, "r");
            long length = fileRead.length();
            if (length == 0L) {
                return result;
            } else {
                long pos = length - 1;
                while (pos > 0) {
                    pos--;
                    fileRead.seek(pos);
                    if (fileRead.readByte() == '\n') {
                        String line = fileRead.readLine();
                        result.add(line);
                        count++;
                        if (count == numRead) {
                            break;
                        }
                    }
                }
                if (pos == 0) {
                    fileRead.seek(0);
                    result.add(fileRead.readLine());
                }
            }
        } catch (IOException e) {
            // ignore
        } finally {
            if (fileRead != null) {
                try {
                    fileRead.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return result;
    }
}
