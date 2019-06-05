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
package com.alipay.sofa.lookout.server.interfaces.exception;

import com.alipay.sofa.lookout.server.interfaces.model.ErrorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by kevin.luy@alipay.com on 2018/3/8.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PromExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(PromExceptionHandler.class);

    @ExceptionHandler(value = { PromClientException.class, PromServerException.class })
    public ResponseEntity<ErrorData> jsonErrorHandler(HttpServletRequest req, Exception e) {
        if (e instanceof PromClientException) {
            return new ResponseEntity<ErrorData>(new ErrorData(e.getMessage()),
                HttpStatus.BAD_REQUEST);
        }
        logger.error(e.getMessage(), e);
        return new ResponseEntity<ErrorData>(new ErrorData(e.getMessage()),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
