package com.example.wlgusdn.iot


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
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
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.metrics.AwsSdkMetrics.setRegion
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament
import com.amazonaws.regions.Region
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


class HospitalActivity : FragmentActivity(), OnMapReadyCallback
{

    var mMap : GoogleMap?=null

     var gpsTracker : GpsTracker?=null

    var  school = LatLng(1.0, 1.0);

  val GPS_ENABLE_REQUEST_CODE = 2001;
    val PERMISSIONS_REQUEST_CODE = 100;
    val REQUIRED_PERMISSIONS  = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            var check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(this@HospitalActivity, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(this@HospitalActivity, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }

    }


    fun checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this@HospitalActivity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this@HospitalActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        }
        else
        {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@HospitalActivity, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@HospitalActivity, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this@HospitalActivity, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this@HospitalActivity, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    fun getCurrentAddress( latitude: Double, longitude : Double) : String {

        //지오코더... GPS를 주소로 변환
       val geocoder =  Geocoder(this@HospitalActivity, Locale.getDefault());

         var addresses : List<Address>

        try {

            addresses = geocoder.getFromLocation(
                latitude,
                longitude,
                7)

        } catch ( ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch ( illegalArgumentException : IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        var address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


   fun showDialogForLocationServiceSetting() {

        var builder = AlertDialog.Builder(this@HospitalActivity);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", DialogInterface.OnClickListener() {dialog: DialogInterface?, which: Int ->

            var callGPSSettingIntent= Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);




        });
        builder.setNegativeButton("취소", DialogInterface.OnClickListener() {dialog: DialogInterface?, which: Int ->
            dialog?.cancel();

        });
        builder.create().show();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            GPS_ENABLE_REQUEST_CODE ->

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

   fun checkLocationServicesStatus() : Boolean {
       var locationManager =  getSystemService(LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    override fun onMapReady(p0: GoogleMap?) {

         mMap = p0;

        // Add a marker in Sydney, Australia, and move the camera.
    }


    val LOG_TAG = "wlgusdn11"

    // --- Constants to modify per your configuration ---

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private val CUSTOMER_SPECIFIC_ENDPOINT = "a1o3rc9x07id8y-ats.iot.ap-northeast-2.amazonaws.com"
    // Name of the AWS IoT policy to attach to a newly created certificate
    private val AWS_IOT_POLICY_NAME = "IOTApp"

    // Region of AWS IoT
    private val MY_REGION = Regions.AP_NORTHEAST_2
    // Filename of KeyStore file on the filesystem
    private val KEYSTORE_NAME = "iot_keystore1"
    // Password for the private key in the KeyStore
    private val KEYSTORE_PASSWORD = "password1"
    // Certificate and key aliases in the KeyStore
    private val CERTIFICATE_ID = "default"

    var hot : Button?=null

    var txtSubcribe:EditText? = null
    var txtTopic:EditText? = null
    var txtMessage:EditText? = null
    var bu : Button?=null

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

    fun subscribeClick() {
        val topic = "\$aws/things/IOTApp/shadow/update"

        Log.d("asdasd", "topic = $topic")

        try
        {
            mqttManager!!.subscribeToTopic(topic, AWSIotMqttQos.QOS0
            ) { topic, data ->
                runOnUiThread {
                    try {
                        val message = String(data, charset("UTF-8"))
                        Log.d("asdasd", "Message arrived:")
                        Log.d("asdasd", "   Topic: $topic")
                        Log.d("asdasd", " Message: $message")



                        val str2 = message.split("{")[3]
                        Log.d("자른거","들어오기전"+str2)
                        if(str2.contains("Longitude"))
                        {

                            Log.d("자른거","들어온거 : "+str2)
                                var lati = message.split(",")[5]
                                lati = lati.split(":")[1]

                                var long = message.split(",")[6]
                                long = long.split(":")[1]
                                Log.d("asdasd", "Lati : " + lati)
                                Log.d("asdasd", "Long : " + long)
                                school = LatLng(lati.toDouble(), long.toDouble())
                                mMap?.addMarker(MarkerOptions().position(school).title("환자위치"));
                                mMap?.moveCamera(CameraUpdateFactory.newLatLng(school));
                                val zoom = CameraUpdateFactory.zoomBy(18.0f); // 범위 높을수록 확대가 커집니다.

                                mMap!!.animateCamera(zoom); // 줌을 당긴다


                                mMap!!.animateCamera(CameraUpdateFactory.newLatLng(school));

                        }
                        else
                        {
                            Log.d("asdasd","리포트가 아님")
                        }
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

    fun publishClick() {
        val topic = "\$aws/things/IOTApp/shadow/update"
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
    var lo : Button?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }
        lo=findViewById(R.id.btnPublish1)

        lo!!.setOnClickListener(object: View.OnClickListener
        {
            override fun onClick(v: View?)
            {

                publishClick()

                gpsTracker = GpsTracker(this@HospitalActivity)

                val latitude = gpsTracker?.latitude
                val longitude = gpsTracker?.longitude

                val address = getCurrentAddress(latitude!!, longitude!!)
                val url = "daummaps://route?sp=${latitude.toString().substring(0,9)},${longitude.toString().substring(0,10)}&ep=${school.latitude.toString()},${school.longitude.toString()}&by=CAR"

                //val url = "daummaps://route?sp=${latitude.toString()},${longitude.toString()}&ep=${school.latitude.toString()},${school.longitude.toString()}&by=CAR"
                val intent:Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent)
                Log.d("asdasd",url)
                Toast.makeText(this@HospitalActivity, "학교위치 ${school.latitude}, ${school.longitude}", Toast.LENGTH_LONG).show()

            }
        })
        bu = findViewById(R.id.sub)
        bu!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                subscribeClick()

            }
        })


        var mapFragment =  getSupportFragmentManager()
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this);


        /* var credentialsProvider = CognitoCachingCredentialsProvider(
             getApplicationContext(),
             "ap-northeast-2:89f11573-bcf8-4e9c-a8ec-f962f0cfa2ef", // 자격 증명 풀 ID
             Regions.AP_NORTHEAST_2 // 리전
         );
 */


      /*  hot!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val intent = Intent(this@HospitalActivity,SecondActivity::class.java)
                startActivity(intent)

            }
        })*/

        val credentialsProvider = CognitoCachingCredentialsProvider(
            applicationContext,
            "ap-northeast-2:4d270aa1-6887-4377-9e0c-742ccf63369c", // 자격 증명 풀 ID
            Regions.AP_NORTHEAST_2 // 리전
        )

        tvClientId = findViewById(R.id.tvClientId1)
        tvStatus = findViewById(R.id.tvStatus1)

        btnConnect = findViewById(R.id.btnConnect1)
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