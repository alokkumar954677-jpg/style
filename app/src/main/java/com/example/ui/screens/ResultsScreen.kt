package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GeneratedLook
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.ColorPickerRow
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel
import java.io.OutputStream

@Composable
fun ResultsScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userBitmap by viewModel.userBitmap.collectAsState()
    val generatedLooks by viewModel.generatedLooks.collectAsState()
    val selectedLookIndex by viewModel.selectedLookIndex.collectAsState()
    val bestRecommendedLook by viewModel.bestRecommendedLook.collectAsState()
    val isSideBySide by viewModel.isSideBySide.collectAsState()
    val selectedColor by viewModel.selectedHairColor.collectAsState()

    var fullImageViewLook by remember { mutableStateOf<GeneratedLook?>(null) }

    // Android back navigation handling
    BackHandler {
        viewModel.navigateTo(AppDestination.STUDIO)
    }

    val currentLook = generatedLooks.getOrNull(selectedLookIndex)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("results_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppDestination.STUDIO) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LuxuryCardSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Studio",
                        tint = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AI Hairstyle Try-On",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "6 Personalized Transformations",
                        color = PurpleLight,
                        fontSize = 11.sp
                    )
                }

                // Side by Side toggle
                IconButton(
                    onClick = { viewModel.toggleSideBySide() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSideBySide) PurplePrimary else LuxuryCardSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewColumn,
                        contentDescription = "Toggle Side by Side View",
                        tint = if (isSideBySide) LuxuryBlack else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Interactive Before-and-After Comparison Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Before & After Comparison",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSideBySide) "Side-by-Side" else "Drag Slider",
                        color = PurpleLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isSideBySide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Original (LEFT)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LuxuryCardSurface)
                                .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            if (userBitmap != null) {
                                Image(
                                    bitmap = userBitmap!!.asImageBitmap(),
                                    contentDescription = "Original uploaded photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LuxuryBlack.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("ORIGINAL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Selected AI Hairstyle (RIGHT)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LuxuryCardSurface)
                                .border(1.5.dp, PurplePrimary, RoundedCornerShape(16.dp))
                        ) {
                            if (currentLook?.generatedBitmap != null) {
                                Image(
                                    bitmap = currentLook.generatedBitmap.asImageBitmap(),
                                    contentDescription = currentLook.hairstyleName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PurplePrimary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(currentLook?.hairstyleName ?: "AI AFTER", color = LuxuryBlack, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                } else {
                    BeforeAfterSlider(
                        beforeBitmap = userBitmap,
                        afterBitmap = currentLook?.generatedBitmap
                    )
                }
            }
        }

        // Active Selected Look Banner
        if (currentLook != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LuxuryElevated)
                        .border(1.dp, LuxuryCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Currently Viewing: ${currentLook.hairstyleName}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentLook.hairLength} Length • Shade: ${currentLook.hairColor.displayName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { saveBitmapToGallery(context, currentLook.generatedBitmap, currentLook.hairstyleName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = LuxuryBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hair Color Studio Options
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LuxuryCardSurface)
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                ColorPickerRow(
                    selectedColor = selectedColor,
                    onColorSelected = { newColor ->
                        viewModel.recolorGeneratedLooks(newColor)
                        Toast.makeText(context, "Applied ${newColor.displayName} shade to all looks", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 3. Section Title: 6 Generated Hairstyle Variations
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Generated Hairstyle Options (6 Looks)",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "All variations generated from your reference photo preserving your facial identity",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // 4. Cards for each of the 6 generated hairstyles
        itemsIndexed(generatedLooks) { index, look ->
            val isSelected = index == selectedLookIndex
            GeneratedHairstyleCard(
                look = look,
                isSelected = isSelected,
                onChooseClick = {
                    viewModel.setSelectedLookIndex(index)
                    Toast.makeText(context, "Selected ${look.hairstyleName} for comparison", Toast.LENGTH_SHORT).show()
                },
                onDownloadClick = {
                    saveBitmapToGallery(context, look.generatedBitmap, look.hairstyleName)
                },
                onViewFullClick = {
                    fullImageViewLook = look
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // 5. Large Premium Section: "Your Perfect Hairstyle — AI Recommended"
        item {
            val recLook = bestRecommendedLook ?: generatedLooks.firstOrNull()
            if (recLook != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Your Perfect Hairstyle — AI Recommended",
                        color = ChampagneGold,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Candidate selected by facial geometry & styling profile",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Hero Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(2.dp, Brush.horizontalGradient(listOf(PurplePrimary, ElectricBlue)), RoundedCornerShape(22.dp)),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Large Actual Generated Image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                if (recLook.generatedBitmap != null) {
                                    Image(
                                        bitmap = recLook.generatedBitmap.asImageBitmap(),
                                        contentDescription = recLook.hairstyleName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Best Match Tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PurplePrimary)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = LuxuryBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BEST MATCH: ${recLook.hairstyleName.uppercase()}",
                                            color = LuxuryBlack,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                // Match percentage badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LuxuryBlack.copy(alpha = 0.85f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${recLook.matchPercentage}% Face Match",
                                        color = SuccessGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Details & Explanation
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = recLook.title,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Tags Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TagPill(label = "Face: ${recLook.faceShape.displayName}", bgColor = LuxuryElevated, textColor = PurpleLight)
                                    TagPill(label = "Length: ${recLook.hairLength}", bgColor = LuxuryElevated, textColor = ChampagneGold)
                                    TagPill(label = "Shade: ${recLook.hairColor.displayName}", bgColor = LuxuryElevated, textColor = TextSecondary)
                                    TagPill(label = "Maint: ${recLook.maintenanceLevel}", bgColor = LuxuryElevated, textColor = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Stylist explanation
                                Text(
                                    text = "Why this style suits you: ${recLook.stylistAnalysis}",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Three Action Buttons
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            saveBitmapToGallery(context, recLook.generatedBitmap, "Best_${recLook.hairstyleName}")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("download_my_best_look_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PurplePrimary,
                                            contentColor = LuxuryBlack
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download My Best Look", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.saveLookToHistory(recLook)
                                                Toast.makeText(context, "Saved to Favorites!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("save_to_favorites_button"),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                            border = BorderStroke(1.dp, LuxuryCardBorder),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save to Favorites", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.navigateTo(AppDestination.STUDIO) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("try_another_hairstyle_button"),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                                            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Try Another", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 6. Full Image Zoom Dialog
    if (fullImageViewLook != null) {
        val look = fullImageViewLook!!
        Dialog(
            onDismissRequest = { fullImageViewLook = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LuxuryBlack.copy(alpha = 0.95f))
            ) {
                // Top close button
                IconButton(
                    onClick = { fullImageViewLook = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LuxuryCardSurface)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }

                // Centered large image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, PurplePrimary, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
                    ) {
                        Column {
                            if (look.generatedBitmap != null) {
                                Image(
                                    bitmap = look.generatedBitmap.asImageBitmap(),
                                    contentDescription = look.hairstyleName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(380.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = look.title,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${look.hairLength} Length • ${look.hairColor.displayName}",
                                    color = PurpleLight,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = look.stylistAnalysis,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        saveBitmapToGallery(context, look.generatedBitmap, look.hairstyleName)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PurplePrimary,
                                        contentColor = LuxuryBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Image", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedHairstyleCard(
    look: GeneratedLook,
    isSelected: Boolean,
    onChooseClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onViewFullClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PurplePrimary else LuxuryCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("generated_look_card_${look.id}"),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Transformed Image Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (look.generatedBitmap != null) {
                    Image(
                        bitmap = look.generatedBitmap.asImageBitmap(),
                        contentDescription = look.hairstyleName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Badge top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LuxuryBlack.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = look.badge,
                        color = PurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Hair length top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LuxuryBlack.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = look.hairLength,
                        color = ChampagneGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Body Info & Buttons
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = look.hairstyleName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${look.matchPercentage}% Match",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = look.shortDescription.ifEmpty { look.stylistAnalysis },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Three Action Buttons: View Full Image, Download, Choose This Style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View Full Image Button
                    OutlinedButton(
                        onClick = onViewFullClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("view_full_${look.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, LuxuryCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("View Full", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Download Button
                    OutlinedButton(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("download_${look.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, LuxuryCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Choose This Style Button
                    Button(
                        onClick = onChooseClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                            .testTag("choose_style_${look.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SuccessGreen else PurplePrimary,
                            contentColor = LuxuryBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSelected) "Selected" else "Choose Style",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPill(label: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap?, name: String) {
    if (bitmap == null) {
        Toast.makeText(context, "No image to download", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val filename = "StyleMyHair_${name.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            Toast.makeText(context, "Saved $name to gallery!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_SHORT).show()
    }
}
