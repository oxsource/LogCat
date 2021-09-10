#include "CircleJournal.h"

CircleJournal::CircleJournal(const char *path, size_t size, float decay) {
    this->size = size;
    this->decay = decay;
    //heads = length + \0 + \n
    this->heads = (size_t) (sizeof(size_t) * sizeof(char)) + 2;
    this->heads_fmt = "%08zu\n";
    this->seek = -1;
    this->buf = (char *) MAP_FAILED;
    this->fd = open(path, O_RDWR | O_APPEND | O_CREAT, S_IRUSR | S_IWUSR);
    LOGW("Logcat constructor: path=%s, fd=%d\n", path, this->fd);
    if (this->fd < 0) {
        return;
    }
    //reset file size
    ftruncate(this->fd, this->size);
    lseek(this->fd, 0, SEEK_SET);
    //read file length
    char *seeks = (char *) malloc(this->heads);
    read(this->fd, seeks, sizeof(size_t));
    this->seek = (size_t) atoi(seeks);
    free(seeks);
    //mmap file
    lseek(this->fd, 0, SEEK_SET);
    this->buf = (char *) mmap(NULL, this->size, PROT_READ | PROT_WRITE, MAP_SHARED, this->fd, 0);
    this->seek = MAP_FAILED == this->buf ? -1 : this->seek;
    LOGW("Logcat constructor: seek=%zu\n", this->seek);
}

CircleJournal::~CircleJournal() {
    LOGW("Logcat destructor called.\n");
    if (MAP_FAILED != this->buf) {
        size_t tSize = this->size > 0 ? this->size : 0;
        munmap(this->buf, tSize);
        this->buf = (char *) MAP_FAILED;
        LOGW("Logcat destructor munmap.\n");
    }
    if (this->fd >= 0) {
        close(this->fd);
        this->fd = -1;
        LOGW("Logcat destructor close file.\n");
    }
}

void CircleJournal::sink(const char *values) {
    if (this->fd < 0) return;
    char *buffer = this->buf ? (this->buf + this->heads) : (char *) MAP_FAILED;
    const size_t buffer_size = buffer == (char *) MAP_FAILED ? 0 : (this->size - this->heads);
    const size_t values_size = (size_t) strlen(values);
    if (buffer_size <= 0 || values_size <= 0) {
        return;
    }
    const size_t targets_size = buffer_size >= values_size ? values_size : buffer_size;
    const float tDecay = this->decay > 0 && this->decay < 1 ? this->decay : 0.5f;
    size_t tSeek = this->seek > buffer_size ? buffer_size : this->seek;
    this->seek = tSeek;
    LOGD("sink values=%s, tSeek=%zu, tDecay=%.2f\n", values, tSeek, tDecay);
    while (tSeek > 0) {
        long surplus = buffer_size - tSeek;
        if (targets_size <= surplus) {
            break;
        }
        long tSize = (long) (surplus * tDecay);
        tSeek = 0 == tSize ? 0 : tSeek - tSize;
        tSeek = tSeek < 0 ? 0 : tSeek;
    }
    if (this->seek > tSeek) {
        LOGD("sink tDecay to tSeek=%zu\n", tSeek);
        long offset = this->seek - tSeek;
        memmove(buffer, buffer + offset, buffer_size - offset);
        this->seek = tSeek;
    }
    memcpy(buffer + this->seek, values, targets_size);
    this->seek += targets_size;
    //update heads with format
    sprintf(this->buf, this->heads_fmt, this->seek);
}