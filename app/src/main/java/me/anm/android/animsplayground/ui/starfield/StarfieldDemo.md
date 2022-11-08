# Starfield Demo

This is an exploration of coordinated animations when developing UIs with Jetpack Compose.  It is composed of three systems that contribute to the animation:

 * Particle simulation, rendered to a Canvas and updated every frame.
 * User input from drag gestures.
 * Fake input from an interruptible demo mode, if there is no user input after some delay.

[![Screenshot of interactive Starfield animation in Android's Jetpack Compose](https://img.youtube.com/vi/ucOoV_310tA/maxresdefault.jpg)](https://youtube.com/shorts/ucOoV_310tA "Interactive Starfield Animation")

Notable implementation details:

 * Starfield' steeringFn controls the vanishing point of the star rendering. It is provided as a function so that it is abstracted from the steering source and can be updated each frame without recomposition.
 * Starfield's onDrawOverlay parameter provides StarfieldDemo a hook to append additional drawing commands into the Canvas frame. This is used to render a indication of steering target, and changes rendering depending on whether the input is from the user (thin and transparent) or from the demo (thick and opaque). The implementation as a function reference minimizes recomposition.
 * The user can interrupt the demo at any point and the target will quickly and smoothly animate back out of demo mode (target becomes thin and text fades away).
 * The demo mode loop keeps track of its state in the ViewModel and can cleanly resume through context changes (e.g., reorient device).