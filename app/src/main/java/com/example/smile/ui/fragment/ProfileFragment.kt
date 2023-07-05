package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppFragment
import com.example.smile.ui.activity.CommunityConventionActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.settingbar.SettingBar
import com.example.smile.widget.view.DrawableTextView
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar


/** 个人页 */
class ProfileFragment : AppFragment() {

    private val userAvatar: ShapeableImageView by lazy { requireView().findViewById(R.id.user_avatar) }
    private val userNickname: TextView by lazy { requireView().findViewById(R.id.user_nickname) }
    private val userSignature: TextView by lazy { requireView().findViewById(R.id.user_signature) }
    private val followed: TextView by lazy { requireView().findViewById(R.id.followed) }
    private val follower: TextView by lazy { requireView().findViewById(R.id.follower) }
    private val leBeans: TextView by lazy { requireView().findViewById(R.id.le_beans) }
    private val post: DrawableTextView by lazy { requireView().findViewById(R.id.post) }
    private val comment: DrawableTextView by lazy { requireView().findViewById(R.id.comment) }
    private val like: DrawableTextView by lazy { requireView().findViewById(R.id.like) }
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
            titleBar(R.id.top_bar)
        }
        onClick()
    }

    /** 点击事件方法 */
    private fun onClick() {
        communityConvention.clickNoRepeat {
            openActivity<CommunityConventionActivity>()
        }
    }
}