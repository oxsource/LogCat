//
// Created by peng on 2021/9/12.
//

#include "Roselle.h"
#include "Caches.h"
#include "MMaps.h"
#include "Swaps.h"

Roselle::Roselle(const char *path, long size) : path(path) {
    const long max_size = 5 * 1024 * 1024;
    long total = size > max_size ? max_size : size;
    this->caches = new Caches();
    const long min_size = 2 * caches->sizes();
    total = total < min_size ? min_size : total;
    this->mmaps = new MMaps(path, total);
    this->swaps = new Swaps(path, total);
    //
    this->caches->withBackup(this->mmaps);
    this->mmaps->withBackup(this->swaps);
    this->swaps->withBackup(this->mmaps);
}

Roselle::~Roselle() {
    delete caches;
    delete mmaps;
    delete swaps;
}

const char *Roselle::name() {
    return "roselle";
}

long Roselle::sizes() {
    return this->mmaps ? this->mmaps->sizes() : 0;
}

long Roselle::frees() {
    return this->mmaps ? this->mmaps->frees() : 0;
}

int Roselle::sink(const char *ins, long length) {
    return this->caches->sink(ins, length);
}

void Roselle::flush() {
    this->caches->flush();
    //consolidate all log file
    const int fd = open(this->path, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR);
    LOGW("%s flush path=%s, fd=%d\n", this->name(), this->path, fd);
    if (fd < 0) return;
    //swaps first, mmaps second
    Buffer *buffers[] = {swaps, mmaps};
    const long mmaps_heads = mmaps->heads();
    const int buf_size = 4 * 1024;
    char buf[buf_size];
    int reads, overs;
    for (auto buffer: buffers) {
        //open read file
        char chs[255];
        sprintf(chs, "%s.%s", this->path, buffer->name());
        const int sfd = open(chs, O_RDONLY, S_IRUSR | S_IWUSR);
        LOGW("%s flush buffer@%s fd=%d\n", this->name(), buffer->name(), sfd);
        if (sfd < 0) continue;
        lseek(sfd, 0, SEEK_SET);
        //read file length
        char *seeks = (char *) malloc(mmaps_heads);
        read(sfd, seeks, mmaps_heads);
        char *stops;
        const long limits = (long) ::strtol(seeks, &stops, 10);
        free(seeks);
        //write to target file
        lseek(sfd, mmaps_heads, SEEK_SET);
        long counts = 0;
        while ((reads = (int) read(sfd, buf, buf_size))) {
            counts += reads;
            overs = (int) (counts - limits);
            reads = overs > 0 ? reads - overs : reads;
            write(fd, buf, reads);
            if (overs <= 0) continue;
            counts -= overs;
            break;
        }
        close(sfd);
        LOGW("%s flush buffer@%s: limits=%ld, counts=%ld\n", this->name(), buffer->name(), limits,
             counts);
    }
    close(fd);
}