package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HairstyleRepository
import com.example.model.HairGender
import com.example.model.HairLength
import com.example.model.HairTexture
import com.example.ui.components.HairstyleCard
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel

@Composable
fun GalleryScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genderFilter by viewModel.genderFilter.collectAsState()
    val lengthFilter by viewModel.lengthFilter.collectAsState()
    val textureFilter by viewModel.textureFilter.collectAsState()
    val savedStyles by viewModel.savedStyles.collectAsState()

    val filteredList = remember(searchQuery, genderFilter, lengthFilter, textureFilter) {
        HairstyleRepository.allHairstyles.filter { style ->
            val matchesQuery = searchQuery.isBlank() ||
                    style.name.contains(searchQuery, ignoreCase = true) ||
                    style.description.contains(searchQuery, ignoreCase = true)
            val matchesGender = genderFilter == null || style.gender == genderFilter || style.gender == HairGender.UNISEX
            val matchesLength = lengthFilter == null || style.length == lengthFilter
            val matchesTexture = textureFilter == null || style.texture == textureFilter
            matchesQuery && matchesGender && matchesLength && matchesTexture
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("gallery_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Hairstyle Gallery",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Explore 28+ professional salon cuts for men & women",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_hairstyle_input"),
                placeholder = { Text("Search by name, fade, crop, bob...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PurplePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuxuryCardSurface,
                    unfocusedContainerColor = LuxuryCardSurface,
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = LuxuryCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Gender Tabs (All, Men, Women)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabButton(
                label = "All Cuts (${HairstyleRepository.allHairstyles.size})",
                selected = genderFilter == null,
                onClick = { viewModel.setGenderFilter(null) },
                modifier = Modifier.weight(1f)
            )
            FilterTabButton(
                label = "Men's",
                selected = genderFilter == HairGender.MEN,
                onClick = { viewModel.setGenderFilter(HairGender.MEN) },
                modifier = Modifier.weight(1f)
            )
            FilterTabButton(
                label = "Women's",
                selected = genderFilter == HairGender.WOMEN,
                onClick = { viewModel.setGenderFilter(HairGender.WOMEN) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Length & Texture Filters Horizontal Scroller
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ChipFilter(
                    label = "All Lengths",
                    selected = lengthFilter == null,
                    onClick = { viewModel.setLengthFilter(null) }
                )
            }
            items(HairLength.values()) { length ->
                ChipFilter(
                    label = length.displayName,
                    selected = lengthFilter == length,
                    onClick = {
                        viewModel.setLengthFilter(if (lengthFilter == length) null else length)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.width(6.dp))
            }
            items(HairTexture.values()) { texture ->
                ChipFilter(
                    label = texture.displayName,
                    selected = textureFilter == texture,
                    onClick = {
                        viewModel.setTextureFilter(if (textureFilter == texture) null else texture)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results count
        Text(
            text = "Showing ${filteredList.size} hairstyles",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )

        // Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredList, key = { it.id }) { style ->
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
}

@Composable
private fun FilterTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PurplePrimary else LuxuryCardSurface)
            .border(1.dp, if (selected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) LuxuryBlack else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ChipFilter(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PurplePrimary.copy(alpha = 0.2f) else LuxuryCardSurface)
            .border(1.dp, if (selected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) PurpleLight else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
