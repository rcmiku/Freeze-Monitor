package com.rcmiku.freeze.monitor.util

object Process {

    private val _freezeType = listOf("V1", "V2", "SIGSTOP")

    fun countProcess(source: String, target: String): Int {
        val regex = Regex("${Regex.escape(target)}[:\\s]")
        return regex.findAll(source).count()
    }

    fun countProcess(source: List<List<String>>, target: String): Int {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        return source.count {
            it.getOrNull(1)?.let { indexValue ->
                regex.containsMatchIn(indexValue)
            } ?: false
        }
    }

    fun sumProcessRes(source: List<List<String>>, target: String): Int {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        return source.sumOf { row ->
            if (row.getOrNull(1)?.let { regex.containsMatchIn(it) } == true) {
                row.getOrNull(2)?.toIntOrNull() ?: 0
            } else {
                0
            }.div(1024)
        }
    }

    fun getFreezeType(source: List<List<String>>, target: String): String? {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        for (list in source) {
            if (regex.containsMatchIn(list[1])) {
                return when (list[0]) {
                    "__refrigerator" -> _freezeType[0]
                    "do_freezer_trap" -> _freezeType[1]
                    "get_signal" -> _freezeType[1]
                    "do_signal_stop" -> _freezeType[2]
                    else -> null
                }
            }
        }
        return null
    }
}