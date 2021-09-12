package com.pizzk.logcat.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pizzk.logcat.RoselleLog
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var count: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RoselleLog.setup(application)
        findViewById<View>(R.id.tv).setOnClickListener {
            count += 1
            when (count % 3) {
                0 -> RoselleLog.d("MainActivity", "debug clicked ${count}.", null)
                1 -> RoselleLog.e(
                    "MainActivity",
                    "error clicked ${count}.",
                    Exception("auto exception")
                )
                2 -> RoselleLog.w("MainActivity", "warn clicked ${count}.", null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RoselleLog.flush()
    }
}