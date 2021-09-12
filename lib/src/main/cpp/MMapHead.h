//
// Created by peng on 2021/9/12.
//

#ifndef LCT_MMAPHEAD_H
#define LCT_MMAPHEAD_H


class MMapHead {
public:
    const char *fmt;

    const long size;

    MMapHead(const char *fmt, long size);

    ~MMapHead();
};


#endif //LCT_MMAPHEAD_H
