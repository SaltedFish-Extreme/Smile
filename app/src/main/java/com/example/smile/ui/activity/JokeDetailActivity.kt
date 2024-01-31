package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.bundle
import com.drake.softinput.setWindowSoftInput
import com.drake.softinput.showSoftInput
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig
import com.example.smile.http.NetApi.JokeForIdAPI
import com.example.smile.model.JokeContentModel
import com.example.smile.ui.adapter.PhotoAdapter
import com.example.smile.ui.dialog.CustomBottomDialogJokeShare
import com.example.smile.ui.dialog.CustomBottomDialogReportContent
import com.example.smile.ui.fragment.JokeDetailCommentFragment
import com.example.smile.ui.fragment.JokeDetailLikeListFragment
import com.example.smile.util.decrypt
import com.example.smile.widget.ext.bindViewPager2
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.init
import com.example.smile.widget.ext.invisible
import com.example.smile.widget.ext.visible
import com.example.smile.widget.ext.visibleOrGone
import com.example.smile.widget.ext.visibleOrInvisible
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.RevealViewLike
import com.example.smile.widget.view.RevealViewUnlike
import com.example.smile.widget.view.ScaleImageView
import com.example.smile.widget.view.SmartTextView
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.shape.view.ShapeEditText
import com.hjq.shape.view.ShapeTextView
import com.huantansheng.easyphotos.ui.widget.PressedImageView
import net.lucode.hackware.magicindicator.MagicIndicator
import per.goweii.swipeback.SwipeBackAbility
import per.goweii.swipeback.SwipeBackDirection

/** 段子详情页 */
class JokeDetailActivity : AppActivity(), SwipeBackAbility.Direction {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val userAvatar: ShapeableImageView by lazy { findViewById(R.id.user_avatar) }
    private val userNickname: TextView by lazy { findViewById(R.id.user_nickname) }
    private val userSignature: TextView by lazy { findViewById(R.id.user_signature) }
    private val hot: ImageView by lazy { findViewById(R.id.hot) }
    private val concern: DrawableTextView by lazy { findViewById(R.id.concern) }
    private val followed: ShapeTextView by lazy { findViewById(R.id.followed) }
    private val omitted: PressedImageView by lazy { findViewById(R.id.omitted) }
    private val jokeText: SmartTextView by lazy { findViewById(R.id.joke_text) }
    private val jokePicture: RecyclerView by lazy { findViewById(R.id.joke_picture) }
    private val releaseTime: SmartTextView by lazy { findViewById(R.id.release_time) }
    private val revealLike: RevealViewLike by lazy { findViewById(R.id.reveal_like) }
    private val likeNum: TextView by lazy { findViewById(R.id.like_num) }
    private val revealUnlike: RevealViewUnlike by lazy { findViewById(R.id.reveal_unlike) }
    private val unlikeNum: TextView by lazy { findViewById(R.id.unlike_num) }
    private val revealComment: PressedImageView by lazy { findViewById(R.id.reveal_comment) }
    private val commentNum: TextView by lazy { findViewById(R.id.comment_num) }
    private val revealShare: PressedImageView by lazy { findViewById(R.id.reveal_share) }
    private val shareNum: TextView by lazy { findViewById(R.id.share_num) }
    private val magicIndicator: MagicIndicator by lazy { findViewById(R.id.magic_indicator) }
    private val viewPager: ViewPager2 by lazy { findViewById(R.id.view_pager) }
    private val llInput: LinearLayout by lazy { findViewById(R.id.ll_input) }
    private val inputBox: ShapeEditText by lazy { findViewById(R.id.input_box) }
    private val send: ScaleImageView by lazy { findViewById(R.id.send) }

    /** Serialize界面传递参数: 段子ID */
    private val jokeId: String by bundle()

    /** 数据集 */
    private lateinit var data: JokeContentModel

    /** fragment集合 */
    private val fragments: ArrayList<Fragment> by lazy { arrayListOf() }

    /** 标题集合 */
    private val titleList: ArrayList<String> by lazy { arrayListOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joke_detail)
        //设置标题
        titleBar.title = getString(R.string.post_detail)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //使软键盘不遮挡输入框(监听回复输入框，使输入框悬浮在软键盘上面)
        setWindowSoftInput(
            float = llInput, editText = inputBox, setPadding = true
        )
        scopeNetLife {
            //根据段子ID，获取段子信息数据
            data = Post<JokeContentModel>(JokeForIdAPI) { param("jokeId", jokeId) }.await()
            //显示页面数据
            showData()
            //设置片段集合
            fragments.add(JokeDetailCommentFragment(jokeId))
            fragments.add(JokeDetailLikeListFragment(jokeId))
            //设置标题集合
            titleList.add(getString(R.string.comment_num, data.info.commentNum))
            titleList.add(getString(R.string.like_num, data.info.likeNum))
            //初始化viewpager2
            viewPager.init(this@JokeDetailActivity, fragments)
            //初始化MagicIndicator
            magicIndicator.bindViewPager2(viewPager, titleList)
            //缓存所有fragment，不会销毁重建
            viewPager.offscreenPageLimit = fragments.size
        }
        //点击事件
        onClick()
    }

    /** 填充页面数据 */
    private fun showData() {
        //用户头像
        Glide.with(this@JokeDetailActivity).load(data.user.avatar).placeholder(R.drawable.account).into(userAvatar)
        //用户昵称
        userNickname.text = data.user.nickName
        //用户签名
        userSignature.text = data.user.signature
        //是否热门
        hot.visibleOrInvisible(data.joke.hot)
        //是否关注
        if (data.info.isAttention) {
            followed.visible()
            concern.invisible()
        } else {
            concern.visible()
            followed.invisible()
        }
        //段子内容(文本)
        jokeText.text = data.joke.content
        //段子内容(图片)
        if (data.joke.type == 2) {
            jokePicture.apply {
                //根据是否有图片来显示隐藏列表
                visibleOrGone(data.joke.imageUrl.split(",").isNotEmpty())
                adapter = if (isVisible) {
                    //将图片集合传递到图片适配器
                    PhotoAdapter(this@JokeDetailActivity, dataList = data.joke.imageUrl.split(",").map { it.decrypt() })
                } else {
                    //适配器设置为空
                    null
                }
            }
        } else {
            //隐藏图片列表
            jokePicture.gone()
        }
        //发布时间
        releaseTime.text = getString(R.string.release_time, data.joke.addTime)
        //是否👍
        revealLike.isChecked = data.info.isLike
        //是否👎
        revealUnlike.isChecked = data.info.isUnlike
        //👍的数量
        likeNum.text = data.info.likeNum.toString()
        //👎的数量
        unlikeNum.text = data.info.disLikeNum.toString()
        //💬的数量
        commentNum.text = data.info.commentNum.toString()
        //分享数量
        shareNum.text = data.info.shareNum.toString()
    }


    /** 点击操作 */
    private fun onClick() {
        //点击评论图标，显示软键盘，聚焦回复输入框，设置默认文本
        revealComment.clickNoRepeat {
            inputBox.showSoftInput()
            inputBox.hint = getString(R.string.comment_hint)
        }
        //点击分享段子
        revealShare.clickNoRepeat {
            //底部弹窗(BottomDialog)
            val bottomDialog = if (data.joke.type == 2) {
                //图片段子
                CustomBottomDialogJokeShare(
                    this@JokeDetailActivity,
                    this@JokeDetailActivity,
                    data.joke.jokesId.toString(),
                    1,
                    data.joke.content,
                    data.joke.imageUrl
                )
            } else {
                //文本段子
                CustomBottomDialogJokeShare(
                    this@JokeDetailActivity,
                    this@JokeDetailActivity,
                    data.joke.jokesId.toString(),
                    0,
                    data.joke.content
                )
            }
            DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
        }
        //点击举报段子
        omitted.clickNoRepeat {
            val bottomDialog = CustomBottomDialogReportContent(this@JokeDetailActivity, jokeId, AppConfig.userId, 0)
            DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }

    /** 当前页禁用侧滑 */
    override fun swipeBackDirection() = SwipeBackDirection.NONE
}