package org.ranjanistic.skivvy

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
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.format.DateFormat
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    var activityManager: ActivityManager? = null
    private var tts: TextToSpeech? = null
    private var outPut: TextView? = null
    private var rotateAnimation: Animation? = null
    private var exitAnimation: Animation? = null
    private var fadeAnimation: Animation? = null
    private var receiver: TextView? = null
    private var CODE_SPEECH_RECORD = 10
    private var CODE_OTHER_APP = 11
    private var CODE_LOCATION_SERVICE = 100
    private var CODE_LOCK_SCREEN = 101
    private var loading: ImageView? = null
    private var icon: ImageView? = null
    private var txt: String? = null
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
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        compName = ComponentName(this, Administrator::class.java)
        adminActive = deviceManger!!.isAdminActive(compName!!)

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.setting)
            .setOnClickListener {
                setTheme(R.style.DarkTheme)
            }
        getPackages()
        outPut = findViewById(R.id.textOutput)
        loading = findViewById(R.id.loader)
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.visibility = View.INVISIBLE
        icon = findViewById(R.id.actionIcon)
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_emerge)
        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        exitAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_exit)
        receiver = findViewById(R.id.receiverBtn)
        var result: Int?
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                result = tts!!.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    Toast.makeText(this, "The Language specified is not supported!", LENGTH_SHORT)
                        .show()
            } else
                Toast.makeText(this, "ERROR OUTPUT", LENGTH_SHORT).show()
        })

        receiver?.setOnClickListener {
            setButtonState(false)
            normalView()
            startVoiceRecIntent()
        }
    }

    override fun onStart() {
        super.onStart()
        setButtonState(true)
    }
    private fun setButtonState(state:Boolean){
        receiver?.isClickable = state
    }

    private fun normalView(){
        txt = null
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.visibility = View.VISIBLE
        loading?.startAnimation(rotateAnimation)
        outPut?.text = null
        icon?.setImageDrawable(null)
    }

    private fun startVoiceRecIntent(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, CODE_SPEECH_RECORD)
        else
            Toast.makeText(this, "Error", LENGTH_SHORT).show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonState(true)
        tts!!.language = Locale.US
        when(requestCode) {
            CODE_SPEECH_RECORD -> {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                if (resultCode == Activity.RESULT_OK && data != null)
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString().toLowerCase(Locale.US)
                if(txt!=null) {
                    outPut?.text = txt
                    if(!respondToCommand(txt)) {
                        if (!appOptions(txt)) {
                            if(!directActions(txt)) {
                                errorView()
                                outPut?.text = getString(recognize_error)
                                speakOut(getString(recognize_error))
                            }
                        }
                    }
                }
                else {
                    errorView()
                    outPut?.text = getString(no_input)
                    speakOut(getString(no_input))
                }
            }
            CODE_OTHER_APP ->{
                speakOut("Job done")
            }
            CODE_LOCATION_SERVICE ->{
                val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    successView(null)
                    speakOut("All satellites on you now")
                } else {
                    errorView()
                    speakOut("GPS not enabled")
                }
            }
            CODE_LOCK_SCREEN ->{
                if(resultCode == Activity.RESULT_OK){
                    deviceLockOps()
                } else {
                    errorView()
                    outPut?.text = getString(device_admin_request)
                    speakOut(getString(device_admin_request))
                }
            }
        }
    }

    private fun respondToCommand(text:String?):Boolean{
        var i = 0
        var flag = false
        val arr: IntArray = intArrayOf(bt, screenshot, lock_screen, wifi, gps, wi_fi)
        while (i < arr.size) {
            if (text == (getString(arr[i]))) {
                flag = true
                outPut?.text = getString(arr[i])
                if (text == getString(bt)) {
                    bluetoothOps()
                } else if (text == getString(wifi)) {
                    wifiOps()
                } else if(text == getString(wi_fi)){
                    wifiOps()
                } else if (text == getString(gps)) {
                    locationOps()
                } else if (text == getString(lock_screen)) {
                    deviceLockOps()
                } else if (text == getString(screenshot)) {
                     //Runtime.getRuntime().exec("input keyevent 120")
                    takeScreenshot()
                } else {
                    flag = false
                    break
                }
                break
            }
            ++i
        }
        return flag
    }
    private fun bluetoothOps(){
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
            speakOut("Bluetooth is off")
        } else {
            successView(getDrawable(ic_bluetooth))
            mBluetoothAdapter.enable()
            speakOut("Bluetooth is on")
        }
    }
    private fun wifiOps(){
        val wifiManager: WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(wifiManager.isWifiEnabled) {
            errorView()
            wifiManager.isWifiEnabled = false
            icon!!.setImageDrawable(getDrawable(ic_wifi_disconnected))
            speakOut("Turned Wi-Fi off")
        } else {
            successView(getDrawable(ic_wifi_connected))
            wifiManager.isWifiEnabled = true
            speakOut("Wi-Fi is looking for available hot spots")
        }
    }
    private fun locationOps(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent,CODE_LOCATION_SERVICE);
    }
    private fun deviceLockOps(){
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (deviceManger!!.isAdminActive(ComponentName(this, Administrator::class.java))) {
            successView(getDrawable(ic_glossylock))
            speakOut("Locked")
            deviceManger!!.lockNow()
        } else {
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
                    if (text.let { packagesAppName[i]!!.indexOf(it) } != -1) {
                        flag = successView(packagesIcon[i])
                        startActivityForResult(Intent(packagesMain[i]), CODE_OTHER_APP)
                        break
                    } else if (text.let { packagesName[i]!!.indexOf(it) } != -1) {
                        flag = successView(packagesIcon[i])
                        startActivityForResult(Intent(packagesMain[i]), CODE_OTHER_APP)
                        break
                    }
                    ++i
                }
            } else {
                speakOut("Internal error")
            }
        } else {
            flag = errorView()
        }
        return flag
    }

    private fun directActions(text: String?):Boolean{
        var flag = false
        fun String.intOrString(): Any {
            return when(val v = toIntOrNull()) {
                null -> this
                else -> v
            }
        }
        return flag
    }
    private fun getPackages(){
        val pm: PackageManager = packageManager
        val packages: List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packagesTotal = packages.size
        packagesAppName = arrayOfNulls(packages.size)
        packagesName = arrayOfNulls(packages.size)
        packagesIcon = arrayOfNulls(packages.size)
        packagesMain = arrayOfNulls(packages.size)
        for ((counter, packageInfo) in packages.withIndex()) {
            packagesAppName[counter] = pm.getApplicationLabel(packageInfo).toString()
            packagesName[counter] = packageInfo.packageName
            packagesIcon[counter] = pm.getApplicationIcon(packageInfo)
            packagesMain[counter] = pm.getLaunchIntentForPackage(packageInfo.packageName)
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
            speakOut("screen shot taken")
        } catch (e: Throwable) {
            errorView()
            speakOut("Can't take screenshot")
            e.printStackTrace()
        }
    }

     public override fun onDestroy() {
         if (tts != null) {
             tts!!.stop()
             tts!!.shutdown()
         }
         super.onDestroy()
     }

    override fun onBackPressed() {
        speakOut("I am, Skivvy")
        loading?.startAnimation(exitAnimation)
        super.onBackPressed()
    }

    private fun errorView():Boolean{
        loading?.startAnimation(fadeAnimation)
        loading?.setImageDrawable(getDrawable(ic_red_dotsincircle))
        return false
    }
    private fun successView(image:Drawable?):Boolean{
        loading?.setImageDrawable(getDrawable(ic_green_dotsincircle))
        icon?.setImageDrawable(image)
        return true
    }
     private fun speakOut(text:String) {
         outPut?.text = text
         tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
     }
 }
