package com.grex.vyay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.CustomColors
import com.grex.vyay.ui.theme.VyayTheme

@Composable
fun OnboardingScreen(
    onSetupComplete: () -> Unit = {},
    initialUserName: String = "",
    saveUserName: (String) -> Unit = {},
    setNotificationsEnabled: (Boolean) -> Unit = {}
) {
    var userName by remember { mutableStateOf(initialUserName) }
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = CustomColors.backgroundPrimaryBottom,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CustomColors.backgroundPrimaryBottom,
                        CustomColors.backgroundPrimaryTop
                    )
                )
            )
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.dawnbackground),
            contentDescription = "Dawn Background",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = 0.99F }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.1f to Color.Gray,
                            0.2f to Color.Black,
                            0.6f to Color.Gray,
                            0.8f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text ="Let's set up your profile.",
                style = MaterialTheme.typography.headlineSmall,
                color = CustomColors.onPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box{
                Box(modifier = Modifier
                    .padding(top = 8.dp)
                    .width(280.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CustomColors.onPrimaryInactive.copy(alpha = 0.1f))
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = {
                        Text(
                            "Your Name",
                            style = TextStyle(
                                color = CustomColors.onPrimaryInactive,
                                textAlign = TextAlign.Start
                            )
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CustomColors.onPrimary,
                        unfocusedBorderColor = CustomColors.onPrimaryInactive,
                    ),
                    singleLine = true,
                    textStyle = TextStyle(color = CustomColors.onPrimary),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    saveUserName(userName)
                    setNotificationsEnabled(true)
                    onSetupComplete()
                },
                enabled = userName.isNotBlank()
            ) {
                Text("Save and Continue")
            }
        }
    }
}

// Preview composable
@Preview(showBackground = true, name = "Empty Name")
@Composable
fun OnboardingScreenPreviewEmpty() {
    VyayTheme {
        OnboardingScreen()
    }
}

@Preview(showBackground = true, name = "With Name")
@Composable
fun OnboardingScreenPreviewWithName() {
    VyayTheme {
        OnboardingScreen(initialUserName = "Murukku")
    }
}