package cn.starrah.thu_course_helper.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import cn.starrah.thu_course_helper.R

class AppWidget : AppWidgetProvider() {
    private val ACTION_BUTTON = "action_button"

    /**
     * 接受广播事件
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(this.javaClass.name, "onReceive")
        if (intent == null) return
        val action = intent.action
        if (action == ACTION_BUTTON) {
            // 只能通过远程对象来设置appWidget中的状态
            val remoteViews = RemoteViews(
                context.packageName,
                R.layout.app_widget_layout
            )

            //val intent = Intent(context, AppWidget::class.java)
            //intent.setAction(ACTION_BUTTON)

            //val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            // 事件
            //remoteViews.setOnClickPendingIntent(R.id.btn, pendingIntent)

            remoteViews.setTextViewText(
                R.id.text,
                "" + System.currentTimeMillis()
            )
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AppWidget::class.java)

            // 更新appWidget
            appWidgetManager.updateAppWidget(componentName, remoteViews)
        }
    }

    /**
     * 到达指定的更新时间或者当用户向桌面添加AppWidget时被调用
     * appWidgetIds:桌面上所有的widget都会被分配一个唯一的ID标识，这个数组就是他们的列表
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(this.javaClass.name, "onUpdate")
        val intent = Intent(context, AppWidget::class.java)
        intent.setAction(ACTION_BUTTON)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        // 小部件在Launcher桌面的布局
        val remoteViews = RemoteViews(
            context.packageName,
            R.layout.app_widget_layout
        )

        // 事件
        remoteViews.setOnClickPendingIntent(R.id.btn, pendingIntent)


        // 更新AppWidget
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    /**
     * 删除AppWidget
     */
    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray
    ) {
        super.onDeleted(context, appWidgetIds)
        Log.d(this.javaClass.name, "onDeleted")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(this.javaClass.name, "onDisabled")
    }

    /**
     * AppWidget首次创建调用
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(this.javaClass.name, "onEnabled")


    }
}