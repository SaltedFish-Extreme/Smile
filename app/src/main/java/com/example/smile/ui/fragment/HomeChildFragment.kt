package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.serialize.intent.bundle
import com.example.smile.R
import com.example.smile.app.AppFragment
import com.example.smile.http.NetApi.HomePictureAPI
import com.example.smile.http.NetApi.HomeRecommendAPI
import com.example.smile.http.NetApi.HomeTextAPI
import com.example.smile.model.JokeContentModel
import com.example.smile.ui.adapter.JokeContentAdapter
import com.example.smile.widget.ext.cancelFloatBtn
import com.example.smile.widget.ext.initFloatBtn
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** 首页子选项卡片段 */
class HomeChildFragment : AppFragment() {

    private val page: PageRefreshLayout by lazy { requireView().findViewById(R.id.page) }
    private val rv: RecyclerView by lazy { requireView().findViewById(R.id.rv) }
    private val fab: FloatingActionButton by lazy { requireView().findViewById(R.id.fab) }

    /** 页面类型，从上个页面传递 */
    private val type: Int by bundle()

    companion object {
        /** 网络请求API路径 */
        private lateinit var API: String
    }

    /** 是否初次切换页面 */
    private var first = true

    /** 适配器 */
    private val adapter: JokeContentAdapter by lazy { JokeContentAdapter(fragment = this) }

    /** 数据集 */
    private lateinit var data: ArrayList<JokeContentModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_child, container, false)
    }

    override fun onResume() {
        //设置API路径(推荐、纯文、趣图)
        when (type) {
            1 -> {
                API = HomeRecommendAPI
            }

            2 -> {
                API = HomeTextAPI
            }

            3 -> {
                API = HomePictureAPI
            }
        }
        //初始化rv悬浮按钮扩展函数
        rv.initFloatBtn(fab)
        //第一次切换
        if (first) {
            //设置RecycleView的Adapter
            rv.adapter = adapter
            //刷新数据
            onRefresh()
        }
        super.onResume()
    }

    /** 页面刷新加载操作，不设置onLoadMore则都会走onRefresh */
    private fun onRefresh() {
        page.onRefresh {
            scope {
                //获取首页子列表数据
                data = Post<ArrayList<JokeContentModel>>(API).await()
                if (first && data.isEmpty()) {
                    //如果数据为空显示空缺省页
                    showEmpty()
                } else {
                    //设置初次创建页面为否
                    first = false
                    index += if (index == 1) { //下拉刷新
                        //去掉视频后可能就没有数据显示了😅所以循环发起请求，直到有除视频之外的数据返回🤔(这里即使没有数据再次发起请求，页码也不会改变，请求完成页码+1)
                        while (data.none { it.joke.type < 3 }) {
                            data = Post<ArrayList<JokeContentModel>>(HomeRecommendAPI).await()
                        }
                        //设置数据
                        adapter.submitList(data.filter { it.joke.type < 3 })
                        //翻页
                        1
                    } else { //上拉加载更多
                        if (data.isEmpty()) {
                            //没有更多数据，结束动画，显示内容(没有更多数据)
                            showContent(false)
                            return@scope
                        }
                        while (data.none { it.joke.type < 3 }) {
                            //如上同理
                            data = Post<ArrayList<JokeContentModel>>(HomeRecommendAPI).await()
                        }
                        //添加数据
                        adapter.addAll(data.filter { it.joke.type < 3 })
                        //翻页
                        1
                    }
                    showContent(true)
                }
            }
        }.refreshing()
    }

    override fun onPause() {
        //取消悬浮按钮
        rv.cancelFloatBtn(fab)
        super.onPause()
    }
}