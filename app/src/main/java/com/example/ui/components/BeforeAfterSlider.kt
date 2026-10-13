package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap?,
    afterBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    initialSliderPosition: Float = 0.5f,
    onPositionChanged: ((Float) -> Unit)? = null
) {
    var sliderPosition by remember(initialSliderPosition) { mutableFloatStateOf(initialSliderPosition) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LuxuryCardSurface)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newPos = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                    sliderPosition = newPos
                    onPositionChanged?.invoke(newPos)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newPos = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    sliderPosition = newPos
                    onPositionChanged?.invoke(newPos)
                }
            }
            .testTag("before_after_slider")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val splitX = widthPx * sliderPosition

        val beforeImage = remember(beforeBitmap) { beforeBitmap?.asImageBitmap() }
        val afterImage = remember(afterBitmap) { afterBitmap?.asImageBitmap() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw After image (Right side / Full background)
            if (afterImage != null) {
                drawImage(
                    image = afterImage,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )
            }

            // Clip & Draw Before image (Left side up to splitX)
            if (beforeImage != null) {
                clipRect(
                    left = 0f,
                    top = 0f,
                    right = splitX,
                    bottom = size.height
                ) {
                    drawImage(
                        image = beforeImage,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            }

            // Divider vertical line with purple gradient glow
            drawLine(
                color = PurplePrimary,
                start = Offset(splitX, 0f),
                end = Offset(splitX, size.height),
                strokeWidth = 4.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(splitX, 0f),
                end = Offset(splitX, size.height),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Draggable floating circular handle
        Box(
            modifier = Modifier
                .offset { IntOffset((splitX - 22.dp.toPx()).toInt(), (heightPx / 2 - 22.dp.toPx()).toInt()) }
                .size(44.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(PurplePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Slide to compare before and after",
                tint = LuxuryBlack,
                modifier = Modifier.size(26.dp)
            )
        }

        // Before Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LuxuryBlack.copy(alpha = 0.75f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "BEFORE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // After Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurplePrimary.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "AI AFTER",
                color = LuxuryBlack,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}
