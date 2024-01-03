package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scope
import com.example.smile.R
import com.example.smile.app.AppConfig.token
import com.example.smile.app.AppFragment
import com.example.smile.http.NetApi.HomeRecommendFollowAPI
import com.example.smile.model.RecommendFollowModel
import com.example.smile.ui.adapter.RecommendFollowAdapter
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.view.DrawableTextView
import com.hjq.toast.Toaster
import com.scwang.smart.refresh.header.MaterialHeader

/** 首页关注选项卡片段 */
class HomeFollowFragment : AppFragment() {

    private val pageRecommendUser: PageRefreshLayout by lazy { requireView().findViewById(R.id.page_recommend_follow) }
    private val rvRecommendUser: RecyclerView by lazy { requireView().findViewById(R.id.rv_recommend_follow) }
    private val refresh: DrawableTextView by lazy { requireView().findViewById(R.id.refresh) }

    /** 是否初次切换页面 */
    private var first = true

    /** 适配器 */
    private val adapter: RecommendFollowAdapter by lazy { RecommendFollowAdapter() }

    /** 数据集 */
    private lateinit var data: ArrayList<RecommendFollowModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //填充视图(未登录状态/已登录状态)
        return if (token.isEmpty()) inflater.inflate(R.layout.fragment_home_follow_not_login, container, false)
        else inflater.inflate(R.layout.fragment_home_follow_logged_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //点击刷新按钮执行刷新操作(未登录时可用)
        if (token.isEmpty()) refresh.clickNoRepeat { pageRecommendUser.refresh() }
        //设置刷新头为标准样式
        pageRecommendUser.setRefreshHeader(MaterialHeader(context))
    }

    override fun onResume() {
        //第一次切换
        if (first) {
            //设置RecycleView的布局管理器(未登录状态不需要处理)
            if (token.isNotEmpty()) {
                rvRecommendUser.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            //设置RecycleView的Adapter
            rvRecommendUser.adapter = adapter
            //刷新数据
            onRefresh()
        }
        super.onResume()
    }

    /** 初始化刷新操作 */
    private fun onRefresh() {
        pageRecommendUser.onRefresh {
            scope {
                //获取首页推荐关注列表数据
                data = Post<ArrayList<RecommendFollowModel>>(HomeRecommendFollowAPI).await()
                if (first && data.isEmpty()) {
                    //如果数据为空显示空缺省页
                    showEmpty()
                } else {
                    //设置初次创建页面为否
                    first = false
                    //设置数据
                    adapter.submitList(data)
                    return@scope
                }
            }.catch {
                //请求出错，吐司错误信息
                Toaster.show(it.message)
            }
        }.refreshing()
    }
}