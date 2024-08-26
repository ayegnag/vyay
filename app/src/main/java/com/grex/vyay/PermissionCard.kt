package com.grex.vyay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grex.vyay.ui.theme.CustomColors


@Composable
fun PermissionCard(
    permissionText: String,
    declinedPermissionText: String,
    isPermanentlyDeclined: Boolean,
    onOkClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            colors = CardDefaults.cardColors(
                containerColor = CustomColors.surface
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(all = 24.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "SMS Permission Required",
                style = MaterialTheme.typography.titleMedium,
                color = CustomColors.onPrimaryInactive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, bottom = 16.dp),
                textAlign = TextAlign.Start,
            )
            Text(
                text = if (isPermanentlyDeclined) {
                    permissionText
                } else {
                    declinedPermissionText
                },
                color = Color.LightGray,
                modifier = Modifier
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                textAlign = TextAlign.Left
            )
            Divider(
                color = CustomColors.onPrimaryInactive,
                modifier = Modifier
                    .alpha(0.4f)
                    .padding(bottom = 6.dp, start = 20.dp, end = 20.dp),
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { onOkClick() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                ) {
                    Text(
                        text = if (isPermanentlyDeclined) {
                            "OK"
                        } else {
                            "Grant permission"
                        },
                        color = CustomColors.onPrimary
                    )
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun PermissionCard() {
//    val isPermanentlyDeclined = false
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(primaryColor)
//            .padding(bottom = 32.dp),
//        contentAlignment = Alignment.Center
//    ) {
//
//        Card(
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0XFF151222),
//            ),
//            modifier = Modifier
//                .fillMaxWidth(0.9f)
//                .padding(all = 24.dp)
//                .align(Alignment.BottomCenter)
//        ) {
//            Text(
//                text = "SMS Permission Required",
//                style = MaterialTheme.typography.titleMedium,
//                color = CustomColors.onPrimaryInactive,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 20.dp, top = 16.dp, bottom = 16.dp),
//                textAlign = TextAlign.Start,
//
//                )
//            Text(
//                text = "This application needs access to your SMS to generate " +
//                        "expense reports. You data remains in your Phone.",
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.LightGray,
//                modifier = Modifier
//                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
//                textAlign = TextAlign.Left
//            )
//            HorizontalDivider(
//                color = CustomColors.onPrimaryInactive,
//                modifier = Modifier
//                    .alpha(0.4f)
//                    .padding(bottom = 6.dp, start = 20.dp, end = 20.dp),
//            )
//            Box(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                TextButton(
//                    onClick = { },
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 16.dp),
//                ) {
//                    Text(
//                        text = if (isPermanentlyDeclined) {
//                            "Grant permission"
//                        } else {
//                            "OK"
//                        },
//                        color = Purple80
//                    )
//                }
//            }
//        }
//    }
//}