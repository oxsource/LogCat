package com.pizzk.logcat

class NativeLib {

    /**
     * A native method that is implemented by the 'logcat' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'logcat' library on application startup.
        init {
            System.loadLibrary("logcat")
        }
    }
}