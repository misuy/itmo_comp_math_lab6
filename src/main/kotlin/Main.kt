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
    val chartPanel: ChartPanel = ChartPanel();

    val scanner: Scanner = if (args.size == 1) Scanner(FileInputStream(args[0])) else Scanner(System.`in`);

    println("Введите ОДУ (\ny'=\n{ваша_функция(x, y)}\n):\ny'=");
    val f: Function = Function(buildCompTree(parseTokens(scanner.nextLine(), constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations)), listOf("x", "y"));

    println("Введите точное решение (y={ваша_функция(x)}):\ny=");
    val exactSolution: Function = Function(buildCompTree(parseTokens(scanner.nextLine(), constants, openingBrackets, closingBrackets, unaryPreOperations, unaryPostOperations, binaryOperations)), listOf("x"))

    println("Введите границы интервала дифференцирования (x_start x_end):");
    val segment: Segment = Segment(scanner.nextDouble(), scanner.nextDouble());

    println("Введите начальное условие (y(x_start)={ваше значение}):\ny(x_start)=");
    val y0: Double = scanner.nextDouble();

    println("Решить уравнение или сравнить методы (1 -- решить, 2 -- сравнить):");
    if (scanner.nextInt() == 1) {
        println("Выберите метод решения (\n1 -- метод Эйлера;\n2 -- усовершенствованный метод Эйлера;\n3 -- метод Адамса;\n):");
        val method: DifferentialEquationsSolvingMethod = methods[scanner.nextInt() - 1];

        println("Введите необходимую точность:");
        val expAccuracy: Double = scanner.nextDouble();

        val result: EquidistantNodes = method.solve(DifferentialEquation(f, exactSolution, segment, y0, expAccuracy));

        println("Количество точек: ${result.dots.size}");

        val height: Double = result.dots.maxOf { it.y } - result.dots.minOf { it.y };
        chartPanel.setChartSegments(Segment(segment.leftBorder - (segment.rightBorder - segment.leftBorder) / 4,  segment.rightBorder + (segment.rightBorder - segment.leftBorder) / 4),
            Segment(result.dots.minOf { it.y } - height / 4, result.dots.maxOf { it.y } + height / 4));
        chartPanel.addDots(DotsGraph(result, Color.BLUE));
        chartPanel.addFunction(FunctionGraph(exactSolution, Color.RED));
    }
    else {
        println("Введите шаг:");
        val h: Double = scanner.nextDouble();
        val equation: DifferentialEquation = DifferentialEquation(f, exactSolution, segment, y0, -1.0);
        val eulerResult: EquidistantNodes = applyEulerMethod(equation, h);
        val advancedEulerResult: EquidistantNodes = applyAdvancedEulerMethod(equation, h);
        val adamsResult: EquidistantNodes = applyAdamsMethod(equation, h);

        for (i: Int in 0 until eulerResult.dots.size) {
            println("Точка ${eulerResult.dots[i].x}:\nМетод Эйлера: ${eulerResult.dots[i].y};\nУсовершенствованный метод Эйлера: ${advancedEulerResult.dots[i].y};\nМетод Адамса: ${adamsResult.dots[i].y};\nИстинное значение: ${exactSolution.getValue(Variables().set("x", eulerResult.dots[i].x))}\n-----");
        }

        val horizontalSegment: Segment = Segment(segment.leftBorder, segment.rightBorder);
        val width: Double = horizontalSegment.rightBorder - horizontalSegment.leftBorder;
        val verticalSegment: Segment = Segment(min(min(eulerResult.dots.minOf { it.y }, advancedEulerResult.dots.minOf { it.y }), adamsResult.dots.minOf { it.y }),
                                               max(max(eulerResult.dots.maxOf { it.y }, advancedEulerResult.dots.maxOf { it.y }), adamsResult.dots.maxOf { it.y }));
        val height: Double = verticalSegment.rightBorder - verticalSegment.leftBorder;
        chartPanel.setChartSegments(Segment(horizontalSegment.leftBorder - width / 4, horizontalSegment.rightBorder + width / 4),
            Segment(verticalSegment.leftBorder - height / 4, verticalSegment.rightBorder + height / 4));
        chartPanel.addDots(DotsGraph(eulerResult, Color.RED));
        chartPanel.addDots(DotsGraph(advancedEulerResult, Color.GREEN));
        chartPanel.addDots(DotsGraph(adamsResult, Color.CYAN));
        chartPanel.addFunction(FunctionGraph(equation.exactSolution, Color.BLUE));
    }

    val frame: JFrame = JFrame("chart");
    frame.add(chartPanel);
    frame.size = FRAME_SIZE;
    frame.isVisible = true;
}
