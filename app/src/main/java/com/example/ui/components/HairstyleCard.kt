package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HairGender
import com.example.model.Hairstyle
import com.example.ui.theme.*

@Composable
fun HairstyleCard(
    hairstyle: Hairstyle,
    isSaved: Boolean,
    onTryClick: () -> Unit,
    onSaveToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(18.dp))
            .testTag("hairstyle_card_${hairstyle.id}"),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Image or Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(LuxuryElevated)
            ) {
                if (hairstyle.imageResId != null) {
                    Image(
                        painter = painterResource(id = hairstyle.imageResId),
                        contentDescription = hairstyle.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Modern gradient thumbnail badge
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(LuxuryElevated, LuxuryDarkSurface)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = hairstyle.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Trending tag
                if (hairstyle.isTrending) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurplePrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LuxuryBlack,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TRENDING",
                                color = LuxuryBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Bookmark icon button
                IconButton(
                    onClick = onSaveToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LuxuryBlack.copy(alpha = 0.6f))
                        .testTag("bookmark_${hairstyle.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save hairstyle",
                        tint = if (isSaved) ChampagneGold else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Gender & Length Badges at bottom of image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BadgePill(
                        text = when (hairstyle.gender) {
                            HairGender.MEN -> "Men"
                            HairGender.WOMEN -> "Women"
                            HairGender.UNISEX -> "Unisex"
                        },
                        bgColor = LuxuryBlack.copy(alpha = 0.7f),
                        textColor = TextSecondary
                    )
                    BadgePill(
                        text = hairstyle.length.displayName,
                        bgColor = LuxuryBlack.copy(alpha = 0.7f),
                        textColor = PurpleLight
                    )
                }
            }

            // Body content
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = hairstyle.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${hairstyle.popularityScore}% Match",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = hairstyle.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Best for Face Shapes
                Text(
                    text = "Suits: " + hairstyle.recommendedFaceShapes.joinToString { it.displayName },
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button
                Button(
                    onClick = onTryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("try_look_${hairstyle.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        contentColor = LuxuryBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Try This Look",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, bgColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
