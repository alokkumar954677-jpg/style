package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HairColor
import com.example.ui.theme.*

@Composable
fun ColorPickerRow(
    selectedColor: HairColor,
    onColorSelected: (HairColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hair Color Studio",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${selectedColor.displayName} (${selectedColor.toneCategory})",
                color = PurpleLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(HairColor.values()) { color ->
                val isSelected = color == selectedColor
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onColorSelected(color) }
                        .testTag("color_swatch_${color.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(color.swatchColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PurplePrimary else LuxuryCardBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (color == HairColor.PLATINUM_BLONDE || color == HairColor.SILVER_GREY) LuxuryBlack else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = color.displayName.split(" ").last(),
                        color = if (isSelected) PurpleLight else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
