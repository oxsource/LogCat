package com.pizzk.logcat.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pizzk.logcat.Logcat
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var count: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.tv).setOnClickListener {
            count += 1
            when (count % 3) {
                0 -> Logcat.d("MainActivity", "debug clicked ${count}.", null)
                1 -> Logcat.e(
                    "MainActivity",
                    "error clicked ${count}.",
                    Exception("auto exception")
                )
                2 -> Logcat.w("MainActivity", "warn clicked ${count}.", null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logcat.submitIfy()
    }
}