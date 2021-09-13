#include <jni.h>
#include "Roselle.h"

static const char *wPath = nullptr;
static long wSize = 0;
static Roselle *roselle = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_log_Roselle_setup(JNIEnv *env, jobject thiz, jstring path, jlong size) {
    wPath = env->GetStringUTFChars(path, nullptr);
    wSize = size;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_log_Roselle_sink(JNIEnv *env, jobject thiz, jstring msg, jint length) {
    if (nullptr == roselle) {
        LOGW("sink will create Roselle.\n");
        if (nullptr == wPath || wSize <= 0) {
            LOGW("bad path or size for Roselle.\n");
            return;
        }
        roselle = new Roselle(wPath, wSize);
    }
    const char *chs = env->GetStringUTFChars(msg, nullptr);
    roselle->sink(chs, length);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_log_Roselle_flush(JNIEnv *env, jobject thiz) {
    if (!roselle) return;
    roselle->flush();
    delete roselle;
    roselle = nullptr;
}