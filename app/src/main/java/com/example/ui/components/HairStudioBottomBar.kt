package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination

@Composable
fun HairStudioBottomBar(
    currentScreen: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("bottom_nav_bar"),
        containerColor = LuxuryDarkSurface,
        tonalElevation = 8.dp
    ) {
        NavItem(
            label = "Home",
            icon = Icons.Default.Home,
            selected = currentScreen == AppDestination.HOME,
            tag = "nav_home",
            onClick = { onNavigate(AppDestination.HOME) }
        )
        NavItem(
            label = "Gallery",
            icon = Icons.Default.Collections,
            selected = currentScreen == AppDestination.GALLERY,
            tag = "nav_gallery",
            onClick = { onNavigate(AppDestination.GALLERY) }
        )
        NavItem(
            label = "AI Studio",
            icon = Icons.Default.AutoAwesome,
            selected = currentScreen == AppDestination.STUDIO || currentScreen == AppDestination.RESULTS,
            tag = "nav_studio",
            onClick = { onNavigate(AppDestination.STUDIO) }
        )
        NavItem(
            label = "For You",
            icon = Icons.Default.Face,
            selected = currentScreen == AppDestination.RECOMMENDATIONS,
            tag = "nav_recommendations",
            onClick = { onNavigate(AppDestination.RECOMMENDATIONS) }
        )
        NavItem(
            label = "Profile",
            icon = Icons.Default.Person,
            selected = currentScreen == AppDestination.DASHBOARD || currentScreen == AppDestination.PRIVACY,
            tag = "nav_dashboard",
            onClick = { onNavigate(AppDestination.DASHBOARD) }
        )
    }
}

@Composable
private fun RowScope.NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) LuxuryBlack else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) PurpleLight else TextMuted
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = PurplePrimary,
            selectedIconColor = LuxuryBlack,
            unselectedIconColor = TextMuted,
            selectedTextColor = PurpleLight,
            unselectedTextColor = TextMuted
        ),
        modifier = Modifier.testTag(tag)
    )
}
