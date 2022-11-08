package me.anm.math

import kotlin.math.abs
import kotlin.math.min

/**
 * Return a value from `0f` to `1f`, indicating how close [x] is to [target].
 * Distances greater than [cutoff] always return zero.
 */
fun proximity(x: Float, target: Float, cutoff: Float): Float {
    val dist = abs(x - target)
    return 1f - min(dist, cutoff) /cutoff
}

/**
 * Linear interpolation from [a] to [b]. A [fraction] of `0f` will return [a], and a [fraction] of
 * `1f` will return [b], with a continuous range between the two and beyond.
 */
fun lerp(a: Float, b: Float, fraction: Float) =
    a * (1-fraction) + (b * fraction)