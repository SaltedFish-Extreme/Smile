package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.http.NetApi.JokeLikeOrCancelAPI
import com.example.smile.http.NetApi.UserFocusOrCancelAPI
import com.example.smile.model.EmptyModel
import com.example.smile.model.NotificationVideoModel
import com.example.smile.ui.dialog.CustomBottomDialogJokeComment
import com.example.smile.ui.dialog.CustomBottomDialogJokeShare
import com.example.smile.util.decrypt
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.invisible
import com.example.smile.widget.ext.visible
import com.example.smile.widget.view.RevealViewLikeVideo
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeImageView
import com.hjq.toast.Toaster
import per.goweii.swipeback.SwipeBackAbility
import per.goweii.swipeback.SwipeBackDirection
import xyz.doikki.videocontroller.StandardVideoController
import xyz.doikki.videoplayer.player.VideoView

/** 首页推送视频页 */
class NotificationVideoActivity : AppActivity(), SwipeBackAbility.Direction {

    private val player: VideoView by lazy { findViewById(R.id.player) }
    private val userAvatar: ShapeableImageView by lazy { findViewById(R.id.user_avatar) }
    private val follow: ShapeImageView by lazy { findViewById(R.id.follow) }
    private val followed: ShapeImageView by lazy { findViewById(R.id.followed) }
    private val revealLike: RevealViewLikeVideo by lazy { findViewById(R.id.reveal_like) }
    private val likeNum: TextView by lazy { findViewById(R.id.like_num) }
    private val comment: ImageView by lazy { findViewById(R.id.comment) }
    private val commentNum: TextView by lazy { findViewById(R.id.comment_num) }
    private val share: ImageView by lazy { findViewById(R.id.share) }
    private val shareNum: TextView by lazy { findViewById(R.id.share_num) }
    private val videoAuthor: TextView by lazy { findViewById(R.id.video_author) }
    private val videoContent: TextView by lazy { findViewById(R.id.video_content) }

    /** Serialize界面传递参数: model对象 */
    private val model: NotificationVideoModel by bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_video)
        //Glide显示头像
        Glide.with(this).load(model.avatar).placeholder(R.drawable.account).into(userAvatar)
        //设置关注图标
        if (model.isAttention) {
            followed.visible()
            follow.invisible()
        } else {
            follow.visible()
            followed.invisible()
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
        onClick()
    }

    /** 点击事件 */
    private fun onClick() {
        userAvatar.clickNoRepeat {
            Toaster.show(model.userId)
        }
        videoAuthor.clickNoRepeat {
            Toaster.show(model.nickName)
        }
        //点赞(取消点赞)
        revealLike.apply {
            setOnClickListener(object : RevealViewLikeVideo.OnClickListener {
                override fun onClick(v: RevealViewLikeVideo) {
                    //发起请求，点赞(取消点赞)
                    scopeNetLife {
                        Post<EmptyModel?>(JokeLikeOrCancelAPI) {
                            param("id", model.jokesId)
                            param("status", isChecked)
                        }.await()
                        //请求成功，点赞数+1/-1
                        "${likeNum.text.toString().toInt() + if (revealLike.isChecked) 1 else -1}".also { likeNum.text = it }
                    }.catch {
                        //请求失败，吐司错误信息，点赞操作回滚
                        Toaster.show(it.message)
                        setChecked(!isChecked, true)
                    }
                }
            })
        }
        //关注
        follow.clickNoRepeat {
            scopeNetLife {
                Post<EmptyModel?>(UserFocusOrCancelAPI) {
                    param("status", 1)
                    param("userId", model.userId)
                }.await()
                //请求成功，显示已关注
                Toaster.show(R.string.follow_success)
                followed.visible()
                follow.invisible()
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
        //取消关注
        followed.clickNoRepeat {
            scopeNetLife {
                Post<EmptyModel?>(UserFocusOrCancelAPI) {
                    param("status", 0)
                    param("userId", model.userId)
                }.await()
                //请求成功，显示关注
                Toaster.show(R.string.follow_cancel)
                follow.visible()
                followed.invisible()
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
        //查看评论
        comment.clickNoRepeat {
            //底部弹窗(BottomDialog)
            val bottomDialog = CustomBottomDialogJokeComment(this, this, model.jokesId.toString())
            DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
        }
        //分享段子
        share.clickNoRepeat {
            //底部弹窗(BottomDialog)
            //视频段子
            val bottomDialog = CustomBottomDialogJokeShare(this, this, model.jokesId.toString(), 2, model.content, model.videoUrl)
            DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
        }
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