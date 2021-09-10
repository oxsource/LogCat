#include <jni.h>
#include "CircleJournal.h"

CircleJournal *logs = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_CircleJournal_open(JNIEnv *env, jobject thiz, jstring path, jlong size,
                                         jfloat decay_factor) {
    delete logs;
    const char *chs = env->GetStringUTFChars(path, nullptr);
    logs = new CircleJournal(chs, size, decay_factor);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_CircleJournal_sink(JNIEnv *env, jobject thiz, jstring msg) {
    if (!logs) return;
    const char *chs = env->GetStringUTFChars(msg, nullptr);
    logs->sink(chs);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pizzk_logcat_CircleJournal_close(JNIEnv *env, jobject thiz) {
    if (!logs) return;
    delete logs;
    logs = nullptr;
}