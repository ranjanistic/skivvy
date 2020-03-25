@file:Suppress( "PrivateProp ertyName")

package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.format.DateFormat
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity(){
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
    private var receiver: TextView? = null
    private lateinit var setting:ImageButton
    private var greet: TextView? = null
    private var tempPackageIndex:Int? = null
    private var loading: ImageView? = null
    private var icon: ImageView? = null
    private var txt: String? = null
    private var tempPhone:String? = null
    private var tempContact:String? = null
    private lateinit var backfall: ImageView
    private lateinit var packagesAppName:Array<String?>
    private lateinit var packagesName:Array<String?>
    private lateinit var packagesMain:Array<Intent?>
    private lateinit var packagesIcon:Array<Drawable?>
    lateinit var skivvy:Skivvy
    private lateinit var context:Context
    private var packagesTotal:Int = 0
    private var deviceManger: DevicePolicyManager? = null
    private var compName: ComponentName? = null
    private var contact:ContactModel = ContactModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        skivvy = this.application as Skivvy
        setViewAndDefaults()
        loadDefaultAnimations()
        normalView()
        setListeners()
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            when(it){
                TextToSpeech.SUCCESS ->{
                    when(tts!!.setLanguage(skivvy.locale)){
                        TextToSpeech.LANG_MISSING_DATA,
                        TextToSpeech.LANG_NOT_SUPPORTED -> outPut!!.text =  getString(language_not_supported)
                    }
                } else -> outPut!!.text =  getString(output_error)
            }
        })
        //TODO: Long running task to be in background
        getLocalPackages()
    }

    private fun setViewAndDefaults(){
        skivvy.locale = Locale.US
        setting = findViewById(R.id.setting)
        outPut = findViewById(R.id.textOutput)
        input = findViewById(R.id.textInput)
        loading = findViewById(R.id.loader)
        icon = findViewById(R.id.actionIcon)
        receiver = findViewById(R.id.receiverBtn)
        greet = findViewById(R.id.greeting)
        backfall = findViewById(R.id.backdrop)
    }

    private fun loadDefaultAnimations(){
        fallAnimation = AnimationUtils.loadAnimation(context,R.anim.fall_back)
        riseAnimation = AnimationUtils.loadAnimation(context,R.anim.rise_back)
        bubbleAnimation = AnimationUtils.loadAnimation(context,R.anim.bubble_wave)
        normalRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_emerge_demerge)
        focusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_focus)
        rotateSlow = AnimationUtils.loadAnimation(context, R.anim.rotate_slow)
        fadeAnimation = AnimationUtils.loadAnimation(context, R.anim.fade)
        exitAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_exit)
        backfall.startAnimation(fallAnimation)
        setting.startAnimation(bubbleAnimation)
        receiver!!.startAnimation(bubbleAnimation)
        greet!!.startAnimation(bubbleAnimation)
    }
    private fun setListeners(){
        setting.setOnClickListener {
            setButtonsClickable(false)
            startActivity(Intent(context,Setup::class.java))
        }
        receiver?.setOnClickListener {
            setButtonsClickable(false)
            normalView()
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        }
    }

    override fun onStart() {
        super.onStart()
        setButtonsClickable(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            skivvy.CODE_CALL_REQUEST -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    when {
                        contact.phoneList[0]!=null -> {
                            speakOut(getString(should_i_call) + "${contact.displayName}?", skivvy.CODE_CONTACT_CALL_CONF)
                        }
                        tempPhone!=null -> {
                            speakOut(getString(should_i_call) + "$tempPhone?", skivvy.CODE_CALL_CONF)
                        }
                        else -> {
                            speakOut(getString(null_variable_error))
                        }
                    }
                } else {
                    tempPhone = null
                    errorView()
                    speakOut(getString(call_permit_denied))
                }
            }
            skivvy.CODE_STORAGE_REQUEST ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takeScreenshot()
                } else {
                    errorView()
                    speakOut(getString(storage_permission_denied))
                }
            }
            skivvy.CODE_CONTACTS_REQUEST->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(tempContact!=null) {
                        contactOps(tempContact!!)
                    } else {
                        speakOut(getString(null_variable_error))
                    }
                }  else {
                    errorView()
                    speakOut(getString(contact_permission_denied))
                }
            }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonsClickable(true)
        tts!!.language = skivvy.locale
        when (requestCode) {
            skivvy.CODE_SPEECH_RECORD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    if (txt != null) {
                        input?.text = txt
                        if (!respondToCommand(txt)) {
                            if (!appOptions(txt)) {
                                if (!directActions(txt!!)) {
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
            skivvy.CODE_OTHER_APP_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    if (txt != null) {
                        if (resources.getStringArray(R.array.acceptances).contains(txt)) {
                            if (tempPackageIndex != null) {
                                successView(packagesIcon[tempPackageIndex!!])
                                speakOut(getString(opening) + packagesAppName[tempPackageIndex!!])
                                startActivityForResult(
                                    Intent(packagesMain[tempPackageIndex!!]),
                                    skivvy.CODE_OTHER_APP
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
                            speakOut(
                                getString(recognize_error) + getString(do_u_want_open) + packagesAppName[tempPackageIndex!!] + "?",
                                skivvy.CODE_OTHER_APP_CONF
                            )
                        }
                    }
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }
            skivvy.CODE_OTHER_APP -> {
                txt = null
                tempPackageIndex = null
            }
            skivvy.CODE_LOCATION_SERVICE -> {
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
            skivvy.CODE_LOCK_SCREEN -> {
                if (resultCode == Activity.RESULT_OK) {
                    deviceLockOps()
                } else {
                    errorView()
                    speakOut(getString(device_admin_failure))
                }
            }
            skivvy.CODE_CALL_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    when {
                        resources.getStringArray(R.array.acceptances).contains(txt) -> {
                            successView(getDrawable(ic_glossyphone))
                            callingOps(tempPhone)
                            tempPhone = null
                        }
                        resources.getStringArray(R.array.denials).contains(txt) -> {
                            tempPhone = null
                            normalView()
                        }
                        else -> {
                            waitingView(getDrawable(ic_glossyphone))
                            speakOut(
                                getString(recognize_error) + getString(should_i_call) + "$tempPhone?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                    }
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }
            skivvy.CODE_CONTACT_CALL_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString().toLowerCase(skivvy.locale)
                    when {
                        resources.getStringArray(R.array.acceptances).contains(txt) -> {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                speakOut(getString(require_physical_permission))
                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), skivvy.CODE_CALL_REQUEST)
                            } else {
                                successView(null)
                                callingOps(contact.phoneList[0], contact.displayName)
                            }
                        }
                        resources.getStringArray(R.array.denials).contains(txt) -> {
                            normalView()
                            speakOut(getString(okay))
                        }
                        else -> {
                            speakOut(getString(recognize_error) + getString(should_i_call) + "${contact.displayName}?", skivvy.CODE_CONTACT_CALL_CONF)
                        }
                    }
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }
        }
    }

    //actions invoking quick commands
    private fun respondToCommand(text:String?):Boolean{
        var flag = true
        val array = arrayOf(R.array.setup_list,R.array.bt_list,R.array.wifi_list,R.array.gps_list,R.array.lock_list,R.array.snap_list)
        when {
            resources.getStringArray(array[0]).contains(text) -> {
              startActivity(Intent(context,Setup::class.java))
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
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    speakOut(getString(require_physical_permission))
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),skivvy.CODE_STORAGE_REQUEST)
                } else {
                    takeScreenshot()
                }
            }
            text == getString(exit) -> {
                finish()
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
        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),skivvy.CODE_LOCATION_SERVICE)
    }
    private fun deviceLockOps(){
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(context, Administrator::class.java)
        if (deviceManger!!.isAdminActive(compName!!)) {
            successView(getDrawable(ic_glossylock))
            speakOut(getString(screen_locked))
            deviceManger!!.lockNow()
        } else {
            waitingView(getDrawable(ic_glossylock))
            speakOut(getString(device_admin_request))
            startActivityForResult(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(device_admin_persuation)), skivvy.CODE_LOCK_SCREEN)
        }
    }

    //actions invoking other applications
    private fun appOptions(text: String?):Boolean{
        var flag = false
        if(text!=null) {
            if (packagesTotal > 0) {
                var i=0
                while (i < packagesTotal) {
                    if(text == getString(app_name).toLowerCase(skivvy.locale)){
                        flag = true
                        speakOut(getString(i_am) +  getString(app_name))
                        break
                    } else if (text == packagesAppName[i]) {
                        flag = successView(packagesIcon[i])
                        speakOut(getString(opening) + packagesAppName[i])
                        startActivityForResult(Intent(packagesMain[i]), skivvy.CODE_OTHER_APP)
                        break
                    } else if (text.let { packagesName[i]!!.indexOf(it) } != -1) {
                        flag = true
                        tempPackageIndex = i
                        waitingView(packagesIcon[i])
                        speakOut(getString(do_u_want_open) + packagesAppName[i]+ "?",skivvy.CODE_OTHER_APP_CONF)
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

    //action invoking direct intents
    private fun directActions(text: String):Boolean{
        var localTxt = text
        if(localTxt.contains(getString(call))) {
            waitingView(getDrawable(ic_glossyphone))
            localTxt = localTxt.replace("call", "",true)
            tempPhone = localTxt.replace("[^0-9]".toRegex(), "")
            if(tempPhone!=null) {
                 if (tempPhone!!.contains("[0-9]".toRegex())) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        speakOut(getString(require_physical_permission))
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),
                            skivvy.CODE_CALL_REQUEST)
                    } else {
                        speakOut(getString(should_i_call) + "$tempPhone?", skivvy.CODE_CALL_CONF)
                    }
                 } else {
                     //speakOut("Looking into contacts")
                    return contactOps(localTxt)
                 }
            } else {
                return false
            }
            return true
        } else if(localTxt.contains("email") || localTxt.contains("mail")){
            speakOut("yeah email")
            return true
        }
        return false
    }
    @SuppressLint("MissingPermission")
    private fun callingOps(number:String?){
        if(number!=null) {
            speakOut(getString(calling)+"$number")
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        } else {
            errorView()
            speakOut(getString(null_variable_error))
        }
    }
    @SuppressLint("MissingPermission")
    private fun callingOps(number:String?,name: String){
        if(number!=null) {
            speakOut(getString(calling)+name)
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        } else {
            errorView()
            speakOut(getString(null_variable_error))
        }
    }

    //TODO: Contacts search and dial
    private fun contactOps(name:String):Boolean{
        waitingView(getDrawable(ic_glossyphone))
        var isContactPresent = false
        var isPhoneNumberPresent = false
        var isImagePresent = false
        tempContact = name.trim()
        Log.d("CID-IN",tempContact!!)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            speakOut(getString(require_physical_permission))
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),skivvy.CODE_CONTACTS_REQUEST)
        } else {
            //TODO: Inspect this
            val cr: ContentResolver = contentResolver
            val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur?.count !! > 0) {
                while (cur.moveToNext()) {
                    val n = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    Log.d("CID",n)
                    if(tempContact ==  n.toLowerCase(skivvy.locale)){
                        isContactPresent = true
                        contact.contactID = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        contact.displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val pUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                        if(pUri!=null){
                            contact.photoID =pUri
                            isImagePresent = true
                        }
                        if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt() > 0) {
                            isPhoneNumberPresent = true
                            val pCur: Cursor? = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contact.contactID),
                                null)
                            var o = 0
                            while (pCur?.moveToNext()!!) {
                                ++o
                            }
                            contact.phoneList = arrayOfNulls(o)
                            pCur.moveToFirst()
                            o = 0
                            while (pCur.moveToNext()) {
                                val s = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                contact.phoneList[o] = s
                                ++o
                            }
                            pCur.close()
                        } else isPhoneNumberPresent = false
                        break
                    } else isContactPresent = false
                }
            }
            cur.close()
            if(isContactPresent){
                if(isImagePresent) {
                    val uri:Uri = Uri.parse(contact.photoID)
                    val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    val d: Drawable = BitmapDrawable(resources, bitmap)
                    waitingView(d)
                }
                if(isPhoneNumberPresent){
                    speakOut(getString(should_i_call) + "${contact.displayName}?",skivvy.CODE_CONTACT_CALL_CONF)
                } else {
                    errorView()
                    speakOut("You don't have ${contact.displayName}'s phone number")
                }
            } else {
                errorView()
                speakOut(getString(contact_not_found))
            }
        }
        return true
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(code:Int){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
        else{
            errorView()
            outPut!!.text = getString(error)
        }
    }

    //gets all packages and respective details available on device
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
                    pm.getApplicationLabel(packageInfo).toString().toLowerCase(skivvy.locale)
                packagesName[counter] = packageInfo.packageName.toLowerCase(skivvy.locale)
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

    private fun normalView(){
        txt = null
        tempPackageIndex = null
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.startAnimation(normalRotate)
        input?.text = null
        outPut?.text = null
        icon?.setImageDrawable(null)
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
        if(image!=null) {
            icon?.setImageDrawable(image)
        }
        return true
    }

    private fun setButtonsClickable(state:Boolean){
        receiver?.isClickable = state
        setting.isClickable = state
    }
     private fun speakOut(text:String) {
         outPut?.text = text
         tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
             override fun onDone(utteranceId: String) {}
             override fun onError(utteranceId: String) {}
             override fun onStart(utteranceId: String) {}
         })
         tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
     }
    private fun speakOut(text:String,code:Int?){
        outPut?.text = text
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if(code!=null){ startVoiceRecIntent(code) }
            }
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    private fun getTrainingStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_TRAINING, false)
    }
    private fun getMuteStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, false)
    }
 }
