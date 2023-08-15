package com.example.smile.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieDrawable
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.util.LottieAnimationUtil
import com.example.smile.util.LottieAnimationUtil.getLottieAnimationList
import com.example.smile.util.LottieAnimationUtil.getLottieDrawable
import com.example.smile.util.vibration
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化底部导航栏
        initBottomNavigationView()
        fab.setOnClickListener {
            //震动一下
            vibration()
            openActivity<ReleaseActivity>()
        }
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
        //不启用第二个菜单项功能，用悬浮按钮代替
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
}