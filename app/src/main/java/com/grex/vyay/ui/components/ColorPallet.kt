package com.grex.vyay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ColorPallet () {
    Column {
        Row {

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.secondary)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onSecondaryContainer)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.tertiary)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onTertiary)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onTertiaryContainer)
            )
        }
        Row {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.error)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.onError)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.errorContainer)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.onErrorContainer)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.background)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onBackground)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surface)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onSurface)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.outline)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
        Box(modifier = Modifier
            .size(30.dp)
            .background(MaterialTheme.colorScheme.scrim)
        )
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.inverseSurface)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.inverseOnSurface)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.inversePrimary)
            )
        }
        Row {
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceDim)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceBright)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
            Box(modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )

        }
    }
}