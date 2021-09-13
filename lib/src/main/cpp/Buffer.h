//
// Created by peng on 2021/9/12.
//

#ifndef LCT_BUFFER_H
#define LCT_BUFFER_H

#include <cstdlib>
#include <cstring>
#include <cstdio>
#include <android/log.h>

#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "ROSELLE_LOGCAT", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ROSELLE_LOGCAT", __VA_ARGS__)

//#define LOGW(...) printf(__VA_ARGS__)
//#define LOGD(...) printf(__VA_ARGS__)

class Buffer {
protected:
    char *value;
    long seek;
    Buffer *backup;

public:
    Buffer();

    virtual ~Buffer() = 0;

    virtual const char *name() = 0;

    virtual long heads();

    virtual long sizes();

    virtual long frees();

    virtual int sink(const char *ins, long length);

    virtual void flush();

    virtual void withBackup(Buffer *buffer);
};

#endif //LCT_BUFFER_H
