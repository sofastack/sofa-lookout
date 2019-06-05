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
package com.alipay.sofa.lookout.server.interfaces.common;

import com.alipay.sofa.lookout.server.interfaces.exception.PromClientException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by kevin.luy@alipay.com on 2018/5/6.
 */
public final class QueryExpressionUtil {
    private QueryExpressionUtil() {
    }

    /**
     * 从查询表达式中剔除掉不需要的的查询条件限制
     *
     * @param expr       目标查询表达式
     * @param labelNames 忽略的查询条件（存在于目标 expr 中的）
     * @return expr 加工处理后的表达式
     */
    public static String ignoreLabelMatches(String expr, List<String> labelNames) {
        if (CollectionUtils.isEmpty(labelNames) || StringUtils.isEmpty(expr)) {
            return expr;
        }
        String newExprStr = expr;
        int leftBracePos = newExprStr.indexOf('{');
        int rightBracePos = -1;
        while (leftBracePos < newExprStr.length() && leftBracePos > 0) {
            rightBracePos = newExprStr.indexOf('}', leftBracePos);
            Preconditions.checkTrue(rightBracePos > leftBracePos, "illegal query expression:" + expr + ",no pair brace {...}");
            String labelSelector = newExprStr.substring(leftBracePos + 1, rightBracePos);
            if (labelSelector.length() > 0) {
                boolean matched = false;
                Stream<String> labelMatcheStream = Splitter.on(",").splitToList(labelSelector).stream();
                int tmp = rightBracePos - labelSelector.length();//minus old length
                //需要排除的labels
                for (String ln : labelNames) {
                    //不包含，实际上也没有携带;
                    if (!labelSelector.contains(ln)) {
                        continue;
                    }
                    //包含需要剔除的
                    matched = true;
                    labelMatcheStream = labelMatcheStream.filter(lm -> !lm.trim().startsWith(ln));
                }
                if (matched) {
                    labelSelector = Joiner.on(',').join(labelMatcheStream.iterator());
                    newExprStr = newExprStr.substring(0, leftBracePos + 1) + labelSelector + newExprStr.substring(rightBracePos);
                    rightBracePos = tmp + labelSelector.length();//add new length. refresh right brace pos
                }
            }
            leftBracePos = newExprStr.indexOf('{', rightBracePos);
        }
        return newExprStr;
    }

    public static void checkNoTagFilter(String queryStr) {
        //check no tag filter query;
        int leftBracePos = queryStr.indexOf('{');
        if (leftBracePos < 0 || (leftBracePos > 0 && queryStr.charAt(leftBracePos + 1) == '}')) {
            throw new PromClientException(
                "In order to improve query performance, you need to add tag filtering!")
                .setRealQuery(queryStr);
        }
    }

    /**
     * 如果PromQL查询语句含有正则匹配查询，必须含有一个精确匹配的tag
     *
     * @param queryStr
     */
    public static void checkRegexMatcher(String queryStr) {
        String newExprStr = queryStr;
        int leftBracePos = newExprStr.indexOf('{');
        int rightBracePos = -1;
        while (leftBracePos < newExprStr.length() && leftBracePos > 0) {
            rightBracePos = newExprStr.indexOf('}', leftBracePos);
            Preconditions.checkTrue(rightBracePos > leftBracePos, "illegal query expression:"
                                                                  + queryStr
                                                                  + ",no pair brace {...}");
            String labelSelector = newExprStr.substring(leftBracePos + 1, rightBracePos);
            int regexPos = labelSelector.indexOf("=~");
            int orPos = labelSelector.indexOf("=~|");
            // =~| 操作不需要精确匹配的tag进行查询
            if (regexPos > -1 && orPos != regexPos) {
                //check left quote
                int regexValuePos = checkRegexValueLeftQuotePos(labelSelector);
                //check right quote
                labelSelector = labelSelector.substring(regexValuePos + 1);
                regexValuePos = checkRegexValueRightQuotePos(labelSelector);
                labelSelector = labelSelector.substring(regexValuePos + 1);
                if (labelSelector.indexOf("=") == -1 && labelSelector.indexOf("!=") == -1) {
                    throw new PromClientException(
                        "In order to improve query performance, when use regex you need to add two tags. one of the tag must use "
                                + "equal operator").setRealQuery(queryStr);
                }

            }
            leftBracePos = newExprStr.indexOf('{', rightBracePos);
        }
    }

    private static int checkRegexValueLeftQuotePos(String labelSelector) {
        Pattern pattern = Pattern.compile("\\s*\"");
        Matcher matcher = pattern.matcher(labelSelector);
        if (matcher.find()) {
            return matcher.start();
        } else {
            throw new PromClientException("illegal query expression:" + labelSelector
                                          + ",no pair quote \"...\" for regex value ");
        }
    }

    private static int checkRegexValueRightQuotePos(String labelSelector) {
        Pattern pattern = Pattern.compile("\"\\s*,?");
        Matcher matcher = pattern.matcher(labelSelector);
        if (matcher.find()) {
            return matcher.start();
        } else {
            throw new PromClientException("illegal query expression:" + labelSelector
                                          + ",no pair quote \"...\" for regex value ");
        }
    }

}
