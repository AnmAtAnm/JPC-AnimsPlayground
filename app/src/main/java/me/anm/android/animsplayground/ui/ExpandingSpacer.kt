package me.anm.android.animsplayground.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RowScope.ExpandingSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(modifier = modifier
        .weight(weight = 1f, fill = true)
    )
}
@Composable
fun ColumnScope.ExpandingSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(modifier = modifier
        .weight(weight = 1f, fill = true)
    )
}