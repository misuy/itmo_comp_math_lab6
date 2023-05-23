package entities

import util.Node
import java.awt.Color

class Function(val compTree: Node, val variableNames: List<String>) {
    fun getValue(variables: Variables): Double {
        return this.compTree.compute(variables);
    }

    fun getValueByX(x: Double): Double {
        if (!this.variableNames.contains("x")) throw IllegalArgumentException();
        val variables: Variables = Variables();
        variables.set("x", x);
        return this.getValue(variables);
    }

    fun getMinAndMaxValuesOnSegment(segment: Segment, step: Double): Pair<Double, Double> {
        var minValue: Double = Double.POSITIVE_INFINITY;
        var maxValue: Double = Double.NEGATIVE_INFINITY;
        if (this.variableNames.size != 1) throw IllegalArgumentException();

        val variables: Variables = Variables();
        val variableName: String = variableNames[0];
        var position: Double = segment.leftBorder;
        var value: Double;
        while (position <= segment.rightBorder) {
            variables.set(variableName, position);
            value = this.getValue(variables);
            if (value < minValue) minValue = value;
            if (value > maxValue) maxValue = value;
            position += step;
        }

        return Pair(minValue, maxValue);
    }
}

class FunctionGraph(val function: Function, val color: Color);