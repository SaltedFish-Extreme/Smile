package com.example.smile.ui.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieDrawable
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppApplication
import com.example.smile.http.NetApi.NotificationVideoAPI
import com.example.smile.model.JokeContentModel
import com.example.smile.model.NotificationVideoModel
import com.example.smile.util.LottieAnimationUtil
import com.example.smile.util.LottieAnimationUtil.getLottieAnimationList
import com.example.smile.util.LottieAnimationUtil.getLottieDrawable
import com.example.smile.util.decrypt
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hjq.toast.Toaster

/** 主页 */
class MainActivity : AppActivity() {

    private val bottomNavigationView: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation_view) }
    private val navHostFragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    private val fab: FloatingActionButton by lazy { findViewById(R.id.fab) }

    companion object {
        /** 菜单编号(和底部导航图中fixFragment的id保持一致) */
        private val menuItemIdList = arrayListOf(R.id.home_page, R.id.tools_page, R.id.release_page, R.id.message_page, R.id.profile_page)

        /** 退出时间 */
        private var exitTime = 0L

        //通知渠道ID
        private const val channelId = "smile_notification"

        //通知渠道名
        private const val channelName = "开口笑通知"

        //通知渠道重要级
        private const val importance = NotificationManager.IMPORTANCE_DEFAULT

        //通知管理器
        private lateinit var notificationManager: NotificationManager

        //通知对象
        private lateinit var notification: Notification

        //通知Id
        private const val notificationId = 1

        //PendingIntent标志，标识Intent传递内容可变
        private val flagBit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化底部导航栏
        initBottomNavigationView()
        fab.clickNoRepeat {
            //震动一下
            vibration()
            openActivity<ReleaseActivity>()
        }
        //显示通知
        createNotificationForNormal()
    }

    private fun initBottomNavigationView() {
        bottomNavigationView.menu.apply {
            //获取底部导航栏标题列表
            val titleList = resources.getStringArray(R.array.bottom_navigation_title)
            for (i in titleList.indices) {
                //遍历标题列表，使用菜单编号和标题 添加菜单项
                add(Menu.NONE, menuItemIdList[i], Menu.NONE, titleList[i])
            }
            //设置菜单图标
            setLottieDrawable(getLottieAnimationList())
        }
        //初始化事件
        initBottomNavigationEvent()
    }

    /**
     * Menu扩展函数，设置菜单图标(Lottie动画)
     *
     * @param lottieAnimationList lottie动画列表
     */
    private fun Menu.setLottieDrawable(lottieAnimationList: ArrayList<LottieAnimationUtil.LottieAnimation>) {
        //遍历底部导航栏菜单列表，将除了中间之外的每项图标替换成LottieDrawable
        (0 until bottomNavigationView.menu.size()).filter { it != 2 }.forEach {
            findItem(menuItemIdList[it]).icon = getLottieDrawable(lottieAnimationList[it], bottomNavigationView)
        }
    }

    /** 初始化底部导航栏事件 */
    private fun initBottomNavigationEvent() {
        //设置底部导航栏项目选中事件
        bottomNavigationView.setOnItemSelectedListener {
            //加载动画
            handlePlayLottieAnimation(it)
            //导航到对应的片段
            navHostFragment.navController.navigate(it.itemId)
            true
        }
        //设置底部导航栏项目重复选中事件
        bottomNavigationView.setOnItemReselectedListener {
            //加载动画
            handlePlayLottieAnimation(it)
        }
        //默认选中第一个菜单项
        bottomNavigationView.selectedItemId = menuItemIdList[0]
        //不启用第三个菜单项功能，用悬浮按钮代替
        bottomNavigationView.menu.findItem(R.id.release_page).isEnabled = false
        //处理长按 MenuItem 提示 TooltipText
        bottomNavigationView.menu.forEach { item ->
            val menuItemView = findViewById<BottomNavigationItemView>(item.itemId)
            menuItemView.setOnLongClickListener {
                //长按菜单项使其执行选中操作事件
                bottomNavigationView.selectedItemId = item.itemId
                true
            }
        }
    }

    /**
     * 加载Lottie动画
     *
     * @param item 菜单项
     */
    private fun handlePlayLottieAnimation(item: MenuItem) {
        val currentIcon = item.icon as? LottieDrawable
        currentIcon?.apply {
            //播放动画
            playAnimation()
        }
        //震动一下
        vibration()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        //返回键退出程序确认
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toaster.show(R.string.exit)
            exitTime = System.currentTimeMillis()
            return
        }
        super.onBackPressed()
    }

    /** 创建普通通知 */
    private fun createNotificationForNormal() {
        scopeNetLife {
            //标志位，用于判断是否再次发起请求
            var flag = false
            //段子内容数据
            var data = JokeContentModel()
            do {
                //发起请求，获取首页推送视频数据
                val originalData = Post<List<JokeContentModel>>(NotificationVideoAPI).await()
                //取第一条数据(后面解析的视频地址大概率会失效，只推送第一条视频)
                originalData[0].run {
                    //判断视频地址是否有效(以指定结尾无效)，无效再次发起请求，直至有效
                    if (!this.joke.videoUrl.decrypt().endsWith("?flag=null")) {
                        flag = true
                    }
                    if (flag) {
                        //视频地址有效，设置段子内容数据
                        data = this
                    }
                }
            } while (!flag)
            // 适配8.0及以上 创建通知通道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = getString(R.string.describe)
                    setShowBadge(false) // 是否在桌面显示角标
                }
                notificationManager = this@MainActivity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
            // 点击意图 // setDeleteIntent 移除意图
            val intent = Intent(AppApplication.context, NotificationVideoActivity::class.java)
            //构建推送数据模型
            data.run {
                val model = NotificationVideoModel(
                    info.commentNum,
                    info.isAttention,
                    info.isLike,
                    info.likeNum,
                    info.shareNum,
                    joke.content,
                    joke.jokesId,
                    joke.videoSize,
                    joke.videoTime,
                    joke.videoUrl,
                    user.avatar,
                    user.nickName,
                    user.userId
                )
                //传递数据模型
                intent.putExtra("model", model)
            }
            //跳转操作意图
            val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, intent, flagBit)
            // 构建配置
            notification = NotificationCompat.Builder(this@MainActivity, channelId).apply {
                setContentTitle(getString(R.string.app_notification_title)) // 标题
                setContentText(data.joke.content) // 文本
                setSmallIcon(R.mipmap.ic_launcher) // 小图标
                setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)) // 大图标
                priority = NotificationCompat.PRIORITY_DEFAULT // 7.0 设置优先级
                setContentIntent(pendingIntent) // 跳转配置
                setAutoCancel(true) // 是否自动消失（点击）or mManager.cancel(mNormalNotificationId)、cancelAll、setTimeoutAfter()
            }.build()
            // 发起通知
            notificationManager.notify(notificationId, notification)
        }.catch {
            //请求出错，吐司错误信息
            Toaster.show(it.message)
        }
    }
}