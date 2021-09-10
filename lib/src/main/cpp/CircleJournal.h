
#ifndef _CIRCLE_JOURNAL_H__
#define _CIRCLE_JOURNAL_H__

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <android/log.h>

#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "CIRCLE_JOURNAL", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "CIRCLE_JOURNAL", __VA_ARGS__)

class CircleJournal {
private:
    size_t size;
    float decay;
    size_t heads;
    const char *heads_fmt;
    int fd;
    char *buf;
    size_t seek;

public:
    CircleJournal(const char *path, size_t size, float decay);

    ~CircleJournal();

    void sink(const char *value);
};

#endif