//
// Created by peng on 2021/9/12.
//

#include "Swaps.h"

Swaps::Swaps(const char *path, long size) : path(path), size(size) {
    this->seek = 0;
    this->value = nullptr;
}

Swaps::~Swaps() = default;

const char *Swaps::name() {
    return "swaps";
}

long Swaps::sizes() {
    return this->size;
}

long Swaps::frees() {
    return this->size;
}

int Swaps::sink(const char *ins, long length) {
    if (!this->backup) return -1;
    char chs1[255];
    sprintf(chs1, "%s.%s", this->path, this->backup->name());
    const int fds = open(chs1, O_RDONLY, S_IRUSR | S_IWUSR);
    LOGD("Swaps sink mmaps fd=%d\n", fds);
    if (fds < 0) return -1;
    //
    char chs2[255];
    sprintf(chs2, "%s.%s", this->path, this->name());
    const int fdd = open(chs2, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR);
    if (fdd < 0) {
        close(fds);
        return -1;
    }
    int reads;
    const int buf_size = 4 * 1024;
    char buf[buf_size];
    while ((reads = (int) read(fds, buf, buf_size))) {
        write(fdd, buf, reads);
    }
    close(fds);
    close(fdd);
    LOGD("Swaps close file.\n");
    return 1;
}

void Swaps::flush() {
    LOGD("Swaps flush do nothing.\n");
}