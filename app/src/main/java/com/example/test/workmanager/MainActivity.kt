package com.example.test.workmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blankj.utilcode.util.SPUtils
import com.example.test.workmanager.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

/*
val continuation = WorkManager.getInstance(context)
    .beginUniqueWork(
        Constants.IMAGE_MANIPULATION_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequest.from(CleanupWorker::class.java)
    ).then(OneTimeWorkRequest.from(WaterColorFilterWorker::class.java))
    .then(OneTimeWorkRequest.from(GrayScaleFilterWorker::class.java))
    .then(OneTimeWorkRequest.from(BlurEffectFilterWorker::class.java))
    .then(
        if (save) {
            workRequest<SaveImageToGalleryWorker>(tag = Constants.TAG_OUTPUT)
        } else /* upload */ {
            workRequest<UploadWorker>(tag = Constants.TAG_OUTPUT)
        }
    )
*/
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // todo 国产定制 ROM 可能需要开启 自启动 才能后台执行 (实测 MIUI 12 是需要开启 自启动，否则会在 APP 启动才能执行后台期间所有任务)

        binding.btnGet.setOnClickListener {
            val text = SPUtils.getInstance().getString("doWork")
            binding.tv.text = "获取上一次 doWork 的执行时间:\n${text}"
        }

        binding.btnClear.setOnClickListener {
            SPUtils.getInstance().remove("doWork")
        }

        binding.btn.setOnClickListener {
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

        binding.btn3.setOnClickListener {
            // 唯一 单例

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

        binding.btn4.setOnClickListener {
            // 取消 3种:
            // by id, `cancelWorkById`
            // by name, `cancelUniqueWork`
            // by tag, `cancelAllWorkByTag`

            WorkManager.getInstance(this).cancelAllWorkByTag("Tag-OneTimeWork")

            WorkManager.getInstance(this).cancelAllWorkByTag("Tag-PeriodicWork")

            WorkManager.getInstance(this).cancelAllWorkByTag("Tag-PeriodicWork")
            WorkManager.getInstance(this).cancelUniqueWork("Name-Singleton-PeriodicWork")

        }

    }

}