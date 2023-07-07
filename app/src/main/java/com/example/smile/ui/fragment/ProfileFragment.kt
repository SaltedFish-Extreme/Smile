package com.example.smile.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppConfig
import com.example.smile.app.AppFragment
import com.example.smile.http.NetApi
import com.example.smile.model.UserInfoModel
import com.example.smile.ui.activity.AnnouncementActivity
import com.example.smile.ui.activity.LoginActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.settingbar.SettingBar
import com.example.smile.widget.view.DrawableTextView
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster

/** 个人页 */
class ProfileFragment : AppFragment() {

    private val topBar: ConstraintLayout by lazy { requireView().findViewById(R.id.top_bar) }
    private val userAvatar: ShapeableImageView by lazy { requireView().findViewById(R.id.user_avatar) }
    private val userNickname: TextView by lazy { requireView().findViewById(R.id.user_nickname) }
    private val userSignature: TextView by lazy { requireView().findViewById(R.id.user_signature) }
    private val followed: TextView by lazy { requireView().findViewById(R.id.followed) }
    private val follower: TextView by lazy { requireView().findViewById(R.id.follower) }
    private val like: TextView by lazy { requireView().findViewById(R.id.like) }
    private val experience: TextView by lazy { requireView().findViewById(R.id.experience) }
    private val post: DrawableTextView by lazy { requireView().findViewById(R.id.post) }
    private val comment: DrawableTextView by lazy { requireView().findViewById(R.id.comment) }
    private val thumbsUp: DrawableTextView by lazy { requireView().findViewById(R.id.thumbs_up) }
    private val collect: DrawableTextView by lazy { requireView().findViewById(R.id.collect) }
    private val communityConvention: SettingBar by lazy { requireView().findViewById(R.id.community_convention) }
    private val customerService: SettingBar by lazy { requireView().findViewById(R.id.customer_service) }
    private val underReview: SettingBar by lazy { requireView().findViewById(R.id.under_review) }
    private val auditFailure: SettingBar by lazy { requireView().findViewById(R.id.audit_failure) }
    private val shareFriends: SettingBar by lazy { requireView().findViewById(R.id.share_friends) }
    private val feedback: SettingBar by lazy { requireView().findViewById(R.id.feedback) }
    private val praise: SettingBar by lazy { requireView().findViewById(R.id.praise) }
    private val setup: SettingBar by lazy { requireView().findViewById(R.id.setup) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //使标题栏和状态栏不重叠
        immersionBar {
            titleBar(topBar)
        }
        if (AppConfig.token.isNotBlank()) {
            scopeNetLife {
                //获取用户信息数据
                val userInfoData = Post<UserInfoModel>(NetApi.UserInfoAPI).await()
                //Glide显示头像
                Glide.with(requireContext()).load(userInfoData.user.avatar).placeholder(R.drawable.ic_account)
                    .transition(DrawableTransitionOptions.withCrossFade(100)).into(userAvatar)
                //显示昵称
                userNickname.text = userInfoData.user.nickname
                //显示签名
                userSignature.text = userInfoData.user.signature
                //显示关注
                followed.text = userInfoData.info.attentionNum.toString()
                //显示粉丝
                follower.text = userInfoData.info.fansNum.toString()
                //显示喜欢
                like.text = userInfoData.info.likeNum.toString()
                //显示经验
                experience.text = userInfoData.info.experienceNum.toString()
            }
        }
        onClick()
    }

    /** 点击事件方法 */
    private fun onClick() {
        topBar.clickNoRepeat {
            openActivity<LoginActivity>()
        }
        communityConvention.clickNoRepeat {
            //跳转公告页，传递标题：开口笑社区自律公约
            openActivity<AnnouncementActivity>("title" to getString(R.string.community_convention_title))
        }
        customerService.clickNoRepeat {
            try {
                //QQ跳转到临时会话界面，如果qq号已经是好友了，直接聊天
                val url = getString(R.string.customer_service_link, getString(R.string.customer_service_number)) //uin是发送过去的qq号码
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show("请检查是否已经安装QQ")
            }
        }
    }
}