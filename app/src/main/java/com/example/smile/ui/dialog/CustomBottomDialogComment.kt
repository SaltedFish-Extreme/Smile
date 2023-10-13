package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.softinput.setWindowSoftInput
import com.example.smile.R
import com.example.smile.http.NetApi.JokeCommentListAPI
import com.example.smile.model.JokeCommentModel
import com.example.smile.ui.adapter.JokeCommentAdapter
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.ScaleImageView
import com.hjq.shape.view.ShapeEditText
import com.hjq.toast.Toaster


/**
 * 自定义底部弹出对话框(段子评论)
 *
 * @param context 上下文对象
 * @property lifecycleOwner 生命周期管理器
 * @property jokeId 段子ID
 */
class CustomBottomDialogComment(context: Context, private val lifecycleOwner: LifecycleOwner, private val jokeId: String) :
    BottomDialog(context, R.style.AndoLoadingDialog) {

    private val ll: LinearLayout by lazy { findViewById(R.id.ll) }
    private val commentTitle: DrawableTextView by lazy { findViewById(R.id.comment_title) }
    private val page: PageRefreshLayout by lazy { findViewById(R.id.page) }
    private val rv: RecyclerView by lazy { findViewById(R.id.rv) }
    private val llInput: LinearLayout by lazy { findViewById(R.id.ll_input) }
    private val inputBox: ShapeEditText by lazy { findViewById(R.id.input_box) }
    private val send: ScaleImageView by lazy { findViewById(R.id.send) }

    /** 段子评论列表数据 */
    private lateinit var data: JokeCommentModel

    /** 段子评论适配器 */
    private val adapter: JokeCommentAdapter by lazy { JokeCommentAdapter(lifecycleOwner) }

    /** 是否初次加载数据 */
    private var first = true

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        rv.adapter = adapter
        //加载数据
        loadData()
        //使软键盘不遮挡输入框(监听回复输入框，使输入框悬浮在软键盘上面)
        setWindowSoftInput(
            float = llInput, transition = ll, editText = inputBox, setPadding = true
        )
        //点击标题右侧图标关闭弹窗
        commentTitle.setOnTouchListener(OnTouchListener { _, event ->
            //getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
            //如果右边没有图片，不再处理
            val drawable: Drawable = commentTitle.compoundDrawables[2] ?: return@OnTouchListener false
            //如果不是按下事件，不再处理
            if (event.action != MotionEvent.ACTION_DOWN) return@OnTouchListener false
            //取右侧drawable位置
            if (event.x > (commentTitle.width - (drawable.intrinsicWidth / 3 * 2))) {
                dismiss()
            }
            true
        })
    }

    /** 加载段子内容数据 */
    private fun loadData() {
        page.onRefresh {
            scope {
                //获取段子评论列表数据
                data = Post<JokeCommentModel>(JokeCommentListAPI) {
                    param("jokeId", jokeId)
                    param("page", index)
                }.await()
                if (first && data.count == 0) {
                    //如果初次加载数据，并且评论数量为0则显示空缺省页
                    commentTitle.text = context.getString(R.string.comment_title, 0)
                    showEmpty()
                } else {
                    //设置初次加载数据为否
                    first = false
                    index += if (index == 1) { //下拉刷新
                        //设置数据
                        adapter.submitList(data.comments)
                        commentTitle.text = context.getString(R.string.comment_title, data.count)
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

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
    }

    override fun initWindow(window: Window) {
        super.initWindow(window)
        window.setBackgroundDrawableResource(R.drawable.rectangle_ando_dialog_bottom)
        window.setWindowAnimations(R.style.AndoBottomDialogAnimation)
    }

    override fun getLayoutId(): Int = R.layout.layout_dialog_bottom_joke_comment
}