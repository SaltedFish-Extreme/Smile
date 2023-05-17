package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scope
import com.example.smile.R
import com.example.smile.http.NetApi
import com.example.smile.model.JokeContentModel
import com.example.smile.ui.adapter.JokeContentAdapter

/** 首页推荐选项卡片段 */
class HomeRecommendFragment : Fragment() {

    private val page: PageRefreshLayout by lazy { requireView().findViewById(R.id.page) }
    private val rv: RecyclerView by lazy { requireView().findViewById(R.id.rv) }

    /** 是否初次切换页面 */
    private var first = true

    /** 适配器 */
    private val adapter: JokeContentAdapter by lazy { JokeContentAdapter(this) }

    /** 数据集 */
    private lateinit var data: ArrayList<JokeContentModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_joke_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置RecycleView的Adapter
        rv.adapter = adapter
        //加载数据
        onRefresh()
    }

    /** 页面刷新加载操作，不设置onLoadMore则都会走onRefresh */
    private fun onRefresh() {
        page.onRefresh {
            scope {
                //获取首页推荐列表数据
                data = Post<ArrayList<JokeContentModel>>(NetApi.HomeRecommendAPI).await()
                if (first && data.isEmpty()) {
                    //如果数据为空显示空缺省页
                    showEmpty()
                } else {
                    //设置初次创建页面为否
                    first = false
                    index += if (index == 1) { //下拉刷新
                        //去掉视频后可能就没有数据显示了😅所以再发一次请求，获取下一次数据添加进去，这样应该就有数据了吧🤔
                        if (data.none { it.joke.type < 3 }) {
                            data.addAll(Post<ArrayList<JokeContentModel>>(NetApi.HomeRecommendAPI).await())
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
                        if (data.none { it.joke.type < 3 }) {
                            //如上同理
                            data.addAll(Post<ArrayList<JokeContentModel>>(NetApi.HomeRecommendAPI).await())
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
}