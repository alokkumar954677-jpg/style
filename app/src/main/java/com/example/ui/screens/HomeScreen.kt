package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HairstyleRepository
import com.example.model.Hairstyle
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.HairstyleCard
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel

@Composable
fun HomeScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val userBitmap by viewModel.userBitmap.collectAsState()
    val savedStyles by viewModel.savedStyles.collectAsState()
    val trendingStyles = remember { HairstyleRepository.allHairstyles.filter { it.isTrending }.take(6) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // 2. Hero Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Beauty Tech Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleGlow)
                        .border(1.dp, PurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✨ NEXT-GEN VIRTUAL SALON TRY-ON",
                        color = PurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Heading
                Text(
                    text = "Discover Your Perfect Hairstyle with AI",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = "Upload your photo, explore trending hairstyles, and find the look that suits you best — before visiting the salon.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Two Hero Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppDestination.STUDIO) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("try_my_hairstyle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = LuxuryBlack
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Try My Hairstyle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppDestination.GALLERY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("explore_hairstyles_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = PurpleLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Styles",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Hero Visual Transformation Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_hair),
                        contentDescription = "Salon hairstyle makeover transformation",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay with callout
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, LuxuryBlack.copy(alpha = 0.9f)),
                                    startY = 100f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Facial Identity Preservation AI",
                                color = ChampagneGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Realistic haircuts tailored to your bone structure",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Interactive Live Before-and-After Demo Preview
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Interactive Hair Preview",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drag the slider to compare before and after transformation",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val sampleBmp = userBitmap
                val synthesizedBmp = remember(sampleBmp) {
                    sampleBmp?.let {
                        com.example.ai.HairImageSynthesizer.synthesizeHairstyle(
                            originalBitmap = it,
                            hairstyle = HairstyleRepository.allHairstyles.first(),
                            hairColor = com.example.model.HairColor.BURGUNDY,
                            faceShape = com.example.model.FaceShape.OVAL,
                            variationIndex = 0
                        )
                    }
                }

                BeforeAfterSlider(
                    beforeBitmap = sampleBmp,
                    afterBitmap = synthesizedBmp,
                    initialSliderPosition = 0.5f
                )
            }
        }

        // 5. Studio Advantage Metrics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    number = "98%",
                    label = "Face Match",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    number = "28+",
                    label = "Salon Cuts",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    number = "9",
                    label = "Color Tones",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6. Trending Hairstyles Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending This Season",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        color = PurpleLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.GALLERY) }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    items(trendingStyles) { style ->
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
                            },
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }
            }
        }

        // 7. How It Works Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LuxuryDarkSurface)
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "How StyleMyHair AI Works",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                StepRow(number = "1", title = "Upload Face Photo", desc = "Take a clear selfie or select a photo with hairline visible.")
                Spacer(modifier = Modifier.height(12.dp))
                StepRow(number = "2", title = "AI Face Shape Detection", desc = "We detect oval, round, square, oblong, heart, or diamond contours.")
                Spacer(modifier = Modifier.height(12.dp))
                StepRow(number = "3", title = "Realistic Salon Preview", desc = "Explore 6 distinct haircuts and hair colors before cutting.")
            }
        }
    }
}

@Composable
private fun MetricCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                color = PurplePrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StepRow(number: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PurplePrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = LuxuryBlack,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
