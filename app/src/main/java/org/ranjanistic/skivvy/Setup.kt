package org.ranjanistic.skivvy

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.ranjanistic.skivvy.manager.SystemFeatureManager
import org.ranjanistic.skivvy.manager.TempDataManager
import java.util.concurrent.Executor


@ExperimentalStdlibApi
class Setup : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var settingsContainer: ConstraintLayout
    private lateinit var settingIcon: ImageView
    private lateinit var training: Switch
    private lateinit var mute: Switch
    private lateinit var urgentVolume: Switch
    private lateinit var urgentSeek: SeekBar
    private lateinit var normalizeVolume: Switch
    private lateinit var normalizeSeek: SeekBar
    private lateinit var theme: Switch
    private lateinit var themeChoices: RadioGroup
    private lateinit var saveTheme: TextView
    private lateinit var response: Switch
    private lateinit var handy: Switch
    private lateinit var angleUnit: Switch
    private lateinit var biometrics: Switch
    private lateinit var voiceAuth: Switch

    private lateinit var voiceHeader: TextView
    private lateinit var appSetupHeader: TextView
    private lateinit var mathsHead: TextView
    private lateinit var securityHead: TextView

    private lateinit var deleteVoiceSetup: TextView
    private lateinit var permissions: TextView
    private lateinit var deviceAdmin: TextView
    private lateinit var writeSettings: TextView
    private lateinit var accessNotifications: TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var context: Context
    private lateinit var scrollView: ScrollView
    private lateinit var noteView: TextView
    private var temp = TempDataManager()
    private var feature = SystemFeatureManager()
    private lateinit var recognitionIntent: Intent
    private var hasAllPermits: Boolean = false

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        context = this
        setTheme(skivvy.getThemeState())
        setContentView(R.layout.activity_setup)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        when {
            skivvy.getThemeState() == R.style.LightTheme -> {
                window.statusBarColor = ContextCompat.getColor(context, R.color.dull_white)
                window.navigationBarColor = ContextCompat.getColor(context, R.color.dull_white)
            }
            skivvy.getThemeState() == R.style.BlackTheme -> {
                window.statusBarColor = ContextCompat.getColor(context, R.color.pitch_black)
                window.navigationBarColor = ContextCompat.getColor(context, R.color.pitch_black)
            }
            else -> {
                window.statusBarColor = ContextCompat.getColor(context, R.color.dead_blue)
                window.navigationBarColor = ContextCompat.getColor(context, R.color.dead_blue)
            }
        }
        setViewAndInitials()
        setListeners()
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
    }

    private fun setViewAndInitials() {
        scrollView = findViewById(R.id.settingScrollView)
        settingsContainer = findViewById(R.id.preferences_container)
        settingIcon = findViewById(R.id.settingIcon)
        findViewById<TextView>(R.id.voice_group_head).text = getString(R.string.voice_and_volume)
        findViewById<TextView>(R.id.appsetup_group_head).text = getString(R.string.app_and_setup)
        findViewById<TextView>(R.id.mathematics_group_head).text =
            getString(R.string.maths_and_setup)
        findViewById<TextView>(R.id.security_group_head).text =
            getString(R.string.security_and_setup)
        findViewById<TextView>(R.id.permit_group_head).text = getString(R.string.user_tasks)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        normalizeVolume = findViewById(R.id.normalizeVolume)
        normalizeSeek = findViewById(R.id.normal_volume_seek)
        urgentVolume = findViewById(R.id.urgentVolume)
        urgentSeek = findViewById(R.id.urgent_volume_seek)
        theme = findViewById(R.id.theme_switch)
        themeChoices = findViewById(R.id.themeChoices)
        saveTheme = findViewById(R.id.save_theme)
        saveTheme.setBackgroundResource(R.drawable.button_square_round_spruce)
        response = findViewById(R.id.parallelResponseBtn)
        handy = findViewById(R.id.handDirectionBtn)
        angleUnit = findViewById(R.id.angleUnitBtn)
        biometrics = findViewById(R.id.biometricsBtn)
        voiceAuth = findViewById(R.id.voice_auth_switch)
        deleteVoiceSetup = findViewById(R.id.delete_voice_key)
        
        permissions = findViewById(R.id.permissionBtn)
        permissions.text = getString(R.string.grant_permissions)
        permissions.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_key_abstract, 0, 0)
        deviceAdmin = findViewById(R.id.deviceAdminBtn)
        deviceAdmin.text = getString(R.string.make_device_admin)
        deviceAdmin.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_key_locked, 0, 0)
        writeSettings = findViewById(R.id.writeSettingsBtn)
        writeSettings.text = getString(R.string.write_sys_settings)
        writeSettings.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_key_abstract, 0, 0)
        accessNotifications = findViewById(R.id.notificationAccessBtn)
        accessNotifications.text = getString(R.string.access_notifs)
        accessNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_key_abstract, 0, 0)
        noteView = findViewById(R.id.end_note)
        val note = BuildConfig.VERSION_NAME + "\n" + getString(R.string.app_tag_line)
        noteView.text = note
        setThumbAttrs(
            training,
            skivvy.getTrainingStatus(),
            getString(R.string.deactivate_training_text),
            getString(R.string.activate_training_text)
        )
        setThumbAttrs(
            mute,
            skivvy.getMuteStatus(),
            getString(R.string.unmute_text),
            getString(R.string.mute_text)
        )
        setThumbAttrs(
            normalizeVolume,
            skivvy.getVolumeNormal(),
            getString(R.string.normal_vol_on_text_) + skivvy.getNormalVolume()
                .toString() + getString(R.string.percent),
            getString(R.string.normal_vol_off_text)
        )
        setVisibilityOf(normalizeSeek, areVisible = skivvy.getVolumeNormal())
        if (skivvy.getVolumeNormal()) {
            normalizeSeek.progress = skivvy.getNormalVolume()
        }
        setThumbAttrs(
            urgentVolume,
            skivvy.getVolumeUrgent(),
            getString(R.string.urgent_vol_on_text_) + skivvy.getUrgentVolume()
                .toString() + getString(R.string.percent),
            getString(R.string.urgent_vol_off_text)
        )
        setVisibilityOf(urgentSeek, areVisible = skivvy.getVolumeUrgent())
        if (skivvy.getVolumeUrgent()) {
            urgentSeek.progress = skivvy.getUrgentVolume()
        }
        setThumbAttrs(
            theme,
            skivvy.isCustomTheme(),
            getString(R.string.set_default_theme),
            getString(R.string.customize_theme)
        )
        val themeNames = arrayOf(
            getString(R.string.default_theme), getString(R.string.dark_theme), getString(
                R.string.light_theme
            )
        )
        for (i in 0 until themeChoices.childCount) {
            (themeChoices.getChildAt(i) as RadioButton).text = themeNames[i]
        }
        setActivenessOf(saveTheme, areAlive = false)
        setVisibilityOf(
            views = arrayOf(themeChoices, saveTheme),
            areVisible = skivvy.isCustomTheme()
        )
        if (skivvy.isCustomTheme()) {
            themeChoices.check(getRadioForTheme(skivvy.getThemeState()))
        }
        setThumbAttrs(
            response,
            skivvy.getParallelResponseStatus(),
            getString(R.string.set_queued_receive),
            getString(R.string.set_parallel_receive)
        )
        setThumbAttrs(
            handy,
            skivvy.getLeftHandy(),
            getString(R.string.left_handy),
            getString(R.string.right_handy)
        )
        setThumbAttrs(
            angleUnit,
            skivvy.getAngleUnit() == skivvy.radian,
            getString(R.string.unit_radian_text),
            getString(R.string.unit_degree_text)
        )
        setVisibilityOf(biometrics, areVisible = skivvy.checkBioMetrics())
        if (skivvy.checkBioMetrics()) {
            setThumbAttrs(
                biometrics,
                skivvy.getBiometricStatus(),
                getString(R.string.disable_fingerprint),
                getString(R.string.enable_fingerprint)
            )
        }
        if (skivvy.getPhraseKeyStatus()) {
            setThumbAttrs(
                voiceAuth,
                true,
                onText = getString(R.string.disable_vocal_authentication)
            )
            setVisibilityOf(deleteVoiceSetup, areVisible = true)
        } else defaultVoiceAuthUIState()
    }

    override fun onStart() {
        super.onStart()
        setVisibilityOf(
            views = arrayOf(permissions, deviceAdmin, writeSettings, accessNotifications),
            eachVisible = arrayOf(
                !skivvy.hasPermissions(context),
                !skivvy.deviceManager.isAdminActive(skivvy.compName),
                !Settings.System.canWrite(context),
                !isNotificationServiceRunning()
            )
        )
        hidePermitHeading(hasAllUserPermits())
        for (i in 0 until settingsContainer.childCount) {
            bubbleThis(settingsContainer.getChildAt(i) as View)
        }
    }

    override fun onResume() {
        super.onResume()
        setVisibilityOf(
            views = arrayOf(permissions, deviceAdmin, writeSettings, accessNotifications),
            eachVisible = arrayOf(
                !skivvy.hasPermissions(context),
                !skivvy.deviceManager.isAdminActive(skivvy.compName),
                !Settings.System.canWrite(context),
                !isNotificationServiceRunning()
            )
        )
        hidePermitHeading(hasAllUserPermits())
    }

    private fun bubbleThis(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.reveal_delay))
    }


    private fun getRadioForTheme(themeID: Int): Int {
        return when (themeID) {
            R.style.DarkTheme -> {
                R.id.dark_theme
            }
            R.style.BlackTheme -> {
                R.id.black_theme
            }
            R.style.LightTheme -> {
                R.id.light_theme
            }
            else -> getRadioForTheme(skivvy.defaultTheme)
        }
    }

    private fun getThemeForRadio(radioID: Int): Int {
        return when (radioID) {
            R.id.black_theme -> {
                R.style.BlackTheme
            }
            R.id.dark_theme -> {
                R.style.DarkTheme
            }
            R.id.light_theme -> {
                R.style.LightTheme
            }
            else -> skivvy.defaultTheme
        }
    }

    private fun setVisibilityOf(
        view: View? = null,
        views: Array<View>? = null,
        areVisible: Boolean? = null,
        eachVisible: Array<Boolean>? = null
    ) {
        val localViews = when {
            views != null -> views
            view != null -> arrayOf(view)
            else -> throw IllegalArgumentException()
        }
        if (eachVisible != null) {
            for ((boolIndex, k) in localViews.withIndex()) {
                k.visibility = when (eachVisible[boolIndex]) {
                    true -> View.VISIBLE
                    false -> View.GONE
                    else -> throw IllegalArgumentException()
                }
            }
        } else {
            for (k in localViews) {
                k.visibility = when (areVisible) {
                    true -> View.VISIBLE
                    false -> View.GONE
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    /**
     *  This will set activeness of view in terms of its alpha, clickability, focusability, according to the boolean value passed.
     *  @param view :Single view as a parameter
     *  @param views Multiple views in form of an array
     *  @param areAlive Single boolean value to set activeness of [view] or [views] passed.
     *  @param eachAlive Multiple boolean values passed as an array, to set activeness of [views]
     *  Avoid passing [eachAlive] simultaneously with [view] and also avoid unequal [views] and [eachAlive], else the function -
     *  @throws IllegalArgumentException
     *  @note Recommended parameter combinations:
     *  [view] with [areAlive] - Single view having single activeness state.
     *  [views] with [areAlive] - All views will have same single activeness state.
     *  [views] with [eachAlive] - Size of arrays must be equal, to modify each view respectively.
     */
    private fun setActivenessOf(
        view: View? = null,
        views: Array<View>? = null,
        areAlive: Boolean? = null,
        eachAlive: Array<Boolean>? = null
    ) {
        val localViews = when {
            views != null -> views
            view != null -> arrayOf(view)
            else -> throw IllegalArgumentException()
        }
        if (eachAlive != null) {
            for ((boolIndex, k) in localViews.withIndex()) {
                when (eachAlive[boolIndex]) {
                    true -> {
                        k.isClickable = true
                        k.isFocusable = true
                        k.alpha = 1F
                    }
                    false -> {
                        k.isClickable = false
                        k.isFocusable = false
                        k.alpha = 0.25F
                    }
                    else -> throw IllegalArgumentException()
                }
            }
        } else {
            for (k in localViews) {
                when (areAlive) {
                    true -> {
                        k.isClickable = true
                        k.isFocusable = true
                        k.alpha = 1F
                    }
                    false -> {
                        k.isClickable = false
                        k.isFocusable = false
                        k.alpha = 0.25F
                    }
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }


    private fun setListeners() {
        settingIcon.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
        }
        scrollView.viewTreeObserver
            .addOnScrollChangedListener {
                val alpha: Float = scrollView.scrollY.toFloat() / window.decorView.height
                settingIcon.alpha = (1 - alpha * 8F)
                settingIcon.translationY = alpha * 500
                settingIcon.translationZ = 0 - alpha * 500
                noteView.alpha = (1 - alpha * 8F)
                noteView.translationY = alpha * 500
                noteView.translationZ = 0 - alpha * 500
            }

        training.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setTrainingStatus(isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.deactivate_training_text),
                getString(R.string.activate_training_text)
            )
            if (isChecked) {
                speakOut(getString(R.string.activate_training_text))
                finish()
            } else speakOut(getString(R.string.deactivate_training_text))
        }
        mute.setOnCheckedChangeListener { view, isChecked ->
            skivvy.saveMuteStatus(isChecked)
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.muted)
                    else -> getString(R.string.speaking)
                }
            )
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.unmute_text),
                getString(R.string.mute_text)
            )
        }
        normalizeVolume.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setVolumeNormal(isChecked)
            setVisibilityOf(normalizeSeek, areVisible = isChecked)
            speakOut(
                when (isChecked) {
                    true -> {
                        normalizeSeek.progress = skivvy.getNormalVolume()
                        getString(R.string.my_own_volume)
                    }
                    else -> getString(R.string.follow_system_volume)
                }
            )
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.normal_vol_on_text_) + skivvy.getNormalVolume()
                    .toString() + getString(R.string.percent),
                getString(R.string.normal_vol_off_text)
            )
        }
        var nPercent = skivvy.getNormalVolume()
        normalizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val txt =
                    getString(R.string.normal_vol_on_text_) + "$progress" + getString(R.string.percent)
                if (fromUser) normalizeVolume.text = txt
                nPercent = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                skivvy.setNormalVolume(nPercent)
                speakOut("${nPercent}%")
            }

        })
        urgentVolume.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setVolumeUrgent(isChecked)
            setVisibilityOf(urgentSeek, areVisible = isChecked)
            speakOut(
                when (isChecked) {
                    true -> {
                        urgentSeek.progress = skivvy.getUrgentVolume()
                        getString(R.string.volume_raised_if_urgent)
                    }
                    else -> getString(R.string.follow_system_volume)
                }
            )
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.urgent_vol_on_text_) + skivvy.getUrgentVolume()
                    .toString() + getString(R.string.percent),
                getString(R.string.urgent_vol_off_text)
            )
        }
        var uPercent = skivvy.getUrgentVolume()
        urgentSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val txt =
                    getString(R.string.urgent_vol_on_text_) + "$progress" + getString(R.string.percent)
                if (fromUser) urgentVolume.text = txt
                uPercent = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                skivvy.setUrgentVolume(uPercent)
                speakOut("${uPercent}%")
            }

        })
        var themeChosen = skivvy.getThemeState()
        theme.setOnCheckedChangeListener { view, isChecked ->
            skivvy.customTheme(isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.set_default_theme),
                getString(R.string.customize_theme)
            )
            setVisibilityOf(views = arrayOf(themeChoices, saveTheme), areVisible = isChecked)
            view.text = when (isChecked) {
                true -> {
                    setActivenessOf(saveTheme, areAlive = true)
                    themeChoices.check(getRadioForTheme(skivvy.getThemeState()))
                    getString(R.string.set_default_theme)
                }
                else -> {
                    if (skivvy.getThemeState() != skivvy.defaultTheme) {
                        skivvy.setThemeState(skivvy.defaultTheme)
                        startActivity(
                            Intent(
                                context,
                                MainActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }
                    getString(R.string.customize_theme)
                }
            }
        }
        themeChoices.setOnCheckedChangeListener { group, checkedId ->
            themeChosen = getThemeForRadio(checkedId)
            setActivenessOf(saveTheme, areAlive = checkedId != getRadioForTheme(skivvy.getThemeState()))
            speakOut((findViewById<RadioButton>(checkedId)).text.toString())
        }
        saveTheme.setOnClickListener {
            if (themeChosen != skivvy.getThemeState()) {
                skivvy.setThemeState(themeChosen)
                startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }
        }
        response.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setParallelResponseStatus(isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.set_queued_receive),
                getString(R.string.set_parallel_receive)
            )
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.parallel_receive_text)
                    else -> getString(R.string.queued_receive_text)
                }, showSnackbar = true
            )
        }
        handy.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setLeftHandy(isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.left_handy),
                getString(R.string.right_handy)
            )
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.you_left_handed)
                    else -> getString(R.string.you_right_handed)
                }, showSnackbar = true
            )
        }
        angleUnit.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAngleUnit(
                when (isChecked) {
                    true -> skivvy.radian
                    else -> skivvy.degree
                }
            )
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.unit_radian_text),
                getString(R.string.unit_degree_text)
            )
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.angles_in_radians)
                    else -> getString(R.string.angles_in_degrees)
                }, showSnackbar = true
            )
        }
        biometrics.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                skivvy.setBiometricsStatus(true)
                setThumbAttrs(
                    view as Switch,
                    true,
                    onText = getString(R.string.disable_fingerprint)
                )
                speakOut(getString(R.string.fingerprint_is_on))
            } else {
                if (skivvy.getBiometricStatus()) {
                    if (skivvy.checkBioMetrics()) {
                        initAndShowBioAuth(skivvy.CODE_BIOMETRIC_CONFIRM)
                    }
                }
            }
        }
        voiceAuth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (skivvy.getVoiceKeyPhrase() == null) {
                    speakOut(
                        getString(R.string.tell_new_secret_phrase),
                        skivvy.CODE_VOICE_AUTH_INIT
                    )
                } else {
                    skivvy.setPhraseKeyStatus(true)
                    setThumbAttrs(
                        voiceAuth,
                        true,
                        onText = getString(R.string.disable_vocal_authentication)
                    )
                    setVisibilityOf(deleteVoiceSetup, areVisible = true)
                    speakOut(getString(R.string.vocal_auth_enabled))
                    if (!skivvy.getBiometricStatus()) {
                        showBiometricRecommendation()
                    }
                }
            } else {
                speakOut(getString(R.string.vocal_auth_disabled))
                defaultVoiceAuthUIState()
            }
        }
        deleteVoiceSetup.setOnClickListener {
            if (skivvy.checkBioMetrics()) {
                initAndShowBioAuth(skivvy.CODE_VOICE_AUTH_CONFIRM)
            } else {
                speakOut(getString(R.string.reset_voice_auth_confirm))
                Snackbar.make(
                    findViewById(R.id.setup_layout),
                    getString(R.string.reset_voice_auth_confirm),
                    5000
                )
                    .setTextColor(ContextCompat.getColor(context, R.color.dull_white))
                    .setBackgroundTint(ContextCompat.getColor(context, R.color.dark_red))
                    .setAction(getString(R.string.reset)) {
                        skivvy.setVoiceKeyPhrase(null)
                        defaultVoiceAuthUIState()
                    }
                    .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .show()
            }
        }
        deleteVoiceSetup.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut(getString(R.string.passphrase_to_be_deleted))
            true
        }
        permissions.setOnClickListener {
            if (!skivvy.hasPermissions(context)) {
                speakOut(getString(R.string.need_all_permissions))
                ActivityCompat.requestPermissions(
                    this, skivvy.permissions,
                    skivvy.CODE_ALL_PERMISSIONS
                )
            }
        }

        permissions.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut(getString(R.string.grant_all_permissions), showSnackbar = true)
            true
        }

        deviceAdmin.setOnClickListener {
            if (!skivvy.deviceManager.isAdminActive(skivvy.compName)) {
                startActivityForResult(
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, skivvy.compName)
                        .putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getString(R.string.device_admin_persuation)
                        ), skivvy.CODE_DEVICE_ADMIN
                )
            }
        }
        deviceAdmin.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut(getString(R.string.make_me_admin_persuation), showSnackbar = true)
            true
        }
        writeSettings.setOnClickListener {
            speakOut(getString(R.string.request_settings_write_permit))
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS),
                skivvy.CODE_SYSTEM_SETTINGS
            )
        }
        writeSettings.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut(getString(R.string.request_settings_write_permit), showSnackbar = true)
            true
        }
        accessNotifications.setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), skivvy.CODE_NOTIFICATION_ACCESS)
        }
        accessNotifications.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut(getString(R.string.request_read_notifs_permit), showSnackbar = true)
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            skivvy.CODE_ALL_PERMISSIONS -> {
                setVisibilityOf(this.permissions, areVisible = !skivvy.hasPermissions(context))
                speakOut(
                    if (skivvy.hasPermissions(context)) getString(R.string.have_all_permits)
                    else getString(R.string.all_permissions_not_granted),
                    showSnackbar = true,
                    isPositiveForSnackbar = skivvy.hasPermissions(context)
                )
            }
        }
    }

    private fun showSnackBar(
        message: String,
        isPositive: Boolean = true,
        duration: Int = 5000
    ) {
        if (isPositive) {
            Snackbar.make(findViewById(R.id.setup_layout), message, duration)
                .setBackgroundTint(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
                .show()
        } else {
            Snackbar.make(findViewById(R.id.setup_layout), message, duration)
                .setBackgroundTint(ContextCompat.getColor(context, R.color.dark_red))
                .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var text = String()
        if (!skivvy.nonVocalRequestCodes.contains(resultCode)) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                    text =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0].toString()
                            .toLowerCase(skivvy.locale)
                }
            } else {
                if (resultCode == skivvy.CODE_VOICE_AUTH_INIT || resultCode == skivvy.CODE_VOICE_AUTH_CONFIRM) {
                    speakOut(getString(R.string.no_input))
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    return
                }
            }
        }
        when (requestCode) {
            skivvy.CODE_VOICE_AUTH_INIT -> {
                defaultVoiceAuthUIState()
                if (text != "") {
                    if (text.length < 5) {
                        speakOut(getString(R.string.phrase_too_short), skivvy.CODE_VOICE_AUTH_INIT)
                    } else {
                        skivvy.setVoiceKeyPhrase(text)
                        speakOut(getString(R.string.confirm_phrase), skivvy.CODE_VOICE_AUTH_CONFIRM)
                    }
                } else {
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    speakOut(getString(R.string.no_input))
                    return
                }
            }
            skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                if (text == skivvy.getVoiceKeyPhrase()) {
                    skivvy.setVoiceKeyPhrase(text)
                    voiceAuth.isChecked = true
                    skivvy.setPhraseKeyStatus(true)
                    setVisibilityOf(deleteVoiceSetup, areVisible = true)
                    speakOut("'${skivvy.getVoiceKeyPhrase()}'" + getString(R.string._is_the_phrase))
                    if (!skivvy.getBiometricStatus() && skivvy.checkBioMetrics()) {
                        showBiometricRecommendation()
                    }
                } else {
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    speakOut(getString(R.string.confirmation_phrase_didnt_match))
                    Snackbar.make(
                        findViewById(R.id.setup_layout),
                        getString(R.string.confirmation_phrase_didnt_match),
                        10000
                    )
                        .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
                        .setBackgroundTint(ContextCompat.getColor(context, R.color.dark_red))
                        .setAction(getString(R.string.try_again)) {
                            startVoiceRecIntent(
                                skivvy.CODE_VOICE_AUTH_INIT,
                                getString(R.string.tell_new_secret_phrase)
                            )
                        }
                        .setActionTextColor(ContextCompat.getColor(context, R.color.light_green))
                        .show()
                }
            }
            skivvy.CODE_DEVICE_ADMIN -> {
                setVisibilityOf(deviceAdmin, areVisible = resultCode != Activity.RESULT_OK)
                speakOut(
                    when (resultCode) {
                        Activity.RESULT_OK -> getString(R.string.device_admin_success)
                        else -> getString(R.string.device_admin_failure)
                    }, showSnackbar = true,
                    isPositiveForSnackbar = resultCode == Activity.RESULT_OK
                )
            }
            skivvy.CODE_SYSTEM_SETTINGS -> {
                setVisibilityOf(writeSettings, areVisible = !Settings.System.canWrite(context))
                speakOut(
                    if (Settings.System.canWrite(context)) getString(R.string.write_settings_permit_allowed)
                    else getString(R.string.write_settings_permit_denied),
                    showSnackbar = true,
                    isPositiveForSnackbar = Settings.System.canWrite(context)
                )
            }
            skivvy.CODE_NOTIFICATION_ACCESS->{
                setVisibilityOf(accessNotifications, areVisible = !isNotificationServiceRunning())
                speakOut(
                    if (isNotificationServiceRunning()) getString(R.string.access_notifs_permit_allowed)
                    else getString(R.string.access_notifs_permit_denied),
                    showSnackbar = true,
                    isPositiveForSnackbar = isNotificationServiceRunning()
                )
            }
        }
    }

    private fun hasAllUserPermits(): Boolean =
        Settings.System.canWrite(context) && skivvy.deviceManager.isAdminActive(skivvy.compName) && skivvy.hasPermissions(
            context
        )

    private fun hidePermitHeading(hide: Boolean) {
        setVisibilityOf(findViewById<TextView>(R.id.permit_group_head), areVisible = !hide)
    }

    private fun showBiometricRecommendation() = Snackbar.make(
        findViewById(R.id.setup_layout),
        getString(R.string.biometric_recommendation_passphrase_enabling),
        25000
    )
        .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
        .setBackgroundTint(ContextCompat.getColor(context, R.color.charcoal))
        .setAction(getString(R.string.enable)) {
            skivvy.setBiometricsStatus(true)
            setThumbAttrs(biometrics, true, onText = getString(R.string.disable_fingerprint))
        }
        .setActionTextColor(ContextCompat.getColor(context, R.color.dull_white))
        .show()

    private fun initAndShowBioAuth(code: Int) {
        executor = ContextCompat.getMainExecutor(this)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setDescription(getString(R.string.biometric_auth_explanation))
            .setNegativeButtonText(getString(R.string.discard))
            .build()
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    when (code) {
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            speakOut(getString(R.string.secret_phrase_deleted))
                            defaultVoiceAuthUIState()
                            skivvy.setVoiceKeyPhrase(null)
                        }
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            speakOut(getString(R.string.fingerprint_off))
                            skivvy.setBiometricsStatus(false)
                            setThumbAttrs(
                                biometrics,
                                false,
                                offText = getString(R.string.enable_fingerprint)
                            )
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (code) {
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            skivvy.setBiometricsStatus(true)
                            setThumbAttrs(
                                biometrics,
                                true,
                                onText = getString(R.string.disable_fingerprint)
                            )
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    when (code) {
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            skivvy.setBiometricsStatus(true)
                            setThumbAttrs(
                                biometrics,
                                true,
                                onText = getString(R.string.disable_fingerprint)
                            )
                        }
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            temp.setAuthAttemptCount(temp.getAuthAttemptCount() - 1)
                            if (temp.getAuthAttemptCount() == 0) {
                                //TODO: Set countdown to delete
                            }
                        }
                    }
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }

    private fun defaultVoiceAuthUIState() {
        skivvy.setPhraseKeyStatus(false)
        setThumbAttrs(voiceAuth, false, offText = getString(R.string.enable_vocal_authentication))
        setVisibilityOf(deleteVoiceSetup, areVisible = false)
    }

    private fun isNotificationServiceRunning(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
//  To get list of all packages having notification access    
//  val packageNames: Set<String> = NotificationManagerCompat.getEnabledListenerPackages(context)
    

    private fun setThumbAttrs(
        switch: Switch,
        isOn: Boolean,
        onText: String? = null,
        offText: String? = null
    ) {
        switch.isChecked = isOn
        switch.thumbTintList = ContextCompat.getColorStateList(context, when (isOn) {
            true -> {
                onText?.let { switch.text = onText }
                R.color.colorPrimary
            }
            else -> {
                offText?.let { switch.text = offText }
                R.color.light_spruce
            }
        })
    }

    private fun speakOut(
        text: String,
        code: Int? = null,
        isParallel: Boolean = skivvy.getParallelResponseStatus(),
        showSnackbar: Boolean = false,
        isPositiveForSnackbar: Boolean = true
    ) {
        if (skivvy.getVolumeNormal())
            feature.setMediaVolume(
                skivvy.getNormalVolume().toFloat(),
                applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
                false
            )
        if (showSnackbar) showSnackBar(text, isPositiveForSnackbar)
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (!isParallel)
                    code?.let { startVoiceRecIntent(code, text) }
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                if (isParallel)
                    code?.let { startVoiceRecIntent(code, text) }
            }
        })
        if (!skivvy.getMuteStatus()) {
            skivvy.tts!!.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "$code"
            )
        } else {
            code?.let { startVoiceRecIntent(code, text) }
            if (!showSnackbar) Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun startVoiceRecIntent(
        code: Int,
        msg: String = getString(R.string.generic_voice_rec_text)
    ) {
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, msg)
        recognitionIntent.resolveActivity(packageManager)
            ?.let { startActivityForResult(recognitionIntent, code) }
    }
}
