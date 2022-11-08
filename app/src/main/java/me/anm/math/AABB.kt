package me.anm.math

import androidx.compose.ui.geometry.Offset

data class AABB (
    val xMin: Float = 0f,
    val xMax: Float = 0f,
    val yMin: Float = 0f,
    val yMax: Float = 0f
) {
    init {
        assert(xMin <= xMax)
        assert(yMin <= yMax)
    }

    val xCenter: Float
        get() = xMin + (xMax-xMin)/2f
    val yCenter: Float
        get() = yMin + (yMax-yMin)/2f
    val width: Float
        get() = xMax - xMin
    val height: Float
        get() = yMax - yMin
    val topLeft get() = Offset(xMin, yMin)
    val topRight get() = Offset(xMax, yMin)
    val bottomLeft get() = Offset(xMin, yMax)
    val bottomRight get() = Offset(xMax, yMax)

    fun contains(x: Float, y: Float) = containsX(x) && containsY(y)
    fun contains(pos: Offset) = containsX(pos.x) && containsY(pos.y)
    fun containsX(x: Float) = x in xMin..xMax
    fun containsY(y: Float) = y in yMin..yMax
    fun isEmpty() = xMin == xMax || yMin == yMax
}

