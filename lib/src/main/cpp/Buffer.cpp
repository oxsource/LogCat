//
// Created by peng on 2021/9/12.
//

#include "Buffer.h"

Buffer::Buffer() {
    this->value = nullptr;
    this->seek = 0;
    this->backup = nullptr;
}

Buffer::~Buffer() = default;

long Buffer::heads() {
    return 0;
}

long Buffer::sizes() {
    return 0;
}

long Buffer::frees() {
    return this->sizes() - this->seek;
}

int Buffer::sink(const char *ins, long length) {
    if (!this->value) return -1;
    const long s_length = length > 0 ? length : ins ? (long) strlen(ins) : 0;
    if (s_length <= 0) return 0;
    if (s_length > this->sizes()) {
        LOGW("%s sink abort because: sizes %ld < %ld.\n", this->name(), this->sizes(), s_length);
        return -2;
    }
    if (this->frees() < s_length) {
        LOGD("%s sink flush because: frees %ld < %ld.\n", this->name(), this->frees(), s_length);
        this->flush();
    }
    memcpy(this->value + this->seek + this->heads(), ins, s_length);
    this->seek += s_length;
    return 1;
}

void Buffer::flush() {
    if (this->backup) {
        this->backup->sink(this->value, this->seek);
    }
    this->seek = 0;
}

void Buffer::withBackup(Buffer *buffer) {
    this->backup = buffer;
}