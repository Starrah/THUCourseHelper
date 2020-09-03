package cn.starrah.thu_course_helper.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import cn.starrah.thu_course_helper.MainActivity
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.data.declares.calendarEntity.CalendarTimeDataWithItem
import cn.starrah.thu_course_helper.data.declares.calendarEnum.CalendarTimeType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalTime

class NotificationTime : BroadcastReceiver() {
    private val BUTTON_UP = "button_up"
    private val BUTTON_DOWN = "button_down"
    private val UPDATE_WIDGET = "update_action"
    private val DELETE_WIDGET = "delete_action"
    private val CHANNEL_ID = "stay_notify"
    private val NOTIFY_ID = 1

    //当前时间段数组
    companion object {
        private var timeList: MutableList<CalendarTimeDataWithItem> = mutableListOf()

        //当前显示的元素
        private var showItem: Int = -1
    }

    /**
     * 接受广播事件
     * 调用时间：每次接收到广播都会调用
     * 操作：切换当前显示的元素
     */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == UPDATE_WIDGET) {
            GlobalScope.launch {
                CREP.initializeDefaultTermIfUninitialized(context, true)
                updateData(context)
                val remoteViews = shiftShow(context)
                updateNotification(remoteViews, context)
            }
        }
        else if (action == DELETE_WIDGET) {
            deleteNotification(context)
        }
        else if (action == BUTTON_UP) {
            showItem -= 1
            if (showItem < 0) {
                showItem = 0
            }
            val remoteViews = shiftShow(context)
            updateNotification(remoteViews, context)
        }
        else if (action == BUTTON_DOWN) {
            showItem++
            if (showItem >= timeList.size) {
                showItem = timeList.size - 1
            }
            val remoteViews = shiftShow(context)
            updateNotification(remoteViews, context)
        }
    }


    /**
     * 描述：切换当前显示的元素（需要先获取当前元素id）
     * 参数：context
     * 返回：得到的remote view
     */
    private fun shiftShow(context: Context): RemoteViews {
        // 小部件在Launcher桌面的布局
        val remoteViews = RemoteViews(context.packageName, R.layout.app_widget_layout)
        //没有日程
        if (timeList.isEmpty() || showItem < 0 || showItem >= timeList.size) {
            val name: String = "今日无日程"
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

            val the_item: CalendarTimeDataWithItem = timeList.get(showItem)
            if (the_item.timeInHour == null) {
                the_item.timeInHour = the_item.timeInCourseSchedule!!.toTimeInHour()
            }

            //名称
            var name: String = the_item.calendarItem.name + "：" + the_item.name
            val current_time: LocalTime = LocalTime.now()
            var description: String = ""
            if (current_time.isAfter(the_item.timeInHour!!.endTime)) {
                description = "（已结束）"
            }
            else if (current_time.isBefore(the_item.timeInHour!!.startTime)) {
                description = "（未开始）"
            }
            else {
                description = "（进行中）"
            }
            name = name + description

            //时间
            val start_time: String = ItemEditActivity.getTimeString(the_item.timeInHour!!.startTime)
            val end_time: String = ItemEditActivity.getTimeString(the_item.timeInHour!!.endTime)
            var time: String = start_time + "-" + end_time
            if (the_item.type == CalendarTimeType.POINT) {
                time = start_time
            }

            //地点
            var place: String = the_item.place
            if (place.isEmpty()) {
                place = "暂无地点"
            }

            remoteViews.setTextViewText(R.id.time_show_name, name)
            remoteViews.setTextViewText(R.id.time_show_time, time)
            remoteViews.setTextViewText(R.id.time_show_place, place)

            //显示button
            remoteViews.setViewVisibility(R.id.button_up, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.button_down, View.VISIBLE)
            if (showItem == 0) {
                remoteViews.setViewVisibility(R.id.button_up, View.INVISIBLE)
            }
            if (showItem == timeList.size - 1) {
                remoteViews.setViewVisibility(R.id.button_down, View.INVISIBLE)
            }
        }

        //按钮绑定函数
        val intent_up = Intent(context, NotificationTime::class.java)
        intent_up.setAction(BUTTON_UP)
        val pendingIntentUp = PendingIntent.getBroadcast(context, 0, intent_up, 0)
        remoteViews.setOnClickPendingIntent(R.id.button_up, pendingIntentUp)

        val intent_down = Intent(context, NotificationTime::class.java)
        intent_down.setAction(BUTTON_DOWN)
        val pendingIntentDown = PendingIntent.getBroadcast(context, 0, intent_down, 0)
        remoteViews.setOnClickPendingIntent(R.id.button_down, pendingIntentDown)
        return remoteViews
    }

    /**
     * 描述：更新通知栏，必须先调用shiftShow
     * 参数：remote view, context
     * 返回：无
     */
    private fun updateNotification(remoteViews: RemoteViews, context: Context) {
        // 更新通知栏
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        mBuilder.setContent(remoteViews)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
            .setTicker("今日日程")
            .setPriority(Notification.PRIORITY_MIN)// 设置该通知优先级
            .setOngoing(true)
            .setSmallIcon(R.drawable.logo)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
        val notify: Notification = mBuilder.build()
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFY_ID, notify);
    }

    /**
     * 描述：关闭通知栏
     * 参数：context
     * 返回：无
     */
    private fun deleteNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFY_ID);
    }

    /**
     * 描述：更新当前的课程/日程数据，以及让当前显示元素变为正在进行的/最近的下一个活动
     * 参数：context
     * 返回：无
     */
    private suspend fun updateData(context: Context) {
        val raw_list = CREP.widgetShowData(false)
        timeList.clear()
        timeList.addAll(raw_list.first)
        showItem = raw_list.second
        if (!timeList.isEmpty() && showItem >= timeList.size) {
            showItem = timeList.size - 1
        }
    }


}