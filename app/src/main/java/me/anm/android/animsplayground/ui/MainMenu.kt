package me.anm.android.animsplayground.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import me.anm.android.animsplayground.ui.ExpandingSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(onGotoStarfield: () -> Unit) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .padding(Dp(8f))
            ) {
                Text(
                    text = "Animation Demos",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        MainMenuColumn(
            modifier = Modifier
                .padding(padding)
        ) {
            MenuItemButton(
                text = "Starfield Parallax",
                onClick = onGotoStarfield
            )
        }
    }
}

@Composable
fun MainMenuColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = Dp(12f), vertical = Dp(0f))
            .fillMaxSize()
    ) {
        ExpandingSpacer()
        content()
        ExpandingSpacer()
    }
}

@Composable
fun ColumnScope.MenuItemButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 300,
    heightDp = 340
)
@Composable
fun MainMenuPreview() {
    AnimsPlaygroundTheme {
        MainMenu(onGotoStarfield = {})
    }
}