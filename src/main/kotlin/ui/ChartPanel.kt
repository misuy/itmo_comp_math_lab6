package ui

import entities.*
import entities.Function
import util.*
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.round


class ChartPanel : JPanel() {
    private lateinit var horizontalSegment: Segment;
    private lateinit var verticalSegment: Segment;

    private val functions: MutableList<FunctionGraph> = mutableListOf();
    private val dots: MutableList<DotsGraph> = mutableListOf();

    init {
        this.addHorizontalAxis();
        this.addVerticalAxis();
    }

    private fun addHorizontalAxis() {
        val expression: String = "x+0*y";
        val tokens: List<Token> = parseTokens(expression, constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations);
        val variableNames: List<String> = getVariableNamesFromTokens(tokens);
        this.functions.add(FunctionGraph(Function(buildCompTree(tokens), variableNames), Color.BLACK));
    }

    private fun addVerticalAxis() {
        val expression: String = "0*x+y";
        val tokens: List<Token> = parseTokens(expression, constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations);
        val variableNames: List<String> = getVariableNamesFromTokens(tokens);
        this.functions.add(FunctionGraph(Function(buildCompTree(tokens), variableNames), Color.BLACK));
    }

    fun setChartSegments(horizontalSegment: Segment, verticalSegment: Segment) {
        this.horizontalSegment = horizontalSegment;
        this.verticalSegment = verticalSegment;
    }

    fun addFunction(function: FunctionGraph) = this.functions.add(function);

    fun addDots(dots: DotsGraph) = this.dots.add(dots);

    override fun paintComponent(g: Graphics?) {
        val graphics: Graphics2D = g as? Graphics2D ?: throw IllegalArgumentException();
        super.paintComponent(graphics);

        val widthPerPixel: Double = (this.horizontalSegment.rightBorder - this.horizontalSegment.leftBorder) / this.width;
        val heightPerPixel: Double = (this.verticalSegment.rightBorder - this.verticalSegment.leftBorder) / this.height;

        this.functions.forEach {
            when (it.function.variableNames.size) {
                1 -> this.plotFunctionByOneVariable(graphics, it, widthPerPixel, heightPerPixel);
                2 -> this.plotFunctionByTwoVariables(graphics, it, widthPerPixel, heightPerPixel);
                else -> throw IllegalArgumentException();
            }
        }

        this.dots.forEach { this.plotDots(graphics, it, widthPerPixel, heightPerPixel); }
    }

    private fun plotFunctionByOneVariable(graphics: Graphics2D, function: FunctionGraph, widthPerPixel: Double, heightPerPixel: Double) {
        graphics.stroke = BasicStroke(3F);
        graphics.color = function.color;
        val variables: Variables = Variables();
        val variableName: String = function.function.variableNames[0];
        for (horizontalPixel: Int in 0 until this.width) {
            variables.set(variableName, horizontalPixelToCoordinate(horizontalPixel, widthPerPixel));
            val value: Double = function.function.getValue(variables);
            for (verticalPixel: Int in 0 until this.height) {
                if (abs(value - verticalPixelToCoordinate(verticalPixel, heightPerPixel)) < heightPerPixel) graphics.drawLine(horizontalPixel, verticalPixel, horizontalPixel, verticalPixel);
            }
        }
    }

    private fun plotFunctionByTwoVariables(graphics: Graphics2D, function: FunctionGraph, widthPerPixel: Double, heightPerPixel: Double) {
        graphics.stroke = BasicStroke(3F);
        graphics.color = function.color;
        val variables: Variables = Variables();
        var prevValue: Double;
        var value: Double
        val horizontalVariableName: String = function.function.variableNames[0];
        val verticalVariableName: String = function.function.variableNames[1];
        for (horizontalPixel: Int in 0 until this.width) {
            variables.set(horizontalVariableName, horizontalPixelToCoordinate(horizontalPixel, widthPerPixel));
            variables.set(verticalVariableName, verticalPixelToCoordinate(0, heightPerPixel));
            value = function.function.getValue(variables);
            for (verticalPixel: Int in 0 until this.height) {
                variables.set(verticalVariableName, verticalPixelToCoordinate(verticalPixel, heightPerPixel));
                prevValue = value;
                value = function.function.getValue(variables);
                if (value * prevValue <= 0) graphics.drawLine(horizontalPixel, verticalPixel, horizontalPixel, verticalPixel);
            }
        }
        for (verticalPixel: Int in 0 until this.height) {
            variables.set(horizontalVariableName, horizontalPixelToCoordinate(0, widthPerPixel));
            variables.set(verticalVariableName, verticalPixelToCoordinate(verticalPixel, heightPerPixel));
            value = function.function.getValue(variables);
            for (horizontalPixel: Int in 0 until this.width) {
                variables.set(horizontalVariableName, horizontalPixelToCoordinate(horizontalPixel, widthPerPixel));
                prevValue = value;
                value = function.function.getValue(variables);
                if (value * prevValue <= 0) graphics.drawLine(horizontalPixel, verticalPixel, horizontalPixel, verticalPixel);
            }
        }
    }


    private fun plotDots(graphics: Graphics2D, dots: DotsGraph, widthPerPixel: Double, heightPerPixel: Double) {
        graphics.stroke = BasicStroke(9F);
        graphics.color = dots.color;
        dots.dots.dots.forEach {
            val horizontalPixel: Int = this.horizontalCoordinateToPixel(it.x, widthPerPixel);
            val verticalPixel: Int = this.verticalCoordinateToPixel(it.y, heightPerPixel);
            graphics.drawLine(horizontalPixel, verticalPixel, horizontalPixel, verticalPixel);
        }
    }

    private fun horizontalPixelToCoordinate(horizontalPixel: Int, widthPerPixel: Double): Double = this.horizontalSegment.leftBorder + horizontalPixel * widthPerPixel;

    private fun horizontalCoordinateToPixel(horizontalCoordinate: Double, widthPerPixel: Double): Int = ((horizontalCoordinate - this.horizontalSegment.leftBorder) / widthPerPixel).toInt();

    private fun verticalPixelToCoordinate(verticalPixel: Int, heightPerPixel: Double): Double = this.verticalSegment.leftBorder + (this.height - verticalPixel) * heightPerPixel;

    private fun verticalCoordinateToPixel(verticalCoordinate: Double, heightPerPixel: Double): Int = (this.height - (verticalCoordinate - this.verticalSegment.leftBorder) / heightPerPixel).toInt();
}

