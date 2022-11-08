package me.anm.android.geom

import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset


fun MutableState<Offset>.set(value: Offset) {
    component2()(value)
}
fun MutableState<Offset>.updateX(x: Float) {
    set(Offset(x, value.y))
}
fun MutableState<Offset>.updateY(y: Float) {
    set(Offset(value.x, y))
}
suspend fun MutableState<Offset>.animateOffsetX(from: Float, to: Float, animationSpec: FloatAnimationSpec) {
    animate(from, to, animationSpec = animationSpec) { x, _ ->
        updateX(x)
    }
}
suspend fun MutableState<Offset>.animateOffsetY(from: Float, to: Float, animationSpec: FloatAnimationSpec) {
    animate(from, to, animationSpec = animationSpec) { y, _ ->
        updateY(y)
    }
}