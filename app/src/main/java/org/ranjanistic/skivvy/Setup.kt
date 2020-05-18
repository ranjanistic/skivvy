package org.ranjanistic.skivvy

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
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

    private class ChildLayout {
        lateinit var settingsContainer: ConstraintLayout
        lateinit var accessPermitGrid: GridLayout
        lateinit var scrollView: ScrollView
    }

    private class Voice {
        lateinit var mute: Switch
        lateinit var urgentVolume: Switch
        lateinit var urgentSeek: SeekBar
        lateinit var normalizeVolume: Switch
        lateinit var normalizeSeek: SeekBar
    }

    private class AppSetup {
        lateinit var theme: Switch
        lateinit var themeChoices: RadioGroup
        lateinit var saveTheme: TextView
        lateinit var response: Switch
        lateinit var handy: Switch
        lateinit var onStartup: Switch
        lateinit var fullScreen: Switch
        lateinit var training: Switch
    }

    private class Maths {
        lateinit var angleUnit: Switch
    }

    private class Security {
        lateinit var biometrics: Switch
        lateinit var voiceAuth: Switch
        lateinit var deleteVoiceSetup: TextView
        lateinit var executor: Executor
        lateinit var biometricPrompt: BiometricPrompt
        lateinit var promptInfo: BiometricPrompt.PromptInfo
    }

    //Switch properties on one class
    private class SwitchAttribute(size: Int = 0) {
        var total = size
        var switch = arrayOfNulls<Switch>(size)
        var state = arrayOfNulls<Boolean>(size)
        var onText = arrayOfNulls<String>(size)
        var offText = arrayOfNulls<String>(size)
    }

    private val childLayout = ChildLayout()
    private val voice = Voice()
    private val appSetup = AppSetup()
    private val maths = Maths()
    private val security = Security()
    private val temp = TempDataManager()
    private val feature = SystemFeatureManager()

    private lateinit var reveal: Animation
    private lateinit var slideIn: Animation
    private lateinit var slideOut: Animation
    private lateinit var settingIcon: ImageView
    private lateinit var noteView: TextView

    class SpecialPermits {
        lateinit var permissions: TextView
        lateinit var deviceAdmin: TextView
        lateinit var writeSettings: TextView
        lateinit var accessNotifications: TextView
        lateinit var drawOver: TextView
        lateinit var batteryOptimize: TextView
    }

    private val special = SpecialPermits()
    private lateinit var context: Context
    private lateinit var recognitionIntent: Intent

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
        window.statusBarColor = getColorAccordingTheme(context)
        window.navigationBarColor = getColorAccordingTheme(context)
        setViewAndInitials()
        setListeners()
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
    }

    private fun getColorAccordingTheme(context: Context): Int {
        return ContextCompat.getColor(
            context,
            when (skivvy.getThemeState()) {
                R.style.LightTheme -> R.color.dull_white
                R.style.BlackTheme -> R.color.pitch_black
                R.style.BlueTheme -> R.color.blue
                else -> R.color.dead_blue
            }
        )
    }

    private fun setViewAndInitials() {
        slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_to_right)
        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        settingIcon = findViewById(R.id.settingIcon)
        noteView = findViewById(R.id.end_note)
        childLayout.scrollView = findViewById(R.id.settingScrollView)
        childLayout.settingsContainer = findViewById(R.id.preferences_container)
        childLayout.accessPermitGrid = findViewById(R.id.accessGrid)
        appSetup.training = findViewById(R.id.trainingModeBtn)
        voice.mute = findViewById(R.id.muteUnmuteBtn)
        voice.normalizeVolume = findViewById(R.id.normalizeVolume)
        voice.normalizeSeek = findViewById(R.id.normal_volume_seek)
        voice.urgentVolume = findViewById(R.id.urgentVolume)
        voice.urgentSeek = findViewById(R.id.urgent_volume_seek)
        appSetup.theme = findViewById(R.id.theme_switch)
        appSetup.themeChoices = findViewById(R.id.themeChoices)
        appSetup.saveTheme = findViewById(R.id.save_theme)
        appSetup.response = findViewById(R.id.parallelResponseBtn)
        appSetup.handy = findViewById(R.id.handDirectionBtn)
        appSetup.onStartup = findViewById(R.id.recordOnStart)
        appSetup.fullScreen = findViewById(R.id.fullScreenMode)
        maths.angleUnit = findViewById(R.id.angleUnitBtn)
        security.biometrics = findViewById(R.id.biometricsBtn)
        security.voiceAuth = findViewById(R.id.voice_auth_switch)
        security.deleteVoiceSetup = findViewById(R.id.delete_voice_key)
        special.permissions = findViewById(R.id.permissionBtn)
        special.deviceAdmin = findViewById(R.id.deviceAdminBtn)
        special.writeSettings = findViewById(R.id.writeSettingsBtn)
        special.accessNotifications = findViewById(R.id.notificationAccessBtn)
        special.drawOver = findViewById(R.id.drawOverBtn)
        special.batteryOptimize = findViewById(R.id.batteryOptBtn)
        noteView.text = "${BuildConfig.VERSION_NAME}\n${getString(R.string.app_tag_line)}"
        findViewById<TextView>(R.id.voice_group_head).text = getString(R.string.voice_and_volume)
        findViewById<TextView>(R.id.appsetup_group_head).text = getString(R.string.app_and_setup)
        findViewById<TextView>(R.id.mathematics_group_head).text =
            getString(R.string.maths_and_setup)
        findViewById<TextView>(R.id.security_group_head).text =
            getString(R.string.security_and_setup)
        appSetup.saveTheme.setBackgroundResource(R.drawable.button_square_round_spruce)

        setPermissionGridData(
            arrayOf(
                special.permissions,
                special.deviceAdmin,
                special.writeSettings,
                special.accessNotifications,
                special.drawOver,
                special.batteryOptimize
            ),
            textIDs = arrayOf(
                R.string.grant_permissions,
                R.string.make_device_admin,
                R.string.write_sys_settings,
                R.string.access_notifs,
                R.string.draw_overlay,
                R.string.battery_opt
            ),
            drawableIDs = arrayOf(
                R.drawable.ic_key_abstract,
                R.drawable.ic_key_locked,
                R.drawable.ic_writesettingsicon,
                R.drawable.ic_message_notification,
                R.drawable.ic_draw_overlay,
                R.drawable.ic_skivvyinbattery
            )
        )
        setVisibilityOf(
            views =
            arrayOf(
                voice.normalizeSeek,
                voice.urgentSeek,
                appSetup.themeChoices,
                appSetup.saveTheme,
                security.biometrics,
                security.deleteVoiceSetup
            ),
            eachVisible =
            arrayOf(
                skivvy.getVolumeNormal(),
                skivvy.getVolumeUrgent(),
                skivvy.isCustomTheme(),
                skivvy.isCustomTheme(),
                skivvy.checkBioMetrics(),
                skivvy.getPhraseKeyStatus()
            )
        )
        setActivenessOf(appSetup.saveTheme, alive = false)
        if (skivvy.getVolumeNormal()) {
            voice.normalizeSeek.progress = skivvy.getNormalVolume()
        }
        if (skivvy.getVolumeUrgent()) {
            voice.urgentSeek.progress = skivvy.getUrgentVolume()
        }
        val themeNames = arrayOf(
            getString(R.string.default_theme),
            getString(R.string.dark_theme),
            getString(R.string.light_theme),
            getString(R.string.blue_theme)
        )
        for (i in 0 until appSetup.themeChoices.childCount) {
            (appSetup.themeChoices.getChildAt(i) as RadioButton).text = themeNames[i]
        }
        if (skivvy.isCustomTheme()) {
            appSetup.themeChoices.check(getRadioForTheme(skivvy.getThemeState()))
        }
        val switches: Array<Switch?> = arrayOf(
            voice.mute,
            voice.normalizeVolume,
            voice.urgentVolume,
            appSetup.theme,
            appSetup.response,
            appSetup.handy,
            appSetup.onStartup,
            appSetup.fullScreen,
            appSetup.training,
            maths.angleUnit,
            security.biometrics,
            security.voiceAuth
        )
        val switchData = SwitchAttribute(switches.size)
        switchData.switch = switches
        switchData.state = arrayOf(
            skivvy.getMuteStatus(),
            skivvy.getVolumeNormal(),
            skivvy.getVolumeUrgent(),
            skivvy.isCustomTheme(),
            skivvy.getParallelResponseStatus(),
            skivvy.getLeftHandy(),
            skivvy.shouldListenStartup(),
            skivvy.shouldFullScreen(),
            skivvy.getTrainingStatus(),
            skivvy.getAngleUnit() == skivvy.radian,
            skivvy.getBiometricStatus(),
            skivvy.getPhraseKeyStatus()
        )
        switchData.onText = arrayOf(
            getString(R.string.unmute_text),
            getString(R.string.normal_vol_on_text_) + skivvy.getNormalVolume()
                .toString() + getString(R.string.percent),
            getString(R.string.urgent_vol_on_text_) + skivvy.getUrgentVolume()
                .toString() + getString(R.string.percent),
            getString(R.string.set_default_theme),
            getString(R.string.set_queued_receive),
            getString(R.string.left_handy),
            getString(R.string.listen_on_click),
            getString(R.string.disable_full_screen),
            getString(R.string.deactivate_training_text),
            getString(R.string.unit_radian_text),
            getString(R.string.disable_fingerprint),
            getString(R.string.disable_vocal_authentication)
        )
        switchData.offText = arrayOf(
            getString(R.string.mute_text),
            getString(R.string.normal_vol_off_text),
            getString(R.string.urgent_vol_off_text),
            getString(R.string.customize_theme),
            getString(R.string.set_parallel_receive),
            getString(R.string.right_handy),
            getString(R.string.listen_on_start),
            getString(R.string.enable_full_screen),
            getString(R.string.activate_training_text),
            getString(R.string.unit_degree_text),
            getString(R.string.enable_fingerprint),
            getString(R.string.enable_vocal_authentication)
        )
        setSwitchesInitials(switchData)
    }

    /**
     * Sets user granted permission view default status.
     * @param textViews The array of permission views as TextViews to be modified
     * @param texts The text to be shown on [textViews].
     * @param textIDs The default is null in case texts are directly passed, else IDs are passed as array.
     * @param drawableIDs The IDs of drawables to be displayed on [textViews]
     */
    private fun setPermissionGridData(
        textViews: Array<TextView>,
        textIDs: Array<Int>,
        texts: Array<String>? = null,
        drawableIDs: Array<Int>
    ) {
        for ((index, access) in textViews.withIndex()) {
            if (texts == null) {
                access.text = getString(textIDs[index])
            } else access.text = texts[index]
            access.setCompoundDrawablesWithIntrinsicBounds(0, drawableIDs[index], 0, 0)
        }
    }

    /**
     * Sets default state of switches in  layout, as per the details passed for each switch data
     * @param switchData The object holding arrays of switch attributes like off text, on text, state, and color according to state.
     */
    private fun setSwitchesInitials(switchData: SwitchAttribute) {
        var index = 0
        while (index < switchData.total) {
            switchData.switch[index]?.let {
                switchData.state[index]?.let { it1 ->
                    setThumbAttrs(
                        it,
                        it1,
                        switchData.onText[index],
                        switchData.offText[index]
                    )
                }
            }
            index++
        }
    }

    /**
     * Sets visibility of user granted access permission views according to their granted condition.
     */
    private fun setPermissionGridView() {
        setVisibilityOf(
            views = arrayOf(
                special.permissions,
                special.deviceAdmin,
                special.writeSettings,
                special.accessNotifications,
                special.drawOver,
                special.batteryOptimize,
                childLayout.accessPermitGrid
            ),
            eachVisible = arrayOf(
                !skivvy.hasPermissions(context),
                !skivvy.deviceManager.isAdminActive(skivvy.compName),
                !Settings.System.canWrite(context),
                !isNotificationServiceRunning(),
                !canDrawOverOtherApps(),
                !isOptedOutBatteryOptimization(),
                !hasAllUserPermits()
            )
        )
    }

    override fun onStart() {
        super.onStart()
        setPermissionGridView()
        slideAnimateChildrenOf(childLayout.settingsContainer, true)
    }

    override fun onResume() {
        super.onResume()
        setPermissionGridView()
    }

    private fun slideIn(view: View) {
        slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_from_right)
        view.startAnimation(slideIn)
    }

    private fun slideOut(view: View) {
        view.startAnimation(slideOut)
    }

    private fun slideAnimateChildrenOf(constraintLayout: ConstraintLayout, isEntering: Boolean) {
        for (i in 0 until constraintLayout.childCount) {
            if (isEntering) {
                slideIn(constraintLayout.getChildAt(i) as View)
            } else {
                slideOut(constraintLayout.getChildAt(i) as View)
            }
        }
    }

    private fun bubbleThis(view: View) {
        reveal = AnimationUtils.loadAnimation(context, R.anim.bubble_wave)
        view.startAnimation(reveal)
    }

    /**
     * Returns radio button ID for corresponding theme ID.
     * @param themeID The id of theme to be checked
     * @return The id of radio button corresponding to its [themeID]
     */
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
            R.style.BlueTheme -> {
                R.id.blue_theme
            }
            else -> getRadioForTheme(skivvy.defaultTheme)
        }
    }

    /**
     * Returns theme ID for corresponding radio button ID.
     * @param radioID The id of radio button to be checked
     * @return The id of theme corresponding to its [radioID]
     */
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
            R.id.blue_theme -> {
                R.style.BlueTheme
            }
            else -> skivvy.defaultTheme
        }
    }

    /**
     *  This will set visibility of view in terms of its according to the boolean value passed.
     *  @param view :Single view as a parameter
     *  @param views Multiple views in form of an array
     *  @param visible Single boolean value to set visibility of [view] or [views] passed.
     *  @param eachVisible Multiple boolean values passed as an array, to set visibility of each of the respective [views]
     *  Avoid passing [eachVisible] simultaneously with [view] and also avoid unequal [views] and [eachVisible], else the function -
     *  @throws IllegalArgumentException
     *  @note Recommended parameter combinations:
     *  [view] with [visible] - Single view having single visibility state.
     *  [views] with [visible] - All views will have same single visibility state.
     *  [views] with [eachVisible] - Size of arrays must be equal, to modify each view respectively.
     */
    private fun setVisibilityOf(
        view: View? = null,
        views: Array<View>? = null,
        visible: Boolean? = null,
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
                k.visibility = when (visible) {
                    true -> View.VISIBLE
                    false -> View.GONE
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    private fun getMinUrgentVolume(): Int {
        skivvy.getNormalVolume().let {
            return if (it > 40) it
            else 40
        }
    }

    /**
     *  This will set activeness of view in terms of its alpha, clickability, focusability, according to the boolean value passed.
     *  @param view :Single view as a parameter
     *  @param views Multiple views in form of an array
     *  @param alive Single boolean value to set activeness of [view] or [views] passed.
     *  @param eachAlive Multiple boolean values passed as an array, to set activeness of [views]
     *  Avoid passing [eachAlive] simultaneously with [view] and also avoid unequal [views] and [eachAlive], else the function -
     *  @throws IllegalArgumentException
     *  @note Recommended parameter combinations:
     *  [view] with [alive] - Single view having single activeness state.
     *  [views] with [alive] - All views will have same single activeness state.
     *  [views] with [eachAlive] - Size of arrays must be equal, to modify each view respectively.
     */
    private fun setActivenessOf(
        view: View? = null,
        views: Array<View>? = null,
        alive: Boolean? = null,
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
                }
            }
        } else {
            for (k in localViews) {
                when (alive) {
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
        childLayout.scrollView.viewTreeObserver
            .addOnScrollChangedListener {
                val alpha: Float =
                    childLayout.scrollView.scrollY.toFloat() / window.decorView.height
                settingIcon.alpha = (1 - alpha * 8F)
                settingIcon.translationY = alpha * 500
                settingIcon.translationZ = 0 - alpha * 500
                noteView.alpha = (1 - alpha * 8F)
                noteView.translationY = alpha * 500
                noteView.translationZ = 0 - alpha * 500
            }

        appSetup.training.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAppModePref(isTraining = isChecked)
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
        voice.mute.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setVoicePreference(voiceMute = isChecked)
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
        voice.normalizeVolume.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setVoicePreference(normalizeVolume = isChecked)
            setVisibilityOf(voice.normalizeSeek, visible = isChecked)
            speakOut(
                when (isChecked) {
                    true -> {
                        voice.normalizeSeek.progress = skivvy.getNormalVolume()
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
        voice.normalizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val txt =
                    getString(R.string.normal_vol_on_text_) + "$progress" + getString(R.string.percent)
                if (fromUser) voice.normalizeVolume.text = txt
                nPercent = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                skivvy.setVoicePreference(normalVolumeLevel = nPercent)
                speakOut("${nPercent}%")
            }
        })
        voice.urgentVolume.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setVoicePreference(urgentVolume = isChecked)
            setVisibilityOf(voice.urgentSeek, visible = isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.urgent_vol_on_text_) + skivvy.getUrgentVolume()
                    .toString() + getString(R.string.percent),
                getString(R.string.urgent_vol_off_text)
            )
            speakOut(
                when (isChecked) {
                    true -> {
                        voice.urgentSeek.progress = skivvy.getUrgentVolume()
                        getString(R.string.volume_raised_if_urgent)
                    }
                    else -> getString(R.string.follow_system_volume)
                }
            )
        }
        var uPercent = skivvy.getUrgentVolume()
        voice.urgentSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val txt =
                    getString(R.string.urgent_vol_on_text_) + "$progress" + getString(R.string.percent)
                if (fromUser) voice.urgentVolume.text = txt
                uPercent = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                skivvy.setVoicePreference(urgentVolumeLevel = uPercent)
                speakOut("${uPercent}%")
            }

        })
        var themeChosen = skivvy.getThemeState()
        appSetup.theme.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAppModePref(isCustomTheme = isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.set_default_theme),
                getString(R.string.customize_theme)
            )
            setVisibilityOf(
                views = arrayOf(appSetup.themeChoices, appSetup.saveTheme),
                visible = isChecked
            )
            view.text = when (isChecked) {
                true -> {
                    setActivenessOf(appSetup.saveTheme, alive = true)
                    appSetup.themeChoices.check(getRadioForTheme(skivvy.getThemeState()))
                    getString(R.string.set_default_theme)
                }
                else -> {
                    if (skivvy.getThemeState() != skivvy.defaultTheme) {
                        skivvy.setAppModePref(customTheme = skivvy.defaultTheme)
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
        appSetup.themeChoices.setOnCheckedChangeListener { _, checkedId ->
            themeChosen = getThemeForRadio(checkedId)
            setActivenessOf(
                appSetup.saveTheme,
                alive = checkedId != getRadioForTheme(skivvy.getThemeState())
            )
            speakOut((findViewById<RadioButton>(checkedId)).text.toString())
        }
        appSetup.saveTheme.setOnClickListener {
            if (themeChosen != skivvy.getThemeState()) {
                skivvy.setAppModePref(customTheme = themeChosen)
                startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }
        }
        appSetup.response.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAppModePref(parallelListen = isChecked)
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
        appSetup.handy.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAppModePref(leftHandy = isChecked)
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
        appSetup.onStartup.setOnCheckedChangeListener { view, isChecked ->
            startActivity(
                Intent(
                    context,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            skivvy.setAppModePref(onStartListen = isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.listen_on_click),
                getString(R.string.listen_on_start)
            )
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.i_listen_at_start)
                    else -> getString(R.string.i_listen_on_tap)
                }, showToast = true
            )
            finish()
        }
        appSetup.fullScreen.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setAppModePref(fullScreen = isChecked)
            setThumbAttrs(
                view as Switch,
                isChecked,
                getString(R.string.disable_full_screen),
                getString(R.string.enable_full_screen)
            )
            speakOut(
                when (isChecked) {
                    true -> getString(R.string.full_screen_on)
                    else -> getString(R.string.full_screen_off)
                }, showSnackbar = true
            )
        }
        maths.angleUnit.setOnCheckedChangeListener { view, isChecked ->
            skivvy.setMathsPref(
                angleUnit = when (isChecked) {
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
        security.biometrics.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                skivvy.setSecurityPref(biometricOn = true)
                setThumbAttrs(
                    view as Switch,
                    true,
                    onText = getString(R.string.disable_fingerprint)
                )
                speakOut(getString(R.string.fingerprint_is_on))
            } else {
                if (skivvy.getBiometricStatus()) {
                    if (skivvy.checkBioMetrics()) {
                        temp.setAuthAttemptCount(4)
                        initAndShowBioAuth(skivvy.CODE_BIOMETRIC_CONFIRM)
                    }
                }
            }
        }
        //TODO:  minimum retry attempts preference
        security.voiceAuth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (skivvy.getVoiceKeyPhrase() == null) {
                    speakOut(
                        getString(R.string.tell_new_secret_phrase),
                        skivvy.CODE_VOICE_AUTH_INIT
                    )
                } else {
                    skivvy.setSecurityPref(vocalAuthOn = true)
                    setThumbAttrs(
                        security.voiceAuth,
                        true,
                        onText = getString(R.string.disable_vocal_authentication)
                    )
                    setVisibilityOf(security.deleteVoiceSetup, visible = true)
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
        security.deleteVoiceSetup.setOnClickListener {
            if (skivvy.checkBioMetrics()) {
                temp.setAuthAttemptCount(4)
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
                        skivvy.setSecurityPref(vocalAuthPhrase = null)
                        defaultVoiceAuthUIState()
                    }
                    .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .show()
            }
        }
        security.deleteVoiceSetup.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.passphrase_to_be_deleted))
            true
        }
        special.permissions.setOnClickListener {
            speakOut(getString(R.string.need_all_permissions), showToast = true)
            ActivityCompat.requestPermissions(
                this, skivvy.permissions,
                skivvy.CODE_ALL_PERMISSIONS
            )
        }

        special.permissions.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.grant_all_permissions), showSnackbar = true)
            true
        }

        special.deviceAdmin.setOnClickListener {
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
        special.deviceAdmin.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.make_me_admin_persuation), showSnackbar = true)
            true
        }
        special.writeSettings.setOnClickListener {
            speakOut(getString(R.string.write_settings_direction_text), showToast = true)
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")),
                skivvy.CODE_SYSTEM_SETTINGS
            )
        }
        special.writeSettings.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.request_settings_write_permit), showSnackbar = true)
            true
        }
        special.accessNotifications.setOnClickListener {
            speakOut(getString(R.string.notification_access_direction_text), showToast = true)
            startActivityForResult(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                skivvy.CODE_NOTIFICATION_ACCESS
            )
        }
        special.accessNotifications.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.request_read_notifs_permit), showSnackbar = true)
            true
        }
        special.drawOver.setOnClickListener {
            speakOut(getString(R.string.draw_overlay_direction_text), showToast = true)
            startActivityForResult(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ),
                skivvy.CODE_OVERLAY_ACCESS
            )
        }
        special.drawOver.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.draw_over_others_request), showSnackbar = true)
            true
        }
        special.batteryOptimize.setOnClickListener {
            speakOut(getString(R.string.draw_overlay_direction_text), showToast = true)
            startActivityForResult(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                ),
                skivvy.CODE_BATTERY_OPT
            )
        }
        special.batteryOptimize.setOnLongClickListener { view ->
            bubbleThis(view)
            speakOut(getString(R.string.draw_over_others_request), showSnackbar = true)
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
                setVisibilityOf(special.permissions, visible = !skivvy.hasPermissions(context))
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
                    skivvy.setSecurityPref(vocalAuthPhrase = null)
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
                        skivvy.setSecurityPref(vocalAuthPhrase = text)
                        speakOut(getString(R.string.confirm_phrase), skivvy.CODE_VOICE_AUTH_CONFIRM)
                    }
                } else {
                    skivvy.setSecurityPref(vocalAuthPhrase = null)
                    defaultVoiceAuthUIState()
                    speakOut(getString(R.string.no_input))
                    return
                }
            }
            skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                if (text == skivvy.getVoiceKeyPhrase()) {
                    skivvy.setSecurityPref(vocalAuthPhrase = text)
                    security.voiceAuth.isChecked = true
                    skivvy.setSecurityPref(vocalAuthOn = true)
                    setVisibilityOf(security.deleteVoiceSetup, visible = true)
                    speakOut("'${skivvy.getVoiceKeyPhrase()}'" + getString(R.string._is_the_phrase))
                    if (!skivvy.getBiometricStatus() && skivvy.checkBioMetrics()) {
                        showBiometricRecommendation()
                    }
                } else {
                    skivvy.setSecurityPref(vocalAuthPhrase = null)
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
                setVisibilityOf(special.deviceAdmin, visible = resultCode != Activity.RESULT_OK)
                speakOut(
                    when (resultCode) {
                        Activity.RESULT_OK -> getString(R.string.device_admin_success)
                        else -> getString(R.string.device_admin_failure)
                    }, showSnackbar = true,
                    isPositiveForSnackbar = resultCode == Activity.RESULT_OK
                )
            }
            skivvy.CODE_SYSTEM_SETTINGS -> {
                setVisibilityOf(special.writeSettings, visible = !Settings.System.canWrite(context))
                speakOut(
                    if (Settings.System.canWrite(context)) getString(R.string.write_settings_permit_allowed)
                    else getString(R.string.write_settings_permit_denied),
                    showSnackbar = true,
                    isPositiveForSnackbar = Settings.System.canWrite(context)
                )
            }
            skivvy.CODE_NOTIFICATION_ACCESS -> {
                setVisibilityOf(
                    special.accessNotifications,
                    visible = !isNotificationServiceRunning()
                )
                speakOut(
                    if (isNotificationServiceRunning()) getString(R.string.access_notifs_permit_allowed)
                    else getString(R.string.access_notifs_permit_denied),
                    showSnackbar = true,
                    isPositiveForSnackbar = isNotificationServiceRunning()
                )
            }
            skivvy.CODE_OVERLAY_ACCESS -> {
                setVisibilityOf(special.drawOver, visible = !canDrawOverOtherApps())
                speakOut(
                    if (canDrawOverOtherApps()) getString(R.string.draw_overlay_allowed)
                    else getString(R.string.draw_overlay_denied),
                    showSnackbar = true,
                    isPositiveForSnackbar = canDrawOverOtherApps()
                )
            }
            skivvy.CODE_BATTERY_OPT -> {
                setVisibilityOf(special.batteryOptimize, visible = !isOptedOutBatteryOptimization())
            }
        }
    }

    private fun hasAllUserPermits(): Boolean =
        Settings.System.canWrite(context) && skivvy.deviceManager.isAdminActive(skivvy.compName) && skivvy.hasPermissions(
            context
        ) && isNotificationServiceRunning() && canDrawOverOtherApps()

    private fun showBiometricRecommendation() = Snackbar.make(
        findViewById(R.id.setup_layout),
        getString(R.string.biometric_recommendation_passphrase_enabling),
        25000
    )
        .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
        .setBackgroundTint(ContextCompat.getColor(context, R.color.charcoal))
        .setAction(getString(R.string.enable)) {
            skivvy.setSecurityPref(biometricOn = true)
            setThumbAttrs(
                security.biometrics,
                true,
                onText = getString(R.string.disable_fingerprint)
            )
        }
        .setActionTextColor(ContextCompat.getColor(context, R.color.dull_white))
        .show()

    private fun initAndShowBioAuth(code: Int) {
        security.executor = ContextCompat.getMainExecutor(this)
        security.promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setDescription(getString(R.string.biometric_auth_explanation))
            .setNegativeButtonText(getString(R.string.discard))
            .build()
        security.biometricPrompt = BiometricPrompt(this, security.executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    when (code) {
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            speakOut(getString(R.string.secret_phrase_deleted))
                            defaultVoiceAuthUIState()
                            skivvy.setSecurityPref(vocalAuthPhrase = null)
                        }
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            speakOut(getString(R.string.fingerprint_off))
                            skivvy.setSecurityPref(biometricOn = false)
                            setThumbAttrs(
                                security.biometrics,
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
                            skivvy.setSecurityPref(biometricOn = true)
                            setThumbAttrs(
                                security.biometrics,
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
                            skivvy.setSecurityPref(biometricOn = true)
                            setThumbAttrs(
                                security.biometrics,
                                true,
                                onText = getString(R.string.disable_fingerprint)
                            )
                            temp.setAuthAttemptCount(temp.getAuthAttemptCount() - 1)
                            if (temp.getAuthAttemptCount() == 0) {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.many_wrong_attempts),
                                    Toast.LENGTH_LONG
                                ).show()
                                finishAffinity()
                            }
                        }
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            temp.setAuthAttemptCount(temp.getAuthAttemptCount() - 1)
                            if (temp.getAuthAttemptCount() == 0) {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.many_wrong_attempts),
                                    Toast.LENGTH_LONG
                                ).show()
                                finishAffinity()
                            }
                        }
                    }
                }
            })
        security.biometricPrompt.authenticate(security.promptInfo)
    }

    private fun defaultVoiceAuthUIState() {
        skivvy.setSecurityPref(vocalAuthOn = false)
        setThumbAttrs(
            security.voiceAuth,
            false,
            offText = getString(R.string.enable_vocal_authentication)
        )
        setVisibilityOf(security.deleteVoiceSetup, visible = false)
    }

    private fun isNotificationServiceRunning(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
//  To get list of all packages having notification access    
//  val packageNames: Set<String> = NotificationManagerCompat.getEnabledListenerPackages(context)

    private fun canDrawOverOtherApps(): Boolean = Settings.canDrawOverlays(this)
    private fun isOptedOutBatteryOptimization(): Boolean =
        (this.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
            packageName
        )

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
        showToast: Boolean = false,
        isPositiveForSnackbar: Boolean = true
    ) {
        if (skivvy.getVolumeNormal())
            feature.setMediaVolume(
                skivvy.getNormalVolume().toFloat(),
                applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
                false
            )
        skivvy.tts?.let {
            it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    if (!isParallel)
                        code?.let { it1 -> startVoiceRecIntent(it1, text) }
                }

                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {
                    if (isParallel)
                        code?.let { it1 -> startVoiceRecIntent(it1, text) }
                }
            })
            if (!skivvy.getMuteStatus()) {
                it.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "$code"
                )
            } else {
                code?.let { it1 -> startVoiceRecIntent(it1, text) }
                if (!showSnackbar && !showToast)
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                else {
                    if (showSnackbar) showSnackBar(text, isPositiveForSnackbar)
                    if (showToast) Toast.makeText(applicationContext, text, Toast.LENGTH_LONG)
                        .show()
                    else return
                }
            }
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
