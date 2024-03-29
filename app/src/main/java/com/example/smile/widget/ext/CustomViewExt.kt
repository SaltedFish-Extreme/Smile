package com.example.smile.widget.ext

import ando.dialog.core.DialogManager
import ando.dialog.core.DialogManager.dismiss
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.core.view.get
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.smile.R
import com.example.smile.widget.viewpager.ScaleTransitionPagerTitleView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.util.Random

/**
 * Created by 咸鱼至尊 on 2021/12/10
 *
 * desc: 自定义View扩展函数
 */

/** 获取屏幕宽度扩展函数 */
val Context.screenWidth get() = resources.displayMetrics.widthPixels

/** 获取屏幕高度扩展函数 */
val Context.screenHeight get() = resources.displayMetrics.heightPixels

/** 获取随机rgb颜色值函数 */
fun randomColor(): Int {
    Random().run {
        //0-255, 如果颜色值过大,就越接近白色,就看不清了,所以需要限定范围
        val red = nextInt(255)
        val green = nextInt(255)
        val blue = nextInt(255)
        //使用rgb混合生成一种新的颜色,Color.rgb生成的是一个int数
        return Color.rgb(red, green, blue)
    }
}

/**
 * 计算颜色函数
 *
 * @param color color值
 * @param alpha alpha值
 * @return 最终的状态栏颜色
 */
fun alphaColor(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int): Int {
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}

/**
 * 计算颜色函数
 *
 * @param color color值
 * @param alpha alpha值[0-1]
 * @return 最终的状态栏颜色
 */
fun alphaColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
    return alphaColor(color, (alpha * 255).toInt())
}

/**
 * 字符串扩展函数
 *
 * desc: Html字符串转Spanned可视化文本
 *
 * @param flag 转换方式，可空
 * @return Spanned可视化文本
 */
fun String.html2Spanned(flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return Html.fromHtml(this, flag)
}

/**
 * 字符串扩展函数
 *
 * desc: Html字符串转String字符串
 *
 * @param flag 转换方式，可空
 * @return String字符串
 */
fun String.html2String(flag: Int = Html.FROM_HTML_MODE_LEGACY): String {
    return Html.fromHtml(this, flag).toString()
}

/**
 * 视图填充扩展函数
 *
 * @param layoutId 布局文件
 * @param attachToRoot 是否附加到根视图
 * @return 当前视图
 */
fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = true): View {
    if (layoutId == -1) {
        return this
    }
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

/**
 * 初始化ViewPager2扩展函数(Fragment使用)
 *
 * @param fragment 当前fragment
 * @param fragments fragment集合
 * @param isUserInputEnabled 是否允许滑动
 * @return ViewPager2
 */
fun ViewPager2.init(fragment: Fragment, fragments: ArrayList<Fragment>, isUserInputEnabled: Boolean = true): ViewPager2 {
    //是否可滑动
    this.isUserInputEnabled = isUserInputEnabled
    //设置适配器
    adapter = object : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
    }
    return this
}

/**
 * 初始化ViewPager2扩展函数(Activity使用)
 *
 * @param activity 当前activity
 * @param fragments fragment集合
 * @param isUserInputEnabled 是否允许滑动
 * @return ViewPager2
 */
fun ViewPager2.init(activity: FragmentActivity, fragments: ArrayList<Fragment>, isUserInputEnabled: Boolean = true): ViewPager2 {
    //是否可滑动
    this.isUserInputEnabled = isUserInputEnabled
    //设置适配器
    adapter = object : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
    }
    //fix：ViewPager2高度无法跟随fragment自适应问题
    viewTreeObserver.addOnGlobalLayoutListener {
        updatePagerHeightForChild(fragments[this.currentItem].view, this@init)
    }
    return this
}

/**
 * ViewPager指示器扩展函数
 *
 * desc: 简化bindViewPager操作
 *
 * @param viewPager ViewPager2对象
 * @param mStringList 用作标题的字符串集合
 * @param action 子项行为，可空
 */
fun MagicIndicator.bindViewPager2(
    viewPager: ViewPager2,
    mStringList: List<String> = arrayListOf(),
    action: (index: Int) -> Unit = {}
) {
    val commonNavigator = CommonNavigator(context)
    commonNavigator.adapter = object : CommonNavigatorAdapter() {

        override fun getCount() = mStringList.size

        override fun getTitleView(context: Context, index: Int): IPagerTitleView {
            return ScaleTransitionPagerTitleView(context).apply {
                //设置文本
                text = mStringList[index].html2Spanned()
                //字体大小
                textSize = 20f
                //选中颜色
                selectedColor = resources.getColor(R.color.color_viewpager_selected, null)
                //未选中颜色
                normalColor = resources.getColor(R.color.color_viewpager_unselected, null)
                //点击事件
                clickNoRepeat {
                    viewPager.currentItem = index
                    action.invoke(index)
                }
            }
        }

        override fun getIndicator(context: Context): IPagerIndicator {
            return LinePagerIndicator(context).apply {
                mode = LinePagerIndicator.MODE_EXACTLY
                //线条的宽高度
                lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                lineWidth = UIUtil.dip2px(context, 30.0).toFloat()
                //线条的圆角
                roundRadius = UIUtil.dip2px(context, 6.0).toFloat()
                startInterpolator = AccelerateInterpolator()
                endInterpolator = DecelerateInterpolator(2.0f)
                //线条的颜色
                setColors(resources.getColor(R.color.color_viewpager_selected, null))
            }
        }
    }
    navigator = commonNavigator

    viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            this@bindViewPager2.onPageSelected(position)
            action.invoke(position)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            this@bindViewPager2.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            this@bindViewPager2.onPageScrollStateChanged(state)
        }
    })
}

/** 隐藏软键盘函数 */
fun hideSoftKeyboard(activity: Activity?) {
    activity?.let { act ->
        val view = act.currentFocus
        view?.let {
            val inputMethodManager = act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

/** 拦截底部导航栏长按弹出吐司扩展函数 */
fun BottomNavigationView.interceptLongClick() {
    val bottomNavigationMenuView = getChildAt(0) as BottomNavigationMenuView
    val size = bottomNavigationMenuView.childCount
    for (index in 0 until size) {
        bottomNavigationMenuView[index].setOnLongClickListener { true }
    }
}

/**
 * 初始化悬浮按钮扩展函数(RecyclerView)
 *
 * desc: 自动隐藏显示悬浮按钮及点击回到RecyclerView的顶部
 *
 * @param floatBtn 悬浮按钮实例
 */
fun RecyclerView.initFloatBtn(floatBtn: FloatingActionButton) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0 && floatBtn.visibility == View.VISIBLE) {
                //rv向下滚动时隐藏
                floatBtn.hide()
            } else if (dy < 0 && floatBtn.visibility != View.VISIBLE) {
                //rv向上滚动时显示
                floatBtn.show()
            }
            //rv滚动到顶部的时候，需要把向上返回顶部的按钮隐藏
            if (!canScrollVertically(-1)) {
                floatBtn.hide()
            }
        }
    })
    floatBtn.clickNoRepeat {
        val layoutManager = layoutManager as LinearLayoutManager
        //如果当前rv的最后一个视图位置的索引大于等于20，则迅速返回顶部，否则带有滚动动画效果返回到顶部
        if (layoutManager.findLastVisibleItemPosition() >= 20) {
            scrollToPosition(0)//没有动画迅速返回到顶部(马上)
        } else {
            smoothScrollToPosition(0)//有滚动动画返回到顶部(有点慢)
        }
    }
}

/**
 * 取消悬浮按钮扩展函数(RecyclerView)
 *
 * desc: 停止滚动|移除滚动监听器|隐藏悬浮按钮|清除点击事件
 *
 * @param floatBtn 悬浮按钮实例
 */
fun RecyclerView.cancelFloatBtn(floatBtn: FloatingActionButton) {
    //rv停止滚动
    stopScroll()
    //删除rv滚动监听器
    clearOnScrollListeners()
    //隐藏悬浮按钮
    floatBtn.hide()
    //清除悬浮按钮点击事件
    floatBtn.setOnClickListener(null)
}

/**
 * 初始化悬浮按钮扩展函数(NestedScrollView)
 *
 * desc: 自动隐藏显示及点击回到NestedScrollView的顶部
 *
 * @param floatBtn 悬浮按钮实例
 */
fun NestedScrollView.initFloatBtn(floatBtn: FloatingActionButton) {
    //监听NestedScrollView滑动到顶部的时候，需要把向上返回顶部的按钮隐藏
    setOnScrollChangeListener { _, _, scrollY, _, _ ->
        if (scrollY <= 100) {
            floatBtn.visibility = View.INVISIBLE
        }
    }
    floatBtn.clickNoRepeat {
        //将NestedScrollView平滑滚动到顶部500毫秒
        smoothScrollTo(0, 0, 500)
    }
}

/**
 * 取消悬浮按钮扩展函数(NestedScrollView)
 *
 * desc: 停止滚动|隐藏悬浮按钮|清除点击事件
 *
 * @param floatBtn 悬浮按钮实例
 */
fun NestedScrollView.cancelFloatBtn(floatBtn: FloatingActionButton) {
    //nv停止滚动
    stopNestedScroll()
    //隐藏悬浮按钮
    floatBtn.hide()
    //清除悬浮按钮点击事件
    floatBtn.setOnClickListener(null)
}

/** TextView点击标题右侧图标关闭弹窗 */
@SuppressLint("ClickableViewAccessibility")
fun TextView.pressRightClose() {
    //按下标题右侧图标关闭弹窗
    setOnTouchListener(View.OnTouchListener { _, event ->
        //getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
        //如果右边没有图片，不再处理
        val drawable: Drawable = compoundDrawables[2] ?: return@OnTouchListener false
        //如果不是按下事件，不再处理
        if (event.action != MotionEvent.ACTION_DOWN) return@OnTouchListener false
        //取右侧drawable位置
        if (event.x > (width - (drawable.intrinsicWidth / 3 * 2)) && event.x < width - (drawable.intrinsicWidth / 3) &&
            event.y > height / 4 && event.y < height / 3 * 2
        ) {
            dismiss()
        }
        true
    })
}

/**
 * 显示自定义确认弹窗
 *
 * @param text 弹窗文本
 * @param invoke 确认选项执行方法
 */
fun Context.showConfirmDialog(text: String, invoke: () -> Unit) {
    DialogManager.with(this, R.style.TransparentBgDialog)
        .useDialog()
        .setContentView(R.layout.layout_dialog_confirm) { v: View ->
            val tv: TextView = v.findViewById(R.id.dialog_tv)
            val cancel: TextView = v.findViewById(R.id.dialog_cancel)
            val sure: TextView = v.findViewById(R.id.dialog_sure)
            tv.text = text
            cancel.clickNoRepeat { dismiss() }
            sure.clickNoRepeat {
                invoke()
            }
        }
        .setCanceledOnTouchOutside(false)
        .show()
}

/**
 * 计算fragment的高度并设置给viewPager
 *
 * @param view fragment对应的视图
 * @param pager ViewPager对象
 */
private fun updatePagerHeightForChild(view: View?, pager: ViewPager2) {
    view?.post {
        val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
        val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(wMeasureSpec, hMeasureSpec)
        if (pager.layoutParams.height != view.measuredHeight) {
            pager.layoutParams = pager.layoutParams.also { lp ->
                lp.height = view.measuredHeight
            }
        }
    }
}

/**
 * 加载动画
 *
 * @param anim 要加载的动画效果资源
 */
fun Context.loadAnimation(@AnimRes anim: Int): Animation = AnimationUtils.loadAnimation(this, anim)