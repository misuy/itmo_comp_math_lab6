package entities

import util.*
import kotlin.math.min

fun factorial(n: Int): Int {
    if (n == 0) return 1;
    return n * factorial(n - 1);
}

fun getFiniteDifference(nodes: EquidistantNodes, idx: Int, power: Int): Double {
    if (idx >= nodes.dots.count()) throw IllegalArgumentException();
    if (power == 0) return nodes.dots[idx].y;
    return getFiniteDifference(nodes, idx + 1, power - 1) - getFiniteDifference(nodes, idx, power - 1);
}

fun getFiniteDifferencesTable(nodes: EquidistantNodes): String {
    return buildString {
        this.append("Таблица конечных разностей:\n");
        this.append("x ");
        for (i: Int in 0 until nodes.dots.size) {
            this.append("d^${i}y ");
        }
        this.append("\n");
        for (i: Int in 0 until nodes.dots.size) {
            this.append("x_$i ");
            for (j: Int in 0 until nodes.dots.size - i) {
                this.append("${getFiniteDifference(nodes, i, j)} ");
            }
            this.append("\n");
        }
    }
}

fun buildLagrangePolynomial(nodes: EquidistantNodes): Function {
    var polynomial: Node = buildConstant(0.0);
    nodes.dots.forEach { it ->
        var numerator: Node = buildConstant(it.y);
        nodes.dots.forEach { it1: Dot ->
            if (it != it1) numerator = buildBinaryOperator(binaryOperations["*"]!!.first, numerator, buildBinaryOperator(binaryOperations["-"]!!.first, buildVariable("x"), buildConstant(it1.x)));
        }
        var denominator: Node = buildConstant(1.0);
        nodes.dots.forEach { it1: Dot ->
            if (it != it1) denominator = buildBinaryOperator(binaryOperations["*"]!!.first, denominator, buildConstant(it.x - it1.x));
        }
        polynomial = buildBinaryOperator(binaryOperations["+"]!!.first, polynomial, buildBinaryOperator(binaryOperations["/"]!!.first, numerator, denominator));
    }
    return Function(polynomial, listOf("x"));
}


fun buildFirstGaussPolynomial(nodes: EquidistantNodes, shift: Int): Function {
    var polynomial: Node = buildConstant(0.0);
    val middle: Int = nodes.dots.count() / 2 + nodes.dots.count() % 2 - 1 + shift;
    val t: Node = buildBinaryOperator(binaryOperations["/"]!!.first, buildBinaryOperator(binaryOperations["-"]!!.first, buildVariable("x"), buildConstant(nodes.dots[middle].x)), buildConstant(nodes.h));
    for (i: Int in 0..2 * min(middle, nodes.dots.count() - middle - 1)) {
        val idx: Int = i / 2;
        var numerator: Node = buildConstant(getFiniteDifference(nodes, middle - idx, i));
        val denominator: Node = buildConstant(factorial(i).toDouble());
        val lower = -idx;
        val higher: Int = idx + i % 2 - 1;
        for (j: Int in lower..higher) {
            numerator = buildBinaryOperator(binaryOperations["*"]!!.first, numerator, buildBinaryOperator(binaryOperations["+"]!!.first, t, buildConstant(j.toDouble())));
        }
        polynomial = buildBinaryOperator(binaryOperations["+"]!!.first, polynomial, buildBinaryOperator(binaryOperations["/"]!!.first, numerator, denominator));
    }
    return Function(polynomial, listOf("x"));
}

fun buildSecondGaussPolynomial(nodes: EquidistantNodes, shift: Int): Function {
    var polynomial: Node = buildConstant(0.0);
    val middle: Int = nodes.dots.count() / 2 + nodes.dots.count() % 2 - 1 + shift;
    val t: Node = buildBinaryOperator(binaryOperations["/"]!!.first, buildBinaryOperator(binaryOperations["-"]!!.first, buildVariable("x"), buildConstant(nodes.dots[middle].x)), buildConstant(nodes.h));
    for (i: Int in 0..2 * min(middle, nodes.dots.count() - middle - 1)) {
        val idx: Int = i / 2 + i % 2;
        var numerator: Node = buildConstant(getFiniteDifference(nodes, middle - idx, i));
        val denominator: Node = buildConstant(factorial(i).toDouble());
        val lower = -idx + 1;
        val higher: Int = idx - i % 2;
        for (j: Int in lower..higher) {
            numerator = buildBinaryOperator(binaryOperations["*"]!!.first, numerator, buildBinaryOperator(binaryOperations["+"]!!.first, t, buildConstant(j.toDouble())));
        }
        polynomial = buildBinaryOperator(binaryOperations["+"]!!.first, polynomial, buildBinaryOperator(binaryOperations["/"]!!.first, numerator, denominator));
    }
    return Function(polynomial, listOf("x"));
}

fun buildStirlingPolynomial(nodes: EquidistantNodes): Function {
    val polynomial: Node = buildBinaryOperator(binaryOperations["/"]!!.first, buildBinaryOperator(binaryOperations["+"]!!.first, buildFirstGaussPolynomial(nodes, 0).compTree, buildSecondGaussPolynomial(nodes, 0).compTree), buildConstant(2.0));
    return Function(polynomial, listOf("x"));
}

fun buildBesselPolynomialDeprecated(nodes: EquidistantNodes): Function {
    var polynomial: Node = buildConstant(0.0);
    val middle: Int = nodes.dots.count() / 2 + nodes.dots.count() % 2 - 1;
    val t: Node = buildBinaryOperator(binaryOperations["/"]!!.first, buildBinaryOperator(binaryOperations["-"]!!.first, buildVariable("x"), buildConstant(nodes.dots[middle].x)), buildConstant(nodes.h));
    for (i: Int in 0..2 * min(middle, nodes.dots.count() - middle - 1)) {
        val idx: Int = i / 2;
        var numerator: Node = if (i % 2 == 1) buildConstant(getFiniteDifference(nodes, middle - idx, i)) else buildConstant((getFiniteDifference(nodes, middle - idx, i) + getFiniteDifference(nodes, middle - idx + 1, i)) / 2);
        val denominator: Node = buildConstant(factorial(i).toDouble());
        val lower = -idx;
        val higher: Int = idx - 1;
        for (j: Int in lower..higher) {
            numerator = buildBinaryOperator(binaryOperations["*"]!!.first, numerator, buildBinaryOperator(binaryOperations["+"]!!.first, t, buildConstant(j.toDouble())));
        }
        if (i % 2 == 1) numerator = buildBinaryOperator(binaryOperations["*"]!!.first, numerator, buildBinaryOperator(binaryOperations["-"]!!.first, t, buildConstant(0.5)));
        polynomial = buildBinaryOperator(binaryOperations["+"]!!.first, polynomial, buildBinaryOperator(binaryOperations["/"]!!.first, numerator, denominator));
    }
    return Function(polynomial, listOf("x"));
}

fun buildBesselPolynomial(nodes: EquidistantNodes): Function {
    val polynomial: Node = buildBinaryOperator(binaryOperations["/"]!!.first, buildBinaryOperator(binaryOperations["+"]!!.first, buildFirstGaussPolynomial(nodes, 0).compTree, buildSecondGaussPolynomial(nodes, 1).compTree), buildConstant(2.0));
    return Function(polynomial, listOf("x"));
}
