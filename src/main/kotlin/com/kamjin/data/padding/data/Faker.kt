package com.kamjin.data.padding.data

import java.time.*
import java.time.format.*

typealias InnerFun = (Maker, String?) -> String

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/28
 */
class Maker {

    var ins: Iterator<Any>? = null

    var i = 1

    /**
     * id increment
     */
    fun autoId(param: String?): Int {
        if (param != null) {
            i = param.toInt()
        }
        return this.i++
    }

    fun formatAutoId(format: String?): String {
        if (format != null) {
            return String.format(format, i++)
        }
        return i++.toString()
    }

    /**
     * order gen for: a,b,c
     */
    fun order(param: String?): Any? {
        if (ins == null) {
            ins = param?.split(",")?.iterator()
        }
        return if (ins?.hasNext() == true) ins?.next() else String.EMPTY
    }

    /**
     * not input the now
     * else random for duration
     * input format:    2020-11-20 11:20:30~2021-11-20 11:20:30
     */
    fun time(param: String?): LocalDateTime {
        if (param == null) {
            return LocalDateTime.now()
        }
        val split = param.split("~").map { LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) }

        //将两个时间转为时间戳
        val start: Long = split[0].toEpochSecond(ZoneOffset.of("+8"));
        val end: Long = split[1].toEpochSecond(ZoneOffset.of("+8"));
        //获取两个时间的随机数
        val difference = (Math.random() * (end - start)).toLong();
        //生成时间
        return LocalDateTime.ofEpochSecond(start + difference, 0, ZoneOffset.ofHours(8));

    }

    /**
     * random choice
     */
    fun choice(param: String?): String {
        return param?.split(",")?.random() ?: ""
    }
}

val String.Companion.EMPTY: String
    get() = ""

fun main() {
    println(String.format("adasd{}asdasda", 1))
    println(String.format("ad%dasd%dasdasda", 213123123))
}