package cn.starrah.thu_course_helper.data

import androidx.room.*
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarItemData
import cn.starrah.thu_course_helper.data.declares.calendarEnum.*
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: CalendarItemType?,
    @ColumnInfo(name = "last_name") val lastName: CalendarItemType?
)


@Dao
interface UserDao {
    @Query("SELECT * FROM CalendarItemData")
    fun getAll(): List<CalendarItemData>
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: CalendarItemData)
//
//    @Delete
//    fun delete(user: User)
}


@Database(entities = arrayOf(CalendarItemData::class), version = 1)
@TypeConverters(CalendarItemType.TC::class, CalendarItemLegalDetailKey.TC::class, CalendarTimeType.TC::class, CalendarRemindType.TC::class, CalendarRemindMethodType.TC::class, CalendarItemData.TC::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

fun main(){
//    val qwq = TimeInHour(
//        LocalTime.parse("08:30", DateTimeFormatter.ISO_TIME),
//        LocalTime.parse("10:00", DateTimeFormatter.ISO_TIME),
//        DayOfWeek.THURSDAY,
//        LocalDate.parse("2020-05-02", DateTimeFormatter.ISO_DATE)
//        )
//    data class YYY (val dt: Calendar?)
//    val qwq = YYY(Calendar.getInstance().apply { timeInMillis = 0; set(2020, 5, 2, 8, 30) })
//    val ttt = SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2020-05-02 08:30")
//    val qwq = TimeInHour(LocalTime.of(8, 30), LocalTime.of(10, 0),
//        date= LocalDate.of(2020,5,1))
//    val str = TimeInHour.TC().toDBDataType(qwq)
//    println(str)
//    val yyy = TimeInHour.TC().fromDBDataType(str)
//    println(qwq == yyy)
//    val a = 0
    val a = LocalDate.of(2020,6,10)
    val b = LocalDate.of(2020,5,1)
    val c = ChronoUnit.DAYS.between(b, a).toInt()
//    val d = b.until(a).get(ChronoUnit.)
    println(c)
    val d = b + Period.ofDays(c)
    println(d)
    println(d == a)


}

