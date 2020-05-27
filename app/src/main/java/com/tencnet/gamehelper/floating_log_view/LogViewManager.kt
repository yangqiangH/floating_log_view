package com.tencnet.gamehelper.floating_log_view

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.imuxuan.floatingview.FloatingView
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 *
 * @Description:    日志浮窗的管理类
 * @Author:         astonyang AstonYang
 * @CreateDate:     2020/5/13 3:11 PM
 * @Version:        1.0
 */
object LogViewManager {

    private const val MAX_LOG_COUNT = 1000
    private val ALL_LOGS = Collections.synchronizedList(LinkedList<LogInfo>())
    val FILTER_LOGS = Collections.synchronizedList(LinkedList<LogInfo>())

    private var isFloatingViewAdded = false;
    private var autoIncrementInteger = 0
    var mLogSetChangedListener: LogDataSetChangedCallback? = null
    val handler = Handler(Looper.getMainLooper())

    var keywordFilter = ""
    set(value) {

        if (value.isBlank()) {

            //显示全部日志
            FILTER_LOGS.clear()
            mLogSetChangedListener?.onChanged()
            FILTER_LOGS.addAll(ALL_LOGS)
            mLogSetChangedListener?.onChanged()

        } else if (field != value){

            field = value

            val temp = ALL_LOGS.filter {
                filter(it.content) != null
            }

            FILTER_LOGS.clear()
            mLogSetChangedListener?.onChanged()
            FILTER_LOGS.addAll(temp)
            mLogSetChangedListener?.onChanged()

        }

        field = value

    }

    val lifecycle by lazy {
        StatisticActivityLifecycleCallback()
    }

    fun initLogView(context: Context) {

        if (isFloatingViewAdded) {
            return
        }

        isFloatingViewAdded = true
        val logView = FloatingLogView(context)

        val application =  context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(lifecycle)

        FloatingView.get().customView(logView)
        FloatingView.get().listener(logView.magnetListener)
        FloatingView.get().layoutParams(ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = 120
            leftMargin = 12
        })
        FloatingView.get().add()
    }

    fun attachNow(activity: Activity) {
        FloatingView.get().attach(activity)
    }

    fun destroyView(context: Context) {
        isFloatingViewAdded = false
        val application =  context.applicationContext as Application
        application.unregisterActivityLifecycleCallbacks(lifecycle)
        val view = FloatingView.get().remove()
        (view.view as? FloatingLogView)?.destroy()
    }

    fun clearAll() {
        ALL_LOGS.clear()
        FILTER_LOGS.clear()
        mLogSetChangedListener?.onChanged()
    }

    fun printLog(tag: String, content: String) {
        if (!isFloatingViewAdded) {
            return
        }
        val runnable = Runnable {
            printLogInner(tag, content)
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    private fun printLogInner(tag: String, content: String) {
        val message = try {
            //做一个简答的格式判定，如果json，则格式化输出。
            when(content[0]) {
                '{' -> {
                    val jo = JSONObject(content)
                    jo.toString(4)
                }

                '[' -> {
                    val jo = JSONArray(content)
                    jo.toString(4)
                }

                else -> {
                    content
                }
            }
        } catch (e: Exception) {
            content
        }

        val logInfo = LogInfo().apply {
            this.tag = tag
            this.content = message
            this.timeMills = System.currentTimeMillis()
            if (autoIncrementInteger == Int.MAX_VALUE) {
                autoIncrementInteger = 0
            }
            this.position = ++autoIncrementInteger
        }

        if (ALL_LOGS.size >= MAX_LOG_COUNT) {
            ALL_LOGS.removeAt(0)
        }
        ALL_LOGS.add(logInfo)

        filter(message)?.let {
            if (FILTER_LOGS.size >= MAX_LOG_COUNT) {
                FILTER_LOGS.removeAt(0)
                mLogSetChangedListener?.onChanged()
            }
            FILTER_LOGS.add(logInfo)
            mLogSetChangedListener?.onChanged()
        }
    }

    private fun filter (content: String) : String? {
        return if (keywordFilter.isEmpty() || content.contains(keywordFilter, true)) {
            content
        } else {
            null
        }
    }

}


class StatisticActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {


    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
        FloatingView.get().attach(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
        FloatingView.get().detach(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }
}

data class LogInfo (var timeMills:Long = 0,
                    var tag:String = "",
                    var content: String = "",
                    var position: Int = 0)

interface LogDataSetChangedCallback {
    fun onChanged();
}