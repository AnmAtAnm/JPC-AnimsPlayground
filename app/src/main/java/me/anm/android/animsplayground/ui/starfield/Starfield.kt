package me.anm.android.animsplayground.ui.starfield

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import kotlin.random.Random
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import me.anm.math.AABB
import me.anm.math.lerp
import me.anm.math.proximity

const val MAX_DUR = 7f

/**
 * A Canvas that renders an particle system animation of flying through stars.
 *
 * @param modifier Modifiers passed through to the Canvas
 * @param steeringFn Function to provide input to the particle system, as an offset (in local
 *     pixels) from the Canvas center.
 * @param onDrawOverlay Function for additional draw commands at the end of every frame.
 */
@Composable
fun Starfield(
    modifier: Modifier = Modifier,
    steeringFn: () -> Offset = { Offset.Zero },
    onDrawOverlay: (DrawScope.() -> Unit)? = null
) {
    var state: StarfieldState by remember { mutableStateOf(StarfieldState()) }
    var lastFrameNanos: Long by remember { mutableStateOf(System.nanoTime()) }

    Canvas(
        modifier = modifier
            .background(Color.Black)
            .onGloballyPositioned {
                state = state.updateBounds(it)
            }
    ) {
        lastFrameNanos.let { // Update canvas when frame is updated.
            state.stars.forEach {
                if (it.alive) {
                    drawCircle(
                        center = state.cameraToCanvas(it.pos),
                        radius = it.radius,
                        color = it.color
                    )
                }
            }
        }
        if (onDrawOverlay != null) {
            onDrawOverlay()
        }
    }

    // Run canvas animation
    LaunchedEffect(Unit) {
        while (isActive) {
            awaitFrame()
            val now = System.nanoTime()
            val dt = (now - lastFrameNanos) / 1_000_000_000F
            state.update(dt, steeringFn())
            lastFrameNanos = now
        }
    }
}

/**
 * @param windowOffset The most recent offset of this view from the parent window. Used to keep
 *     stars aligned within the window.
 * @param canvasSize The most recent size of the Canvas, used to update cameraScale.
 * @param cameraBounds The bounds of the camera in the particle system's coordinate system.
 * @param particleCount Target number of particles in the simulation.
 * @param explosiveness How quickly the actual paticle count will rise to the desired count, in the
 *     range of (0f, 1f].
 * @param stars The backing data of the star particles.
 */
data class StarfieldState(
    val windowOffset: Offset = Offset.Zero,
    val canvasSize: IntSize = IntSize(0, 0),
    val cameraBounds: AABB = AABB(),
    val particleCount: Int = 200,
    val explosiveness: Float = 1f,
    val stars: List<Star> = emptyList()
) {
    val cameraScale = Offset(
        x = calcNonZeroScale(canvasSize.width.toFloat(), cameraBounds.width),
        y = calcNonZeroScale(canvasSize.height.toFloat(), cameraBounds.height)
    )
}

/**
 * Calculate a ratio for scaling of a / b, with expectation that both are non-zero and finite.
 * Otherwise, return 1 as the scale.
 */
fun calcNonZeroScale(a: Float, b: Float): Float {
    if (a == 0f || !a.isFinite() || b == 0f || !b.isFinite()) {
        return 1f
    } else {
        return a / b
    }
}

/**
 * Creates an new StarfieldState with updated camera bounds for the Canvas layout. Also calls
 * [maybeAddStars] to possibly update the particles, particularly during the first layout.
 *
 * Unimplmeneted: Updating the stars for an updated offset within the window.
 */
fun StarfieldState.updateBounds(layoutCoordinates: LayoutCoordinates): StarfieldState {
    val newWindowOffset = layoutCoordinates.localToWindow(Offset(0f,0f))
    val newCameraBounds: AABB
    if (!cameraBounds.isEmpty() && !canvasSize.isEmpty()) {
        if (canvasSize == layoutCoordinates.size) { // No change
            newCameraBounds = cameraBounds
        } else {
            // TODO: Map old camera bounds to new location, and shift all stars without moving
            //       their screen location
            val halfWidth = layoutCoordinates.size.width/2f
            val halfHeight = layoutCoordinates.size.height/2f
            newCameraBounds = AABB(
                xMin = -halfWidth,
                xMax = halfWidth,
                yMin = -halfHeight,
                yMax = halfHeight
            )
        }
    } else {
        val halfWidth = layoutCoordinates.size.width/2f
        val halfHeight = layoutCoordinates.size.height/2f
        newCameraBounds = AABB(
            xMin = -halfWidth,
            xMax = halfWidth,
            yMin = -halfHeight,
            yMax = halfHeight
        )
    }

    return copy(
        windowOffset = newWindowOffset,
        canvasSize = layoutCoordinates.size,
        cameraBounds = newCameraBounds,
        stars = maybeAddStars(prev = this, cameraBounds = newCameraBounds)
    )
}

/**
 * Return a list of old stars and possibly new stars. New stars are only created when there
 * is space in the camera bounds is not empty and the the previous particle count is less than
 * the desired particle count.
 */
fun maybeAddStars(
    prev: StarfieldState,
    particleCount: Int = prev.particleCount,
    cameraBounds: AABB = prev.cameraBounds,
    explosiveness: Float = prev.explosiveness
): List<Star> {
    val newStarsNeeded = particleCount - prev.stars.size
    if (newStarsNeeded <= 0 || cameraBounds.isEmpty()) {
        return prev.stars
    }

    val newStarCount =
        if (newStarsNeeded == 1)
                1
        else
            (newStarsNeeded * explosiveness).toInt()
    return prev.stars + List(newStarCount) { newStar(cameraBounds) }
}

fun StarfieldState.update(dt: Float, uiSteeringTarget: Offset) {
    // Convert to steering to camera/particle coordinates
    val steeringTarget =
        Offset(
            uiSteeringTarget.x/cameraScale.x,
            uiSteeringTarget.y/cameraScale.y
        )
    stars.forEach { it.update(dt, cameraBounds, steeringTarget) }
}

fun StarfieldState.cameraToCanvas(pos: Offset) =
    Offset(
        x = if (cameraScale.x == 0f) 0f else (pos.x - cameraBounds.xMin)/cameraScale.x,
        y = if (cameraScale.y == 0f) 0f else (pos.y - cameraBounds.yMin)/cameraScale.y
    )

/**
 * Randomize saturation with a bias for high saturation in yellow stars.
 */
fun getSaturationForHue(hue: Float): Float {
    val randSat = Math.pow(Random.nextDouble(), 5.2).toFloat()
    val yellowness = proximity(hue, 60f /* yellow */, 15f /* plus or minus */)

    return lerp(randSat, 1f, yellowness)
}

/**
 * Return a random star color, with bias toward whites and yellows, but also including red, orange,
 * blue and cyan.
 */
fun randomStarColor() : Color {
    // Choose a color from yellow to blue, skipping purple
    val hue = ((Random.nextFloat()* 120f) - 60f)
        .let { if (it >= 0f) it else it + 240 }

    return Color.hsv(
        hue = hue,
        saturation = getSaturationForHue(hue),
        value = 1f
    )
}

private fun IntSize.isEmpty() = width == 0 || height == 0


@Preview(
    "Starfield",
    widthDp = 200,
    heightDp = 240
)
@Composable
fun StarfieldPreview() {
    Starfield(modifier = Modifier.fillMaxSize())
}
