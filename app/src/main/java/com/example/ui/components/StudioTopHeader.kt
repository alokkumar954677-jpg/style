package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination

@Composable
fun StudioTopHeader(
    currentScreen: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuxuryDarkSurface)
            .border(1.dp, LuxuryCardBorder)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("studio_top_header")
    ) {
        // Logo & Quick Login Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(AppDestination.HOME) }
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "StyleMyHair Logo",
                        tint = LuxuryBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "StyleMyHair AI",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "AI Hairstyle Studio",
                        color = PurpleLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Login / Profile Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(LuxuryElevated)
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(AppDestination.DASHBOARD) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Login",
                        tint = PurpleLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Login",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Links: Home, Hairstyle Gallery, AI Try-On, My Favorites
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                HeaderNavLink(
                    label = "Home",
                    isSelected = currentScreen == AppDestination.HOME,
                    onClick = { onNavigate(AppDestination.HOME) }
                )
            }
            item {
                HeaderNavLink(
                    label = "Hairstyle Gallery",
                    isSelected = currentScreen == AppDestination.GALLERY,
                    onClick = { onNavigate(AppDestination.GALLERY) }
                )
            }
            item {
                HeaderNavLink(
                    label = "AI Try-On",
                    isSelected = currentScreen == AppDestination.STUDIO || currentScreen == AppDestination.RESULTS,
                    onClick = { onNavigate(AppDestination.STUDIO) },
                    hasSparkle = true
                )
            }
            item {
                HeaderNavLink(
                    label = "My Favorites",
                    isSelected = currentScreen == AppDestination.DASHBOARD,
                    onClick = { onNavigate(AppDestination.DASHBOARD) }
                )
            }
        }
    }
}

@Composable
private fun HeaderNavLink(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    hasSparkle: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PurplePrimary else LuxuryCardSurface)
            .border(1.dp, if (isSelected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasSparkle) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isSelected) LuxuryBlack else PurpleLight,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (isSelected) LuxuryBlack else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
