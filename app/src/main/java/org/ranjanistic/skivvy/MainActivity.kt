@file:Suppress( "PrivatePropertyName")

package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.media.MediaRouter
import android.media.VolumeProvider
import android.media.VolumeShaper
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.format.DateFormat
import android.util.Log
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MotionEventCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import java.io.File
import java.io.FileOutputStream
import java.security.Permissions
import java.util.*


class MainActivity : AppCompatActivity(){
    var activityManager: ActivityManager? = null
    private var tts: TextToSpeech? = null
    private var outPut: TextView? = null
    private var input: TextView? = null
    private var focusRotate: Animation? = null
    private var normalRotate: Animation? = null
    private var rotateSlow: Animation? = null
    private var exitAnimation: Animation? = null
    private var fadeAnimation: Animation? = null
    private var bubbleAnimation: Animation? = null
    private var fallAnimation: Animation? = null
    private var riseAnimation:Animation? = null
    private lateinit var locale:Locale
    private var receiver: TextView? = null
    private var greet: TextView? = null
    private var CODE_CALL_REQUEST = 1000
    private var CODE_STORAGE_REQUEST = 1001
    private var CODE_SPEECH_RECORD = 10
    private var CODE_OTHER_APP = 11
    private var CODE_OTHER_APP_CONF = 12
    private  var CODE_CALL_CONF = 13
    private var CODE_LOCATION_SERVICE = 100
    private var CODE_LOCK_SCREEN = 101
    private var tempPackageIndex:Int? = null
    private var loading: ImageView? = null
    private var icon: ImageView? = null
    private var txt: String? = null
    private var tempPhone:String? = null
    private lateinit var backfall: ImageView
    private lateinit var packagesAppName:Array<String?>
    private lateinit var packagesName:Array<String?>
    private lateinit var packagesMain:Array<Intent?>
    private lateinit var packagesIcon:Array<Drawable?>

    private var packagesTotal:Int = 0
    private var deviceManger: DevicePolicyManager? = null
    private var compName: ComponentName? = null
    var adminActive:Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        compName = ComponentName(this, Administrator::class.java)
        adminActive = deviceManger!!.isAdminActive(compName!!)
        locale = Locale.US
        bubbleAnimation = AnimationUtils.loadAnimation(this,R.anim.bubble_wave)
        fallAnimation = AnimationUtils.loadAnimation(this,R.anim.fall_back)
        riseAnimation = AnimationUtils.loadAnimation(this,R.anim.rise_back)
         backfall = findViewById(R.id.backdrop)
        backfall.startAnimation(fallAnimation)
        val setting:ImageButton = findViewById(R.id.setting)
        setting.setOnClickListener {
            startActivity(Intent(this,Setup::class.java))
        }
        setting.startAnimation(bubbleAnimation)
        outPut = findViewById(R.id.textOutput)
        input = findViewById(R.id.textInput)
        loading = findViewById(R.id.loader)
        icon = findViewById(R.id.actionIcon)
        normalRotate = AnimationUtils.loadAnimation(this, R.anim.rotate_emerge_demerge)
        focusRotate = AnimationUtils.loadAnimation(this, R.anim.rotate_focus)
        rotateSlow = AnimationUtils.loadAnimation(this, R.anim.rotate_slow)
        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        exitAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_exit)
        receiver = findViewById(R.id.receiverBtn)
        receiver!!.startAnimation(bubbleAnimation)
        greet = findViewById(R.id.greeting)
        greet!!.startAnimation(bubbleAnimation)
        normalView()
        var result: Int?
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                result = tts!!.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    Toast.makeText(this, getString(language_not_supported), LENGTH_SHORT)
                        .show()
            } else
                Toast.makeText(this, getString(output_error), LENGTH_SHORT).show()
        })

        receiver?.setOnClickListener {
            setButtonState(false)
            normalView()
            startVoiceRecIntent(CODE_SPEECH_RECORD)
        }
        getLocalPackages()
    }

    override fun onStart() {
        super.onStart()
        setButtonState(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CODE_CALL_REQUEST -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    speakOut(getString(should_i_call)+"$tempPhone ?", CODE_CALL_CONF)
                } else {
                    errorView()
                    speakOut(getString(call_permit_denied))
                }
            }
            CODE_STORAGE_REQUEST ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takeScreenshot()
                } else {
                    errorView()
                    speakOut(getString(storage_permission_denied))
                }
            }
        }
    }

    private fun setButtonState(state:Boolean){
        receiver?.isClickable = state
    }

    @SuppressLint("MissingPermission")
    private fun callingOps(number:String?){
        if(number!=null) {
            successView(getDrawable(ic_glossyphone))
            speakOut("Calling $number")
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        } else {
            errorView()
            speakOut(getString(null_variable_error))
        }
    }
    private fun normalView(){
        txt = null
        tempPackageIndex = null
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.startAnimation(normalRotate)
        input?.text = null
        outPut?.text = null
        icon?.setImageDrawable(null)
    }

    private fun startVoiceRecIntent(code:Int){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
        else
            Toast.makeText(this, getString(error), LENGTH_SHORT).show()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (MotionEventCompat.getActionMasked(event)) {
            MotionEvent.ACTION_DOWN -> {
                backfall.startAnimation(fallAnimation)
                loading?.startAnimation(normalRotate)
                true
            }
            MotionEvent.ACTION_UP -> {
                backfall.startAnimation(riseAnimation)
                loading?.startAnimation(exitAnimation)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonState(true)
        tts!!.language = locale
            when (requestCode) {
                CODE_SPEECH_RECORD -> {
                        if (resultCode == Activity.RESULT_OK  && data!=null) {
                            txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                                .toLowerCase(locale)
                            if (txt != null) {
                                input?.text = txt
                                if (!respondToCommand(txt)) {
                                    if (!appOptions(txt)) {
                                        if (!directActions(txt)) {
                                            errorView()
                                            speakOut(getString(recognize_error))
                                        }
                                    }
                                }
                            }
                        } else {
                            speakOut(getString(no_input))
                        }
                }
                CODE_OTHER_APP_CONF -> {
                    if (resultCode == Activity.RESULT_OK && data!=null) {
                        txt =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                                .toLowerCase(locale)
                        if (txt != null) {
                            if (resources.getStringArray(R.array.acceptances).contains(txt)) {
                                if (tempPackageIndex != null) {
                                    successView(packagesIcon[tempPackageIndex!!])
                                    speakOut(getString(opening) + packagesAppName[tempPackageIndex!!])
                                    startActivityForResult(
                                        Intent(packagesMain[tempPackageIndex!!]),
                                        CODE_OTHER_APP
                                    )
                                    tempPackageIndex = null
                                } else {
                                    errorView()
                                    speakOut(getString(null_variable_error))
                                }
                            } else if (resources.getStringArray(R.array.denials).contains(txt)) {
                                tempPackageIndex = null
                                normalView()
                                speakOut(getString(okay))
                            } else {
                                waitingView(packagesIcon[tempPackageIndex!!])
                                speakOut(getString(recognize_error) + getString(do_u_want_open) + packagesAppName[tempPackageIndex!!] + "?")
                            }
                        }
                    } else {
                        normalView()
                        speakOut(getString(no_input))
                    }
                }
                CODE_OTHER_APP -> {
                    txt = null
                    tempPackageIndex = null
                }
                CODE_LOCATION_SERVICE -> {
                    val locationManager =
                        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        successView(getDrawable(ic_location_pointer))
                        speakOut(getString(gps_enabled))
                    } else {
                        errorView()
                        speakOut(getString(gps_is_off))
                    }
                }
                CODE_LOCK_SCREEN -> {
                    if (resultCode == Activity.RESULT_OK) {
                        deviceLockOps()
                    } else {
                        errorView()
                        speakOut(getString(device_admin_request))
                    }
                }
                CODE_CALL_CONF  ->{
                    if(resultCode  == Activity.RESULT_OK && data!=null){
                        txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString().toLowerCase(locale)
                        when {
                            resources.getStringArray(R.array.acceptances).contains(txt) -> {
                                callingOps(tempPhone)
                                tempPhone = null
                            }
                            resources.getStringArray(R.array.denials).contains(txt) -> {
                                tempPhone = null
                                normalView()
                            }
                            else -> {
                                waitingView(getDrawable(ic_glossyphone))
                                speakOut(getString(recognize_error) + getString(should_i_call)+"$tempPhone ?", CODE_CALL_CONF)
                            }
                        }
                    }
                }
            }
    }

    private fun respondToCommand(text:String?):Boolean{
        var flag = true
        val array = arrayOf(R.array.setup_list,R.array.bt_list,R.array.wifi_list,R.array.gps_list,R.array.lock_list,R.array.snap_list)
        when {
            resources.getStringArray(array[0]).contains(text) -> {
              startActivity(Intent(this,Setup::class.java))
            }
            resources.getStringArray(array[1]).contains(text) -> {
                bluetoothOps()
            }
            resources.getStringArray(array[2]).contains(text) -> {
                waitingView(getDrawable(ic_wifi_connected))
                wifiOps()
            }
            resources.getStringArray(array[3]).contains(text) -> {
                locationOps()
            }
            resources.getStringArray(array[4]).contains(text) -> {
                deviceLockOps()
            }
            resources.getStringArray(array[5]).contains(text) -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),CODE_STORAGE_REQUEST)
                } else {
                    takeScreenshot()
                }
            }
            text == getString(exit) -> {
                this.finish()
            }
            else -> {
                flag = false
            }
        }
        return flag
    }
    private fun bluetoothOps(){
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
            speakOut(getString(bt_off))
        } else {
            successView(getDrawable(ic_bluetooth))
            mBluetoothAdapter.enable()
            speakOut(getString(bt_on))
        }
    }
    private fun wifiOps(){
        val wifiManager: WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
            speakOut(getString(wifi_off))
        } else {
            successView(getDrawable(ic_wifi_connected))
            wifiManager.isWifiEnabled = true
            speakOut(getString(wifi_on))
        }
    }
    private fun locationOps(){
        waitingView(getDrawable(ic_location_pointer))
        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),CODE_LOCATION_SERVICE)
    }
    private fun deviceLockOps(){
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (deviceManger!!.isAdminActive(ComponentName(this, Administrator::class.java))) {
            successView(getDrawable(ic_glossylock))
            speakOut(getString(screen_locked))
            deviceManger!!.lockNow()
        } else {
            waitingView(getDrawable(ic_glossylock))
            speakOut(getString(device_admin_request))
            startActivityForResult(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(device_admin_persuation)), CODE_LOCK_SCREEN)
        }
    }

    private fun appOptions(text: String?):Boolean{
        var flag = false
        if(text!=null) {
            if (packagesTotal > 0) {
                var i=0
                while (i < packagesTotal) {
                    if(text == getString(app_name).toLowerCase(locale)){
                        flag = true
                        speakOut("I am " +  getString(app_name))
                        break
                    } else if (text == packagesAppName[i]) {
                        flag = successView(packagesIcon[i])
                        speakOut(getString(opening) + packagesAppName[i])
                        startActivityForResult(Intent(packagesMain[i]), CODE_OTHER_APP)
                        break
                    } else if (text.let { packagesName[i]!!.indexOf(it) } != -1) {
                        flag = true
                        tempPackageIndex = i
                        waitingView(packagesIcon[i])
                        speakOut(getString(do_u_want_open) + packagesAppName[i]+ "?")
                        break
                    } else {
                        flag = false
                    }
                    ++i
                }
            } else {
                speakOut(getString(internal_error))
            }
        } else {
            flag = errorView()
        }
        return flag
    }

    private fun directActions(text: String?):Boolean{
        var localTxt = text
        if(localTxt!!.contains("call")) {
            waitingView(getDrawable(ic_glossyphone))
            localTxt = localTxt.replace("call", "")
            tempPhone = localTxt.replace("[^0-9]".toRegex(), "")
            if(tempPhone!=null) {
                if (tempPhone!!.contains("[0-9]".toRegex())) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            CODE_CALL_REQUEST
                        )
                    } else {
                        speakOut(getString(should_i_call) + "$tempPhone ?", CODE_CALL_CONF)
                    }
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        }
        return false
    }
    private fun getLocalPackages(){
        var counter = 0
        val pm: PackageManager = packageManager
        val packages: List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packagesTotal = packages.size
        packagesAppName = arrayOfNulls(packagesTotal)
        packagesName = arrayOfNulls(packagesTotal)
        packagesIcon = arrayOfNulls(packagesTotal)
        packagesMain = arrayOfNulls(packagesTotal)
        for (packageInfo in packages) {
            if(pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                packagesAppName[counter] =
                    pm.getApplicationLabel(packageInfo).toString().toLowerCase(locale)
                packagesName[counter] = packageInfo.packageName.toLowerCase(locale)
                packagesIcon[counter] = pm.getApplicationIcon(packageInfo)
                packagesMain[counter] = pm.getLaunchIntentForPackage(packageInfo.packageName)
                ++counter
            } else {
                --packagesTotal
            }
        }
    }
    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {
            val mPath: String = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
            val v1 = window.decorView.rootView
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            speakOut(getString(snap_success))
        } catch (e: Throwable) {
            errorView()
            speakOut(getString(snap_failed))
            e.printStackTrace()
        }
    }

     public override fun onDestroy() {
         loading?.startAnimation(exitAnimation)
         speakOut(getString(exit_msg))
         if (tts != null) {
             tts!!.stop()
             tts!!.shutdown()
         }
         super.onDestroy()
     }

    override fun onBackPressed() {
        speakOut(getString(exit_msg))
        loading?.startAnimation(exitAnimation)
        super.onBackPressed()
    }

    private fun waitingView(image:Drawable?){
        loading?.startAnimation(rotateSlow)
        loading?.setImageDrawable(getDrawable(ic_yellow_dotsincircle))
        if(image!=null){
            icon?.setImageDrawable(image)
        }
    }
    private fun errorView():Boolean{
        loading?.startAnimation(fadeAnimation)
        loading?.setImageDrawable(getDrawable(ic_red_dotsincircle))
        return false
    }
    private fun successView(image:Drawable?):Boolean{
        loading?.startAnimation(focusRotate)
        loading?.setImageDrawable(getDrawable(ic_green_dotsincircle))
        icon?.setImageDrawable(image)
        return true
    }
     private fun speakOut(text:String) {
         outPut?.text = text
         tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
             override fun onDone(utteranceId: String) {
                 if(tempPackageIndex!=null){
                     startVoiceRecIntent(CODE_OTHER_APP_CONF)
                 }
             }
             override fun onError(utteranceId: String) {}
             override fun onStart(utteranceId: String) {}
         })
         tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
     }
    private fun speakOut(text:String,code:Int?){
        outPut?.text = text
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if(code!=null){
                    startVoiceRecIntent(code)
                }
            }
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    private fun getMuteStatus():Boolean{
        return getSharedPreferences("voicePreference", MODE_PRIVATE)
            .getBoolean("muted", false)
    }
 }
