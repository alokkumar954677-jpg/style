package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.HairstyleRepository
import com.example.model.HairTexture
import com.example.model.Hairstyle
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.ColorPickerRow
import com.example.ui.theme.*
import com.example.util.ImageUtils
import com.example.viewmodel.HairStudioViewModel
import java.io.File

@Composable
fun StudioScreen(
    viewModel: HairStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userBitmap by viewModel.userBitmap.collectAsState()
    val isUserCustomPhoto by viewModel.isUserCustomPhoto.collectAsState()
    val faceAnalysis by viewModel.faceAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val selectedHairstyle by viewModel.selectedHairstyle.collectAsState()
    val customPrompt by viewModel.customHairstylePrompt.collectAsState()
    val selectedColor by viewModel.selectedHairColor.collectAsState()
    val selectedTexture by viewModel.selectedHairTexture.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStatus by viewModel.generationStatus.collectAsState()
    val generationError by viewModel.generationError.collectAsState()

    var showCameraDialog by remember { mutableStateOf(false) }

    // 1. Fallback GetContent Launcher (reliable on all Android versions)
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = ImageUtils.decodeUriAndCorrect(context, uri)
            if (bitmap != null) {
                viewModel.setUserBitmap(bitmap, uri)
                Toast.makeText(context, "Photo loaded successfully! / फोटो आ गया!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not decode photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. Modern Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = ImageUtils.decodeUriAndCorrect(context, uri)
            if (bitmap != null) {
                viewModel.setUserBitmap(bitmap, uri)
                Toast.makeText(context, "Photo loaded successfully! / फोटो आ गया!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not decode photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openGalleryPicker: () -> Unit = {
        try {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            try {
                getContentLauncher.launch("image/*")
            } catch (e2: Exception) {
                Toast.makeText(context, "Cannot open gallery: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 3. System Camera Preview Launcher (Direct Bitmap contract - guaranteed to work)
    val systemCameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            viewModel.setUserBitmap(bmp)
            Toast.makeText(context, "Photo captured! / फोटो आ गया!", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. System Camera Full-Res Launcher (Via FileProvider)
    var systemCameraUri by remember { mutableStateOf<Uri?>(null) }
    val systemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = systemCameraUri
        if (success && uri != null) {
            val bmp = ImageUtils.decodeUriAndCorrect(context, uri)
            if (bmp != null) {
                viewModel.setUserBitmap(bmp, uri)
                Toast.makeText(context, "Photo captured! / फोटो आ गया!", Toast.LENGTH_SHORT).show()
            } else {
                systemCameraPreviewLauncher.launch(null)
            }
        }
    }

    val openSystemCamera: () -> Unit = {
        try {
            val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            systemCameraUri = uri
            systemCameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("StudioScreen", "FileProvider camera launch error: ${e.message}", e)
            try {
                systemCameraPreviewLauncher.launch(null)
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not open camera: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 5. Camera Permission Launcher for Live In-App Viewfinder
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraDialog = true
        } else {
            Toast.makeText(context, "Live camera permission denied. You can still use 'Device Camera' or 'Upload Photo'.", Toast.LENGTH_LONG).show()
        }
    }

    val openLiveCamera: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showCameraDialog = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .testTag("studio_screen"),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Try-On Studio",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Transform your hairstyle with facial identity preservation",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurplePrimary.copy(alpha = 0.2f))
                            .border(1.dp, PurplePrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI READY",
                            color = PurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Instructions notice
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, LuxuryCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "For best results, upload a clear, front-facing photo with good lighting and your hairline visible.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Photo Upload Box / Preview Area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                if (userBitmap != null) {
                    // Preview card with controls
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, PurplePrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = LuxuryDarkSurface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            ) {
                                Image(
                                    bitmap = userBitmap!!.asImageBitmap(),
                                    contentDescription = "Active face photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Active photo status pill
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(LuxuryBlack.copy(alpha = 0.85f))
                                        .border(
                                            1.dp,
                                            if (isUserCustomPhoto) SuccessGreen else PurplePrimary,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUserCustomPhoto) Icons.Default.CheckCircle else Icons.Default.Face,
                                            contentDescription = null,
                                            tint = if (isUserCustomPhoto) SuccessGreen else ChampagneGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isUserCustomPhoto) "✓ Your Photo Active / आपका फोटो" else "✨ Demo Model (अपना फोटो लगाएं)",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Remove button at top right
                                IconButton(
                                    onClick = { viewModel.clearUserPhoto() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed.copy(alpha = 0.85f))
                                        .testTag("remove_photo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove photo",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Action buttons below photo
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = openLiveCamera,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("replace_with_live_camera_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PurplePrimary,
                                            contentColor = LuxuryBlack
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Live Camera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = openSystemCamera,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("replace_with_device_camera_button"),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                        border = BorderStroke(1.dp, LuxuryCardBorder),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Phone Camera", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = openGalleryPicker,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("replace_with_gallery_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LuxuryCardSurface,
                                            contentColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Upload, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Upload Picker Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(PurpleGlow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Add Your Face Photo / अपना फोटो लगाएं",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Take a photo or upload from device to generate new hairstyles",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Primary 1: Upload from Device (Most reliable on emulators)
                            Button(
                                onClick = openGalleryPicker,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("upload_from_device_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurplePrimary,
                                    contentColor = LuxuryBlack
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Photo / फोटो अपलोड करें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Primary 2 & 3: Live Camera and Device Camera App
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = openLiveCamera,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("take_selfie_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Live Camera", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = openSystemCamera,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("open_system_camera_app_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = BorderStroke(1.dp, LuxuryCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Phone Camera", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Sample models quick switcher
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Or choose a ready studio face portrait:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨ Maya (Front)",
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuxuryElevated)
                                .border(1.dp, PurplePrimary, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSamplePhoto(R.drawable.img_portrait_model) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                        Text(
                            text = "Alex (Men)",
                            color = PurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuxuryCardSurface)
                                .border(1.dp, LuxuryCardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSamplePhoto(R.drawable.img_hair_men) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                        Text(
                            text = "Elena (Women)",
                            color = PurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuxuryCardSurface)
                                .border(1.dp, LuxuryCardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSamplePhoto(R.drawable.img_hair_women) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice explaining how to get real face photo in cloud emulator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuxuryDarkSurface)
                            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Camera mein photo nahi aa raha? Agar emulator mein camera black hai, toh 'Upload Photo' dabakar apne phone ya computer se apna photo chunein, ya 'Live Camera' / 'Phone Camera' use karein.",
                                color = ChampagneGold,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Face Shape Diagnostic Card (if available)
        if (faceAnalysis != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detected Face Shape:",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = faceAnalysis!!.detectedShape.displayName,
                                    color = ChampagneGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${faceAnalysis!!.confidence}% Match",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = faceAnalysis!!.stylistSummary,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Hairstyle Selection Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Select Hairstyle Preset",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(HairstyleRepository.allHairstyles) { style ->
                        val isSelected = style.id == selectedHairstyle.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PurplePrimary else LuxuryCardSurface)
                                .border(1.dp, if (isSelected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.selectHairstyle(style) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = style.name,
                                color = if (isSelected) LuxuryBlack else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Style Prompt (Optional)
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { viewModel.setCustomHairstylePrompt(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Custom styling notes (optional)", color = TextMuted, fontSize = 12.sp) },
                    placeholder = { Text("e.g. Add textured fringe, messy spikes, or soft curtain flow", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LuxuryCardSurface,
                        unfocusedContainerColor = LuxuryCardSurface,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = LuxuryCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Hair Color Studio Row
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                ColorPickerRow(
                    selectedColor = selectedColor,
                    onColorSelected = { viewModel.selectHairColor(it) }
                )
            }
        }

        // Hair Texture Preference
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Hair Texture",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HairTexture.values().forEach { texture ->
                        val isSelected = texture == selectedTexture
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PurplePrimary.copy(alpha = 0.25f) else LuxuryCardSurface)
                                .border(1.dp, if (isSelected) PurplePrimary else LuxuryCardBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setHairTexture(texture) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = texture.displayName,
                                color = if (isSelected) PurpleLight else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Error message if any
        if (generationError != null) {
            item {
                Text(
                    text = generationError!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }

        // Generate Action Button
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (isGenerating) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, PurplePrimary, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = PurplePrimary,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = generationStatus,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Preserving facial features & rendering 6 realistic cuts...",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.generateHairstylePreviews() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("generate_hairstyles_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = LuxuryBlack
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = userBitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Generate My Hairstyles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    if (showCameraDialog) {
        CameraCaptureDialog(
            onDismiss = { showCameraDialog = false },
            onPhotoCaptured = { bmp ->
                viewModel.setUserBitmap(bmp)
                showCameraDialog = false
                Toast.makeText(context, "Photo loaded successfully! / फोटो आ गया है", Toast.LENGTH_SHORT).show()
            },
            onOpenSystemCamera = {
                showCameraDialog = false
                openSystemCamera()
            },
            onOpenGallery = {
                showCameraDialog = false
                openGalleryPicker()
            }
        )
    }
}

