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

    println("Выберите метод решения (\n1 -- метод Эйлера;\n2 -- усовершенствованный метод Эйлера;\n3 -- метод Адамса;\n):");
    val method: DifferentialEquationsSolvingMethod = methods[scanner.nextInt() - 1];

    println("Введите ОДУ (\ny'=\n{ваша_функция(x, y)}\n):\ny'=");
    scanner.nextLine();
    val f: Function = Function(buildCompTree(parseTokens(scanner.nextLine(), constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations)), listOf("x", "y"));

    println("Введите границы интервала дифференцирования (x_start x_end):");
    val segment: Segment = Segment(scanner.nextDouble(), scanner.nextDouble());

    println("Введите начальное условие (y(x_start)={ваше значение}):\ny(x_start)=");
    val y0: Double = scanner.nextDouble();

    println("Введите необходимую точность:");
    val expAccuracy: Double = scanner.nextDouble();

    println("Введите точное решение (y={ваша_функция(x)}):\ny=");
    scanner.nextLine();
    val exactSolution: Function = Function(buildCompTree(parseTokens(scanner.nextLine(), constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations)), listOf("x"));

    val equation: DifferentialEquation = DifferentialEquation(f, exactSolution, segment, y0, expAccuracy);

    val result: EquidistantNodes = method.solve(equation);

    println("Количество точек: ${result.dots.size}");

    val chartPanel: ChartPanel = ChartPanel();
    val height: Double = result.dots.maxOf { it.y } - result.dots.minOf { it.y };
    chartPanel.setChartSegments(Segment(segment.leftBorder - (segment.rightBorder - segment.leftBorder) / 4,  segment.rightBorder + (segment.rightBorder - segment.leftBorder) / 4),
        Segment(result.dots.minOf { it.y } - height / 4, result.dots.maxOf { it.y } + height / 4));
    chartPanel.addDots(DotsGraph(result, Color.BLUE));
    chartPanel.addFunction(FunctionGraph(exactSolution, Color.RED));

    val frame: JFrame = JFrame("chart");
    frame.add(chartPanel);
    frame.size = FRAME_SIZE;
    frame.isVisible = true;
}
