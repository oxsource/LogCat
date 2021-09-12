//
// Created by peng on 2021/9/12.
//

#ifndef LCT_MMAPS_H
#define LCT_MMAPS_H

#include <sys/mman.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "Buffer.h"
#include "MMapHead.h"

class MMaps : public Buffer {
private:
    const char *path;
    const long size;
    int fd;
    MMapHead *head;

    void prepare();

public:
    MMaps(const char *path, long size);

    ~MMaps() override;

    const char *name() override;

    long heads() override;

    long sizes() override;

    long frees() override;

    int sink(const char *ins, long length) override;
};


#endif //LCT_MMAPS_H
