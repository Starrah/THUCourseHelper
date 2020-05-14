package cn.starrah.thu_course_helper.data.utils

import java.time.LocalTime

fun <T: Comparable<T>> clamp(value: T, min: T, max: T): T {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Int, min: Int, max: Int): Int {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Long, min: Long, max: Long): Long {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Float, min: Float, max: Float): Float {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Double, min: Double, max: Double): Double {
    return if (value < min) min else if (value > max) max else value
}

fun invLerp(value: Int, a: Int, b: Int, allowOverflow: Boolean = false): Double {
    val r = (value - a).toDouble() / (b - a)
    return if (allowOverflow) r else clamp(r, 0.0, 1.0)
}

fun invLerp(value: Long, a: Long, b: Long, allowOverflow: Boolean = false): Double {
    val r = (value - a).toDouble() / (b - a)
    return if (allowOverflow) r else clamp(r, 0.0, 1.0)
}

fun invLerp(value: Float, a: Float, b: Float, allowOverflow: Boolean = false): Double {
    val r = (value - a).toDouble() / (b - a)
    return if (allowOverflow) r else clamp(r, 0.0, 1.0)
}

fun invLerp(value: Double, a: Double, b: Double, allowOverflow: Boolean = false): Double {
    val r = (value - a) / (b - a)
    return if (allowOverflow) r else clamp(r, 0.0, 1.0)
}

/**
 * 对[LocalTime]的逆线性插值算法：返回[value]表示的时间在[a]到[b]之间的位置，（当`value`介于`a`,`b`之间时）介于0~1之间。
 *
 * 例如，若`value`为`a`和`b`的中间值，则返回值为0.5。
 * @param [value] 被逆线性插值的值
 * @param [a] 插值的起点（`value==a`时返回值恰为0）；为null时表示0点。
 * @param [b] 插值的终点（`value==b`时返回值恰为1）；为null时表示24点。
 * @param [allowOverflow] 是否允许返回值超出0~1的范围；若此项为false，则`value<a`时恒返回0、`value>b`时恒返回1。
 * @return 逆差值结果，（通常）介于0~1之间
 */
fun invLerp(value: LocalTime, a: LocalTime? = null, b: LocalTime? = null, allowOverflow: Boolean = false): Float {
    val aa = a?.toSecondOfDay()?:0
    val bb = b?.toSecondOfDay()?:86400
    return invLerp(value.toSecondOfDay(), aa, bb, allowOverflow).toFloat()
}