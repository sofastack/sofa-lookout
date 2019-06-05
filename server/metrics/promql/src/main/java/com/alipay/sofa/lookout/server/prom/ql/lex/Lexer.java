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
package com.alipay.sofa.lookout.server.prom.ql.lex;

import com.alipay.sofa.lookout.server.prom.util.NoopUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;

import static com.alipay.sofa.lookout.server.prom.ql.lex.ItemType.*;

/**
 * refer to the Lexer of prometheus
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Lexer {

    static final String lineComment = "#";
    static final char EOF = Character.MIN_VALUE;


    String input;     // The string being scanned.
    StateFn state;   // The next lexing function to enter.
    int pos = 0;       // Current position in the input.
    int start;       // Start position of this item.
    int width;       // Width of last char read from input.
    int lastPos;       // Position of most recent item returned by nextItem.
    LinkedList<Item> queue = new LinkedList<>();  // Channel of scanned items.
    int parenDepth;  // Nesting depth of ( ) exprs.
    boolean braceOpen; // Whether a { is opened.
    boolean bracketOpen; // Whether a [ is opened.
    char stringOpen; // Quote rune of the string currently being read.

    // seriesDesc is set when a series description for the testing
    // language is lexed.
    boolean seriesDesc;


    private Lexer(String input) {
        this.input = input;
    }


    // lex creates a new scanner for the input string.
    public static Lexer lex(String input) {
        Lexer l = new Lexer(input);
        //caller do resolve
        l.run();
        return l;
    }

    // run runs the state machine for the lexer.(函数类型，直接用函数名赋予引用)
    private void run() {
        state = lexStatements;
        while (state != null) {
            state = state.invoke(this);
        }
    }


    // next returns the next rune in the input.
    char next() {
        if (pos >= input.length()) {
            width = 0;
            return EOF;
        }
        char r = input.charAt(pos);
        width = 1;//char num;
        pos += width;
        return r;
    }

    // peek returns but does not consume the next rune in the input.
    char peek() {
        char r = next();
        backup();
        return r;
    }

    // backup steps back one rune. Can only be called once per call of next.
    void backup() {
        pos -= width;
    }

    // emit passes an item back to the client.
    private void emit(ItemType t) {
        queue.add(new Item(t, start, input.substring(start, pos)));
        start = pos;//标记下一次启动地址；
    }

    // ignore skips over the pending input before this point.
    void ignore() {
        start = pos;
    }

    // accept consumes the next rune if it's from the valid set.
//判断下一个字符，是否包含存在于valid字符串中；
    boolean accept(String valid) {
        if (StringUtils.contains(valid, next())) {
            return true;
        }
        backup();
        return false;
    }

    // acceptRun consumes a run of runes from the valid set.
//如果匹配，则继续循环匹配下一个，效果相当于消费掉了匹配的；
    void acceptRun(String valid) {
        while (StringUtils.contains(valid, next())) {
            NoopUtils.noop();
        }
        backup();
    }

    // lineNumber reports which line we're on, based on the position of
// the previous item returned by nextItem. Doing it this way
// means we don't have to worry about peek double counting.
//含有多少个换行符+1；表示当前行；
    public int lineNumber() {
        int number = StringUtils.countMatches(input.substring(0, lastPos), "\n");
        return 1 + number;
    }

    // linePosition reports at which character in the current line
// we are on.
    public int linePosition() {
        int lb = input.substring(0, lastPos).lastIndexOf("\n");

        if (lb < 0) {
            return 1 + lastPos;
        }
        return 1 + lastPos - lb;
    }

    // errorf returns an error token and terminates the scan by passing
// back a nil pointer that will be the next state, terminating l.nextItem.
    StateFn errorf(String format, Object... args) {
        queue.add(new Item(itemError, start, String.format(format, args)));
        return null;
    }

//    StateFn errorf(String format) throws InterruptedException {
//        return errorf(format, null);
//    }

    // nextItem returns the next item from the input.
    public Item nextItem() {
        Item item = queue.poll();
        if (item == null) {//may be null
            return null;
        }
        lastPos = item.pos;
        return item;
    }


    // lexStatements is the top-level state for lexing.
    static StateFn lexStatements = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            if (l.braceOpen) {
                return lexInsideBraces;
            }

            if (StringUtils.startsWith(l.input.substring(l.pos), lineComment)) {
                return lexLineComment;
            }
            char r = l.next();
            if (r == EOF) {
                if (l.parenDepth != 0) {
                    return l.errorf("unclosed left parenthesis");
                } else if (l.bracketOpen) {
                    return l.errorf("unclosed left bracket");
                }
                l.emit(itemEOF);
                return null;
            } else if (r == ',') {
                l.emit(itemComma);
            } else if (isSpace(r)) {
                return lexSpace;
            } else if (r == '*')
                l.emit(itemMUL);
            else if (r == '/')
                l.emit(itemDIV);
            else if (r == '%')
                l.emit(itemMOD);
            else if (r == '+')
                l.emit(itemADD);
            else if (r == '-')
                l.emit(itemSUB);
            else if (r == '^')
                l.emit(itemPOW);
            else if (r == '=') {
                char t = l.peek();
                if (t == '=') {
                    l.next();
                    l.emit(itemEQL);
                } else if (t == '~') {
                    return l.errorf("unexpected character after '=': %q", t);
                } else {
                    l.emit(itemAssign);
                }
            } else if (r == '!') {
                char t = l.next();
                if (t == '=') {
                    l.emit(itemNEQ);
                } else {
                    return l.errorf("unexpected character after '!': %q", t);
                }
            } else if (r == '<') {
                //可能(< or <=)，就需要peek试探；
                char t = l.peek();
                if (t == '=') {
                    l.next();
                    l.emit(itemLTE);
                } else {
                    l.emit(itemLSS);
                }
            } else if (r == '>') {
                char t = l.peek();
                if (t == '=') {
                    l.next();
                    l.emit(itemGTE);
                } else {
                    l.emit(itemGTR);
                }
            } else if (isDigit(r) || (r == '.' && isDigit(l.peek()))) {
                l.backup();
                return lexNumberOrDuration;
            } else if (r == '"' || r == '\'') {
                l.stringOpen = r;
                return lexString;
            } else if (r == '`') {
                l.stringOpen = r;
                return lexRawString;
            } else if (isAlpha(r) || r == ':') {
                l.backup();
                return lexKeywordOrIdentifier;
            } else if (r == '(') {
                l.emit(itemLeftParen);
                l.parenDepth++;
                return lexStatements;
            } else if (r == ')') {
                l.emit(itemRightParen);
                l.parenDepth--;
                if (l.parenDepth < 0) {
                    return l.errorf("unexpected right parenthesis %q", r);
                }
                return lexStatements;
            } else if (r == '{') {
                l.emit(itemLeftBrace);
                l.braceOpen = true;
                return lexInsideBraces.invoke(l);
            } else if (r == '[') {
                if (l.bracketOpen) {
                    return l.errorf("unexpected left bracket %q", r);
                }
                l.emit(itemLeftBracket);
                l.bracketOpen = true;
                return lexDuration;
            } else if (r == ']') {
                if (!l.bracketOpen) {
                    return l.errorf("unexpected right bracket %q", r);
                }
                l.emit(itemRightBracket);
                l.bracketOpen = false;
            } else {
                return l.errorf("unexpected character: %q", r);
            }
            return lexStatements;
        }
    };

    // lexInsideBraces scans the inside of a vector selector. Keywords are ignored and
// scanned as identifiers.
    static StateFn lexInsideBraces = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {

            if (StringUtils.startsWith(l.input.substring(l.pos), lineComment)) {
                return lexLineComment;
            }

            char r = l.next();
            if (r == EOF) {
                return l.errorf("unexpected end of input inside braces");
            } else if (isSpace(r)) {
                return lexSpace;
            } else if (isAlpha(r)) {
                l.backup();
                return lexIdentifier;
            } else if (r == ',') {
                l.emit(itemComma);
            } else if (r == '"' || r == '\'') {
                l.stringOpen = r;
                return lexString;
            } else if (r == '`') {
                l.stringOpen = r;
                return lexRawString;
            } else if (r == '=') {
                if (l.next() == '~') {
                    if (l.peek() == '|') {
                        l.next();
                        l.emit(itemLiteralOr);
                    } else {
                        l.emit(itemEQLRegex);
                    }
                } else {
                    l.backup();
                    l.emit(itemEQL);
                }
            } else if (r == '!') {
                char nr = l.next();
                switch (nr) {
                    case '~':
                        if (l.peek() == '|') {
                            l.next();
                            l.emit(itemNotLiteralOr);
                        } else {
                            l.emit(itemNEQRegex);
                        }
                        break;
                    case '=':
                        l.emit(itemNEQ);
                        break;
                    default:
                        return l.errorf("unexpected character after '!' inside braces: %q", nr);
                }
            } else if (r == '{') {
                return l.errorf("unexpected left brace %q", r);
            } else if (r == '}') {
                l.emit(itemRightBrace);
                l.braceOpen = false;
                if (l.seriesDesc) {
                    return lexValueSequence;
                }
                return lexStatements;
            } else {
                l.errorf("unexpected character inside braces: %q", r);
            }

            return lexInsideBraces;
        }
    };


    // lexValueSequence scans a value sequence of a series description.
    static StateFn lexValueSequence = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            char r = l.next();
            if (r == EOF) {
                return lexStatements;
            } else if (isSpace(r)) {
                lexSpace.invoke(l);
            } else if (r == '+') {
                l.emit(itemADD);
            } else if (r == '-') {
                l.emit(itemSUB);
            } else if (r == 'x') {
                l.emit(itemTimes);
            } else if (r == '_') {
                l.emit(itemBlank);
            } else if (isDigit(r) || (r == '.' && isDigit(l.peek()))) {
                l.backup();
                lexNumber.invoke(l);
            } else if (isAlpha(r)) {
                l.backup();
                // We might lex invalid items here but this will be caught by the parser.
                return lexKeywordOrIdentifier;
            } else {
                return l.errorf("unexpected character in series sequence: %q", r);
            }
            return lexValueSequence;
        }
    };


    // lexEscape scans a string escape sequence. The initial escaping character (\)
// has already been seen.
//
// NOTE: This function as well as the helper function digitVal() and associated
// tests have been adapted from the corresponding functions in the "go/scanner"
// package of the Go standard library to work for Prometheus-style strings.
// None of the actual escaping/quoting logic was changed in this function - it
// was only modified to integrate with our lexer.
    static void lexEscape(Lexer l) {
        int n = 0;
        int base = 0;
        int max = 0;


        char ch = l.next();


        if (ch == 'a' || ch == 'b' || ch == 'f' || ch == 'n' || ch == 'r' || ch == 't' || ch == 'v' || ch == '\\' || ch == l.stringOpen) {
            return;
        } else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6' || ch == '7') {
            n = 3;
            base = 8;
            max = 255;
        } else if (ch == 'x') {
            ch = l.next();
            n = 2;
            base = 16;
            max = 255;
        } else if (ch == 'u') {
            ch = l.next();
            n = 4;
            base = 16;
            max = Character.MAX_CODE_POINT;
        } else if (ch == 'U') {
            ch = l.next();
            n = 8;
            base = 16;
            max = Character.MAX_CODE_POINT;
        } else if (ch == EOF) {
            l.errorf("escape sequence not terminated");
        } else {
            l.errorf("unknown escape sequence %#U", ch);
        }

        int x = 0;
        while (n > 0) {
            int d = digitVal(ch);
            if (d >= base) {
                if (ch == EOF) {
                    l.errorf("escape sequence not terminated");
                }
                l.errorf("illegal character %#U in escape sequence", ch);
            }
            x = x * base + d;
            ch = l.next();
            n--;
        }

        if (x > max || 0xD800 <= x && x < 0xE000) {
            l.errorf("escape sequence is an invalid Unicode code point");
        }
    }

    // digitVal returns the digit value of a rune or 16 in case the rune does not
// represent a valid digit.
    private static int digitVal(char ch) {
        if ('0' <= ch && ch <= '9') {
            return (int) (ch - '0');
        } else if ('a' <= ch && ch <= 'f') {
            return (int) (ch - 'a' + 10);
        } else if ('A' <= ch && ch <= 'F') {
            return (int) (ch - 'A' + 10);
        }
        return 16; // Larger than any legal digit val.
    }

    // lexString scans a quoted string. The initial quote has already been seen.
    static StateFn lexString = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            while (true) {
                char x = l.next();


                if (x == '\\')
                    lexEscape(l);
                else if (x == EOF || x == '\n') {
                    return l.errorf("unterminated quoted string");
                } else if (x == l.stringOpen) {
                    break;
                }
            }
            l.emit(itemString);
            return lexStatements;
        }
    };


    // lexRawString scans a raw quoted string. The initial quote has already been seen.
    static StateFn lexRawString = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            for (; true; ) {
                char r = l.next();
                if (r == EOF)
                    return l.errorf("unterminated raw string");
                else if (r == l.stringOpen) {
                    break;
                }
            }
            l.emit(itemString);
            return lexStatements;
        }
    };


    // lexSpace scans a run of space characters. One space has already been seen.
    static StateFn lexSpace = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            while (isSpace(l.peek())) {
                l.next();
            }
            l.ignore();
            return lexStatements;
        }
    };


    // lexLineComment scans a line comment. Left comment marker is known to be present.
    static StateFn lexLineComment = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            l.pos += lineComment.length();
            char r;
            for (r = l.next(); !isEndOfLine(r) && r != EOF; ) {
                r = l.next();
            }
            l.backup();
            l.emit(itemComment);
            return lexStatements;
        }
    };


    static StateFn lexDuration = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            if (scanNumber(l)) {
                return l.errorf("missing unit character in duration");
            }
            // Next two chars must be a valid unit and a non-alphanumeric.
            if (l.accept("smhdwy")) {
                if (isAlphaNumeric(l.next())) {
                    return l.errorf("bad duration syntax: %q", l.input.substring(l.start, l.pos));
                }
                l.backup();
                l.emit(itemDuration);
                return lexStatements;
            }
            return l.errorf("bad duration syntax: %q", l.input.substring(l.start, l.pos));
        }
    };


    // lexNumber scans a number: decimal, hex, oct or float.
    static StateFn lexNumber = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            if (!scanNumber(l)) {
                return l.errorf("bad number syntax: %q", l.input.substring(l.start, l.pos));
            }
            l.emit(itemNumber);
            return lexStatements;
        }
    };


    // lexNumberOrDuration scans a number or a duration item.
    static StateFn lexNumberOrDuration = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            if (scanNumber(l)) {
                l.emit(itemNumber);
                return lexStatements;
            }
            // Next two chars must be a valid unit and a non-alphanumeric.
            if (l.accept("smhdwy")) {
                if (isAlphaNumeric(l.next())) {
                    return l.errorf("bad number or duration syntax: %q", l.input.substring(l.start, l.pos));
                }
                l.backup();
                l.emit(itemDuration);
                return lexStatements;
            }
            return l.errorf("bad number or duration syntax: %q", l.input.substring(l.start, l.pos));
        }
    };


    // scanNumber scans numbers of different formats. The scanned item is
// not necessarily a valid number. This case is caught by the parser.
    private static boolean scanNumber(Lexer l) {
        String digits = "0123456789";
        // Disallow hexadecimal in series descriptions as the syntax is ambiguous.
        //是否十六进制；
        if (!l.seriesDesc && l.accept("0") && l.accept("xX")) {
            digits = "0123456789abcdefABCDEF";
        }
        l.acceptRun(digits);
        if (l.accept(".")) {
            l.acceptRun(digits);
        }
        if (l.accept("eE")) {
            l.accept("+-");
            l.acceptRun("0123456789");
        }
        // Next thing must not be alphanumeric unless it's the times token
        // for series repetitions.
        char r = l.peek();
        if ((l.seriesDesc && r == 'x') || !isAlphaNumeric(r)) {
            return true;
        }
        return false;
    }

    // lexIdentifier scans an alphanumeric identifier. The next character
// is known to be a letter.
    static StateFn lexIdentifier = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            for (; isAlphaNumeric(l.next()); ) {
                // absorb
            }
            l.backup();
            l.emit(itemIdentifier);
            return lexStatements;
        }
    };


    // lexKeywordOrIdentifier scans an alphanumeric identifier which may contain
// a colon rune. If the identifier is a keyword the respective keyword item
// is scanned.
    static StateFn lexKeywordOrIdentifier = new StateFn() {
        @Override
        public StateFn invoke(Lexer l) {
            for (; true; ) {
                String word = null;
                char r = l.next();

                if (isAlphaNumeric(r) || r == ':' || r == '.') {
                    NoopUtils.noop();
                } else {
                    l.backup();
                    word = l.input.substring(l.start, l.pos);

                    ItemType kw = ItemTypeSupport.key.get(StringUtils.lowerCase(word));
                    if (kw != null) {
                        l.emit(kw);
                    } else if (!StringUtils.contains(word, ":")) {
                        l.emit(itemIdentifier);
                    } else {
                        l.emit(itemMetricIdentifier);
                    }
                    break;
                }
            }
            if (l.seriesDesc && l.peek() != '{') {
                return lexValueSequence;
            }
            return lexStatements;
        }
    };


    private static boolean isSpace(char r) {
        return r == ' ' || r == '\t' || r == '\n' || r == '\r';
    }

    // isEndOfLine reports whether r is an end-of-line character.
    private static boolean isEndOfLine(char r) {
        return r == '\r' || r == '\n';
    }

    // isAlphaNumeric reports whether r is an alphabetic, digit, or underscore.
    private static boolean isAlphaNumeric(char r) {
        return isAlpha(r) || isDigit(r);
    }

    // isDigit reports whether r is a digit. Note: we cannot use unicode.IsDigit()
// instead because that also classifies non-Latin digits as digits. See
// https://github.com/prometheus/prometheus/issues/939.
    private static boolean isDigit(char r) {
        return '0' <= r && r <= '9';
    }

    // isAlpha reports whether r is an alphabetic or underscore.
    private static boolean isAlpha(char r) {
        return r == '_' || ('a' <= r && r <= 'z') || ('A' <= r && r <= 'Z');
    }

    // isLabel reports whether the string can be used as label. 字母或数字都可以
    public static boolean isLabel(String s) {
        if (StringUtils.isEmpty(s) || !isAlpha(s.charAt(0))) {
            return false;
        }
        char[] chars = s.toCharArray();
        for (int i = 1; i < s.length(); i++) {
            if (!isAlphaNumeric(chars[i])) {
                return false;
            }
        }
        return true;
    }


    //===getter&&setter===

    public String getInput() {
        return input;
    }

    public int getLastPos() {
        return lastPos;
    }

    public void setSeriesDesc(boolean seriesDesc) {
        this.seriesDesc = seriesDesc;
    }
}
