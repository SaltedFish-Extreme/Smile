package com.example.smile.ui.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.drake.serialize.intent.bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.model.NotificationVideoModel
import com.example.smile.util.decrypt
import com.example.smile.widget.view.RevealViewLikeVideo
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeImageView
import per.goweii.swipeback.SwipeBackAbility
import per.goweii.swipeback.SwipeBackDirection
import xyz.doikki.videocontroller.StandardVideoController
import xyz.doikki.videoplayer.player.VideoView

/** 首页推送视频页 */
class NotificationVideoActivity : AppActivity(), SwipeBackAbility.Direction {

    private val player: VideoView by lazy { findViewById(R.id.player) }
    private val userAvatar: ShapeableImageView by lazy { findViewById(R.id.user_avatar) }
    private val follow: ShapeImageView by lazy { findViewById(R.id.follow) }
    private val revealLike: RevealViewLikeVideo by lazy { findViewById(R.id.reveal_like) }
    private val likeNum: TextView by lazy { findViewById(R.id.like_num) }
    private val comment: ImageView by lazy { findViewById(R.id.comment) }
    private val commentNum: TextView by lazy { findViewById(R.id.comment_num) }
    private val share: ImageView by lazy { findViewById(R.id.share) }
    private val shareNum: TextView by lazy { findViewById(R.id.share_num) }
    private val videoAuthor: TextView by lazy { findViewById(R.id.video_author) }
    private val videoContent: TextView by lazy { findViewById(R.id.video_content) }

    /** Serialize界面传递参数: model */
    private val model: NotificationVideoModel by bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_video)
        //Glide显示头像
        Glide.with(this).load(model.avatar).placeholder(R.drawable.ic_account).into(userAvatar)
        //设置关注图标
        if (model.isAttention) {
            follow.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_check))
        } else {
            follow.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_plus))
        }
        //设置是否喜欢
        revealLike.isChecked = model.isLike
        //设置喜欢数量
        likeNum.text = model.likeNum.toString()
        //设置评论数量
        commentNum.text = model.commentNum.toString()
        //设置分享数量
        shareNum.text = model.shareNum.toString()
        //设置视频作者
        videoAuthor.text = getString(R.string.at, model.nickName)
        //设置视频内容
        videoContent.text = model.content
        player.setUrl(model.videoUrl.decrypt()) //设置视频地址
        val controller = StandardVideoController(this) //视频控制器
        controller.addDefaultControlComponent(model.content, false) //设置标题
        player.setVideoController(controller) //设置控制器
        player.start() //开始播放，不调用则不自动播放
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        player.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!player.onBackPressed()) {
            super.onBackPressed()
        }
    }

    /** 当前页禁用侧滑 */
    override fun swipeBackDirection() = SwipeBackDirection.NONE
}