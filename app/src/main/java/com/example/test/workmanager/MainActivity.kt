package com.example.test.workmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blankj.utilcode.util.SPUtils
import com.example.test.workmanager.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // WorkerManager 官方文档
        // https://developer.android.google.cn/topic/libraries/architecture/workmanager?hl=zh-cn

        // todo 国产定制 ROM 可能需要开启 自启动 才能后台执行 (实测 MIUI 12 是需要开启 自启动，否则会在 APP 启动才能执行后台期间所有任务)
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${packageName}")
            })
        }

        binding.btnGet.setOnClickListener {
            val text = SPUtils.getInstance().getString("doWork")
            binding.tv.text = "获取保存的 doWork 的执行时间:\n${text}"
        }

        binding.btnClear.setOnClickListener {
            SPUtils.getInstance().remove("doWork")
        }

        binding.btn1.setOnClickListener {
            // 一次性

//            val request = OneTimeWorkRequest.Companion.from(TestWork::class.java)
            val request = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag-OneTimeWork")
                .build()

            WorkManager.getInstance(this)
                .enqueue(request)
        }

        binding.btn2.setOnClickListener {
            // 周期

            // 可以定义的最短重复间隔是 15 分钟（与 JobScheduler API 相同）。
            val request = PeriodicWorkRequestBuilder<TestWorker>(15, TimeUnit.MINUTES)
                .addTag("Tag-PeriodicWork")
                .build()

            WorkManager.getInstance(this)
                .enqueue(request)
        }

        binding.btn11.setOnClickListener {
            // 一次性 单例
            // REPLACE 若队列中有相同标识的 Worker 则取消原有 Worker 然后新的 Worker 入队
            // KEEP 若队列中有相同标识的 Worker 则放弃入队 保持原有 Worker
            // APPEND_OR_REPLACE 若队列中有相同标识的 Worker 则附加在原有 Worker 后面，如果原有 Worker 执行失败，新 Worker 也跟随
            // APPEND_OR_REPLACE 若队列中有相同标识的 Worker 则附加在原有 Worker 后面，如果原有 Worker 执行失败，新 Worker 会重新入队

            // by id
            val request = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag-Singleton-OneTimeWork")
                .build()
            WorkManager.getInstance(this)
                .enqueueUniqueWork("Name-Singleton-OneTimeWork", ExistingWorkPolicy.KEEP, request)
            val id = request.id
            WorkManager.getInstance(this).cancelWorkById(id)
        }

        binding.btn22.setOnClickListener {
            // 周期 单例
            // KEEP 若队列中有相同标识的 Worker 则放弃入队 保持原有 Worker
            // CANCEL_AND_REENQUEUE 若队列中有相同标识的 Worker 则取消原有 Worker 然后新的 Worker 入队
            // UPDATE 若队列中有相同标识的 Worker 则更新替代原有 Worker 并继承状态 (例如 8 小时的任务，剩下 5 小时将会执行，那么新的 Worker 将在 5 小时后执行)

            val request = PeriodicWorkRequestBuilder<TestWorker>(15, TimeUnit.MINUTES)
                .addTag("Tag-Singleton-PeriodicWork")
                .build()

            WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                    "Name-Singleton-PeriodicWork", // 用于唯一标识工作请求
                    ExistingPeriodicWorkPolicy.KEEP, // 如果已有使用该名称且尚未完成的唯一工作链，应执行什么操作
                    request
                )
        }

        binding.btn3.setOnClickListener {
            // 取消指定 Worker
            // 3种:
            // by id, `cancelWorkById`
            // by name, `cancelUniqueWork`
            // by tag, `cancelAllWorkByTag`

            // by id
            val request = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag")
                .build()
            WorkManager.getInstance(this)
                .enqueueUniqueWork("Name", ExistingWorkPolicy.APPEND, request)
            val id = request.id
            WorkManager.getInstance(this).cancelWorkById(id)

            // by tag
            WorkManager.getInstance(this).cancelAllWorkByTag("Tag-OneTimeWork")
            WorkManager.getInstance(this).cancelAllWorkByTag("Tag-PeriodicWork")

            // by name
            WorkManager.getInstance(this).cancelAllWorkByTag("Name-Singleton-OneTimeWork")
            WorkManager.getInstance(this).cancelUniqueWork("Name-Singleton-PeriodicWork")
        }

        binding.btn33.setOnClickListener {
            // 取消所有 Worker
            WorkManager.getInstance(this).cancelAllWork()
        }

        binding.btn4.setOnClickListener {
            // 链接多个 Worker

            val request = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag-request")
                .build()

            val requestCache = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag-requestCache")
                .build()

            val requestUpload = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag("Tag-requestUpload")
                .build()

            WorkManager.getInstance(this)
                // request 也可以替代为传入 requestList, `beginUniqueWork` 提供了 request 和 requestList 的重载函数
                .beginUniqueWork("Name", ExistingWorkPolicy.APPEND, request)
                .then(requestCache)
                .then(requestUpload)
                .enqueue()

        }

    }

}