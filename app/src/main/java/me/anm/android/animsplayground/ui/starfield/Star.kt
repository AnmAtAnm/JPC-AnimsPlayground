package me.anm.android.animsplayground.ui.starfield

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import me.anm.math.AABB
import me.anm.math.lerp
import kotlin.random.Random

data class Star (
    var alive: Boolean = false,
    var initialRadius: Float = 1f,
    var radius: Float = initialRadius,
    var x: Float = 0f,
    var y: Float = 0f,
    var dx: Float = 0f,
    var dy: Float = 0f,
    var dur: Float = 0f,
    var color: Color = randomStarColor()
) {
    val pos get() = Offset(x,y)
    val vel get() = Offset(dx, dy)

    fun update(dt: Float, bounds: AABB, steer: Offset) {
        alive = bounds.contains(x, y) && dur < MAX_DUR
        if (!alive) {
            recycle(bounds)
        } else {
            dur += dt
            radius = initialRadius + (dur / MAX_DUR)
            x += (dx - steer.x) * dt
            y += (dy - steer.y) * dt
        }
    }

    fun recycle(bounds: AABB) {
        alive = true
        dur = 0f
        initialRadius = Random.nextFloat() + 1f

        x = lerp(bounds.xMin, bounds.xMax, Random.nextFloat())
        y = lerp(bounds.yMin, bounds.yMax, Random.nextFloat())
        dx = x - bounds.xCenter
        dy = y - bounds.yCenter
    }
}

fun newStar(bounds: AABB): Star {
    return Star().apply { recycle(bounds) }
}
