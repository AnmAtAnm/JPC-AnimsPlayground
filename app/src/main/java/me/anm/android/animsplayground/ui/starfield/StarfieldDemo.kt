package me.anm.android.animsplayground.ui.starfield

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.math.max
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.anm.android.animsplayground.ui.theme.AnimsPlaygroundTheme
import me.anm.android.geom.animateOffsetX
import me.anm.android.geom.animateOffsetY
import me.anm.android.geom.set

const val TARGET_ALPHA_PRESSED = 0.6f
const val TARGET_ALPHA_TUTORIAL = 1f

const val TARGET_STROKE_PRESSED = 2f
const val TARGET_STROKE_TUTORIAL = 8f

val TwoSecTween = FloatTweenSpec(duration = 2_000)
val QuarterSecTween = FloatTweenSpec(duration = 250)
val SoftSpring = FloatSpringSpec(stiffness = Spring.StiffnessLow)

/** Demo playback state indicators for the view model. */
enum class DemoPhase {
    PENDING, FADE_IN, PAUSE, HORIZONTAL, VERTICAL
}

class StarfieldDemoViewModel(
    demoCompletedOnce_initialValue: Boolean = false,
    lastTouchTimeNanos_initialValue: Long = System.nanoTime()
): androidx.lifecycle.ViewModel() {
    var lastTouchTimeNanos by mutableStateOf(lastTouchTimeNanos_initialValue)
    var demoPhase by mutableStateOf(DemoPhase.PENDING)
    var demoInputWeight = Animatable(0f, Float.VectorConverter)
    var demoCompletedOnce: Boolean by mutableStateOf( demoCompletedOnce_initialValue )

    // Should this be saved outside the Composable?
    val demoUiAlpha = Animatable(0f, Float.VectorConverter)
    val targetAlpha = Animatable(0f, Float.VectorConverter)
    val targetStroke = Animatable(1f, Float.VectorConverter)

    // Delay to allow user input to preempt the demo.
    /** Return true if it is time to start the demo. */
    fun isDemoTime(): Boolean {
        val nowNanos = System.nanoTime()
        val secsSince = (nowNanos - lastTouchTimeNanos) / 1_000_000_000
        // Longer delay if the user has seen the demo at least once.
        val secsDelay = if (demoCompletedOnce) 20f else 6f
        return secsSince > secsDelay
    }

    suspend fun resetDemo() {
        demoPhase = DemoPhase.PENDING
        lastTouchTimeNanos = System.nanoTime()
    }

    suspend fun snapAnimatablesToPhaseStart() {
        when (demoPhase) {
            DemoPhase.PENDING -> {
                // Do not update targetAlpha. Animated by touch code demo is pending.
                demoUiAlpha.snapTo(0f)
                targetStroke.snapTo(TARGET_STROKE_PRESSED)
            }
            DemoPhase.FADE_IN -> {
                demoUiAlpha.snapTo(0f)
                targetAlpha.snapTo(0f)
                targetStroke.snapTo(TARGET_STROKE_TUTORIAL)
            }
            else -> {
                demoUiAlpha.snapTo(1f)
                targetAlpha.snapTo(TARGET_ALPHA_TUTORIAL)
                targetStroke.snapTo(TARGET_STROKE_TUTORIAL)
            }
        }

    }
}

/**
 *  Starts a loop that coordinates the timing phases of
 */
suspend fun CoroutineScope.runDemoLoop(
    viewModel: StarfieldDemoViewModel,
    composeScope: CoroutineScope,
    fakeInput: MutableState<Offset>,
    maxFakeInput: State<Offset>
) {
    // Perform the phases of the demo, looping after back after VERTICAL
    while (isActive) {
        with(viewModel) {
            // Jump to the beginning of the current phase
            when (demoPhase) {
                DemoPhase.PENDING -> {
                    awaitFrame()
                    if (isDemoTime()) {
                        demoInputWeight.snapTo(1f)
                        demoPhase = DemoPhase.FADE_IN
                    }
                }
                DemoPhase.FADE_IN -> {
                    viewModel.snapAnimatablesToPhaseStart()
                    // Simultaneous reveal text and target circle.
                    animate(
                        0f,
                        TARGET_ALPHA_TUTORIAL,
                        animationSpec = TwoSecTween
                    ) { value, _ ->
                        composeScope.launch {
                            demoUiAlpha.snapTo(max(value, demoUiAlpha.value))
                            targetAlpha.snapTo(value)
                        }
                    }
                    demoPhase = DemoPhase.PAUSE
                }
                DemoPhase.PAUSE -> {
                    viewModel.snapAnimatablesToPhaseStart()
                    fakeInput.set(Offset.Zero)
                    delay(1_000L)
                    demoPhase = DemoPhase.HORIZONTAL
                }
                DemoPhase.HORIZONTAL -> {
                    viewModel.snapAnimatablesToPhaseStart()
                    fakeInput.animateOffsetX(
                        from = 0f,
                        to = maxFakeInput.value.x,
                        animationSpec = SoftSpring
                    )
                    demoCompletedOnce = true  //  Has now seen enough of the demo.
                    fakeInput.animateOffsetX(
                        from = maxFakeInput.value.x,
                        to = -maxFakeInput.value.x,
                        animationSpec = SoftSpring
                    )
                    fakeInput.animateOffsetX(
                        from = -maxFakeInput.value.x,
                        to = 0f,
                        animationSpec = SoftSpring
                    )
                    demoPhase = DemoPhase.VERTICAL
                }
                DemoPhase.VERTICAL -> {
                    viewModel.snapAnimatablesToPhaseStart()
                    fakeInput.animateOffsetY(
                        from = 0f,
                        to = -maxFakeInput.value.y,
                        animationSpec = SoftSpring
                    )
                    fakeInput.animateOffsetY(
                        from = -maxFakeInput.value.y,
                        to = maxFakeInput.value.y,
                        animationSpec = SoftSpring
                    )
                    fakeInput.animateOffsetY(
                        from = maxFakeInput.value.y,
                        to = 0f,
                        animationSpec = SoftSpring
                    )
                    demoPhase = DemoPhase.PAUSE // Loop
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarfieldDemo(viewModel: StarfieldDemoViewModel = viewModel()) {
    val overCornerRadius = Dp(8f)
    val overlayModifier = Modifier
        .background(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(overCornerRadius)
        )
        .padding(overCornerRadius)

    AnimsPlaygroundTheme(darkTheme = true) {
        Scaffold() { padding ->
            var boxSize: IntSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { boxSize = it.size }
            ) {
                val scope = rememberCoroutineScope()

                var overlayTop: Float by remember { mutableStateOf(0f) }

                val demoInput = remember { mutableStateOf(Offset.Zero) }
                val demoMaxInput = remember { derivedStateOf {
                    val bottomMargin = boxSize.height - (boxSize.height - overlayTop)
                    Offset(boxSize.width * 0.3f, (boxSize.height * 0.5f) - bottomMargin)
                } }

                var steering by remember { mutableStateOf( Offset.Zero ) }
                val steeringFn = { steering + demoInput.value }
                val onNoSteeringInput = {
                    scope.launch {
                        viewModel.targetAlpha.snapTo(0f)
                        viewModel.targetStroke.snapTo(TARGET_ALPHA_PRESSED)
                        viewModel.demoUiAlpha.snapTo(0f)

                        viewModel.resetDemo()
                    }
                    steering = Offset.Zero
                }

                LaunchedEffect(viewModel.lastTouchTimeNanos) {
                    val startTime = viewModel.lastTouchTimeNanos
                    try {
                        val demo = launch {
                            runDemoLoop(viewModel, scope, demoInput, demoMaxInput)
                        }
                        while (isActive && viewModel.lastTouchTimeNanos == startTime) {
                            awaitFrame()
                        }
                        demo.cancel()
                        viewModel.demoInputWeight.animateTo(
                            targetValue = 0f,
                            animationSpec = QuarterSecTween
                        )
                        demo.join()
                    } finally {
                        demoInput.set(Offset.Zero)
                    }
                }

                Starfield(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput("Starfield") {
                            detectDragGestures(
                                onDrag = { _, dragAmount ->
                                    scope.launch {
                                        viewModel.demoUiAlpha.animateTo(0f)
                                        viewModel.targetStroke.animateTo(TARGET_STROKE_PRESSED)
                                        viewModel.targetAlpha.animateTo(TARGET_ALPHA_PRESSED)

                                        viewModel.resetDemo()
                                    }
                                    steering += dragAmount
                                },
                                onDragEnd = onNoSteeringInput,
                                onDragCancel = onNoSteeringInput
                            )
                        },
                    steeringFn = steeringFn,
                    onDrawOverlay = {
                        drawCircle(
                            color = Color.White,
                            alpha = viewModel.targetAlpha.value,
                            radius = 100f,
                            center = center + steering + (demoInput.value * viewModel.demoInputWeight.value),
                            style = Stroke(width = viewModel.targetStroke.value)
                        )
                    }
                )
                // Align overs at the bottom center of the screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding.calculateBottomPadding() + Dp(50f)),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = overlayModifier
                            .alpha(viewModel.demoUiAlpha.value)
                            .onGloballyPositioned {
                                overlayTop = it.positionInParent().y
                            }
                    ) {
                        Text(text = "Drag to steer")
                    }
                }
            }
        }
    }
}

@Preview(
    "Starfied Demo",
    showSystemUi = true,
)
@Composable
fun StarfieldDemoPreview() {
    AnimsPlaygroundTheme {
        StarfieldDemo()
    }
}