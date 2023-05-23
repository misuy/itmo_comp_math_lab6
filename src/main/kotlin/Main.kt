//^^

import entities.*
import entities.Function
import ui.ChartPanel
import util.*
import java.awt.Color
import java.awt.Dimension
import java.io.FileInputStream
import java.util.*
import javax.swing.JFrame
import kotlin.math.max
import kotlin.math.min

val FRAME_SIZE: Dimension = Dimension(1000, 1000);

fun main(args: Array<String>) {
    val scanner: Scanner = if (args.size == 1) Scanner(FileInputStream(args[0])) else Scanner(System.`in`);

    println("1 -- ввести точки;\n2 -- сгенерировать точки на основе функции;");
    val nodes: EquidistantNodes = when (scanner.nextInt()) {
        1 -> {
            println("Введите количество точек. Затем введите точки в формате: x y");
            readEquidistantNodes(scanner);
        }
        2 -> {
            println("Введите функцию от x. Затем введите интервал и количество точек на нем.");
            scanner.nextLine();
            val nodesFunction: Function = Function(buildCompTree(parseTokens(scanner.nextLine(), constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations)), listOf("x"));
            val nodesSegment: Segment = Segment(scanner.nextDouble(), scanner.nextDouble());
            val nodesCount: Int = scanner.nextInt();
            getEquidistantNodesByFunction(nodesFunction, nodesSegment, nodesCount);
        }
        else -> throw IllegalArgumentException();
    }

    println(getFiniteDifferencesTable(nodes));

    val chartPanel: ChartPanel = ChartPanel();
    val horizontalSegmentSize: Double = nodes.dots.maxOf { it.x } - nodes.dots.minOf { it.x };
    val verticalSegmentSize: Double = nodes.dots.maxOf { it.y } - nodes.dots.minOf { it.y };
    chartPanel.setChartSegments(
        Segment(
            min(nodes.dots.minOf { it.x }, 0.0) - horizontalSegmentSize / 4,
            max(nodes.dots.maxOf { it.x }, 0.0) + horizontalSegmentSize / 4
        ),
        Segment(
            min(nodes.dots.minOf { it.y }, 0.0) - verticalSegmentSize / 4,
            max(nodes.dots.maxOf { it.y }, 0.0) + verticalSegmentSize / 4
        )
    );

    val lagrangePolynomial: Function = buildLagrangePolynomial(nodes);
    chartPanel.addFunction(FunctionGraph(lagrangePolynomial, Color.BLUE));

    val firstGaussPolynomial: Function = buildFirstGaussPolynomial(nodes, 0);
    chartPanel.addFunction(FunctionGraph(lagrangePolynomial, Color.GREEN));

    val secondGaussPolynomial: Function = buildSecondGaussPolynomial(nodes, 0);
    val stirlingPolynomial: Function = buildStirlingPolynomial(nodes);
    chartPanel.addFunction(FunctionGraph(stirlingPolynomial, Color.ORANGE));

    val besselPolynomial: Function? = if (nodes.dots.count() % 2 == 0) buildBesselPolynomial(nodes) else null;
    if (besselPolynomial == null) println("Полином Бесселя не построен, т.к. число узлов нечетно");
    else chartPanel.addFunction(FunctionGraph(besselPolynomial, Color.CYAN));

    chartPanel.addDots(DotsGraph(nodes, Color.RED));

    val frame: JFrame = JFrame("chart");
    frame.add(chartPanel);
    frame.size = FRAME_SIZE;
    frame.isVisible = true;

    val variables: Variables = Variables();

    var flag: Boolean = true;
    while (flag) {
        println();
        println("Введите значение аргумента: ");
        try {
            val x: Double = scanner.nextDouble();
            println("x: $x");
            variables.set("x", x);

            println("Значение полинома Лагранжа: ${lagrangePolynomial.getValue(variables)};");
            println("Значение первого полинома Гаусса: ${firstGaussPolynomial.getValue(variables)};");
            println("Значение второго полинома Гаусса: ${secondGaussPolynomial.getValue(variables)};");
            if (stirlingPolynomial != null) println("Значение полинома Стирлинга: ${stirlingPolynomial.getValue(variables)};");
            if (besselPolynomial != null) println("Значение полинома Бесселя: ${besselPolynomial.getValue(variables)};");
        }
        catch (ex: NoSuchElementException) {
            println("bye-bye");
            flag = false;
        }
    }
}
