//
// Created by peng on 2021/9/12.
//

#include "MMaps.h"

MMaps::MMaps(const char *path, long size) : path(path), size(size) {
    LOGW("MMaps constructor path=%s, size=%ld.\n", path, size);
    //heads = max_length + \0 + \n, max support eight-digit number
    this->head = new MMapHead("%08ld\n", 8 + 2);
    this->fd = -1;
}

MMaps::~MMaps() {
    if (this->value) {
        long tSize = this->size > 0 ? this->size : 0;
        munmap(this->value, tSize);
        this->value = nullptr;
        LOGW("MMaps destructor munmap.\n");
    }
    if (this->fd >= 0) {
        close(this->fd);
        this->fd = -1;
        LOGW("MMaps destructor close file.\n");
    }
}

void MMaps::prepare() {
    if (this->fd > 0) return;
    this->seek = -1;
    this->value = nullptr;
    char chs[255];
    sprintf(chs, "%s.%s", this->path, this->name());
    this->fd = open(chs, O_RDWR | O_APPEND | O_CREAT, S_IRUSR | S_IWUSR);
    LOGW("MMaps setup path=%s, fd=%d\n", chs, this->fd);
    if (this->fd < 0) return;
    //reset file size
    ftruncate(this->fd, this->size);
    lseek(this->fd, 0, SEEK_SET);
    //read file length
    char *seeks = (char *) malloc(this->head->size);
    read(this->fd, seeks, this->head->size);
    char *stops;
    this->seek = (long) ::strtol(seeks, &stops, 10);
    free(seeks);
    //mmap file
    lseek(this->fd, 0, SEEK_SET);
    void *buffer = mmap(nullptr, this->size, PROT_READ | PROT_WRITE, MAP_SHARED, this->fd, 0);
    this->value = MAP_FAILED == buffer ? nullptr : (char *) buffer;
    this->seek = this->value ? this->seek : -1;
    LOGW("MMaps setup: seek=%ld\n", this->seek);
}

const char *MMaps::name() {
    return "mmaps";
}

long MMaps::heads() {
    return this->head->size;
}

long MMaps::sizes() {
    return this->fd < 0 || !this->value ? 0 : this->size - this->heads();
}

long MMaps::frees() {
    return this->fd < 0 || !this->value ? 0 : this->Buffer::frees();
}

int MMaps::sink(const char *ins, long length) {
    this->prepare();
    if (this->fd < 0 || !this->value) return -1;
    if (this->Buffer::sink(ins, length) <= 0) return -2;
    //update heads with format
    sprintf(this->value, this->head->fmt, this->seek);
    return 1;
}