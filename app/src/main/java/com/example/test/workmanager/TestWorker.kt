package com.example.test.workmanager

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.Locale

// 10 分钟的执行期限
class TestWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID_9527"
        const val NOTIFICATION_ID = 9527
        val dateFormat by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA) }
    }

    override fun doWork(): Result {

        // do something
        val time = dateFormat.format(System.currentTimeMillis())
        LogUtils.e("doWork time=${time}")
        ToastUtils.showLong("doWork time=${time}")
        val lastText = SPUtils.getInstance().getString("doWork")
        val tempText = if (lastText.isNullOrBlank()) {
            time
        } else {
            "${lastText}\n${time}"
        }
        SPUtils.getInstance().put("doWork", tempText)

        return Result.success()
    }

    // 如果未能实现对应的 getForegroundInfo 方法，那么在旧版平台上调用 setExpedited 时，可能会导致运行时崩溃
    override fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("通知标题")
            .setContentText("通知内容")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(null)
            .build()
    }

}