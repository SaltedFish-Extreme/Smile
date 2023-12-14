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
import com.drake.brv.PageRefreshLayout
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppConfig
import com.example.smile.app.AppFragment
import com.example.smile.http.NetApi.UserInfoAPI
import com.example.smile.model.UserInfoModel
import com.example.smile.ui.activity.AnnouncementActivity
import com.example.smile.ui.activity.FeedbackActivity
import com.example.smile.ui.activity.SettingActivity
import com.example.smile.util.judgeLoginOperation
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.settingbar.SettingBar
import com.example.smile.widget.view.DrawableTextView
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

/** 个人页 */
class ProfileFragment : AppFragment() {

    private val page: PageRefreshLayout by lazy { requireView().findViewById(R.id.page) }
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
        if (AppConfig.token.isNotBlank()) {
            //初次创建页面，如果存在token，说明用户已登录，请求用户信息
            getUserInfo()
        }
        //下拉页面，刷新用户信息
        page.onRefresh {
            getUserInfo()
        }
        onClick()
    }

    /** 获取用户个人信息 */
    private fun getUserInfo() {
        scopeDialog {
            //延迟0.5秒，转会儿圈
            delay(500)
            //获取用户信息数据
            val userInfoData = Post<UserInfoModel>(UserInfoAPI).await()
            //Glide显示头像
            Glide.with(requireContext()).load(userInfoData.user.avatar).placeholder(R.drawable.account)
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
            //保存用户个人信息数据
            AppConfig.UserPersonalInformationModel = userInfoData.user
        }.catch {
            //获取数据出错，吐司错误信息(用户登录状态过期或者未登录会跳转登录页面吐司自定义错误信息)
            Toaster.show(it.message)
        }.finally {
            //刷新完成
            page.finishRefresh()
        }
    }

    /** 点击事件方法 */
    private fun onClick() {
        //todo 点击顶部个人栏
        topBar.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show(userNickname.text)
            }
        }
        //todo 点击关注
        followed.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as TextView).text)
            }
        }
        //todo 点击粉丝
        follower.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as TextView).text)
            }
        }
        //todo 点击喜欢
        like.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as TextView).text)
            }
        }
        //todo 点击经验
        experience.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as TextView).text)
            }
        }
        //todo 点击帖子
        post.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as DrawableTextView).text)
            }
        }
        //todo 点击评论
        comment.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as DrawableTextView).text)
            }
        }
        //todo 点击点赞
        thumbsUp.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as DrawableTextView).text)
            }
        }
        //todo 点击收藏
        collect.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as DrawableTextView).text)
            }
        }
        //点击社区公约
        communityConvention.clickNoRepeat {
            //跳转公告页，传递标题：开口笑社区自律公约
            openActivity<AnnouncementActivity>("title" to getString(R.string.community_convention_title))
        }
        //点击我的客服
        customerService.clickNoRepeat {
            try {
                //QQ跳转到临时会话界面，如果qq号已经是好友了，直接聊天
                val url = getString(R.string.customer_service_link, getString(R.string.customer_service_number)) //uin是发送过去的qq号码
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show(R.string.qq_exist)
            }
        }
        //todo 点击审核中
        underReview.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as SettingBar).getLeftText())
            }
        }
        //todo 点击审核失败
        auditFailure.clickNoRepeat {
            requireContext().judgeLoginOperation {
                Toaster.show((it as SettingBar).getLeftText())
            }
        }
        //todo 点击分享好友
        shareFriends.clickNoRepeat {
            Toaster.show((it as SettingBar).getLeftText())
        }
        //点击意见反馈
        feedback.clickNoRepeat { openActivity<FeedbackActivity>() }
        //todo 点击赏个好评
        praise.clickNoRepeat {
            Toaster.show((it as SettingBar).getLeftText())
        }
        //点击设置
        setup.clickNoRepeat {
            //跳转设置页，传递是否登录
            openActivity<SettingActivity>("loginOrNo" to AppConfig.token.isNotBlank())
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(topBar) }
    }
}