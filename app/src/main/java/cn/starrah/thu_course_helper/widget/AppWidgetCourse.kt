package cn.starrah.thu_course_helper.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalTime

class AppWidgetCourse : AppWidgetProvider() {
    private val BUTTON_UP = "button_up"
    private val BUTTON_DOWN = "button_down"
    private val UPDATE_WIDGET = "update_action"

    //当前时间段数组
    companion object {
        private var timeList: MutableList<CalendarTimeDataWithItem> = mutableListOf()

        //当前显示的元素
        private var showItem:Int = -1
    }

    /**
     * 接受广播事件
     * 调用时间：每次接收到广播都会调用
     * 操作：切换当前显示的元素
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent == null) return
        val action = intent.action
        if(action == UPDATE_WIDGET) {
            GlobalScope.launch {
                CREP.initializeDefaultTermIfUninitialized(context, true)
                updateData(context)
                shiftShow(context)
            }
        }
        else if (action == BUTTON_UP) {
            showItem -= 1
            if(showItem < 0) {
                showItem = 0
            }
            shiftShow(context)
        }
        else if(action == BUTTON_DOWN) {
            showItem ++
            if(showItem >= timeList.size) {
                showItem = timeList.size - 1
            }
            shiftShow(context)
        }
    }




    /**
     * 到达指定的更新时间或者当用户向桌面添加AppWidget时被调用
     * appWidgetIds:桌面上所有的widget都会被分配一个唯一的ID标识，这个数组就是他们的列表
     * 调用时间：每隔30分钟会自动调用，进入app时也会调用
     * 操作：更新数据和绑定按钮事件
     */

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        GlobalScope.launch {
            updateData(context)
        }
    }


    /**
     * 描述：切换当前显示的元素（需要先获取当前元素id）
     * 参数：context
     * 返回：无
     */
    private fun shiftShow(context: Context) {
        // 小部件在Launcher桌面的布局
        val remoteViews = RemoteViews(context.packageName, R.layout.app_widget_layout)
        //没有日程
        if(timeList.isEmpty() || showItem < 0 || showItem >= timeList.size) {
            var name:String = "今日无课程"
            remoteViews.setTextViewText(R.id.time_show_name, name)
            remoteViews.setViewVisibility(R.id.time_show_time_place, View.INVISIBLE)
            remoteViews.setViewVisibility(R.id.time_show_place_place, View.INVISIBLE)
            remoteViews.setViewVisibility(R.id.button_up, View.INVISIBLE)
            remoteViews.setViewVisibility(R.id.button_down, View.INVISIBLE)
        }
        else {
            //显示元素
            remoteViews.setViewVisibility(R.id.time_show_time_place, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.time_show_place_place, View.VISIBLE)

            var the_item: CalendarTimeDataWithItem = timeList.get(showItem)
            if(the_item.timeInHour == null) {
                the_item.timeInHour = the_item.timeInCourseSchedule!!.toTimeInHour()
            }

            //名称
            var name:String = the_item.calendarItem.name + the_item.name
            var current_time:LocalTime = LocalTime.now()
            var description:String = ""
            if(current_time.isAfter(the_item.timeInHour!!.endTime)) {
                description = "（已结束）"
            }
            else if(current_time.isBefore(the_item.timeInHour!!.startTime)) {
                description = "（未开始）"
            }
            else {
                description = "（进行中）"
            }
            name = name + description

            //时间
            var start_time:String = ItemEditActivity.getTimeString(the_item.timeInHour!!.startTime)
            var end_time:String = ItemEditActivity.getTimeString(the_item.timeInHour!!.endTime)
            var time:String = start_time + "-" + end_time
            if(the_item.type == CalendarTimeType.POINT) {
                time = start_time
            }

            //地点
            var place:String = the_item.place
            if(place.isEmpty()) {
                place = "暂无地点"
            }

            remoteViews.setTextViewText(R.id.time_show_name, name)
            remoteViews.setTextViewText(R.id.time_show_time, time)
            remoteViews.setTextViewText(R.id.time_show_place, place)

            //显示button
            remoteViews.setViewVisibility(R.id.button_up, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.button_down, View.VISIBLE)
            if(showItem == 0) {
                remoteViews.setViewVisibility(R.id.button_up, View.INVISIBLE)
            }
            if(showItem == timeList.size - 1) {
                remoteViews.setViewVisibility(R.id.button_down, View.INVISIBLE)
            }
        }

        //按钮绑定函数
        val intent_up = Intent(context, AppWidgetCourse::class.java)
        intent_up.setAction(BUTTON_UP)
        val pendingIntentUp = PendingIntent.getBroadcast(context, 0, intent_up, 0)
        remoteViews.setOnClickPendingIntent(R.id.button_up, pendingIntentUp)

        val intent_down = Intent(context, AppWidgetCourse::class.java)
        intent_down.setAction(BUTTON_DOWN)
        val pendingIntentDown = PendingIntent.getBroadcast(context, 0, intent_down, 0)
        remoteViews.setOnClickPendingIntent(R.id.button_down, pendingIntentDown)

        // 更新appWidget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, AppWidgetCourse::class.java)
        appWidgetManager.updateAppWidget(componentName, remoteViews)
    }

    /**
     * 描述：更新当前的课程/日程数据，以及让当前显示元素变为正在进行的/最近的下一个活动
     * 参数：context
     * 返回：无
     */
    private suspend fun updateData(context: Context) {
        var raw_list = CREP.widgetShowData(true)
        timeList.clear()
        timeList.addAll(raw_list.first)
        showItem = raw_list.second
        if(!timeList.isEmpty() && showItem >= timeList.size) {
            showItem = timeList.size - 1
        }
    }


}