package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scope
import com.example.smile.R
import com.example.smile.app.AppFragment
import com.example.smile.http.NetApi
import com.example.smile.model.JokeCommentModel
import com.example.smile.ui.adapter.JokeCommentAdapter
import com.example.smile.widget.ext.screenHeight
import com.hjq.toast.Toaster

/** 段子详情评论片段 */
class JokeDetailCommentFragment(private val jokeId: String) : AppFragment() {

    private val page: PageRefreshLayout by lazy { requireView().findViewById(R.id.page) }
    private val rv: RecyclerView by lazy { requireView().findViewById(R.id.rv) }

    /** 是否初次切换页面 */
    private var first = true

    /** 适配器 */
    private val adapter: JokeCommentAdapter by lazy { JokeCommentAdapter(this) }

    /** 数据集 */
    private lateinit var data: JokeCommentModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_rv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置rv列表最小高度，防止数据过少时列表显示不全的问题
        rv.minimumHeight = requireContext().screenHeight / 2
        //禁用page下拉刷新手势操作
        page.setEnableRefresh(false)
    }

    override fun onResume() {
        super.onResume()
        //第一次切换
        if (first) {
            //设置RecycleView的Adapter
            rv.adapter = adapter
            //刷新数据
            onRefresh()
        }
    }

    /** 页面刷新加载操作，不设置onLoadMore则都会走onRefresh */
    private fun onRefresh() {
        page.onRefresh {
            scope {
                //获取段子评论列表数据
                data = Post<JokeCommentModel>(NetApi.JokeCommentListAPI) {
                    param("jokeId", jokeId)
                    param("page", index)
                }.await()
                if (first && data.count == 0) {
                    //如果初次加载数据，并且评论数量为0则显示空缺省页
                    showEmpty()
                } else {
                    //设置初次加载数据为否
                    first = false
                    index += if (index == 1) { //下拉刷新
                        //设置数据
                        adapter.submitList(data.comments)
                        //翻页
                        1
                    } else { //上拉加载更多
                        if (data.comments.isEmpty()) {
                            //没有更多数据，结束动画，显示内容(没有更多数据)
                            showContent(false)
                            return@scope
                        }
                        //添加数据
                        adapter.addAll(data.comments)
                        //翻页
                        1
                    }
                    showContent(true)
                }
            }.catch {
                //请求出错，吐司错误信息
                Toaster.show(it.message)
            }
        }.refreshing()
    }
}