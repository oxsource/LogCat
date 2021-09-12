#include <jni.h>
#include "Roselle.h"

static jstring wPath = nullptr;
static jlong wSize = 0;
static Roselle *roselle = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_Roselle_setup(JNIEnv *env, jobject thiz, jstring path, jlong size) {
    wPath = path;
    wSize = size;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_Roselle_sink(JNIEnv *env, jobject thiz, jstring msg, jint length) {
    if (!roselle) {
        LOGW("sink will create Roselle.\n");
        if (!wPath || wSize <= 0) return;
        const char *chs = env->GetStringUTFChars(wPath, nullptr);
        roselle = new Roselle(chs, wSize);
    }
    const char *chs = env->GetStringUTFChars(msg, nullptr);
    roselle->sink(chs, length);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_Roselle_flush(JNIEnv *env, jobject thiz) {
    if (!roselle) return;
    roselle->flush();
    delete roselle;
    roselle = nullptr;
}