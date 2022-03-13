package me.arsam.sms_retriever

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.*
import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.credentials.HintRequest.Builder
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult.RESOLUTION_REQUIRED
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry

class SmsRetrieverPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {

    private lateinit var mContext: Context
    private var mActivity: FlutterActivity? = null
    private var mBinding: ActivityPluginBinding? = null
    private var mChannel: MethodChannel? = null

    private var pendingResult: MethodChannel.Result? = null
    private var smsReceiver: SmsBroadcastReceiver? = null
    private var consentReceiver: ConsentBroadcastReceiver? = null

    var sms: String? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mContext = flutterPluginBinding.applicationContext

        mChannel =
            MethodChannel(flutterPluginBinding.binaryMessenger, "ars_sms_retriever/method_ch")
        mChannel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        unregisterReceiver(smsReceiver)
        unregisterReceiver(consentReceiver)
        ignoreIllegalState {
            pendingResult?.success(null)
        }

        mChannel?.setMethodCallHandler(null)
        mChannel = null
        mActivity = null
        mBinding?.removeActivityResultListener(this)
        mBinding = null
    }

    override fun onDetachedFromActivity() {
        unregisterReceiver(smsReceiver)
        unregisterReceiver(consentReceiver)
        ignoreIllegalState {
            pendingResult?.success(null)
        }

        mActivity = null
        mBinding?.removeActivityResultListener(this)
        mBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        mActivity = binding.activity as FlutterActivity
        mBinding = binding
        binding.addActivityResultListener(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        mActivity = binding.activity as FlutterActivity
        mBinding = binding
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        unregisterReceiver(smsReceiver)
        unregisterReceiver(consentReceiver)
        ignoreIllegalState {
            pendingResult?.success(null)
        }

        mActivity = null
        mBinding?.removeActivityResultListener(this)
        mBinding = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getAppSignature" -> {
                val signatures = AppSignatureHelper(mContext).getAppSignatures()
                if (signatures.size > 0) {
                    result.success(signatures[0])
                } else {
                    result.success(null)
                }
            }
            "requestPhoneNumber" -> {
                pendingResult = result
                requestPhoneNumber()
            }
            "storePhoneNumber" -> {
                val url: String? = call.argument<String?>("url")
                val phoneNumber: String? = call.argument<String?>("phoneNumber")
                val credential: Credential =
                    Credential.Builder(phoneNumber).setAccountType(url).build()

                val mCredentialsClient = Credentials.getClient(mContext)
                mCredentialsClient.save(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        result.success(null)
                    } else {
                        val e = task.exception
                        if (e is ResolvableApiException) {
                            // Try to resolve the save request. This will prompt the user if
                            // the credential is new.
                            if (e.statusCode == RESOLUTION_REQUIRED) {
                                try {
                                    pendingResult = result
                                    if (mActivity != null) {
                                        e.startResolutionForResult(
                                            mActivity as Activity,
                                            STORE_PHONE_NUMBER_REQUEST
                                        )
                                    }
                                } catch (exception: IntentSender.SendIntentException) {
                                    // Could not resolve the request
                                    Log.e(TAG, "Failed to send resolution.", exception)
                                    result.success(null)
                                }
                            } else {
                                result.success(null)
                            }
                        } else {
                            result.success(null)
                        }
                    }
                }
            }
            "retrieveStoredPhoneNumber" -> {
                val url: String? = call.argument<String?>("url")

                val mCredentialsClient: CredentialsClient = Credentials.getClient(mContext)
                val mCredentialRequest = CredentialRequest.Builder().setAccountTypes(url).build()
                mCredentialsClient.request(mCredentialRequest).addOnCompleteListener(
                    OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // See "Handle successful credential requests"
                            if (task.result != null) {
                                val credential: Credential? = task.result!!.credential
                                if (credential != null) {
                                    result.success(credential.id)
                                    return@OnCompleteListener
                                }
                            }
                            result.success(null)
                        } else {
                            val e = task.exception
                            if (e is ResolvableApiException) {
                                // Try to resolve the save request. This will prompt the user if
                                // the credential is new.
                                if (e.statusCode == RESOLUTION_REQUIRED) {
                                    try {
                                        pendingResult = result
                                        if (mActivity != null) {
                                            e.startResolutionForResult(
                                                mActivity as Activity,
                                                RETRIEVE_PHONE_NUMBER_REQUEST
                                            )
                                        }
                                    } catch (exception: IntentSender.SendIntentException) {
                                        // Could not resolve the request
                                        Log.e(TAG, "Failed to send resolution.", exception)
                                        result.success(null)
                                    }
                                } else {
                                    result.success(null)
                                }
                            } else {
                                result.success(null)
                            }
                        }
                    })
            }
            "deleteStoredPhoneNumber" -> {
                val url: String? = call.argument<String?>("url")
                val phoneNumber: String? = call.argument<String?>("phoneNumber")
                val credential: Credential =
                    Credential.Builder(phoneNumber).setAccountType(url).build()

                val mCredentialsClient: CredentialsClient = Credentials.getClient(mContext)
                mCredentialsClient.delete(credential).addOnCompleteListener {
                    result.success(null)
                }
            }
            "startSmsListener" -> {
                pendingResult = result
                smsReceiver = SmsBroadcastReceiver()
                mContext.registerReceiver(
                    smsReceiver,
                    IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                    SmsRetriever.SEND_PERMISSION,
                    null,
                )
                SmsRetriever.getClient(mContext).startSmsRetriever()
            }
            "stopSmsListener" -> {
                unregisterReceiver(smsReceiver)
                result.success(null)
            }
            "startConsentListener" -> {
                pendingResult = result
                consentReceiver = ConsentBroadcastReceiver()
                mContext.registerReceiver(
                    consentReceiver,
                    IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                    SmsRetriever.SEND_PERMISSION,
                    null,
                )
                SmsRetriever.getClient(mContext).startSmsUserConsent(
                    call.argument("senderPhoneNumber")
                )
            }
            "stopConsentListener" -> {
                unregisterReceiver(consentReceiver)
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    // Obtain the phone number from the result
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ): Boolean {
        when (requestCode) {
            CREDENTIAL_PICKER_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        val credential: Credential? = data.getParcelableExtra(Credential.EXTRA_KEY)
                        if (credential != null) {
                            ignoreIllegalState {
                                pendingResult?.success(credential.id)
                            }
                            return false
                        }
                    }
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                }
            }
            SMS_CONSENT_REQUEST -> {
                if (resultCode == RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    ignoreIllegalState {
                        pendingResult?.success(message)
                    }
                } else {
                    // Consent denied
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                }
            }
            STORE_PHONE_NUMBER_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                } else {
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                }
            }
            RETRIEVE_PHONE_NUMBER_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        val credential: Credential? = data.getParcelableExtra(Credential.EXTRA_KEY)
                        if (credential != null) {
                            ignoreIllegalState {
                                pendingResult?.success(credential.id)
                            }
                            return false
                        }
                    }
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                } else {
                    ignoreIllegalState {
                        pendingResult?.success(null)
                    }
                }
            }
        }
        return false
    }

    private fun requestPhoneNumber() {
        val hintRequest: HintRequest = Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val intent: PendingIntent = Credentials.getClient(mContext).getHintPickerIntent(hintRequest)
        if (mActivity != null) {
            startIntentSenderForResult(
                mActivity!!,
                intent.intentSender,
                CREDENTIAL_PICKER_REQUEST,
                null,
                0,
                0,
                0,
                null
            )
        }
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver?) {
        try {
            receiver?.let {
                mContext.unregisterReceiver(it)
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Unregistering receiver failed.", exception)
        }
    }

    private fun ignoreIllegalState(fn: () -> Unit) {
        try {
            fn()
        } catch (e: IllegalStateException) {
            Log.d(
                TAG,
                "ignoring exception: $e. See https://github.com/flutter/flutter/issues/29092 for details."
            )
        }
    }

    /**
     * BroadcastReceiver to wait for SMS messages. This can be registered either
     * in the AndroidManifest or at runtime.  Should filter Intents on
     * SmsRetriever.SMS_RETRIEVED_ACTION.
     */
    inner class SmsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents
                        sms = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        ignoreIllegalState {
                            pendingResult?.success(sms)
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        ignoreIllegalState {
                            pendingResult?.success(null)
                        }
                    }
                }
            }
        }
    }

    inner class ConsentBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                // Unregister consentReceiver after receiving data.
                unregisterReceiver(consentReceiver)

                val extras = intent.extras
                val smsRetrieverStatus = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get consent intent
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            this@SmsRetrieverPlugin.mActivity?.startActivityForResult(
                                consentIntent,
                                SMS_CONSENT_REQUEST
                            )
                        } catch (e: ActivityNotFoundException) {
                            ignoreIllegalState {
                                pendingResult?.success(null)
                            }
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        ignoreIllegalState {
                            pendingResult?.success(null)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val CREDENTIAL_PICKER_REQUEST = 130479
        private const val SMS_CONSENT_REQUEST = 130480
        private const val STORE_PHONE_NUMBER_REQUEST = 130481
        private const val RETRIEVE_PHONE_NUMBER_REQUEST = 130482
    }
}

