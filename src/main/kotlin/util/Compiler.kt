package util

import java.util.Stack
import kotlin.math.*

enum class TokenType {
    OPENING_BRACKET, CLOSING_BRACKET, CONSTANT, VARIABLE, UNARY_PRE_OPERATION, UNARY_POST_OPERATION, BINARY_OPERATION
}

abstract class Token(private val tokenType: TokenType) {
    fun getType(): TokenType {
        return this.tokenType;
    }
}

class OpeningBracketToken() : Token(TokenType.OPENING_BRACKET);

class ClosingBracketToken() : Token(TokenType.CLOSING_BRACKET);

class ConstantToken(private val value: Double) : Token(TokenType.CONSTANT) {
    fun getValue(): Double {
        return this.value;
    }
}

class VariableToken(private val name: String) : Token(TokenType.VARIABLE) {
    fun getName(): String {
        return this.name;
    }
}

abstract class UnaryOperationToken(private val tokenType: TokenType, private val unaryOperation: (Double) -> Double) : Token(tokenType) {
    fun getUnaryOperation(): (Double) -> Double {
        return this.unaryOperation;
    }
}

class UnaryPreOperationToken(private val unaryOperation: (Double) -> Double) : UnaryOperationToken(TokenType.UNARY_PRE_OPERATION, unaryOperation);

class UnaryPostOperationToken(private val unaryOperation: (Double) -> Double) : UnaryOperationToken(TokenType.UNARY_POST_OPERATION, unaryOperation);

class BinaryOperationToken(private val binaryOperation: (Double, Double) -> Double, private val priority: Int) : Token(TokenType.BINARY_OPERATION) {
    fun getBinaryOperation(): (Double, Double) -> Double {
        return this.binaryOperation;
    }

    fun getPriority(): Int {
        return this.priority;
    }
}



val openingBrackets: Set<String> = setOf("(");
val closingBrackets: Set<String> = setOf(")");
val constants: Map<String, Double> = mapOf(
    "pi" to PI,
    "e" to E,
);
val unaryPreOperations: Map<String, (Double) -> Double> = mapOf(
    "sin" to fun(value: Double): Double = sin(value),
    "cos" to fun(value: Double): Double = cos(value),
    "ln" to fun(value: Double): Double = log(value, E),
);
val unaryPostOperations: Map<String, (Double) -> Double> = mapOf();
val binaryOperations: Map<String, Pair<(Double, Double) -> Double, Int>> = mapOf(
    "+" to Pair(fun(leftValue: Double, rightValue: Double): Double = leftValue + rightValue, 1),
    "-" to Pair(fun(leftValue: Double, rightValue: Double): Double = leftValue - rightValue, 1),
    "*" to Pair(fun(leftValue: Double, rightValue: Double): Double = leftValue * rightValue, 2),
    "/" to Pair(fun(leftValue: Double, rightValue: Double): Double = leftValue / rightValue, 2),
    "^" to Pair(fun(leftValue: Double, rightValue: Double): Double = leftValue.pow(rightValue), 3),
);



fun findConstantToken(str: String, constants: Map<String, Double>): Token? {
    if (str.toDoubleOrNull() != null) return ConstantToken(str.toDouble());
    return constants[str]?.let { ConstantToken(it) };
}

fun findVariableToken(str: String): Token {
    return VariableToken(str);
}

fun findConstantOrVariableToken(str: String, constants: Map<String, Double>): Token {
    return findConstantToken(str, constants) ?: findVariableToken(str);
}



fun findOpeningBracketToken(str: String, openingBrackets: Set<String>): Token? {
    if (openingBrackets.contains(str)) return OpeningBracketToken();
    return null;
}

fun findClosingBracketToken(str: String, closingBrackets: Set<String>): Token? {
    if (closingBrackets.contains(str)) return ClosingBracketToken();
    return null;
}

fun findUnaryPreOperationToken(str: String, unaryPreOperations: Map<String, (Double) -> Double>): Token? {
    return unaryPreOperations[str]?.let { UnaryPreOperationToken(it) };
}

fun findUnaryPostOperationToken(str: String, unaryPostOperations: Map<String, (Double) -> Double>): Token? {
    return unaryPostOperations[str]?.let { UnaryPostOperationToken(it) };
}

fun findBinaryOperationToken(str: String, binaryOperations: Map<String, Pair<(Double, Double) -> Double, Int>>): Token? {
    return binaryOperations[str]?.let { BinaryOperationToken(it.first, it.second) };
}

fun findNotConstantOrVariableToken(
    str: String,
    openingBrackets: Set<String>,
    closingBrackets: Set<String>,
    unaryPreOperations: Map<String, (Double) -> Double>,
    unaryPostOperations: Map<String, (Double) -> Double>,
    binaryOperations: Map<String, Pair<(Double, Double) -> Double, Int>>
): Token? {
    return findOpeningBracketToken(str, openingBrackets) ?:
        findClosingBracketToken(str, closingBrackets) ?:
        findUnaryPreOperationToken(str, unaryPreOperations) ?:
        findUnaryPostOperationToken(str, unaryPostOperations) ?:
        findBinaryOperationToken(str, binaryOperations);
}



fun parseTokens(expression: String,
                constants: Map<String, Double>,
                openingBrackets: Set<String>,
                closingBrackets: Set<String>,
                unaryPreOperations: Map<String, (Double) -> Double>,
                unaryPostOperations: Map<String, (Double) -> Double>,
                binaryOperations: Map<String, Pair<(Double, Double) -> Double, Int>>
): List<Token> {
    val tokens: MutableList<Token> = mutableListOf();
    val buffer: StringBuffer = StringBuffer();
    for (lit: Char in expression) {
        buffer.append(lit);
        for (i in 0..buffer.length) {
            val token: Token? = findNotConstantOrVariableToken(buffer.substring(i), openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations);
            if (token != null) {
                if (i != 0) tokens.add(findConstantOrVariableToken(buffer.substring(0, i), constants));
                token.let { tokens.add(it) };
                buffer.delete(0, buffer.length);
                break;
            }
        }
    }
    if (buffer.isNotEmpty()) tokens.add(findConstantOrVariableToken(buffer.toString(), constants));
    return tokens;
}


fun getVariableNamesFromTokens(tokens: List<Token>): List<String> {
    val names: MutableSet<String> = mutableSetOf();
    for (token: Token in tokens) {
        if (token.getType() == TokenType.VARIABLE) names.add((token as VariableToken).getName());
    }
    return names.sorted().toList();
}



fun applyOperation(operationToken: Token, nodesStack: Stack<Node>) {
    if (operationToken.getType() == TokenType.UNARY_POST_OPERATION) {
        if (nodesStack.isEmpty()) throw IllegalArgumentException();
        nodesStack.push(buildUnaryOperator((operationToken as UnaryPostOperationToken).getUnaryOperation(), nodesStack.pop()));
    }
    else if (operationToken.getType() == TokenType.UNARY_PRE_OPERATION) {
        if (nodesStack.isEmpty()) throw IllegalArgumentException();
        nodesStack.push(buildUnaryOperator((operationToken as UnaryPreOperationToken).getUnaryOperation(), nodesStack.pop()));
    }
    else if (operationToken.getType() == TokenType.BINARY_OPERATION) {
        if (nodesStack.size < 2) throw IllegalArgumentException();
        val rightChild: Node = nodesStack.pop();
        val leftChild: Node = nodesStack.pop();
        nodesStack.push(buildBinaryOperator((operationToken as BinaryOperationToken).getBinaryOperation(), leftChild, rightChild));
    }
    else throw IllegalArgumentException();
}

fun serveToken(token: Token, nodesStack: Stack<Node>, tokensStack: Stack<Token>) {
    if (token.getType() == TokenType.CONSTANT) nodesStack.push(buildConstant((token as ConstantToken).getValue()));
    else if (token.getType() == TokenType.VARIABLE) nodesStack.push(buildVariable((token as VariableToken).getName()));
    else if (token.getType() == TokenType.UNARY_POST_OPERATION) applyOperation(token, nodesStack);
    else if (token.getType() == TokenType.BINARY_OPERATION) {
        while (tokensStack.isNotEmpty()) {
            if (tokensStack.peek().getType() == TokenType.BINARY_OPERATION) {
                if ((tokensStack.peek() as BinaryOperationToken).getPriority() >= (token as BinaryOperationToken).getPriority()) applyOperation(tokensStack.pop(), nodesStack);
                else break;
            }
            else if (tokensStack.peek().getType() == TokenType.UNARY_PRE_OPERATION) applyOperation(tokensStack.pop(), nodesStack);
            else break;
        }
        tokensStack.push(token);
    }
    else if (token.getType() == TokenType.CLOSING_BRACKET) {
        while (tokensStack.isNotEmpty()) {
            val topToken: Token = tokensStack.pop();
            if (topToken.getType() == TokenType.OPENING_BRACKET) break;
            applyOperation(topToken, nodesStack);
        }
    }
    else if (token.getType() in setOf(TokenType.OPENING_BRACKET, TokenType.UNARY_PRE_OPERATION)) tokensStack.push(token);
    else throw IllegalArgumentException();
}

fun buildCompTree(tokens: List<Token>): Node {
    val nodesStack: Stack<Node> = Stack();
    val tokensStack: Stack<Token> = Stack();

    for (token: Token in tokens) {
        serveToken(token, nodesStack, tokensStack);
    }

    while (tokensStack.isNotEmpty()) applyOperation(tokensStack.pop(), nodesStack);

    if (nodesStack.size != 1) throw IllegalArgumentException();
    return nodesStack.pop();
}