package com.rcmiku.freeze.monitor.util

object Shell {

    private fun execute(command: String): Pair<Int, String?> = runCatching {
        Runtime.getRuntime().exec("su").run {
            outputStream.use {
                it.write(command.toByteArray())
            }
            waitFor() to (inputStream.takeIf { it.available() > 0 } ?: errorStream).use {
                it.bufferedReader().readText()
            }
        }
    }.getOrElse { -1 to it.message }

    fun cmd(command: String) = execute(command)

    fun isGranted(command: String = "whoami"): Boolean =
        (cmd(command = command).second?.trimEnd() ?: false) == "root"

    fun getRunningProcess(command: String = "ps -e | awk '{print \$6 \" \" \$9 \" \" \$5}'") =
        cmd(command = command).second

    fun pmPackageList(command: String = "pm list package") = cmd(command = command).second

}