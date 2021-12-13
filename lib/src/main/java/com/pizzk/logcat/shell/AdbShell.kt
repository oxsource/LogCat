package com.pizzk.logcat.shell


import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

class AdbShell : BashShell() {
    companion object {
        private const val TAG = "Roselle.AdbShell"
        private const val PROGRAM = "/system/bin/sh"
        private const val PARAMS = "-c"

        //返回结果状态码
        private const val RET_FAILURE = "-1"
        private const val BLACK_REGEX = "\\s*|\t|\r|\n"

        @JvmStatic
        fun nimble(cmd: String): String {
            val shell: BashShell = obtain(AdbShell::class.java)
            return try {
                val resp: String = shell.execute(cmd)
                val pattern: Pattern = BLACK_REGEX.toRegex().toPattern()
                pattern.matcher(resp).replaceAll("")
            } catch (e: Exception) {
                ""
            } finally {
                recycle(shell)
            }
        }
    }

    //执行进程
    private var process: Process? = null

    override fun connect(): Boolean {
        if (seal || null != process) return false
        return true
    }

    override fun disconnect() {
        val ps: Process = process ?: return
        catcher({ ps.inputStream.close() }, "close inputStream")
        catcher(ps::destroy, "destroy process")
        process = null
    }

    override fun read(buf: StringBuffer): Boolean {
        return try {
            val ps: Process = process ?: throw Exception("process is null")
            val ins: InputStream = ps.inputStream
            val reader = BufferedReader(InputStreamReader(ins))
            var line: String?
            while (true) {
                line = reader.readLine()
                line ?: break
                buf.append("$line\n")
            }
            true
        } catch (e: Exception) {
            disconnect()
            Log.e(TAG, "$name read exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun write(cmd: String): Boolean {
        return try {
            if (cmd.isEmpty()) throw Exception("write cmd: is empty")
            val pb: ProcessBuilder = ProcessBuilder()
                .command(PROGRAM, PARAMS, cmd)
                .redirectErrorStream(true)
            process = pb.start()
            null != process
        } catch (e: Exception) {
            disconnect()
            Log.e(TAG, "$name write exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun execute(cmd: String, retry: Boolean): String {
        val error: String = RET_FAILURE
        return try {
            lock.lock()
            if (!connect()) {
                Log.d(TAG, "$name is busy")
                return error
            }
            Log.d(TAG, "$name write command: $cmd")
            if (!write(cmd)) {
                if (retry) Log.e(TAG, "$name write command failed, reconnect!")
                return if (retry) execute(cmd, retry = false) else error
            }
            val buf = StringBuffer()
            read(buf)
            val result: String = buf.toString()
            buf.setLength(0)
            Log.d(TAG, "$name read result: $result")
            result
        } catch (e: Exception) {
            Log.d(TAG, "$name execute exception: ${e.message}")
            error
        } finally {
            disconnect()
            lock.unlock()
        }
    }

    override fun successful(result: String): Boolean {
        return !result.startsWith(RET_FAILURE)
    }
}