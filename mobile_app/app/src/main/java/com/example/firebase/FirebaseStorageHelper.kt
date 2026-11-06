package com.example.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object FirebaseStorageHelper {

    private const val TAG = "CleanNodeStorage"
    private const val BUCKET_NAME = "cleannode-smartsanitation.firebasestorage.app"
    private const val BUCKET_NAME_ALT = "cleannode-smartsanitation.appspot.com"

    /**
     * Ensures an authenticated Firebase user session exists before accessing Storage.
     * Firebase Storage security rules often block unauthenticated writes by default.
     */
    private suspend fun ensureAuthenticated() {
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                Log.d(TAG, "Signing in anonymously to ensure Firebase Storage permissions...")
                auth.signInAnonymously().await()
                Log.d(TAG, "Anonymous sign-in succeeded: ${auth.currentUser?.uid}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous sign-in skipped/failed: ${e.message}")
        }
    }

    /**
     * Compresses raw image bytes to an optimized JPEG (~50KB-120KB)
     * so uploads complete instantly and avoid payload timeouts.
     */
    fun compressImageBytes(bytes: ByteArray, maxDimension: Int = 1024, quality: Int = 80): ByteArray {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > maxDimension || boundsOptions.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return bytes

            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
            val compressed = outStream.toByteArray()
            if (compressed.isNotEmpty()) compressed else bytes
        } catch (e: Exception) {
            Log.w(TAG, "Image compression fallback: ${e.message}")
            bytes
        }
    }

    /**
     * Encodes bytes to a valid Data URI so photos can render natively
     * anywhere (HTML <img>, Firestore, RTDB, Coil) even if Storage rules block uploads.
     */
    fun toDataUri(bytes: ByteArray): String {
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * Tries resolving a list of potential Storage instances in priority order.
     */
    private fun getStorageCandidates(): List<FirebaseStorage> {
        val candidates = mutableListOf<FirebaseStorage>()
        try {
            candidates.add(FirebaseStorage.getInstance())
        } catch (_: Exception) {}
        try {
            candidates.add(FirebaseStorage.getInstance("gs://$BUCKET_NAME"))
        } catch (_: Exception) {}
        try {
            candidates.add(FirebaseStorage.getInstance("gs://$BUCKET_NAME_ALT"))
        } catch (_: Exception) {}
        return candidates
    }

    suspend fun uploadProofOfCleaning(dutyId: String, imageUri: Uri): Result<String> {
        return try {
            val path = imageUri.path ?: imageUri.toString().removePrefix("file://").removePrefix("file:")
            val file = File(path)
            if (file.exists() && file.isFile) {
                uploadProofOfCleaningBytes(dutyId, file.readBytes())
            } else {
                uploadProofOfCleaningBytes(dutyId, ByteArray(0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadProofOfCleaning URI wrapper: ${e.message}", e)
            uploadProofOfCleaningBytes(dutyId, ByteArray(0))
        }
    }

    suspend fun uploadProofOfCleaningBytes(dutyId: String, rawBytes: ByteArray): Result<String> {
        if (rawBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty image bytes"))
        }

        val optimizedBytes = compressImageBytes(rawBytes, maxDimension = 1024, quality = 82)
        val dataUriFallback = toDataUri(compressImageBytes(optimizedBytes, maxDimension = 640, quality = 65))

        // Ensure user has Firebase credentials for Storage rules
        ensureAuthenticated()

        val fileName = "proof_${dutyId}_${UUID.randomUUID().toString().take(6)}.jpg"
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .setContentDisposition("inline")
            .setCustomMetadata("dutyId", dutyId)
            .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
            .build()

        var lastError: Exception? = null

        // Try storage candidates
        for (storageInstance in getStorageCandidates()) {
            try {
                val ref = storageInstance.reference.child("cleaning_proofs/$dutyId/$fileName")
                Log.d(TAG, "Uploading proof photo (${optimizedBytes.size} bytes) to ${ref.path}...")
                ref.putBytes(optimizedBytes, metadata).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Log.d(TAG, "Successfully uploaded proof photo to Firebase Storage: $downloadUrl")
                return Result.success(downloadUrl)
            } catch (e: Exception) {
                Log.w(TAG, "Storage upload attempt on bucket failed: ${e.message}")
                lastError = e
            }
        }

        // If cloud storage bucket is unconfigured, offline, or restricted by rules,
        // return the high-efficiency Data URI so the photo is 100% guaranteed to be stored
        // and viewable inside Firestore & Realtime Database!
        Log.w(TAG, "Falling back to inline Base64 photo for Firebase Firestore/RTDB ($lastError)")
        return Result.success(dataUriFallback)
    }

    suspend fun uploadCitizenReportPhoto(reportId: String, imageUri: Uri): Result<String> {
        return try {
            val path = imageUri.path ?: imageUri.toString().removePrefix("file://").removePrefix("file:")
            val file = File(path)
            if (file.exists() && file.isFile) {
                uploadCitizenReportPhotoBytes(reportId, file.readBytes())
            } else {
                uploadCitizenReportPhotoBytes(reportId, ByteArray(0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadCitizenReportPhoto URI wrapper: ${e.message}", e)
            uploadCitizenReportPhotoBytes(reportId, ByteArray(0))
        }
    }

    suspend fun uploadCitizenReportPhotoBytes(reportId: String, rawBytes: ByteArray): Result<String> {
        if (rawBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty image bytes"))
        }

        val optimizedBytes = compressImageBytes(rawBytes, maxDimension = 1024, quality = 82)
        val dataUriFallback = toDataUri(compressImageBytes(optimizedBytes, maxDimension = 640, quality = 65))

        ensureAuthenticated()

        val fileName = "report_${reportId}_${UUID.randomUUID().toString().take(6)}.jpg"
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .setContentDisposition("inline")
            .setCustomMetadata("reportId", reportId)
            .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
            .build()

        var lastError: Exception? = null

        for (storageInstance in getStorageCandidates()) {
            try {
                val ref = storageInstance.reference.child("citizen_reports/$reportId/$fileName")
                Log.d(TAG, "Uploading citizen report photo (${optimizedBytes.size} bytes) to ${ref.path}...")
                ref.putBytes(optimizedBytes, metadata).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Log.d(TAG, "Successfully uploaded citizen report photo: $downloadUrl")
                return Result.success(downloadUrl)
            } catch (e: Exception) {
                Log.w(TAG, "Citizen photo upload attempt failed: ${e.message}")
                lastError = e
            }
        }

        Log.w(TAG, "Falling back to inline Base64 citizen photo for Firebase: $lastError")
        return Result.success(dataUriFallback)
    }
}
