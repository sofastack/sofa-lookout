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
package com.alipay.sofa.lookout.server.prom.ql.parse;

import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.labels.MatchType;
import com.alipay.sofa.lookout.server.prom.labels.Matcher;
import com.alipay.sofa.lookout.server.prom.ql.ast.*;
import com.alipay.sofa.lookout.server.prom.ql.func.Function;
import com.alipay.sofa.lookout.server.prom.ql.lex.Item;
import com.alipay.sofa.lookout.server.prom.ql.lex.ItemType;
import com.alipay.sofa.lookout.server.prom.ql.lex.Lexer;
import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;
import com.alipay.sofa.lookout.server.prom.util.NoopUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.alipay.sofa.lookout.server.prom.labels.Labels.MetricName;
import static com.alipay.sofa.lookout.server.prom.labels.MatchType.*;
import static com.alipay.sofa.lookout.server.prom.ql.ast.Card.*;
import static com.alipay.sofa.lookout.server.prom.ql.lex.ItemType.*;
import static com.alipay.sofa.lookout.server.prom.ql.lex.Lexer.isLabel;

/**
 * Parse an expression from a text input
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Parser {

    Lexer  lex;
    //为了缓存peek出来的值；通过peekcount方便next()的真实消费
    Item[] token = new Item[3];
    int    peekCount;

    private Parser(Lexer lexer) {
        lex = lexer;
    }

    private static Parser newParser(String input) {
        return new Parser(Lexer.lex(input));
    }

    public static Expr parseExpr(String input) {
        Parser p = newParser(input);
        Expr expr = p.parseExpr();
        p.typecheck(expr);
        return expr;
    }

    //parses the input into a metric
    public static Labels parseMetric(String input) {
        Parser p = newParser(input);
        Labels m = p.metric();
        if (p.peek().getTyp() != itemEOF) {
            p.errorf("could not parse remaining input %s...",
                p.lex.getInput().substring(p.lex.getLastPos()));
        }
        return m;
    }

    private static List<Matcher> parseMetricSelector(String input) {
        Parser p = newParser(input);
        String name = "";
        ItemType t = p.peek().getTyp();
        if (t == itemMetricIdentifier || t == itemIdentifier) {
            name = p.next().getVal();
        }
        VectorSelector vs = vectorSelector(p, name);
        if (p.peek().getTyp() != itemEOF) {
            p.errorf("could not parse remaining input %s...",
                p.lex.getInput().substring(p.lex.getLastPos()));
        }
        return vs.getMatchers();
    }

    // parseSeriesDesc parses the description of a time series.
    //    public static Labels parseSeriesDesc(String input) {
    //        Parser p = newParser(input);
    //        p.lex.setSeriesDesc(true);
    //        return p.parseSeriesDesc();
    //    }

    // parseStmts parses a sequence of statements from the input.
    //    private Statements parseStmts() {
    //        Statements stmts = new Statements();
    //        for (; peek().getTyp() != itemEOF; ) {
    //            if (peek().getTyp() == itemComment) {
    //                continue;
    //            }
    //            stmts.add(stmt());
    //        }
    //        return stmts;
    //    }

    private Expr parseExpr() {
        Expr expr = null;
        for (; peek().getTyp() != itemEOF;) {
            if (peek().getTyp() == itemComment) {
                continue;
            }
            if (expr != null) {
                errorf("could not parse remaining input %s...",
                    lex.getInput().substring(lex.getLastPos()));
            }
            expr = expr();
        }

        if (expr == null) {
            errorf("no expression found in input");
        }
        return expr;
    }

    // typecheck checks correct typing of the parsed statements or expression.
    void typecheck(Node node) {
        checkType(node);
    }

    // next returns the next token.
    Item next() {
        if (peekCount > 0) {
            peekCount--;
        } else {
            Item t = lex.nextItem();
            // Skip comments.
            for (; t.getTyp() == itemComment;) {
                t = lex.nextItem();
            }
            token[0] = t;
        }
        if (token[peekCount].getTyp() == itemError) {
            errorf("%s", token[peekCount].getVal());
        }
        return token[peekCount];
    }

    // peek returns but does not consume the next token.
    private Item peek() {
        if (peekCount > 0) {
            return token[peekCount - 1];
        }
        peekCount = 1;

        Item t = lex.nextItem();
        // Skip comments.
        for (; t.getTyp() == itemComment;) {
            t = lex.nextItem();
        }
        token[0] = t;
        return token[0];
    }

    // backup backs the input stream up one token.
    private void backup() {
        peekCount++;
    }

    // errorf formats the error and terminates processing.
    private void errorf(String format, Object... args) {
        error(String.format(format, args));
    }

    // error terminates processing.
    private void error(String error) {
        ParseErr perr = new ParseErr(lex.lineNumber(), lex.linePosition(), error);
        String input2 = StringUtils.trim(lex.getInput());
        int number = StringUtils.countMatches(input2, "\n");
        if (number == 0) {
            perr.line = 0;
        }
        throw new IllegalStateException(perr.toString());
    }

    // expect consumes the next token and guarantees it has the required type.
    private Item expect(ItemType exp, String context) {
        Item token = next();
        if (token.getTyp() != exp) {
            errorf("unexpected %s in %s, expected %s", token.desc(), context, exp.desc());
        }
        return token;
    }

    // expectOneOf consumes the next token and guarantees it has one of the required types.
    private Item expectOneOf(ItemType exp1, ItemType exp2, String context) {
        Item token = next();
        if (token.getTyp() != exp1 && token.getTyp() != exp2) {
            errorf("unexpected %s in %s, expected %s or %s", token.desc(), context, exp1.desc(),
                exp2.desc());
        }
        return token;
    }

    private Statement stmt() {
        Item tok = peek();
        if (tok.getTyp() == itemIdentifier || tok.getTyp() == itemMetricIdentifier) {
            return recordStmt();
        } else if (tok.getTyp() == itemAlert) {
            //alertStmt();
            throw new UnsupportedOperationException("alert stmt");
        }
        errorf("no valid statement detected");
        return null;
    }

    // recordStmt parses a recording rule.
    private RecordStmt recordStmt() {
        String ctx = "record statement";

        String name = expectOneOf(itemIdentifier, itemMetricIdentifier, ctx).getVal();
        Labels lset = null;
        //解析{}
        if (peek().getTyp() == itemLeftBrace) {
            lset = labelSet();
        }

        expect(itemAssign, ctx);
        Expr expr = expr();
        return new RecordStmt(name, expr, lset);
    }

    // expr parses any expression.
    private Expr expr() {
        // Parse the starting expression.
        Expr expr = unaryExpr();
        // Loop through the operations and construct a binary operation tree based
        // on the operators' precedence.
        ItemType op = null;
        for (; true;) {
            // If the next token is not an operator the expression is done.
            op = peek().getTyp();
            if (!op.isOperator()) {
                return expr;
            }
            next(); // Consume operator.

            // Parse optional operator matching options. Its validity
            // is checked in the type-checking stage.
            VectorMatching vecMatching = new VectorMatching(CardOneToOne);
            if (op.isSetOperator()) {
                vecMatching.setCard(CardManyToMany);
            }
            boolean returnBool = false;
            // Parse bool modifier.
            if (peek().getTyp() == itemBool) {
                if (!op.isComparisonOperator()) {
                    errorf("bool modifier can only be used on comparison operators");
                }
                next();
                returnBool = true;
            }

            // Parse ON/IGNORING clause.
            if (peek().getTyp() == itemOn || peek().getTyp() == itemIgnoring) {
                if (peek().getTyp() == itemOn) {
                    vecMatching.setOn(true);
                }
                next();
                vecMatching.setMatchingLabels(labels());

                // Parse grouping.
                ItemType t = peek().getTyp();
                if (t == itemGroupLeft || t == itemGroupRight) {
                    next();
                    if (t == itemGroupLeft) {
                        vecMatching.setCard(CardManyToOne);
                    } else {
                        vecMatching.setCard(CardOneToMany);
                    }
                    if (peek().getTyp() == itemLeftParen) {
                        vecMatching.setInclude(labels());
                    }
                }
            }

            for (String ln : vecMatching.getMatchingLabels()) {
                for (String ln2 : vecMatching.getInclude()) {
                    if (StringUtils.equals(ln, ln2) && vecMatching.isOn()) {
                        errorf("label %q must not occur in ON and GROUP clause at once", ln);
                    }
                }
            }
            // Parse the next operand.
            Expr rhs = unaryExpr();
            // Assign the new root based on the precedence of the LHS and RHS operators.
            expr = balance(expr, op, rhs, vecMatching, returnBool);
        }
    }

    private BinaryExpr balance(Expr lhs, ItemType op, Expr rhs, VectorMatching vecMatching,
                               boolean returnBool) {
        if (lhs instanceof BinaryExpr) {
            BinaryExpr lhsBE = (BinaryExpr) lhs;
            int precd = lhsBE.getOp().precedence() - op.precedence();
            if ((precd < 0) || (precd == 0 && op.isRightAssociative())) {
                BinaryExpr balanced = balance(lhsBE.getRhs(), op, rhs, vecMatching, returnBool);
                if (lhsBE.getOp().isComparisonOperator() && !lhsBE.isReturnBool()
                    && balanced.type() == ValueType.scalar
                    && lhsBE.getLhs().type() == ValueType.scalar) {
                    errorf("comparisons between scalars must use BOOL modifier");
                }
                return new BinaryExpr(lhsBE.getOp(), lhsBE.getLhs(), balanced,
                    lhsBE.getVectorMatching(), lhsBE.isReturnBool());
            }
        }

        if (op.isComparisonOperator() && !returnBool && rhs.type() == ValueType.scalar
            && lhs.type() == ValueType.scalar) {
            errorf("comparisons between scalars must use BOOL modifier");
        }
        return new BinaryExpr(op, lhs, rhs, vecMatching, returnBool);

    }

    // unaryExpr parses a unary expression.
    //
    //		<Vector_selector> | <Matrix_selector> | (+|-) <number_literal> | '(' <expr> ')'
    //
    private Expr unaryExpr() {
        Item t = peek();
        switch (t.getTyp()) {
            case itemADD:
            case itemSUB: {
                next();
                Expr e = unaryExpr();

                // Simplify unary expressions for number literals.
                // 如果是 +1 or -1
                if (e instanceof NumberLiteral) {
                    NumberLiteral nl = (NumberLiteral) e;
                    if (t.getTyp() == itemSUB) {
                        nl.setVal(nl.getVal() * -1);
                    }
                    return nl;
                }
                return new UnaryExpr(t.getTyp(), e);
            }
            case itemLeftParen: {
                next();
                Expr e = expr();
                expect(itemRightParen, "paren expression");
                return new ParenExpr(e);
            }
        }
        Expr e = primaryExpr();
        // Expression might be followed by a range selector.
        if (peek().getTyp() == itemLeftBracket) {

            if (!(e instanceof VectorSelector)) {
                errorf(
                    "range specification must be preceded by a metric selector, but follows a %T instead",
                    e);
            }
            VectorSelector vs = (VectorSelector) e;
            e = rangeSelector(vs);
        }

        // Parse optional offset.
        if (peek().getTyp() == itemOffset) {
            Duration offset = offset();
            if (e instanceof VectorSelector) {
                ((VectorSelector) e).setOffset(offset);
            } else if (e instanceof MatrixSelector) {
                ((MatrixSelector) e).setOffset(offset);

            } else {
                errorf(
                    "offset modifier must be preceded by an instant or range selector, but follows a %T instead",
                    e);
            }
        }
        return e;
    }

    // rangeSelector parses a Matrix (a.k.a. range) selector based on a given
    // Vector selector.
    //
    //		<Vector_selector> '[' <duration> ']'
    //
    private MatrixSelector rangeSelector(VectorSelector vs) {
        String ctx = "range selector";
        next();
        String erangeStr = expect(itemDuration, ctx).getVal();
        Duration erange = parseDuration(erangeStr);
        expect(itemRightBracket, ctx);
        return new MatrixSelector(vs.getName(), vs.getMatchers(), erange);
    }

    // number parses a number.
    float number(String val) {
        try {
            float f = Float.parseFloat(val);
            return f;
        } catch (Throwable e) {
            throw new IllegalStateException("error parsing number", e);
        }

    }

    // primaryExpr parses a primary expression.
    //
    //		<metric_name> | <function_call> | <Vector_aggregation> | <literal>
    //
    private Expr primaryExpr() {
        Item t = next();
        float f;
        if (t.getTyp() == itemNumber) {
            f = number(t.getVal());
            return new NumberLiteral(f);
        } else if (t.getTyp() == itemString) {
            return new StringLiteral(t.getVal());
        } else if (t.getTyp() == itemLeftBrace) {
            // Metric selector without metric name.
            backup();
            return vectorSelector(this, "");
        } else if (t.getTyp() == itemIdentifier) {
            // Check for function call.
            if (peek().getTyp() == itemLeftParen) {
                return call(t.getVal());
            }
            return vectorSelector(this, t.getVal());
        } else if (t.getTyp() == itemMetricIdentifier) {
            return vectorSelector(this, t.getVal());
        } else if (t.getTyp().isAggregator()) {
            backup();
            return aggrExpr();
        } else {
            errorf("no valid expression found");
        }

        return null;
    }

    // labels parses a list of labelnames.这个是用在ignoring 或者 on场景的吧。
    //
    //		'(' <label_name>, ... ')'
    //
    private List<String> labels() {
        String ctx = "grouping opts";
        expect(itemLeftParen, ctx);
        Item id = null;
        List<String> labels = new ArrayList<String>();
        if (peek().getTyp() != itemRightParen) {
            for (; true;) {
                id = next();
                if (!isLabel(id.getVal())) {
                    errorf("unexpected %s in %s, expected label", id.desc(), ctx);
                }
                labels.add(id.getVal());
                if (peek().getTyp() != itemComma) {
                    break;
                }
                next();
            }
        }
        expect(itemRightParen, ctx);

        return labels;
    }

    // aggrExpr parses an aggregation expression.
    //
    //		<aggr_op> (<Vector_expr>) [by|without <labels>]
    //		<aggr_op> [by|without <labels>] (<Vector_expr>)
    //
    private AggregateExpr aggrExpr() {
        String ctx = "aggregation";

        Item agop = next();
        if (!agop.getTyp().isAggregator()) {
            errorf("expected aggregation operator but got %s", agop);
        }
        List<String> grouping = new ArrayList<>();
        boolean without = false;

        boolean modifiersFirst = false;
        ItemType t = peek().getTyp();
        if (t == itemBy || t == itemWithout) {
            if (t == itemWithout) {
                without = true;
            }
            next();
            grouping = labels();
            modifiersFirst = true;
        }

        expect(itemLeftParen, ctx);
        Expr param = null;
        if (agop.getTyp().isAggregatorWithParam()) {
            param = expr();
            expect(itemComma, ctx);
        }
        Expr e = expr();
        expect(itemRightParen, ctx);

        if (!modifiersFirst) {
            t = peek().getTyp();
            if (t == itemBy || t == itemWithout) {
                if (grouping.size() > 0) {
                    errorf("aggregation must only contain one grouping clause");
                }
                if (t == itemWithout) {
                    without = true;
                }
                next();
                grouping = labels();
            }
        }

        return new AggregateExpr(agop.getTyp(), e, param, grouping, without);


    }

    // call parses a function call.

    //	<func_name> '(' [ <arg_expr>, ...] ')'

    Call call(String name) {
        String ctx = "function call";
        Function fn = Function.getFunction(name);
        expect(itemLeftParen, ctx);
        // Might be call without args.
        if (peek().getTyp() == itemRightParen) {
            next(); // Consume.
            return new Call(fn, null);
        }

        Expressions args = new Expressions();
        for (; true;) {
            args.add(expr());
            // Terminate if no more arguments.
            if (peek().getTyp() != itemComma) {
                break;
            }
            next();
        }

        // Call must be closed.
        expect(itemRightParen, ctx);

        return new Call(fn, args);
    }

    // labelSet parses a set of label matchers
    //
    //		'{' [ <labelname> '=' <match_string>, ... ] '}'
    //
    Labels labelSet() {
        Labels labels = new Labels();
        // List<Label> set = new ArrayList<>();
        List<Matcher> matchers = labelMatchers(itemEQL);
        for (Matcher lm : matchers) {
            Label label = new Label(lm.getName(), lm.getValue());
            labels.add(label);
        }
        return labels;
    }

    // labelMatchers parses a set of label matchers.
    //
    //		'{' [ <labelname> <match_op> <match_string>, ... ] '}'
    //
    List<Matcher> labelMatchers(ItemType... operators) {
        String ctx = "label matching";
        List<Matcher> matchers = new ArrayList<>();
        expect(itemLeftBrace, ctx);

        // Check if no matchers are provided.
        if (peek().getTyp() == itemRightBrace) {
            next();
            return matchers;
        }

        for (; true; ) {
            Item label = expect(itemIdentifier, ctx);
            ItemType op = next().getTyp();
            if (!op.isOperator()) {
                errorf("expected label matching operator but got %s", op);
            }
            boolean validOp = false;
            for (ItemType allowedOp : operators) {
                if (op == allowedOp) {
                    validOp = true;
                }
            }
            if (!validOp) {
                errorf("operator must be one of %q, is %q", operators, op);
            }

            String val = unquoteString(expect(itemString, ctx).getVal());

            // Map the item to the respective match type.
            MatchType matchType = null;
            switch (op) {
                case itemEQL:
                    matchType = MatchEqual;
                    break;
                case itemNEQ:
                    matchType = MatchNotEqual;
                    break;
                case itemEQLRegex:
                    matchType = MatchRegexp;
                    break;
                case itemNEQRegex:
                    matchType = MatchNotRegexp;
                    break;
                case itemLiteralOr:
                    matchType = MatchLiteralOr;
                    break;
                case itemNotLiteralOr:
                    matchType = MatchNotLiteralOr;
                    break;
                default:
                    errorf("item %q is not a metric match type", op);
            }

            Matcher m = new Matcher(matchType, label.getVal(), val);
            matchers.add(m);

            if (peek().getTyp() == itemIdentifier) {
                errorf("missing comma before next identifier %q", peek().getVal());
            }

            // Terminate list if last matcher.
            if (peek().getTyp() != itemComma) {
                break;
            }
            next();

            // Allow comma after each item in a multi-line listing.
            if (peek().getTyp() == itemRightBrace) {
                break;
            }
        }

        expect(itemRightBrace, ctx);

        return matchers;
    }

    // metric parses a metric.
    //
    //		<label_set>
    //		<metric_identifier> [<label_set>]
    //
    Labels metric() {
        String name = "";
        Labels m = new Labels();

        ItemType t = peek().getTyp();
        if (t == itemIdentifier || t == itemMetricIdentifier) {
            name = next().getVal();
            t = peek().getTyp();
        }
        if (t != itemLeftBrace && name.equals("")) {
            errorf("missing metric name or metric selector");
        }
        if (t == itemLeftBrace) {
            m = labelSet();
        }
        if (!name.equals("")) {
            Label label = new Label(MetricName, name);
            m.add(label);
            //            sort.Sort(m)
        }
        return m;
    }

    // offset parses an offset modifier.
    //
    //		offset <duration>
    //
    Duration offset() {
        String ctx = "offset";
        next();
        Item offi = expect(itemDuration, ctx);
        Duration offset = parseDuration(offi.getVal());
        return offset;
    }

    // VectorSelector parses a new (instant) vector selector.
    //
    //		<metric_identifier> [<label_matchers>]
    //		[<metric_identifier>] <label_matchers>
    //
    static VectorSelector vectorSelector(Parser p, String name) {
        List<Matcher> matchers = new ArrayList<>();
        // Parse label matching if any.
        Item t = p.peek();
        if (t.getTyp() == itemLeftBrace) {
            matchers = p.labelMatchers(itemEQL, itemNEQ, itemEQLRegex, itemNEQRegex, itemLiteralOr, itemNotLiteralOr);
        }
        // Metric name must not be set in the label matchers and before at the same time.
        if (!name.equals("")) {
            for (Matcher m : matchers) {
                if (m.getName() == MetricName) {
                    p.errorf("metric name must not be set twice: %q or %q", name, m.getValue());
                }
            }
            // Set name label matching.
            Matcher m = new Matcher(MatchEqual, MetricName, name);
            matchers.add(m);
        }

        if (matchers.size() == 0) {
            p.errorf("vector selector must contain label matchers or metric name");
        }
        // A Vector selector must contain at least one non-empty matcher to prevent
        // implicit selection of all metrics (e.g. by a typo).
        boolean notEmpty = false;
        for (Matcher lm : matchers) {
            if (!lm.matches("")) {
                notEmpty = true;
                break;
            }
        }
        if (!notEmpty) {
            p.errorf("vector selector must contain at least one non-empty matcher");
        }

        return new VectorSelector(name, matchers);
    }

    // expectType checks the type of the node and raises an error if it
    // is not of the expected type.
    void expectType(Node node, ValueType want, String context) {
        ValueType t = checkType(node);
        if (t != want) {
            errorf("expected type %s in %s, got %s", typeString(want), context, typeString(t));
        }
    }

    ValueType checkType(Node node) {
        ValueType typ = null;
        //no value type
        if (node instanceof Statements || node instanceof Expressions || node instanceof Statement) {
            typ = ValueType.none;
        } else if (node instanceof Expr) {
            typ = ((Expr) node).type();
        } else {
            errorf("unknown node type: %s", node);
        }

        // Recursively check correct typing for child nodes and raise
        // errors in case of bad typing.
        //Statements
        if (node instanceof Statements) {
            for (Statement s : ((Statements) node).getStmts()) {
                expectType(s, ValueType.none, "statement list");
            }
        } else if (node instanceof EvalStmt) {
            ValueType ty = checkType(((EvalStmt) node).getExpr());
            if (ty == ValueType.none) {
                errorf("evaluation statement must have a valid expression type but got %s",
                    typeString(ty));
            }
        } else if (node instanceof RecordStmt) {

            ValueType ty = checkType(((RecordStmt) node).getExpr());
            if (ty != ValueType.vector && ty != ValueType.scalar) {
                errorf(
                    "record statement must have a valid expression of type instant vector or scalar but got %s",
                    typeString(ty));
            }
        } else if (node instanceof Expressions) {
            for (Expr e : ((Expressions) node).getExpressions()) {
                ValueType ty = checkType(e);
                if (ty == ValueType.none) {
                    errorf("expression must have a valid expression type but got %s",
                        typeString(ty));
                }
            }
        } else if (node instanceof AggregateExpr) {
            AggregateExpr n = ((AggregateExpr) node);
            if (!n.getOp().isAggregator()) {
                errorf("aggregation operator expected in aggregation expression but got %q",
                    ((AggregateExpr) node).getOp());
            }
            expectType(n.getExpr(), ValueType.vector, "aggregation expression");
            if (n.getOp() == itemTopK || n.getOp() == itemBottomK || n.getOp() == itemQuantile) {
                expectType(n.getParam(), ValueType.scalar, "aggregation parameter");
            }
            if (n.getOp() == itemCountValues) {
                expectType(n.getParam(), ValueType.string, "aggregation parameter");
            }
        } else if (node instanceof BinaryExpr) {
            BinaryExpr n = (BinaryExpr) node;
            ValueType lt = checkType(((BinaryExpr) node).getLhs());
            ValueType rt = checkType(((BinaryExpr) node).getRhs());

            if (!n.getOp().isOperator()) {
                errorf("binary expression does not support operator %q", n.getOp());
            }
            if ((lt != ValueType.scalar && lt != ValueType.vector)
                || (rt != ValueType.scalar && rt != ValueType.vector)) {
                errorf("binary expression must contain only scalar and instant vector types");
            }

            if ((lt != ValueType.vector || rt != ValueType.vector) && n.getVectorMatching() != null) {
                if (n.getVectorMatching().getMatchingLabels().size() > 0) {
                    errorf("vector matching only allowed between instant vectors");
                }
                n.setVectorMatching(null);
            } else {
                // Both operands are Vectors.
                if (n.getOp().isSetOperator()) {
                    if ((n.getVectorMatching().getCard() == CardOneToMany)
                        || (n.getVectorMatching().getCard() == CardManyToOne)) {
                        errorf("no grouping allowed for %q operation", n.getOp());
                    }
                    if (n.getVectorMatching().getCard() != CardManyToMany) {
                        errorf("set operations must always be many-to-many");
                    }
                }
            }

            if ((lt == ValueType.scalar || rt == ValueType.scalar) && n.getOp().isSetOperator()) {
                errorf("set operator %q not allowed in binary scalar expression", n.getOp());
            }
        } else if (node instanceof Call) {
            Call n = (Call) node;
            int nargs = n.getFunc().getArgTypes().length;
            if (n.getFunc().getVariadic() == 0) {
                if (nargs != n.getArgs().getExpressions().size()) {
                    errorf("expected %d argument(s) in call to %q, got %d", nargs, n.getFunc()
                        .getName(), n.getArgs().getExpressions().size());
                }
            } else {
                int na = nargs - 1;
                int nargsmax = na + n.getFunc().getVariadic();
                if (na > n.getArgs().getExpressions().size()) {
                    errorf("expected at least %d argument(s) in call to %s, got %d", na, n
                        .getFunc().getName(), n.getArgs().getExpressions().size());
                } else if (n.getFunc().getVariadic() > 0
                           && nargsmax < n.getArgs().getExpressions().size()) {
                    errorf("expected at most %d argument(s) in call to %s, got %d", nargsmax, n
                        .getFunc().getName(), n.getArgs().getExpressions().size());
                }
            }
            int i = 0;
            for (Expr arg : n.getArgs().getExpressions()) {
                if (i > n.getFunc().getArgTypes().length) {
                    i = n.getFunc().getArgTypes().length - 1;
                }
                expectType(arg, n.getFunc().getArgTypes()[i],
                    String.format("call to function %s", n.getFunc().getName()));
                i++;
            }
        } else if (node instanceof ParenExpr) {
            checkType(((ParenExpr) node).getExpr());
        } else if (node instanceof UnaryExpr) {
            UnaryExpr n = (UnaryExpr) node;
            if (n.getOp() != itemADD && n.getOp() != itemSUB) {
                errorf("only + and - operators allowed for unary expressions");
            }
            ValueType t = checkType(n.getExpr());
            if (t != ValueType.scalar && t != ValueType.vector) {
                errorf(
                    "unary expression only allowed on expressions of type scalar or instant vector, got %s",
                    typeString(t));
            }
        } else if (node instanceof NumberLiteral || node instanceof MatrixSelector
                   || node instanceof StringLiteral || node instanceof VectorSelector) {
            NoopUtils.noop();
        } else {
            errorf("unknown node type: %T", node);
        }

        return typ;
    }

    public static String typeString(ValueType t) {
        switch (t) {
            case vector:
                return "instant vector";
            case matrix:
                return "range vector";
            default:
                return t.toString();
        }
    }

    /**
     * 去除头尾双引号(如果有)
     *
     * @param s
     * @return
     */
    private String unquoteString(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    private static final Pattern pattern = Pattern.compile("^([0-9]+)(y|w|d|h|m|s|ms)$");

    /**
     * resolve time expression
     *
     * @param ds
     * @return
     */
    public static Duration parseDuration(String ds) {
        java.util.regex.Matcher matcher = pattern.matcher(ds);
        if (!matcher.matches()) {
            throw new IllegalStateException("illegal duration:" + ds);
        }
        if (ds.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(ds.substring(0, ds.length() - 2)));
        } else if (ds.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(ds.substring(0, ds.length() - 1)));
        } else if (ds.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(ds.substring(0, ds.length() - 1)));
        } else if (ds.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(ds.substring(0, ds.length() - 1)));
        } else if (ds.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(ds.substring(0, ds.length() - 1)));
        } else if (ds.endsWith("w")) {
            return Duration.ofDays(7 * Long.parseLong(ds.substring(0, ds.length() - 1)));
        } else if (ds.endsWith("y")) {
            return Duration.ofDays(365 * Long.parseLong(ds.substring(0, ds.length() - 1)));
        }
        throw new IllegalStateException("illegal duration expr:" + ds);
    }

}
