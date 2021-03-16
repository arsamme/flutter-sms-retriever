package me.arsam.sms_retriever

import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.credentials.HintRequest.Builder
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry


class SmsRetrieverPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

    private lateinit var context: Context
    private var activity: FlutterActivity? = null

    private lateinit var channel: MethodChannel
    private var pendingResult: MethodChannel.Result? = null

    private var receiver: MySMSBroadcastReceiver? = null
    var sms: String? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "ars_sms_retriever/method_ch")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getAppSignature" -> {
                val signature = AppSignatureHelper(this.context).getAppSignatures()[0]
                result.success(signature)
            }
            "requestPhoneNumber" -> {
                requestPhoneNumber()
                pendingResult = result
            }
            "startListening" -> {
                receiver = MySMSBroadcastReceiver()
                this.context.registerReceiver(receiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
                startListening()
                pendingResult = result
            }
            "stopListening" -> unregister()
            else -> result.notImplemented()
        }
    }


    // Obtain the phone number from the result
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?): Boolean {
        if (requestCode == CREDENTIAL_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val credential: Credential = data!!.getParcelableExtra(Credential.EXTRA_KEY)
                Log.i("ars tag", credential.id)
                pendingResult?.success("09027777254")
            }
        }
        return false
    }

    private fun requestPhoneNumber() {
        val hintRequest: HintRequest = Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()

        val mGoogleApiClient = GoogleApiClient.Builder(context)
                .addApi(Auth.CREDENTIALS_API)
                .build()

        val intent: PendingIntent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest)
        if (activity != null) {
            startIntentSenderForResult(activity!!, intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, null)
        }
    }


    private fun startListening() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        val client = SmsRetriever.getClient(this.context)

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        val task = client.startSmsRetriever()

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener {
            // Successfully started retriever, expect broadcast intent
        }

        task.addOnFailureListener {
            // Failed to start retriever, inspect Exception for more details
        }


    }

    private fun unregister() {
        try {
            this.context.unregisterReceiver(receiver)
        } catch (e: Throwable) {
            Log.e(javaClass::getSimpleName.name, e.toString())
        }
    };


    /**
     * BroadcastReceiver to wait for SMS messages. This can be registered either
     * in the AndroidManifest or at runtime.  Should filter Intents on
     * SmsRetriever.SMS_RETRIEVED_ACTION.
     */
    inner class MySMSBroadcastReceiver : BroadcastReceiver() {


        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents
                        sms = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        pendingResult?.success(sms)
                    }

                    CommonStatusCodes.TIMEOUT -> {
                    }
                }
            }
        }
    }

    companion object {
        private const val CREDENTIAL_PICKER_REQUEST = 130479
    }
}

