package com.pizzk.logcat.app

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pizzk.logcat.Logcat

class MainActivity : AppCompatActivity() {
    private var count: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logcat.setAlias("XiaoMing")
        Logcat.fetch()
        findViewById<View>(R.id.tv).setOnClickListener {
            count += 1
            when (count % 4) {
                0 -> Logcat.d("MainActivity", "debug clicked ${count}.", null)
                1 -> Logcat.e(
                    "MainActivity",
                    "error clicked ${count}.",
                    Exception("auto exception")
                )
                2 -> Logcat.w("MainActivity", "warn clicked ${count}.", null)
                3 -> throw Exception("panic")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logcat.flush()
    }
}