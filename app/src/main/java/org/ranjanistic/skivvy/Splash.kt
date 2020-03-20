package org.ranjanistic.skivvy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
