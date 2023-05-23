package util

import entities.Variables
import kotlin.math.*


fun interface Node {
    fun compute(variables: Variables): Double;
}

fun buildConstant(value: Double): Node {
    return Node { value };
}

fun buildVariable(name: String): Node {
    return Node { variables: Variables -> ( variables.get(name) ) };
}

fun buildUnaryOperator(unaryOperation: (Double) -> Double, child: Node): Node {
    return Node { variables: Variables -> ( unaryOperation(child.compute(variables)) ) };
}

fun buildBinaryOperator(binaryOperation: (Double, Double) -> Double, leftChild: Node, rightChild: Node): Node {
    return Node { variables: Variables -> ( binaryOperation(leftChild.compute(variables), rightChild.compute(variables)) ) };
}
