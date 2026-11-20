import java.util.HashMap;
import java.util.Map;

public class ScientificCalculatorService {
    private double ans = 0.0;
    private boolean degreeMode = true;
    private final Map<String, Double> variables = new HashMap<>();

    public double evaluate(String expression) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }
        expression = expression.replace('×', '*').replace('÷', '/');
        Parser parser = new Parser(expression);
        double result = parser.parseExpression();
        parser.skipWhitespace();
        if (!parser.isAtEnd()) {
            throw new IllegalArgumentException("Unexpected character: " + parser.peek());
        }
        ans = result;
        return result;
    }

    public double getAns() {
        return ans;
    }

    public boolean isDegreeMode() {
        return degreeMode;
    }

    public void setDegreeMode(boolean degreeMode) {
        this.degreeMode = degreeMode;
    }

    public void toggleDegreeMode() {
        degreeMode = !degreeMode;
    }

    public double getVariable(String name) {
        return variables.getOrDefault(name, 0.0);
    }

    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    private class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        boolean isAtEnd() {
            return pos >= input.length();
        }

        char peek() {
            return input.charAt(pos);
        }

        void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(peek())) {
                pos++;
            }
        }

        double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (isAtEnd()) {
                    break;
                }
                char c = peek();
                if (c == '+') {
                    pos++;
                    value += parseTerm();
                } else if (c == '-') {
                    pos++;
                    value -= parseTerm();
                } else {
                    break;
                }
            }
            return value;
        }

        double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (isAtEnd()) {
                    break;
                }
                char c = peek();
                if (c == '*') {
                    pos++;
                    value *= parseFactor();
                } else if (c == '/') {
                    pos++;
                    double divisor = parseFactor();
                    if (divisor == 0.0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    value /= divisor;
                } else {
                    break;
                }
            }
            return value;
        }

        double parseFactor() {
            return parseUnary();
        }

        double parseUnary() {
            skipWhitespace();
            if (isAtEnd()) {
                throw new IllegalArgumentException("Unexpected end");
            }
            char c = peek();
            if (c == '+') {
                pos++;
                return parseUnary();
            }
            if (c == '-') {
                pos++;
                return -parseUnary();
            }
            return parsePower();
        }

        double parsePower() {
            double value = parsePrimary();
            skipWhitespace();
            if (!isAtEnd() && peek() == '^') {
                pos++;
                double exponent = parseUnary();
                value = Math.pow(value, exponent);
            }
            return value;
        }

        double parsePrimary() {
            skipWhitespace();
            if (isAtEnd()) {
                throw new IllegalArgumentException("Unexpected end");
            }
            char c = peek();
            if (c == '(') {
                pos++;
                double value = parseExpression();
                skipWhitespace();
                if (isAtEnd() || peek() != ')') {
                    throw new IllegalArgumentException("Missing )");
                }
                pos++;
                return value;
            }
            if (c == 'π') {
                pos++;
                return Math.PI;
            }
            if (c == '√') {
                pos++;
                return Math.sqrt(parseUnary());
            }
            if (Character.isDigit(c) || c == '.') {
                return parseNumber();
            }
            if (Character.isLetter(c)) {
                return parseIdentifier();
            }
            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        double parseNumber() {
            int start = pos;
            while (!isAtEnd() && (Character.isDigit(peek()) || peek() == '.')) {
                pos++;
            }
            try {
                return Double.parseDouble(input.substring(start, pos));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number");
            }
        }

        double parseIdentifier() {
            int start = pos;
            while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
                pos++;
            }
            String name = input.substring(start, pos);
            if (name.equals("e")) {
                return Math.E;
            }
            if (name.equals("Ans")) {
                return ans;
            }
            if (name.length() == 1) {
                char var = name.charAt(0);
                if (var >= 'A' && var <= 'Z') {
                    return getVariable(name);
                }
            }
            skipWhitespace();
            if (isAtEnd() || peek() != '(') {
                throw new IllegalArgumentException("Expected ( after " + name);
            }
            pos++;
            double arg = parseExpression();
            skipWhitespace();
            if (isAtEnd() || peek() != ')') {
                throw new IllegalArgumentException("Missing ) after function");
            }
            pos++;
            return applyFunction(name, arg);
        }

        double applyFunction(String name, double arg) {
            switch (name.toLowerCase()) {
                case "sin":
                    return Math.sin(toRadians(arg));
                case "cos":
                    return Math.cos(toRadians(arg));
                case "tan":
                    return Math.tan(toRadians(arg));
                case "asin":
                    return fromRadians(Math.asin(arg));
                case "acos":
                    return fromRadians(Math.acos(arg));
                case "atan":
                    return fromRadians(Math.atan(arg));
                case "log":
                    return Math.log10(arg);
                case "ln":
                    return Math.log(arg);
                case "sqrt":
                    return Math.sqrt(arg);
                case "abs":
                    return Math.abs(arg);
                case "exp":
                    return Math.exp(arg);
                default:
                    throw new IllegalArgumentException("Unknown function: " + name);
            }
        }

        double toRadians(double value) {
            return degreeMode ? Math.toRadians(value) : value;
        }

        double fromRadians(double value) {
            return degreeMode ? Math.toDegrees(value) : value;
        }
    }
}