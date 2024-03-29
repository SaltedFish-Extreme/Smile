package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.drake.serialize.intent.openActivity
import com.drake.serialize.intent.withArguments
import com.example.smile.R
import com.example.smile.app.AppFragment
import com.example.smile.ui.activity.SearchActivity
import com.example.smile.widget.ext.bindViewPager2
import com.example.smile.widget.ext.init
import com.gyf.immersionbar.ktx.immersionBar
import net.lucode.hackware.magicindicator.MagicIndicator

/** 首页 */
class HomeFragment : AppFragment() {

    private val viewpagerToolbar: Toolbar by lazy { requireView().findViewById(R.id.viewpager_toolbar) }
    private val magicIndicator: MagicIndicator by lazy { requireView().findViewById(R.id.magic_indicator) }
    private val viewpager: ViewPager2 by lazy { requireView().findViewById(R.id.viewpager) }

    companion object {
        /** fragment集合 */
        private val fragments: ArrayList<Fragment> by lazy { arrayListOf() }

        /** 分类集合 */
        private val classifyList: ArrayList<String> by lazy { arrayListOf("关注", "推荐", "纯文", "趣图") }
    }

    init {
        //切换夜间模式会保留之前添加过的片段 防止出现问题 先清空集合
        fragments.clear()
        //将子fragment添加进集合(关注、推荐、纯文、趣图)
        fragments.add(HomeFollowFragment())
        //传递参数，根据type区分页面
        fragments.add(HomeChildFragment().withArguments("type" to 1))
        fragments.add(HomeChildFragment().withArguments("type" to 2))
        fragments.add(HomeChildFragment().withArguments("type" to 3))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //初始化viewpager2(不允许滑动)
        viewpager.init(this, fragments, false)
        //初始化MagicIndicator
        magicIndicator.bindViewPager2(viewpager, classifyList) {
            //如果是第一个子项(关注)，隐藏toolbar右侧菜单，否则显示
            viewpagerToolbar.menu.findItem(R.id.search).apply {
                isVisible = it != 0
                //设置菜单item点击事件，跳转搜索页面
                setOnMenuItemClickListener {
                    openActivity<SearchActivity>()
                    true
                }
            }
        }
        //缓存所有fragment，不会销毁重建
        viewpager.offscreenPageLimit = fragments.size
        //设置viewpager默认选中页面
        viewpager.currentItem = 1
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(viewpagerToolbar) }
    }
}