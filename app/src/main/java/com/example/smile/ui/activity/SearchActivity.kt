package com.example.smile.ui.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.channel.receiveEventLive
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig
import com.example.smile.http.NetApi
import com.example.smile.model.JokeContentModel
import com.example.smile.ui.adapter.JokeContentAdapter
import com.example.smile.ui.adapter.SearchAdapter
import com.example.smile.widget.ext.cancelFloatBtn
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.ext.initFloatBtn
import com.example.smile.widget.ext.visible
import com.example.smile.widget.view.ClearEditText
import com.example.smile.widget.view.PressAlphaTextView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gyf.immersionbar.ktx.immersionBar
import com.huantansheng.easyphotos.ui.widget.PressedTextView
import per.goweii.swipeback.SwipeBackAbility
import per.goweii.swipeback.SwipeBackDirection

class SearchActivity : AppActivity(), SwipeBackAbility.Direction {

    private val llSearchBlock: LinearLayout by lazy { findViewById(R.id.ll_search_block) }
    private val searchBox: ClearEditText by lazy { findViewById(R.id.search_box) }
    private val cancel: PressAlphaTextView by lazy { findViewById(R.id.cancel) }
    private val clearHistory: PressedTextView by lazy { findViewById(R.id.clear_history) }
    private val rvHistory: RecyclerView by lazy { findViewById(R.id.rv_history) }
    private val rvHot: RecyclerView by lazy { findViewById(R.id.rv_hot) }
    private val page: PageRefreshLayout by lazy { findViewById(R.id.page) }
    private val rv: RecyclerView by lazy { findViewById(R.id.rv) }
    private val fab: FloatingActionButton by lazy { findViewById(R.id.fab) }
    private val frame: FrameLayout by lazy { findViewById(R.id.frame) }

    /** 热门搜索适配器 */
    private val hotAdapter: SearchAdapter by lazy { SearchAdapter(0) }

    /** 历史搜索适配器 */
    private val historyAdapter: SearchAdapter by lazy { SearchAdapter(1) }

    /** 段子内容适配器 */
    private val adapter: JokeContentAdapter by lazy { JokeContentAdapter(activity = this) }

    /** 是否初次切换页面 */
    private var first = true

    /** 段子内容数据集 */
    private lateinit var data: ArrayList<JokeContentModel>

    /** 搜索内容 */
    private lateinit var searchContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        //使顶部和状态栏不重叠
        immersionBar {
            titleBarMarginTop(R.id.ll_search_bar)
        }
        //默认隐藏段子内容页面
        frame.gone()
        //没有存储过搜索热词就发起请求
        if (AppConfig.SearchHot.isEmpty()) {
            scopeNetLife {
                //请求搜索热词数据
                val hotData = Post<List<String>>(NetApi.HomeHotSearchAPI).await()
                //给adapter设置数据
                hotAdapter.submitList(hotData)
                //存储搜索热词数据
                AppConfig.SearchHot.addAll(hotData)
            }
        } else {
            //存储过搜索热词直接获取设置给adapter
            hotAdapter.submitList(AppConfig.SearchHot)
        }
        //设置搜索历史数据
        historyAdapter.submitList(AppConfig.SearchHistory)
        //初始化热门搜索RecyclerView
        rvHot.run {
            //使用横向线性布局
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            //禁用嵌套滚动
            isNestedScrollingEnabled = false
            //设置adapter
            adapter = hotAdapter
        }
        //初始化历史搜索RecyclerView
        rvHistory.run {
            //使用伸缩布局
            layoutManager = FlexboxLayoutManager(context)
            //禁用嵌套滚动
            isNestedScrollingEnabled = false
            //设置adapter
            adapter = historyAdapter
        }
        //清除按钮点击清空历史记录
        clearHistory.clickNoRepeat {
            if (historyAdapter.items.isNotEmpty()) {
                historyAdapter.submitList(emptyList())
                AppConfig.SearchHistory = arrayListOf()
            }
        }
        //取消按钮点击关闭当前页面
        cancel.clickNoRepeat { finish() }
        //监听输入框输入完成事件
        searchBox.addTextChangedListener {
            //文本为空
            if (searchBox.text.isNullOrBlank()) {
                searchCancel()
            } else {
                //文本不为空
                searchComplete(it.toString())
            }
        }
        //输入框监听软键盘操作
        searchBox.setOnEditorActionListener { it, actionId, _ ->
            //当点击软键盘的搜索键时搜索输入框中内容
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //文本为空
                if (searchBox.text.isNullOrBlank()) {
                    searchCancel()
                    return@setOnEditorActionListener false
                } else {
                    //文本不为空
                    searchComplete(it.text.toString())
                }
            }
            return@setOnEditorActionListener false
        }
        //设置段子内容列表适配器
        rv.adapter = adapter
        //接收消息事件，将传递的搜索内容设置到搜索框上，会自动调用监听输入完成事件，进行搜索
        receiveEventLive<String>("click_search_block") {
            searchBox.setText(it)
        }
    }

    /**
     * 搜索完成
     *
     * @param str 搜索关键词
     */
    private fun searchComplete(str: String) {
        //搜索完成重置首次标识
        first = true
        //隐藏搜索提示块页面
        llSearchBlock.gone()
        //显示段子内容页面
        frame.visible()
        //设置搜索内容
        searchContent = str
        //显示加载中页面
        page.showLoading()
        //开始加载数据
        loadData()
        //初始化rv悬浮按钮扩展函数
        rv.initFloatBtn(fab)
        //存储搜索记录
        updateKey(str)
    }

    /** 取消搜索 */
    private fun searchCancel() {
        //取消搜索重置首次标识
        first = true
        //隐藏输入法
        hideSoftKeyboard(this)
        //清除输入框焦点
        searchBox.clearFocus()
        //取消悬浮按钮
        rv.cancelFloatBtn(fab)
        //隐藏段子内容页面
        frame.gone()
        //显示搜索提示块页面
        llSearchBlock.visible()
    }

    /** 更新搜索记录 */
    private fun updateKey(keyStr: String) {
        historyAdapter.apply {
            if (items.contains(keyStr)) {
                //当搜索记录中包含该数据时 删除此记录
                remove(keyStr)
            }
            if (items.size >= 50) {
                //如果集合的size 有50个以上了，删除最后一个，应该不会有搜索这么多次的吧🤔
                remove(items.last())
            }
            //添加新数据到第一条
            add(0, keyStr)
            //重新赋值序列化对象
            AppConfig.SearchHistory = items
        }
    }

    /** 加载段子内容数据 */
    private fun loadData() {
        page.onRefresh {
            scope {
                //获取搜索段子内容列表数据
                data = Post<ArrayList<JokeContentModel>>(NetApi.HomeSearchJokeAPI) {
                    param("keyword", searchContent)
                    param("page", index)
                }.await()
                if (first && data.isEmpty()) {
                    //如果数据为空显示空缺省页
                    showEmpty()
                } else {
                    //设置初次创建页面为否
                    first = false
                    if (index == 1) { //下拉刷新
                        //去掉视频后可能就没有数据显示了😅所以再发一次请求，获取下一次数据，这样应该就有数据了吧🤔
                        if (data.none { it.joke.type < 3 }) {
                            //先页码+1再继续发起请求
                            index += 1
                            data = Post<ArrayList<JokeContentModel>>(NetApi.HomeSearchJokeAPI) {
                                param("keyword", searchContent)
                                param("page", index)
                            }.await()
                        }
                        //设置数据
                        adapter.submitList(data.filter { it.joke.type < 3 })
                        //翻页
                        index += 1
                    } else { //上拉加载更多
                        if (data.isEmpty()) {
                            //没有更多数据，结束动画，显示内容(没有更多数据)
                            showContent(false)
                            return@scope
                        }
                        if (data.none { it.joke.type < 3 }) {
                            //如上同理 先页码+1再继续发起请求
                            index += 1
                            data = Post<ArrayList<JokeContentModel>>(NetApi.HomeSearchJokeAPI) {
                                param("keyword", searchContent)
                                param("page", index)
                            }.await()
                        }
                        //添加数据
                        adapter.addAll(data.filter { it.joke.type < 3 })
                        //翻页
                        index += 1
                    }
                    showContent(true)
                }
            }
        }.refreshing()
    }

    /** 当前页禁用侧滑 */
    override fun swipeBackDirection() = SwipeBackDirection.NONE
}