package com.example.smile.ui.activity

import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.UserPersonalInformationModel
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.settingbar.SettingBar
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar

class UserInfoActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val userAvatar: SettingBar by lazy { findViewById(R.id.user_avatar) }
    private val avatar: ShapeableImageView by lazy { findViewById(R.id.avatar) }
    private val userNickname: SettingBar by lazy { findViewById(R.id.user_nickname) }
    private val userSign: SettingBar by lazy { findViewById(R.id.user_sign) }
    private val userGender: SettingBar by lazy { findViewById(R.id.user_gender) }
    private val userBirthday: SettingBar by lazy { findViewById(R.id.user_birthday) }
    private val myInvitationCode: SettingBar by lazy { findViewById(R.id.my_invitation_code) }
    private val bindInvitationCode: SettingBar by lazy { findViewById(R.id.bind_invitation_code) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        //设置标题
        titleBar.title = getString(R.string.user_info)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //用户头像
        UserPersonalInformationModel.apply {
            //显示头像
            Glide.with(this@UserInfoActivity).load(avatar).placeholder(R.drawable.account)
                .transition(DrawableTransitionOptions.withCrossFade(100)).into(this@UserInfoActivity.avatar)
            //显示昵称
            userNickname.setRightText(nickname)
            //显示签名
            userSign.setRightText(signature)
            //显示性别
            userGender.setRightText(sex)
            //显示生日
            userBirthday.setRightText(birthday)
            //显示邀请码
            myInvitationCode.setRightText(inviteCode)
            //显示绑定邀请码
            bindInvitationCode.setRightText(invitedCode ?: getString(R.string.not_bind))
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }
}