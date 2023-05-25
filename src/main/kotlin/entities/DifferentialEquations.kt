package entities

import kotlin.math.abs
import kotlin.math.pow

fun calcFiniteDifference(equation: DifferentialEquation, nodes: EquidistantNodes, i: Int, power: Int): Double {
    if (power == 0) return equation.f.getValue(Variables().set("x", nodes.dots.get(i).x).set("y", nodes.dots.get(i).y));
    return calcFiniteDifference(equation, nodes, i, power - 1) - calcFiniteDifference(equation, nodes, i - 1, power - 1);
}

fun rungeRule(prevNodes: EquidistantNodes, nodes: EquidistantNodes, accuracyOrder: Int, accuracy: Double): Boolean {
    return abs(prevNodes.dots.last().y - nodes.dots.last().y) / ((2.0).pow(accuracyOrder) - 1) < accuracy;
}

fun checkAccuracy(equation: DifferentialEquation, nodes: EquidistantNodes): Boolean {
    return nodes.dots.maxOf { abs(it.y - equation.exactSolution.getValue(Variables().set("x", it.x))) } < equation.expAccuracy;
}

class DifferentialEquation(val f: Function, val exactSolution: Function, val solvingSegment: Segment, val y0: Double, val expAccuracy: Double);

fun interface DifferentialEquationsSolvingMethod {
    fun solve(equation: DifferentialEquation): EquidistantNodes;
}

fun applyEulerMethod(equation: DifferentialEquation, h: Double): EquidistantNodes {
    val nodes: EquidistantNodes = EquidistantNodes(mutableListOf());
    val variables: Variables = Variables();
    var x: Double = equation.solvingSegment.leftBorder;
    nodes.dots.add(Dot(x, equation.y0));
    x += h;
    while (x <= equation.solvingSegment.rightBorder) {
        variables.set("x", nodes.dots.last().x);
        variables.set("y", nodes.dots.last().y);
        nodes.dots.add(Dot(x, nodes.dots.last().y + h * equation.f.getValue(variables)));
        x += h;
    }
    return nodes;
}

val eulerMethod: DifferentialEquationsSolvingMethod = DifferentialEquationsSolvingMethod { equation: DifferentialEquation ->
    var h: Double = equation.solvingSegment.rightBorder - equation.solvingSegment.leftBorder;
    val prevNodes: EquidistantNodes = EquidistantNodes(mutableListOf());
    val variables: Variables = Variables();
    while (true) {
        val nodes: EquidistantNodes = applyEulerMethod(equation, h);
        if (prevNodes.dots.size != 0) if (rungeRule(prevNodes, nodes, 1, equation.expAccuracy)) break;
        prevNodes.dots = nodes.dots.toMutableList();
        h /= 2;
    }

    prevNodes;
}

fun applyAdvancedEulerMethod(equation: DifferentialEquation, h: Double): EquidistantNodes {
    val nodes: EquidistantNodes = EquidistantNodes(mutableListOf());
    val variables: Variables = Variables();
    var x: Double = equation.solvingSegment.leftBorder;
    nodes.dots.add(Dot(x, equation.y0));
    x += h;
    while (x <= equation.solvingSegment.rightBorder) {
        variables.set("x", nodes.dots.last().x);
        variables.set("y", nodes.dots.last().y);
        val fValue: Double = equation.f.getValue(variables);
        variables.set("x", x);
        variables.set("y", nodes.dots.last().y + h * fValue);
        nodes.dots.add(Dot(x, nodes.dots.last().y + h * (fValue + equation.f.getValue(variables)) / 2));
        x += h;
    }

    return nodes;
}

val advancedEulerMethod: DifferentialEquationsSolvingMethod = DifferentialEquationsSolvingMethod { equation: DifferentialEquation ->
    var h: Double = equation.solvingSegment.rightBorder - equation.solvingSegment.leftBorder;
    val prevNodes: EquidistantNodes = EquidistantNodes(mutableListOf());
    val variables: Variables = Variables();
    while (true) {
        val nodes: EquidistantNodes = applyAdvancedEulerMethod(equation, h);
        if (prevNodes.dots.size != 0) if (rungeRule(prevNodes, nodes, 2, equation.expAccuracy)) break;
        prevNodes.dots = nodes.dots.toMutableList();
        h /= 2;
    }

    prevNodes;
}

fun applyAdamsMethod(equation: DifferentialEquation, h: Double): EquidistantNodes {
    val nodes: EquidistantNodes = EquidistantNodes(mutableListOf());
    val variables: Variables = Variables();
    var x: Double = equation.solvingSegment.leftBorder;
    nodes.dots.add(Dot(x, equation.y0));
    var counter: Int = 1;
    x += h;
    while (x <= equation.solvingSegment.rightBorder) {
        if (counter < 4) {
            variables.set("x", nodes.dots.last().x);
            variables.set("y", nodes.dots.last().y);
            nodes.dots.add(Dot(x, nodes.dots.last().y + h * equation.f.getValue(variables)));
        }
        else {
            nodes.dots.add(Dot(x, nodes.dots.last().y + h * calcFiniteDifference(equation, nodes, counter - 1, 0) + h.pow(2) * calcFiniteDifference(equation, nodes, counter - 1, 1) / 2 + 5 * h.pow(3) * calcFiniteDifference(equation, nodes, counter - 1, 2) / 12 + 3 * h.pow(4) * calcFiniteDifference(equation, nodes, counter - 1, 3) / 8));
        }
        x += h;
        counter++;
    }

    return nodes;
}

val adamsMethod: DifferentialEquationsSolvingMethod = DifferentialEquationsSolvingMethod { equation: DifferentialEquation ->
    var h: Double = equation.solvingSegment.rightBorder - equation.solvingSegment.leftBorder;
    var nodes: EquidistantNodes;
    val variables: Variables = Variables();
    while (true) {
        nodes = applyAdamsMethod(equation, h);
        if (checkAccuracy(equation, nodes)) break;
        h /= 2;
    }

    nodes;
}

val methods: List<DifferentialEquationsSolvingMethod> = listOf(eulerMethod, advancedEulerMethod, adamsMethod);