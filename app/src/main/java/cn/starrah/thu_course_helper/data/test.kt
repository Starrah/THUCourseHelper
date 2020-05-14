import cn.starrah.thu_course_helper.data.declares.time.TimeInHour
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TTT(
    var dt: LocalDate?,
    @JSONField(format = "HH:mm")
    var tm: LocalTime?,
    val w: DayOfWeek?
)
fun main(){
    val a = TTT(LocalDate.now(), LocalTime.now(), LocalDate.now().dayOfWeek)
    val b = TTT(null, LocalTime.now(), null)
    val c = JSON.toJSONString(a)
    val d = JSON.toJSONString(b)
    println(c)
    println(d)
    val e = JSON.parseObject(c, TTT::class.java)
    val f = JSON.parseObject(d, TTT::class.java)
    println(e == a)
    println(f == b)
    val g = TimeInHour(LocalTime.now(), LocalTime.now().plusHours(1), DayOfWeek.THURSDAY, null)
    println(JSON.toJSONString(g))
}

