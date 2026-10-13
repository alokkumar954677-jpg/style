package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.*
import com.example.util.ImageUtils
import java.io.File

@Composable
fun CameraCaptureDialog(
    onDismiss: () -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit,
    onOpenSystemCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isFrontCamera by remember { mutableStateOf(true) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var showPresetSelector by remember { mutableStateOf(false) }
    var activePreviewView by remember { mutableStateOf<PreviewView?>(null) }

    // Captured photo confirmation preview
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuxuryBlack)
        ) {
            if (capturedPhoto != null) {
                // ==========================================
                // PHOTO CONFIRMATION REVIEW SCREEN
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text(
                            text = "Photo Captured! / फोटो आ गया!",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check your face photo before styling",
                            color = ChampagneGold,
                            fontSize = 12.sp
                        )
                    }

                    // Photo preview container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, PurplePrimary, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = capturedPhoto!!.asImageBitmap(),
                            contentDescription = "Captured selfie preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Action buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val photo = capturedPhoto
                                if (photo != null) {
                                    onPhotoCaptured(photo)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_captured_photo_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurplePrimary,
                                contentColor = LuxuryBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use This Photo / यह फोटो लगाएं",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { capturedPhoto = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("retake_photo_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryCardBorder),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Retake / दोबारा फोटो लें", fontSize = 14.sp)
                        }
                    }
                }
            } else if (!showPresetSelector) {
                // ==========================================
                // LIVE CAMERA VIEWFINDER
                // ==========================================
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        activePreviewView = previewView

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val provider = cameraProviderFuture.get()
                                cameraProvider = provider
                                bindCameraSafe(
                                    ctx,
                                    provider,
                                    lifecycleOwner,
                                    previewView,
                                    isFrontCamera,
                                    onCaptureReady = { capture ->
                                        imageCapture = capture
                                        isCameraReady = true
                                        cameraError = null
                                    },
                                    onError = { err ->
                                        cameraError = err
                                    }
                                )
                            } catch (e: Exception) {
                                Log.e("CameraDialog", "Camera init failed", e)
                                cameraError = e.message
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = { previewView ->
                        activePreviewView = previewView
                        val provider = cameraProvider
                        if (provider != null) {
                            bindCameraSafe(
                                context,
                                provider,
                                lifecycleOwner,
                                previewView,
                                isFrontCamera,
                                onCaptureReady = { capture ->
                                    imageCapture = capture
                                    isCameraReady = true
                                    cameraError = null
                                },
                                onError = { err ->
                                    cameraError = err
                                }
                            )
                        }
                    }
                )

                // Face Alignment Frame
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 250.dp, height = 320.dp)
                        .border(2.dp, PurplePrimary.copy(alpha = 0.6f), RoundedCornerShape(130.dp))
                )
                Text(
                    text = "Align face in frame / चेहरा बीच में रखें",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 180.dp)
                        .background(LuxuryBlack.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )

                // If camera hardware has an error / emulator fallback notice
                if (cameraError != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LuxuryDarkSurface.copy(alpha = 0.95f))
                            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = PurpleLight,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Camera device not ready",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Open device camera or upload your photo directly:",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenSystemCamera,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Device Camera", color = LuxuryBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onOpenGallery,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryCardSurface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Upload Photo", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Top Bar Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LuxuryBlack.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close camera",
                                tint = TextPrimary
                            )
                        }

                        // Facing indicator pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(LuxuryBlack.copy(alpha = 0.75f))
                                .border(1.dp, PurplePrimary, RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isFrontCamera) "🤳 FRONT CAMERA" else "📷 BACK CAMERA",
                                color = PurpleLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Flip Front/Back
                        IconButton(
                            onClick = {
                                isFrontCamera = !isFrontCamera
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LuxuryBlack.copy(alpha = 0.7f))
                                .testTag("switch_camera_facing_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Switch Camera Front/Back",
                                tint = PurplePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Emulator / Device hint bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuxuryBlack.copy(alpha = 0.8f))
                            .border(1.dp, LuxuryCardBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
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
                                text = "Tap shutter below to capture, or use 'Device Camera' / 'Upload Photo'.",
                                color = ChampagneGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Bottom Controls Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Upload Photo directly from gallery
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onOpenGallery,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryCardSurface)
                                    .border(1.dp, LuxuryCardBorder, CircleShape)
                                    .testTag("camera_dialog_upload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = "Upload Photo from Gallery",
                                    tint = PurpleLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Gallery", color = TextSecondary, fontSize = 10.sp)
                        }

                        // Main Shutter Button
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(4.dp, PurplePrimary, CircleShape)
                                .clickable(enabled = !isCapturing) {
                                    isCapturing = true

                                    // 1. Instant snapshot from previewView
                                    val instantFrame = activePreviewView?.bitmap

                                    val capture = imageCapture
                                    if (capture != null) {
                                        val photoFile = File(context.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                        capture.takePicture(
                                            outputOptions,
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                    isCapturing = false
                                                    val decoded = ImageUtils.decodeAndCorrectOrientation(photoFile.absolutePath, isFrontCamera)
                                                        ?: instantFrame
                                                    if (decoded != null) {
                                                        capturedPhoto = decoded
                                                    } else {
                                                        Toast.makeText(context, "Opening device camera...", Toast.LENGTH_SHORT).show()
                                                        onOpenSystemCamera()
                                                    }
                                                }

                                                override fun onError(exc: ImageCaptureException) {
                                                    isCapturing = false
                                                    Log.e("CameraDialog", "Capture error: ${exc.message}", exc)
                                                    // Fallback to instant frame from PreviewView
                                                    if (instantFrame != null) {
                                                        capturedPhoto = if (isFrontCamera) ImageUtils.mirrorHorizontally(instantFrame) else instantFrame
                                                    } else {
                                                        Toast.makeText(context, "Opening device camera...", Toast.LENGTH_SHORT).show()
                                                        onOpenSystemCamera()
                                                    }
                                                }
                                            }
                                        )
                                    } else if (instantFrame != null) {
                                        isCapturing = false
                                        capturedPhoto = if (isFrontCamera) ImageUtils.mirrorHorizontally(instantFrame) else instantFrame
                                    } else {
                                        isCapturing = false
                                        Toast.makeText(context, "Opening device camera...", Toast.LENGTH_SHORT).show()
                                        onOpenSystemCamera()
                                    }
                                }
                                .testTag("shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(color = PurplePrimary, modifier = Modifier.size(36.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary)
                                )
                            }
                        }

                        // Studio Model Presets
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showPresetSelector = true },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryCardSurface)
                                    .border(1.dp, LuxuryCardBorder, CircleShape)
                                    .testTag("camera_dialog_preset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Studio Models",
                                    tint = ChampagneGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Models", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary button to launch device camera app
                    TextButton(
                        onClick = onOpenSystemCamera,
                        modifier = Modifier.testTag("open_system_camera_button")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Or Open Phone Device Camera (सिस्टम कैमरा खोलें)",
                            color = PurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // ==========================================
                // PRESET STUDIO FACES SELECTOR
                // ==========================================
                PresetSelfieView(
                    onSelectPreset = { bmp ->
                        onPhotoCaptured(bmp)
                    },
                    onBackToCamera = { showPresetSelector = false }
                )
            }
        }
    }
}

private fun bindCameraSafe(
    context: Context,
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    isFront: Boolean,
    onCaptureReady: (ImageCapture) -> Unit,
    onError: (String) -> Unit
) {
    try {
        cameraProvider.unbindAll()

        val primarySelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        val fallbackSelector = if (isFront) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

        val cameraSelector = try {
            if (cameraProvider.hasCamera(primarySelector)) {
                primarySelector
            } else if (cameraProvider.hasCamera(fallbackSelector)) {
                fallbackSelector
            } else {
                primarySelector
            }
        } catch (e: Exception) {
            primarySelector
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            capture
        )

        onCaptureReady(capture)
    } catch (e: Exception) {
        Log.e("CameraDialog", "bindCameraSafe error: ${e.message}", e)
        onError(e.message ?: "Camera binding error")
    }
}

@Composable
private fun PresetSelfieView(
    onSelectPreset: (Bitmap) -> Unit,
    onBackToCamera: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToCamera) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Select Studio Face Portrait",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "High-definition portraits ready for realistic hairstyle transformations",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Preset 1: Passport Model
            item {
                PresetModelCard(
                    title = "Maya (Neutral Front)",
                    drawableRes = R.drawable.img_portrait_model,
                    onClick = {
                        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_portrait_model)
                        if (bmp != null) onSelectPreset(bmp)
                    }
                )
            }

            // Preset 2: Men Model
            item {
                PresetModelCard(
                    title = "Alex (Man Portrait)",
                    drawableRes = R.drawable.img_hair_men,
                    onClick = {
                        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_hair_men)
                        if (bmp != null) onSelectPreset(bmp)
                    }
                )
            }

            // Preset 3: Women Model
            item {
                PresetModelCard(
                    title = "Elena (Woman Portrait)",
                    drawableRes = R.drawable.img_hair_women,
                    onClick = {
                        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_hair_women)
                        if (bmp != null) onSelectPreset(bmp)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onBackToCamera,
            colors = ButtonDefaults.buttonColors(
                containerColor = PurplePrimary,
                contentColor = LuxuryBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Live Camera", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PresetModelCard(
    title: String,
    drawableRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, PurplePrimary, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardSurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}
