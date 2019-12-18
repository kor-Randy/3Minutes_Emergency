package com.example.wlgusdn.iot

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
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
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.metrics.AwsSdkMetrics.setRegion
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament
import com.amazonaws.regions.Region
import java.nio.charset.Charset
import java.util.*


class MainActivity : PermissionsActivity() {
    override fun onPermissionsOkay() {


    }


    val LOG_TAG = "wlgusdn11"

    // --- Constants to modify per your configuration ---
    var gps : GpsTracker?=null

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private val CUSTOMER_SPECIFIC_ENDPOINT = ""
    // Name of the AWS IoT policy to attach to a newly created certificate
    private val AWS_IOT_POLICY_NAME = ""

    // Region of AWS IoT
    private val MY_REGION = Regions.AP_NORTHEAST_2
    // Filename of KeyStore file on the filesystem
    private val KEYSTORE_NAME = "iot_keystore1"
    // Password for the private key in the KeyStore
    private val KEYSTORE_PASSWORD = "password1"
    // Certificate and key aliases in the KeyStore
    private val CERTIFICATE_ID = "default"

    var hot : Button?=null

    var blueConnect : Button?=null



    var mIotAndroidClient:AWSIotClient? = null
    var mqttManager:AWSIotMqttManager? = null
    var clientId:String? = null
    var keystorePath:String? = null
    var keystoreName:String? = null
    var keystorePassword:String? = null

    var clientKeyStore: KeyStore? = null
    var certificateId:String? = null
    var bu4 : Button?=null

    fun connectClick(view: View) {
        Log.d(LOG_TAG, "clientId = " + clientId!!)

        try
        {
            mqttManager!!.connect(clientKeyStore,
                AWSIotMqttClientStatusCallback { status, throwable ->
                    Log.d(LOG_TAG, "Status = " + (status).toString())

                    runOnUiThread {


                        if (throwable != null) {
                            Log.e(LOG_TAG, "Connection error.", throwable)
                        }
                    }
                })
        }
        catch (e:Exception) {
            Log.e(LOG_TAG, "Connection error.", e)

        }

    }


    fun publishClick(view:View) {
        val aa="$"
        val topic =aa+"aws/things/IOTApp/shadow/update"
        val msg = "{\"state\":{\"desired\":{\"Siren\":\"OFF\"}}}"

        try
        {
            mqttManager!!.publishString(msg, topic, AWSIotMqttQos.QOS0)
            Log.e(LOG_TAG, "Publish Success.")
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


        bu4 = findViewById(R.id.button4)
        bu4!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val intent = Intent(this@MainActivity,HospitalActivity::class.java)
                startActivity(intent)

            }
        })

        blueConnect = findViewById(R.id.button3)

        blueConnect!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                 if (bt!!.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt!!.disconnect();
                } else {
                    val intent = Intent(this@MainActivity, DeviceList::class.java)
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }


            }
        })

        bt = BluetoothSPP(this@MainActivity)
       /* var credentialsProvider = CognitoCachingCredentialsProvider(
            getApplicationContext(),
            "ap-northeast-2:89f11573-bcf8-4e9c-a8ec-f962f0cfa2ef", // 자격 증명 풀 ID
            Regions.AP_NORTHEAST_2 // 리전
        );
*/
        if (!bt!!.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt!!.setOnDataReceivedListener(object :BluetoothSPP.OnDataReceivedListener {
            override fun onDataReceived(data: ByteArray?, message: String?) {

                //여기서 블루투스를 받아온다
                val value = message?.toInt()
                Log.d("asdasd","블루투스 넘어온값 : " +value.toString())
                val second : SecondActivity = SecondActivity(this@MainActivity)
                HotSpotOnOff(value)

                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show();
            } //데이터 수신

        });

        bt!!.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceDisconnected() {

                val intent = Intent(this@MainActivity,locationService::class.java)

                stopService(intent)

                with(NotificationManagerCompat.from(locationService.sercon!!)) {
                    // notificationId is a unique int for each notification that you must define
                    cancel(1)
                }
                Toast.makeText(getApplicationContext()
                    , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            override fun onDeviceConnected(name: String?, address: String?) {
                Toast.makeText(getApplicationContext()
                    , "Connected to " + name + "\n" + address
                    , Toast.LENGTH_SHORT).show();  }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                    , "Unable to connect", Toast.LENGTH_SHORT).show();
            } //연결됐을 때

        });



        hot = findViewById(R.id.button2)

        hot!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

Toast.makeText(this@MainActivity,"HotSpot버튼",Toast.LENGTH_LONG).show()

            }
        })

        val credentialsProvider = CognitoCachingCredentialsProvider(
            applicationContext,
            "ap-northeast-2:4d270aa1-6887-4377-9e0c-742ccf63369c", // 자격 증명 풀 ID
            Regions.AP_NORTHEAST_2 // 리전
        )


        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString()


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




    override fun onDestroy() {
        super.onDestroy()
        bt!!.stopService()
    }

    override fun onStart() {
        super.onStart()
          if (!bt!!.isBluetoothEnabled()) { //
            var intent =Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt!!.isServiceAvailable()) {
                bt!!.setupService();
                bt!!.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                //setup()
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        val intent = Intent(this@MainActivity,locationService::class.java)

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE)
        {
            if (resultCode == Activity.RESULT_OK) {


                Log.d("asdasd","44")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)

                    Log.d("asdasd","33")

                }
                else
                {
                    startService(intent)
                }
                bt!!.connect(data);
            }
        }
        else if (requestCode == BluetoothState.REQUEST_ENABLE_BT)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Log.d("asdasd","11")
                bt!!.setupService();
                bt!!.startService(BluetoothState.DEVICE_OTHER);


            } else {
                Toast.makeText(getApplicationContext()
                    , "Bluetooth was not enabled."
                    , Toast.LENGTH_SHORT).show();
                finish();
            }
        }


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
                    runOnUiThread { }
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

                    runOnUiThread { }
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

    fun HotSpotOnOff(i : Int?)
    {

        if(i==1)
        {
            val intent = Intent(getString(R.string.intent_action_turnon))
            sendImplicitBroadcast(this@MainActivity, intent)
        }
        else
        {
            val intent = Intent(getString(R.string.intent_action_turnoff))
            sendImplicitBroadcast(this@MainActivity, intent)
        }

    }
    private fun sendImplicitBroadcast(ctxt: Context, i: Intent) {
        val pm = ctxt.packageManager
        val matches = pm.queryBroadcastReceivers(i, 0)

        for (resolveInfo in matches) {
            val explicit = Intent(i)
            val cn = ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name
            )

            explicit.component = cn
            ctxt.sendBroadcast(explicit)
        }
    }
    companion object
    {
        var bt : BluetoothSPP?=null
    }

}