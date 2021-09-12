//
// Created by peng on 2021/9/12.
//

#ifndef LCT_CACHES_H
#define LCT_CACHES_H

#include "Buffer.h"

class Caches : public Buffer {
private:
    const long size;
public:
    Caches();

    ~Caches() override;

    const char *name() override;

    long sizes() override;
};

#endif //LCT_CACHES_H
