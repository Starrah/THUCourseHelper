package cn.starrah.thu_course_helper.data.declares

import androidx.room.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * 一个常量类，负责存储大节和小时的转换关系
 */

class CourseConstants{
    /*每个大节的小节数目*/
    companion object StaticParams {
        var SmallNumber: Array<Int> = arrayOf(0, 2, 3, 2, 2, 2, 3)

        /*每个大节开始的时候，对应的小节数目*/
        var StartSmallNumber: Array<Int> = arrayOf(0, 0, 2, 5, 7, 9, 11)


        /*每个大节的开始时间*/
        var StartTime: Array<LocalTime> = arrayOf(
            LocalTime.parse("14:53"),
            LocalTime.parse("08:00"),
            LocalTime.parse("09:50"),
            LocalTime.parse("13:30"),
            LocalTime.parse("15:20"),
            LocalTime.parse("17:10"),
            LocalTime.parse("19:20")
        )

        /*每个小节的时间，分钟*/
        var ClassTime: Int = 45

        /*课间休息时间，分钟*/
        var RestTime: Int = 5
    }
}