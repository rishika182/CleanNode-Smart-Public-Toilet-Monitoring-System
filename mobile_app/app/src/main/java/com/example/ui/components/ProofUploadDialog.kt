package com.example.ui.components

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun ProofUploadDialog(
    facilityName: String,
    lang: AppLanguage,
    onConfirm: (photoUri: String, notes: String, mopped: Boolean, disinfected: Boolean, waterChecked: Boolean, soapRefilled: Boolean, trashEmptied: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var floorMopped by remember { mutableStateOf(true) }
    var bowlDisinfected by remember { mutableStateOf(true) }
    var waterSupplyChecked by remember { mutableStateOf(true) }
    var soapRefilled by remember { mutableStateOf(true) }
    var trashEmptied by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("Chemical sanitization completed, odor scrubbed, fresh water flow verified.") }
    var photoCaptured by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessingImage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isProcessingImage = true
            coroutineScope.launch {
                capturedBitmap = bitmap
                photoCaptured = true
                isProcessingImage = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {}
        }
    }

    fun launchCameraSafe() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {}
        } else {
            try {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            } catch (_: Exception) {}
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessingImage = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            capturedBitmap = bitmap
                            photoCaptured = true
                        }
                        isProcessingImage = false
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessingImage = false
                    }
                }
            }
        }
    }

    fun launchGallerySafe() {
        try {
            galleryLauncher.launch("image/*")
        } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.KANNADA -> "ಸ್ವಚ್ಛತೆಯ ಫೋಟೋ ಪುರಾವೆ"
                        AppLanguage.HINDI -> "सफाई प्रमाण फोटो अपलोड"
                        AppLanguage.ENGLISH -> "Proof of Cleaning"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = facilityName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Real Camera Photo Preview Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("photo_preview_box")
                        .clickable { launchCameraSafe() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (photoCaptured) CleanGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessingImage) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Processing image...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (capturedBitmap != null) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Proof",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (photoCaptured) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Photo Captured",
                                    tint = CleanGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cleaning Proof Photo Attached",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CleanGreen
                                )
                                Text(
                                    text = "Tap to retake with Camera",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Take Photo",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to Capture Photo Proof",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Camera or Gallery verification",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { launchCameraSafe() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📸 Open Camera", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { launchGallerySafe() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🖼️ Pick Gallery", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (lang) {
                        AppLanguage.KANNADA -> "ಸ್ವಚ್ಛತಾ ಪರಿಶೀಲನಾ ಪಟ್ಟಿ:"
                        AppLanguage.HINDI -> "सफाई चेकलिस्ट:"
                        AppLanguage.ENGLISH -> "Cleaning Checklist:"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                CheckboxItem(
                    label = when (lang) {
                        AppLanguage.KANNADA -> "ನೆಲ ಒರೆಸಲಾಗಿದೆ ಮತ್ತು ಸೋಂಕುಮುಕ್ತಗೊಳಿಸಲಾಗಿದೆ"
                        AppLanguage.HINDI -> "फर्श पोंछा और कीटाणुरहित किया गया"
                        AppLanguage.ENGLISH -> "Floor Mopped & Disinfected"
                    },
                    checked = floorMopped,
                    onCheckedChange = { floorMopped = it },
                    tag = "check_floor_mopped"
                )

                CheckboxItem(
                    label = when (lang) {
                        AppLanguage.KANNADA -> "ಕಮೋಡ್/ಬೇಸಿನ್ ರಾಸಾಯನಿಕದಿಂದ ತೊಳೆಯಲಾಗಿದೆ"
                        AppLanguage.HINDI -> "शौचालय सीट रासायनिक उपचारित"
                        AppLanguage.ENGLISH -> "Toilet Bowl / Urinal Scrubbed"
                    },
                    checked = bowlDisinfected,
                    onCheckedChange = { bowlDisinfected = it },
                    tag = "check_bowl_disinfected"
                )

                CheckboxItem(
                    label = when (lang) {
                        AppLanguage.KANNADA -> "ನೀರಿನ ಸರಬರಾಜು ಮತ್ತು ಫ್ಲಶ್ ಪರಿಶೀಲಿಸಲಾಗಿದೆ"
                        AppLanguage.HINDI -> "पानी आपूर्ति एवं फ्लश जांचा गया"
                        AppLanguage.ENGLISH -> "Water Supply & Flush Flow Verified"
                    },
                    checked = waterSupplyChecked,
                    onCheckedChange = { waterSupplyChecked = it },
                    tag = "check_water_checked"
                )

                CheckboxItem(
                    label = when (lang) {
                        AppLanguage.KANNADA -> "ಸೋಪು ಡಿಸ್ಪೆನ್ಸರ್ ಮರುಪೂರಣ ಮಾಡಲಾಗಿದೆ"
                        AppLanguage.HINDI -> "साबुन डिस्पेंसर भरा गया"
                        AppLanguage.ENGLISH -> "Soap Dispenser Refilled"
                    },
                    checked = soapRefilled,
                    onCheckedChange = { soapRefilled = it },
                    tag = "check_soap_refilled"
                )

                CheckboxItem(
                    label = when (lang) {
                        AppLanguage.KANNADA -> "ಕಸದ ಬುಟ್ಟಿ ಖಾಲಿ ಮಾಡಲಾಗಿದೆ"
                        AppLanguage.HINDI -> "कचरा पेटी खाली की गई"
                        AppLanguage.ENGLISH -> "Trash Bin Emptied"
                    },
                    checked = trashEmptied,
                    onCheckedChange = { trashEmptied = it },
                    tag = "check_trash_emptied"
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text(when (lang) {
                            AppLanguage.KANNADA -> "ಟಿಪ್ಪಣಿಗಳು / ಅಭಿಪ್ರಾಯ"
                            AppLanguage.HINDI -> "टिप्पणियाँ"
                            AppLanguage.ENGLISH -> "Cleaning Notes / Remarks"
                        })
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_proof_notes"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPhotoUri = if (capturedBitmap != null) {
                        try {
                            val cacheFile = java.io.File(context.cacheDir, "proof_captured_${System.currentTimeMillis()}.jpg")
                            val fos = java.io.FileOutputStream(cacheFile)
                            capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                            fos.flush()
                            fos.close()
                            "file://${cacheFile.absolutePath}"
                        } catch (_: Exception) {
                            try {
                                val bytes = ByteArrayOutputStream()
                                capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, bytes)
                                val base64 = android.util.Base64.encodeToString(bytes.toByteArray(), android.util.Base64.NO_WRAP)
                                "data:image/jpeg;base64,$base64"
                            } catch (_: Exception) {
                                "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500"
                            }
                        }
                    } else {
                        "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500"
                    }

                    onConfirm(
                        finalPhotoUri,
                        notes,
                        floorMopped,
                        bowlDisinfected,
                        waterSupplyChecked,
                        soapRefilled,
                        trashEmptied
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreen),
                modifier = Modifier.testTag("btn_confirm_proof")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.KANNADA -> "ಸಲ್ಲಿಸಿ ಮತ್ತು ಪೂರ್ಣಗೊಳಿಸಿ"
                        AppLanguage.HINDI -> "जमा करें एवं पूरा करें"
                        AppLanguage.ENGLISH -> "Submit & Complete"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_proof")
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.KANNADA -> "ರದ್ದುಮಾಡಿ"
                        AppLanguage.HINDI -> "रद्द करें"
                        AppLanguage.ENGLISH -> "Cancel"
                    }
                )
            }
        }
    )
}

@Composable
private fun CheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
            .testTag(tag)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
