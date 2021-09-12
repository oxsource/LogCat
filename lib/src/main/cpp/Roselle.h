//
// Created by peng on 2021/9/12.
//

#ifndef LCT_ROSELLE_H
#define LCT_ROSELLE_H

#include "Buffer.h"

/**
 * three core buffer:
 * 1. Caches(memory)
 * 2. MMaps(mmap)
 * 3. Swaps(file)
 *
 * when current buffer sinking oversize, it will sink to backup buffer, then empty current and to be continue;
 * if set Roselle size as 5MB, MMaps and Swaps can bear the weight of 5M by oneself(Roselle max size is twice of init size);
 *
 */
class Roselle : public Buffer {
private:
    const char *path;
    Buffer *caches;
    Buffer *mmaps;
    Buffer *swaps;

public:
    Roselle(const char *path, long size);

    ~Roselle() override;

    const char *name() override;

    long sizes() override;

    long frees() override;

    int sink(const char *ins, long length) override;

    void flush() override;
};


#endif //LCT_ROSELLE_H
