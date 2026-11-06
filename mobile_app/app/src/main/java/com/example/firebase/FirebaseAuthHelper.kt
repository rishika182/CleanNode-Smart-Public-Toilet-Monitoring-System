package com.example.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

object FirebaseAuthHelper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (_: Exception) { null }

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (credential: PhoneAuthCredential) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.localizedMessage ?: "Phone verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtpAndSignIn(
        verificationId: String,
        otpCode: String,
        onSuccess: (user: FirebaseUser) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        signInWithCredential(credential, onSuccess, onError)
    }

    fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (user: FirebaseUser) -> Unit,
        onError: (message: String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError("Authentication succeeded but user profile was null")
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Invalid verification code")
                }
            }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
    }
}
