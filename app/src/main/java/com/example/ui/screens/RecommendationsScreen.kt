package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HairstyleRepository
import com.example.model.FaceShape
import com.example.ui.components.HairstyleCard
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel

@Composable
fun RecommendationsScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val faceAnalysis by viewModel.faceAnalysis.collectAsState()
    val maintenancePref by viewModel.maintenancePreference.collectAsState()
    val savedStyles by viewModel.savedStyles.collectAsState()

    val currentShape = faceAnalysis?.detectedShape ?: FaceShape.OVAL

    val recommendedStyles = remember(currentShape, maintenancePref) {
        HairstyleRepository.getRecommendedForFace(currentShape)
            .filter { maintenancePref == "All" || it.maintenance.equals(maintenancePref, ignoreCase = true) || maintenancePref.isEmpty() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("recommendations_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Recommended for Your Face",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tailored to your facial geometry & bone structure",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Face Shape Analysis Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, PurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurpleGlow)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Diagnosed Face Shape",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = currentShape.displayName,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${faceAnalysis?.confidence ?: 96}% Confidence",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentShape.description,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Proportions Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuxuryElevated)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricRow(label = "Facial Ratio", value = faceAnalysis?.facialRatio ?: "1 : 1.35 (Golden Ratio)")
                        MetricRow(label = "Forehead", value = faceAnalysis?.foreheadWidth ?: "Proportional")
                        MetricRow(label = "Cheekbones", value = faceAnalysis?.cheekboneWidth ?: "Harmonious Apex")
                        MetricRow(label = "Jawline", value = faceAnalysis?.jawlineStructure ?: "Smooth Taper")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Flattering styling tip
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentShape.flatteringTips,
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Maintenance Level Selector
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preferred Maintenance Level",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Low", "Medium", "High").forEach { level ->
                        val isSelected = maintenancePref == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PurplePrimary else LuxuryCardSurface)
                                .border(1.dp, if (isSelected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setMaintenancePreference(level) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                color = if (isSelected) LuxuryBlack else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Disclaimer Note
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LuxuryCardSurface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Face-shape recommendations are expert guidelines, not absolute rules. Personal vibe and confidence are what matters most!",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Recommended Hairstyles Header
        item {
            Text(
                text = "Top Flattering Styles (${recommendedStyles.size})",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // List of Recommended Hairstyles
        items(recommendedStyles, key = { it.id }) { style ->
            val isSaved = savedStyles.any { it.hairstyleId == style.id }
            HairstyleCard(
                hairstyle = style,
                isSaved = isSaved,
                onTryClick = {
                    viewModel.selectHairstyle(style)
                    viewModel.navigateTo(AppDestination.STUDIO)
                },
                onSaveToggle = {
                    viewModel.toggleSaveHairstyle(style)
                }
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
