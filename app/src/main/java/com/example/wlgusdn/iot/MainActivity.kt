package com.example.wlgusdn.iot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.AWSMobileClient
import java.util.UUID.randomUUID
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.AWSIotClient

import android.widget.TextView
import android.widget.EditText
import com.amazonaws.regions.Regions
import java.io.UnsupportedEncodingException
import java.security.KeyStore
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import android.security.KeyChain.getPrivateKey
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.metrics.AwsSdkMetrics.setRegion
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament
import com.amazonaws.regions.Region
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity() {


     val LOG_TAG = MainActivity::class.java!!.getCanonicalName()

    // --- Constants to modify per your configuration ---

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private val CUSTOMER_SPECIFIC_ENDPOINT = ""
    // Name of the AWS IoT policy to attach to a newly created certificate
    private val AWS_IOT_POLICY_NAME = ""

    // Region of AWS IoT
    private val MY_REGION = Regions.AP_NORTHEAST_2
    // Filename of KeyStore file on the filesystem
    private val KEYSTORE_NAME = "iot_keystore"
    // Password for the private key in the KeyStore
    private val KEYSTORE_PASSWORD = "password"
    // Certificate and key aliases in the KeyStore
    private val CERTIFICATE_ID = ""

     var txtSubcribe:EditText? = null
     var txtTopic:EditText? = null
     var txtMessage:EditText? = null

     var tvLastMessage:TextView? = null
     var tvClientId:TextView? = null
     var tvStatus:TextView? = null

     var btnConnect: Button? = null

     var mIotAndroidClient:AWSIotClient? = null
     var mqttManager:AWSIotMqttManager? = null
     var clientId:String? = null
     var keystorePath:String? = null
     var keystoreName:String? = null
     var keystorePassword:String? = null

     var clientKeyStore: KeyStore? = null
     var certificateId:String? = null

     fun connectClick(view: View) {
Log.d(LOG_TAG, "clientId = " + clientId!!)

try
{
mqttManager!!.connect(clientKeyStore,
    AWSIotMqttClientStatusCallback { status, throwable ->
        Log.d(LOG_TAG, "Status = " + (status).toString())

        runOnUiThread {
            tvStatus!!.text = status.toString()
            if (throwable != null) {
                Log.e(LOG_TAG, "Connection error.", throwable)
            }
        }
    })
}
catch (e:Exception) {
Log.e(LOG_TAG, "Connection error.", e)
    tvStatus!!.text = "Error! " + e.message
}

}

     fun subscribeClick(view:View) {
val topic = txtSubcribe!!.text.toString()

Log.d(LOG_TAG, "topic = $topic")

try
{
mqttManager!!.subscribeToTopic(topic, AWSIotMqttQos.QOS0
) { topic, data ->
    runOnUiThread {
        try {
            val message = String(data, charset("UTF-8"))
            Log.d(LOG_TAG, "Message arrived:")
            Log.d(LOG_TAG, "   Topic: $topic")
            Log.d(LOG_TAG, " Message: $message")

            tvLastMessage!!.text = message
        } catch (e: UnsupportedEncodingException) {
            Log.e(LOG_TAG, "Message encoding error.", e)
        }
    }
}
}
catch (e:Exception) {
Log.e(LOG_TAG, "Subscription error.", e)
}

}

     fun publishClick(view:View) {
val topic = txtTopic!!.text.toString()
val msg = txtMessage!!.text.toString()

try
{
mqttManager!!.publishString(msg, topic, AWSIotMqttQos.QOS0)
}
catch (e:Exception) {
Log.e(LOG_TAG, "Publish error.", e)
}

}


     fun disconnectClick(view:View) {
try
{
mqttManager!!.disconnect()
}
catch (e:Exception) {
Log.e(LOG_TAG, "Disconnect error.", e)
}

}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       var credentialsProvider = CognitoCachingCredentialsProvider(
    getApplicationContext(),
    "ap-northeast-2:89f11573-bcf8-4e9c-a8ec-f962f0cfa2ef", // 자격 증명 풀 ID
    Regions.AP_NORTHEAST_2 // 리전
);

        txtSubcribe = findViewById(R.id.txtSubcribe)
        txtTopic = findViewById(R.id.txtTopic)
        txtMessage = findViewById(R.id.txtMessage)

        tvLastMessage = findViewById(R.id.tvLastMessage)
        tvClientId = findViewById(R.id.tvClientId)
        tvStatus = findViewById(R.id.tvStatus)

        btnConnect = findViewById(R.id.btnConnect)
        btnConnect!!.setEnabled(false)

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString()
        tvClientId!!.setText(clientId)

        // Initialize the AWS Cognito credentials provider
        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails) {
                initIoTClient()
            }

            override fun onError(e: Exception) {
                Log.e(LOG_TAG, "onError: ", e)
            }
        })


    }

    fun initIoTClient() {
        val region = Region.getRegion(MY_REGION)

        // MQTT Client
        mqttManager = AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT)

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager!!.setKeepAlive(10)

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        val lwt = AWSIotMqttLastWillAndTestament(
            "my/lwt/topic",
            "Android client lost connection", AWSIotMqttQos.QOS0
        )
        mqttManager!!.setMqttLastWillAndTestament(lwt)

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = AWSIotClient(AWSMobileClient.getInstance())
        mIotAndroidClient!!.setRegion(region)

        keystorePath = filesDir.path
        keystoreName = KEYSTORE_NAME
        keystorePassword = KEYSTORE_PASSWORD
        certificateId = CERTIFICATE_ID

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)!!) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(
                        certificateId, keystorePath,
                        keystoreName, keystorePassword
                    )!!
                ) {
                    Log.i(
                        LOG_TAG, "Certificate " + certificateId
                                + " found in keystore - using for MQTT."
                    )
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(
                        certificateId,
                        keystorePath, keystoreName, keystorePassword
                    )
                    /* initIoTClient is invoked from the callback passed during AWSMobileClient initialization.
                    The callback is executed on a background thread so UI update must be moved to run on UI Thread. */
                    runOnUiThread { btnConnect!!.setEnabled(true) }
                } else {
                    Log.i(LOG_TAG, "Key/cert $certificateId not found in keystore.")
                }
            } else {
                Log.i(LOG_TAG, "Keystore $keystorePath/$keystoreName not found.")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e)
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.")

            Thread(Runnable {
                try {
                    // Create a new private key and certificate. This call
                    // creates both on the server and returns them to the
                    // device.
                    val createKeysAndCertificateRequest = CreateKeysAndCertificateRequest()
                    createKeysAndCertificateRequest.isSetAsActive = true
                    val createKeysAndCertificateResult: CreateKeysAndCertificateResult
                    createKeysAndCertificateResult =
                        mIotAndroidClient!!.createKeysAndCertificate(createKeysAndCertificateRequest)
                    Log.i(
                        LOG_TAG,
                        "Cert ID: " +
                                createKeysAndCertificateResult.certificateId +
                                " created."
                    )

                    // store in keystore for use in MQTT client
                    // saved as alias "default" so a new certificate isn't
                    // generated each run of this application
                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                        certificateId,
                        createKeysAndCertificateResult.certificatePem,
                        createKeysAndCertificateResult.keyPair.privateKey,
                        keystorePath, keystoreName, keystorePassword
                    )

                    // load keystore from file into memory to pass on
                    // connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(
                        certificateId,
                        keystorePath, keystoreName, keystorePassword
                    )

                    // Attach a policy to the newly created certificate.
                    // This flow assumes the policy was already created in
                    // AWS IoT and we are now just attaching it to the
                    // certificate.
                    val policyAttachRequest = AttachPrincipalPolicyRequest()
                    policyAttachRequest.policyName = AWS_IOT_POLICY_NAME
                    policyAttachRequest.principal = createKeysAndCertificateResult
                        .certificateArn
                    mIotAndroidClient!!.attachPrincipalPolicy(policyAttachRequest)

                    runOnUiThread { btnConnect!!.setEnabled(true) }
                } catch (e: Exception) {
                    Log.e(
                        LOG_TAG,
                        "Exception occurred when generating new private key and certificate.",
                        e
                    )
                }
            }).start()
        }
    }


}
