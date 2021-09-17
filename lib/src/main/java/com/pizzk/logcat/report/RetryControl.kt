package com.pizzk.logcat.report

import java.util.concurrent.atomic.AtomicInteger

class RetryControl {
    private val steps: List<Long> = listOf(60 * 1000, 2 * 60 * 1000, 5 * 60 * 1000)
    private val index: AtomicInteger = AtomicInteger()

    init {
        reset()
    }

    fun get(): Long {
        return steps[index.get()]
    }

    fun next() {
        val value = (index.get() + 1) % steps.size
        index.set(value)
    }

    fun reset() {
        index.set(0)
    }
}