package com.example.smile.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieDrawable
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig
import com.example.smile.app.AppConfig.getLottieAnimationList
import com.example.smile.app.AppConfig.getLottieDrawable
import com.example.smile.util.vibration
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ktx.immersionBar

class MainActivity : AppActivity() {

    private val toolbar: MaterialToolbar by lazy { findViewById(R.id.toolbar) }
    private val bottomNavigationView: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation_view) }
    private val navHostFragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }

    companion object {
        /** 菜单编号(和底部导航图中fixFragment的id保持一致) */
        private val menuItemIdList = arrayListOf(R.id.home_page, R.id.evenly_page, R.id.release_page, R.id.information_page, R.id.profile_page)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //使标题栏和状态栏不重叠
        immersionBar {
            titleBar(toolbar)
        }
        //使用toolBar并使其外观与功能和actionBar一致
        setSupportActionBar(toolbar)
        //初始化底部导航栏
        initBottomNavigationView()
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
    private fun Menu.setLottieDrawable(lottieAnimationList: ArrayList<AppConfig.LottieAnimation>) {
        for (i in 0 until bottomNavigationView.menu.size()) {
            //遍历底部导航栏菜单列表，将每项图标替换成LottieDrawable
            findItem(menuItemIdList[i]).icon = getLottieDrawable(lottieAnimationList[i], bottomNavigationView)
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
        //处理长按 MenuItem 提示 TooltipText
        bottomNavigationView.menu.forEach {
            val menuItemView = findViewById<BottomNavigationItemView>(it.itemId)
            menuItemView.setOnLongClickListener {
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
}