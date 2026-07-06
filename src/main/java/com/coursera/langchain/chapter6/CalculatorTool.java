package com.coursera.langchain.chapter6;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Safe arithmetic expression evaluator exposed as an LLM tool.
 *
 * <p>Mirrors the Python course's calculator tool, which relies on
 * {@code eval()} restricted to the {@code math} module. Rather than reach
 * for a scripting engine (or Java's own {@code eval}-like mechanisms), this
 * is a small hand-written recursive-descent parser that only understands
 * numbers, {@code + - * / ^}, parentheses, and a fixed allow-list of math
 * functions/constants — nothing else can execute, so there is no code
 * injection risk.
 */
public class CalculatorTool {

    @Tool("""
            Evaluate a mathematical expression and return the numeric result. \
            Supports +, -, *, /, ^ (power), parentheses, and functions like \
            sqrt, pow, sin, cos, tan, log, log10, exp, abs, min, max, floor, \
            ceil, round, and the constants pi and e. Example input: '2 ^ 10' \
            or 'sqrt(144)'.""")
    public String calculator(@P("the mathematical expression to evaluate") String expression) {
        try {
            double value = new ExpressionEvaluator(expression).parse();
            return formatNumber(value);
        } catch (Exception e) {
            return "Error evaluating expression: " + e.getMessage();
        }
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    /** Minimal recursive-descent parser/evaluator for arithmetic expressions. */
    private static final class ExpressionEvaluator {
        private final String text;
        private int pos;

        ExpressionEvaluator(String text) {
            this.text = text;
        }

        double parse() {
            double result = parseExpression();
            skipWhitespace();
            if (pos != text.length()) {
                throw new IllegalArgumentException("Unexpected character at position " + pos);
            }
            return result;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (peek('+')) {
                    pos++;
                    value += parseTerm();
                } else if (peek('-')) {
                    pos++;
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseUnary();
            while (true) {
                skipWhitespace();
                if (peek('*')) {
                    pos++;
                    value *= parseUnary();
                } else if (peek('/')) {
                    pos++;
                    value /= parseUnary();
                } else {
                    return value;
                }
            }
        }

        private double parseUnary() {
            skipWhitespace();
            if (peek('-')) {
                pos++;
                return -parseUnary();
            }
            if (peek('+')) {
                pos++;
                return parseUnary();
            }
            return parsePower();
        }

        private double parsePower() {
            double base = parsePrimary();
            skipWhitespace();
            if (peek('^')) {
                pos++;
                return Math.pow(base, parseUnary());
            }
            return base;
        }

        private double parsePrimary() {
            skipWhitespace();
            if (peek('(')) {
                pos++;
                double value = parseExpression();
                skipWhitespace();
                expect(')');
                return value;
            }
            if (Character.isDigit(peek()) || peek() == '.') {
                return parseNumber();
            }
            if (Character.isLetter(peek())) {
                return parseIdentifier();
            }
            throw new IllegalArgumentException("Unexpected character '" + peek() + "' at position " + pos);
        }

        private double parseNumber() {
            int start = pos;
            while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) {
                pos++;
            }
            return Double.parseDouble(text.substring(start, pos));
        }

        private double parseIdentifier() {
            int start = pos;
            while (pos < text.length() && Character.isLetterOrDigit(text.charAt(pos))) {
                pos++;
            }
            String name = text.substring(start, pos).toLowerCase();
            skipWhitespace();
            if (peek('(')) {
                pos++;
                List<Double> args = new ArrayList<>();
                skipWhitespace();
                if (!peek(')')) {
                    args.add(parseExpression());
                    skipWhitespace();
                    while (peek(',')) {
                        pos++;
                        args.add(parseExpression());
                        skipWhitespace();
                    }
                }
                expect(')');
                return applyFunction(name, args);
            }
            return applyConstant(name);
        }

        private double applyFunction(String name, List<Double> args) {
            return switch (name) {
                case "sqrt" -> Math.sqrt(args.get(0));
                case "pow" -> Math.pow(args.get(0), args.get(1));
                case "sin" -> Math.sin(args.get(0));
                case "cos" -> Math.cos(args.get(0));
                case "tan" -> Math.tan(args.get(0));
                case "log" -> Math.log(args.get(0));
                case "log10" -> Math.log10(args.get(0));
                case "exp" -> Math.exp(args.get(0));
                case "abs" -> Math.abs(args.get(0));
                case "min" -> Math.min(args.get(0), args.get(1));
                case "max" -> Math.max(args.get(0), args.get(1));
                case "floor" -> Math.floor(args.get(0));
                case "ceil" -> Math.ceil(args.get(0));
                case "round" -> (double) Math.round(args.get(0));
                default -> throw new IllegalArgumentException("Unknown function: " + name);
            };
        }

        private double applyConstant(String name) {
            return switch (name) {
                case "pi" -> Math.PI;
                case "e" -> Math.E;
                default -> throw new IllegalArgumentException("Unknown identifier: " + name);
            };
        }

        private char peek() {
            return pos < text.length() ? text.charAt(pos) : '\0';
        }

        private boolean peek(char c) {
            return pos < text.length() && text.charAt(pos) == c;
        }

        private void expect(char c) {
            if (!peek(c)) {
                throw new IllegalArgumentException("Expected '" + c + "' at position " + pos);
            }
            pos++;
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }
    }
}
