package org.ranjanistic.skivvy

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.IntegerRes
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*
import kotlin.collections.ArrayList

 class MainActivity : AppCompatActivity() {
    var outPut:TextView? = null
     var rotateAnimation: Animation? = null
     var receiver: TextView? = null
    var VOICE_REQUEST_CODE = 10
     var loading: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        outPut = findViewById(R.id.textOutput)
        loading = findViewById(R.id.loader)
        rotateAnimation = AnimationUtils.loadAnimation(this,R.anim.rotation)
        receiver = findViewById(R.id.receiverBtn)
        receiver?.setOnClickListener {
            loading?.startAnimation(rotateAnimation)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            if (intent.resolveActivity(packageManager)!=null)
                startActivityForResult(intent,VOICE_REQUEST_CODE)
            else
                Toast.makeText(this,"ERror",LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == VOICE_REQUEST_CODE)
            if(resultCode == Activity.RESULT_OK && data!=null)
                outPut?.text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0]
        loading?.clearAnimation()
    }
}
