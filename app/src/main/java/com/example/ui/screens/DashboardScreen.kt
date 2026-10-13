package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HairstyleRepository
import com.example.data.local.GenerationHistoryEntity
import com.example.data.local.SavedStyleEntity
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedStyles by viewModel.savedStyles.collectAsState()
    val historyItems by viewModel.generationHistory.collectAsState()
    val faceAnalysis by viewModel.faceAnalysis.collectAsState()

    var isSignedIn by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = LuxuryBlack,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSignedIn) "Studio Member" else "Guest Stylist",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSignedIn) "member@stylemyhair.ai" else "Local Session (Private)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Face Profile: ${faceAnalysis?.detectedShape?.displayName ?: "Analyzed in Studio"}",
                                color = PurpleLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Google Sign-In or Sign-Out Toggle
                        Button(
                            onClick = {
                                isSignedIn = !isSignedIn
                                val msg = if (isSignedIn) "Signed in with Google" else "Signed out to guest"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignedIn) LuxuryElevated else PurplePrimary,
                                contentColor = if (isSignedIn) TextPrimary else LuxuryBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSignedIn) "Sign Out" else "Sign In",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Privacy Policy Shortcut Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(AppDestination.PRIVACY) }
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxuryDarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Photo Privacy & Zero-Retention Policy",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Photos processed securely on-device with zero permanent storage",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Privacy Policy",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Saved Favorite Hairstyles Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Hairstyles (${savedStyles.size})",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                if (savedStyles.isEmpty()) {
                    Text(
                        text = "Browse Gallery",
                        color = PurpleLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.GALLERY) }
                    )
                }
            }
        }

        if (savedStyles.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.BookmarkBorder,
                    title = "No Saved Styles Yet",
                    subtitle = "Tap the bookmark icon in the gallery to save hairstyles you want to try later."
                )
            }
        } else {
            items(savedStyles, key = { it.hairstyleId }) { saved ->
                SavedStyleItem(
                    item = saved,
                    onTryClick = {
                        val style = HairstyleRepository.getHairstyleById(saved.hairstyleId)
                        if (style != null) {
                            viewModel.selectHairstyle(style)
                        }
                        viewModel.navigateTo(AppDestination.STUDIO)
                    },
                    onRemove = {
                        val style = HairstyleRepository.getHairstyleById(saved.hairstyleId)
                        if (style != null) {
                            viewModel.toggleSaveHairstyle(style)
                        }
                    }
                )
            }
        }

        // Generated Transformations History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generation History (${historyItems.size})",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                if (historyItems.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.clearAllHistory()
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (historyItems.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.History,
                    title = "No Generated Looks Yet",
                    subtitle = "Upload a photo in the AI Studio to generate your first hair transformation preview."
                )
            }
        } else {
            items(historyItems, key = { it.id }) { history ->
                HistoryItemCard(
                    item = history,
                    onDelete = {
                        viewModel.deleteHistoryItem(history.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun SavedStyleItem(
    item: SavedStyleEntity,
    onTryClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.gender} • ${item.length} length",
                    color = PurpleLight,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onTryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        contentColor = LuxuryBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Try", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: GenerationHistoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(item.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.hairstyleName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.matchPercentage}%",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Shade: ${item.hairColor} • Face: ${item.faceShape}",
                    color = PurpleLight,
                    fontSize = 12.sp
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }
}
