package entities

import java.awt.Color
import java.util.*
import kotlin.math.abs

val EPS: Double = 0.01;

class Dot(val x: Double, val y: Double);

open class Dots(var dots: MutableList<Dot>);

class EquidistantNodes(dots: MutableList<Dot>) : Dots(dots) {
    val h: Double get() = if (dots.size > 1) dots[1].x - dots[0].x else 0.0;

    init {
        for (i: Int in 0 until (dots.size - 1)) {
            if (abs((dots[i + 1].x - dots[i].x) - this.h) > EPS) throw IllegalArgumentException("Неравномерная сетка");
        }
    }
}

class DotsGraph(val dots: Dots, val color: Color);

fun readDots(scanner: Scanner): Dots {
    val dots: MutableList<Dot> = mutableListOf();
    val dotsCount: Int = scanner.nextInt();
    for (i: Int in 0 until dotsCount) {
        dots.add(Dot(scanner.nextDouble(), scanner.nextDouble()));
    }
    return Dots(dots);
}

fun readEquidistantNodes(scanner: Scanner): EquidistantNodes {
    return EquidistantNodes(readDots(scanner).dots);
}

fun getEquidistantNodesByFunction(function: Function, segment: Segment, dotsCount: Int): EquidistantNodes {
    val step: Double = (segment.rightBorder - segment.leftBorder) / dotsCount;
    val dots: MutableList<Dot> = mutableListOf();
    var x: Double = segment.leftBorder;
    val variables: Variables = Variables();
    while (x < segment.rightBorder) {
        variables.set("x", x);
        dots.add(Dot(x, function.getValue(variables)));
        x += step;
    }
    return EquidistantNodes(dots);
}