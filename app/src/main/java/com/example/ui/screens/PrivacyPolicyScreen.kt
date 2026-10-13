package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
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
import com.example.viewmodel.HairStudioViewModel

@Composable
fun PrivacyPolicyScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppDestination.DASHBOARD)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("privacy_policy_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppDestination.DASHBOARD) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LuxuryCardSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Security & Privacy Policy",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            PrivacySectionCard(
                icon = Icons.Default.CameraAlt,
                title = "1. Camera & Device Photo Access",
                body = "StyleMyHair AI requests camera permissions solely when you tap 'Take Selfie'. The camera feed is used only for real-time capture and is never accessed in the background."
            )
        }

        item {
            PrivacySectionCard(
                icon = Icons.Default.Lock,
                title = "2. Ephemeral Face Photo Processing",
                body = "Your photos are processed securely to measure facial proportions, detect hairline geometry, and render hairstyle transformations. We adhere strictly to zero permanent face data storage."
            )
        }

        item {
            PrivacySectionCard(
                icon = Icons.Default.Security,
                title = "3. Facial Identity Preservation Guarantee",
                body = "Our AI engine is designed to maintain your natural facial identity, eyes, nose, lips, and skin tone. We only alter hair contours, volume, and salon color grades."
            )
        }

        item {
            PrivacySectionCard(
                icon = Icons.Default.Delete,
                title = "4. Your Complete Deletion Control",
                body = "You can replace or remove your active photo at any moment in the Studio, and delete saved favorite styles or generation logs with one tap."
            )
        }
    }
}

@Composable
private fun PrivacySectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PurpleGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = body,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
