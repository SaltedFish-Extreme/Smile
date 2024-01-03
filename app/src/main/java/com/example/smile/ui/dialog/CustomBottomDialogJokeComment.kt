package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.PageRefreshLayout
import com.drake.channel.receiveEventLive
import com.drake.channel.sendEvent
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.setWindowSoftInput
import com.drake.softinput.showSoftInput
import com.drake.statelayout.Status
import com.example.smile.R
import com.example.smile.http.NetApi.ChildCommentJokeAPI
import com.example.smile.http.NetApi.CommentJokeAPI
import com.example.smile.http.NetApi.JokeCommentListAPI
import com.example.smile.model.JokeCommentChildModel
import com.example.smile.model.JokeCommentModel
import com.example.smile.ui.adapter.JokeCommentAdapter
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.pressRightClose
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
class CustomBottomDialogJokeComment(context: Context, private val lifecycleOwner: LifecycleOwner, private val jokeId: String) :
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

    companion object {
        /** 要回复的评论ID */
        private var commentId = ""

        /** 是否是回复子评论 */
        private var isReplyChild = "false"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        rv.adapter = adapter
        //加载数据
        loadData()
        //使软键盘不遮挡输入框(监听回复输入框，使输入框悬浮在软键盘上面)
        setWindowSoftInput(
            float = llInput, transition = ll, editText = inputBox, setPadding = true
        )
        //按下标题右侧图标关闭弹窗
        commentTitle.pressRightClose()
        //接收消息事件，打开软键盘，设置输入框提示文本
        lifecycleOwner.receiveEventLive<String>(context.getString(R.string.channel_tag_input_hint_enter)) {
            inputBox.showSoftInput()
            inputBox.hint = it
        }
        //接收消息事件，设置被回复段子ID，是否回复子评论
        lifecycleOwner.receiveEventLive<String>(context.getString(R.string.channel_tag_comment_reply_info)) {
            commentId = it.substringBefore(";")
            isReplyChild = it.substringAfter(";")
        }
        //发送图标点击事件
        send.clickNoRepeat {
            if (inputBox.text.isNullOrBlank()) {
                Toaster.show(R.string.please_input_reply)
            } else {
                //输入框提示文本为默认，则说明是评论段子
                if (inputBox.hint == context.getString(R.string.comment_hint)) {
                    //发起请求，评论段子，一级评论
                    lifecycleOwner.scopeNetLife {
                        val data = Post<JokeCommentModel.Comment>(CommentJokeAPI) {
                            param("content", inputBox.text.toString())
                            param("jokeId", jokeId)
                        }.await()
                        Toaster.show(R.string.reply_success)
                        //清空输入框
                        inputBox.setText("")
                        //请求成功，添加数据，将这条回复添加到最上方
                        adapter.add(0, data)
                        //如果之前显示的是空页面，则显示内容页
                        if (page.stateLayout?.status == Status.EMPTY) {
                            page.showContent()
                        }
                    }.catch {
                        //请求失败，吐司错误信息
                        Toaster.show(it.message)
                    }
                }
                //输入框提示文本以“回复：”开头，则说明是评论段子子评论
                else if (inputBox.hint.startsWith(context.getString(R.string.reply_sub_string_before))) {
                    //发起请求，评论段子子评论
                    lifecycleOwner.scopeNetLife {
                        val data = Post<JokeCommentChildModel>(ChildCommentJokeAPI) {
                            param("commentId", commentId)
                            param("content", inputBox.text.toString())
                            param("isReplyChild", isReplyChild)
                        }.await()
                        Toaster.show(R.string.reply_success)
                        //清空输入框
                        inputBox.setText("")
                        //请求成功，发送消息事件，传递添加数据
                        sendEvent(data, context.getString(R.string.channel_tag_reply_child_comment))
                    }.catch {
                        //请求失败，吐司错误信息
                        Toaster.show(it.message)
                    }
                }
            }
        }
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