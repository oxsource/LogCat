//
// Created by peng on 2021/9/12.
//

#ifndef LCT_SWAPS_H
#define LCT_SWAPS_H

#include <sys/mman.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "Buffer.h"

class Swaps : public Buffer {
private:
    const char *path;
    const long size;
public:
    Swaps(const char *path, long size);

    ~Swaps() override;

    const char *name() override;

    long sizes() override;

    long frees() override;

    int sink(const char *ins, long length) override;

    void flush() override;
};


#endif //LCT_SWAPS_H
