package com.grex.vyay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.grex.vyay.ui.theme.Copper
import com.grex.vyay.ui.theme.Grey
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import com.grex.vyay.ui.theme.primaryActive

@Composable
fun FooterNavBar(navController: NavController) {
    NavigationBar(
        modifier = Modifier
            .background(backgroundPrimaryBottom)
            .fillMaxWidth()
            .wrapContentHeight()
            .height(56.dp)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                shadowElevation = 20f
            },
        containerColor = backgroundPrimaryTop,
        contentColor = Copper,
        tonalElevation = 0.dp,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            Screen.Home,
            Screen.Reports,
            Screen.Statements,
            Screen.Settings,
        )
//        fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
//            return if (condition) {
//                then(modifier(Modifier))
//            } else {
//                this
//            }
//        }

        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                },
                icon = {
                    if (currentRoute == screen.route) {
                        Icon(
                            imageVector = screen.selectedIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .offset(x = (-1).dp, y = (1).dp)
                                .blur(4.dp),
                            tint = Copper
                        )
                    }
                    Icon(
                        imageVector = if (currentRoute == screen.route) screen.selectedIcon else screen.icon,
                        contentDescription = null
                    )
                },
//                label = { Text(text = screen.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryActive,
                    selectedTextColor = primaryActive,
                    unselectedIconColor = Grey,
                    unselectedTextColor = Grey,
                    indicatorColor = backgroundPrimaryTop
                ),
            )
        }
    }
}