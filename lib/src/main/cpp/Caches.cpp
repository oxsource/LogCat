//
// Created by peng on 2021/9/12.
//

#include "Caches.h"

Caches::Caches() : size(4 * 1024) {
    this->value = static_cast<char *>(malloc(this->size));
    this->seek = 0;
}

Caches::~Caches() {
    if (this->value) free(value);
    this->value = nullptr;
    this->seek = 0;
}

const char *Caches::name() {
    return "caches";
}

long Caches::sizes() {
    return this->size;
}
