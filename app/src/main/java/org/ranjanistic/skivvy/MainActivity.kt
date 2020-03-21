package org.ranjanistic.skivvy

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
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
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    var deviceManger: DevicePolicyManager? = null
    var compName: ComponentName? = null
    var activityManager: ActivityManager? = null
    private var tts: TextToSpeech? = null
    private var outPut: TextView? = null
    private var rotateAnimation: Animation? = null
    private var exitAnimation: Animation? = null
    private var fadeAnimation: Animation? = null
    private var receiver: TextView? = null
    private var CODE_SPEECH_RECORD = 10
    private var loading: ImageView? = null
    private var icon: ImageView? = null
    private var txt: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceManger = getSystemService(
            Context.DEVICE_POLICY_SERVICE
        ) as DevicePolicyManager
        activityManager = getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        compName = ComponentName(this, Administrator::class.java)

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.setting)
            .setOnClickListener {
                setTheme(R.style.DarkTheme)
            }
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
            loading?.setImageDrawable(getDrawable(ic_dotsincircle))
            loading?.visibility = View.VISIBLE
            loading?.startAnimation(rotateAnimation)
            outPut?.text = null
            icon?.setImageDrawable(null)
            startVoiceRecIntent()
        }
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
        tts!!.language = Locale.US
        when(requestCode) {
            CODE_SPEECH_RECORD -> {
                if (resultCode == Activity.RESULT_OK && data != null)
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toLowerCase(Locale.ROOT)
                if(txt!=null) {
                    outPut?.text = txt
                    respondToCommand(txt)
                    txt = null
                }
                else {
                    errorView()
                    outPut?.text = getString(no_input)
                    speakOut(getString(no_input))
                }
            }
        } 
    }

    private fun respondToCommand(txt:String?){
        var localTxt = txt
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var i = 0
        var flag = false
        val arr: IntArray = intArrayOf(bt, screenshot, lock_screen, wifi, gps)
        while (i < arr.size) {
            if (localTxt == (getString(arr[i]))) {
                outPut?.text = getString(arr[i])
                successView()
                if (localTxt == getString(bt)) {
                    icon!!.setImageDrawable(getDrawable(ic_bluetooth))
                    if (mBluetoothAdapter.isEnabled) {
                        errorView()
                        speakOut("Bluetooth is already on")
                    } else {
                        speakOut("Turning on bluetooth")
                        mBluetoothAdapter.enable()
                    }
                } else if (localTxt == getString(wifi)) {
                    speakOut("Turning on Wi-Fi")
                } else if (localTxt == getString(gps)) {
                    speakOut("All satelllites on you")
                } else if (localTxt == getString(lock_screen)) {
                    val active: Boolean? =
                        compName?.let { deviceManger?.isAdminActive(it) }
                    if (active!!) {
                        deviceManger?.lockNow()
                        speakOut("Locked")
                    }
                } else if (localTxt == getString(screenshot)) {
                    speakOut("screen shot taken")
                    takeScreenshot()
                } else {
                    speakOut("Will understand it soon")
                    flag = false
                    break
                }
                flag = true
                localTxt = null
                break
            }
            ++i
        }
        if (!flag) {
            when (localTxt) {
                "whatsapp" -> {
                    successView()
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.component =
                        ComponentName("com.whatsapp", "com.whatsapp.Main")
                    startActivity(intent)
                    localTxt = null
                }
                "insta", "instagram" -> {
                    successView()
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.component = ComponentName(
                        "com.instagram.android",
                        "com.instagram.mainactivity.LauncherActivity"
                    )
                    startActivity(intent)
                    localTxt = null
                }
                "camera" -> {
                    successView()
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.component =
                        ComponentName("com.android.camera", "com.android.camera.Camera")
                    startActivity(intent)
                    localTxt = null
                }
                else -> {
                    errorView()
                    outPut?.text = getString(recognize_error)
                    speakOut(getString(recognize_error))
                    localTxt = null
                }
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
            val quality = 500
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
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
        loading?.startAnimation(exitAnimation)
        super.onBackPressed()
    }

    private fun errorView(){
        loading?.startAnimation(fadeAnimation)
        loading?.setImageDrawable(getDrawable(ic_red_dotsincircle))
    }
    private fun successView(){
        loading?.setImageDrawable(getDrawable(ic_green_dotsincircle))
    }
     private fun speakOut(text:String) {
         tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
     }
 }
