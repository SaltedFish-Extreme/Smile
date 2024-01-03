package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import ando.widget.option.list.MODE_CHECK_SINGLE
import ando.widget.option.list.OptConfig
import ando.widget.option.list.OptSetting
import ando.widget.option.list.OptionItem
import ando.widget.option.list.OptionView
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.UserPersonalInformationModel
import com.example.smile.http.NetApi.BindInvitationCodeAPI
import com.example.smile.http.NetApi.UpdateUserInfoAPI
import com.example.smile.http.NetApi.UserInfoAPI
import com.example.smile.model.EmptyModel
import com.example.smile.model.UserInfoModel
import com.example.smile.ui.dialog.CustomDateTimePickerDialog
import com.example.smile.util.albumUploadPicture
import com.example.smile.util.copyText
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.settingbar.SettingBar
import com.google.android.material.imageview.ShapeableImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.toast.Toaster
import java.util.Date

/** 用户信息页 */
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
        //显示数据
        showData()
        //点击事件
        click()
    }

    /** 显示页面数据 */
    private fun showData() {
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

    /** 点击条目操作 */
    private fun click() {
        userAvatar.clickNoRepeat {
            //打开相册上传头像图片
            albumUploadPicture {
                //更新头像信息
                updateUserInfo("0", it[0].path)
            }
        }
        userNickname.clickNoRepeat { showEditTextDialog(getString(R.string.edit_dialog_title_nickname)) }
        userSign.clickNoRepeat { showEditTextDialog(getString(R.string.edit_dialog_title_signature)) }
        userGender.clickNoRepeat { showGenderOptionDialog() }
        userBirthday.clickNoRepeat { showDatePickerDialog() }
        myInvitationCode.clickNoRepeat { copyText(myInvitationCode.getRightText().toString()) }
        bindInvitationCode.clickNoRepeat {
            if (bindInvitationCode.getRightText() == getString(R.string.not_bind)) {
                //未绑定邀请码则显示绑定邀请码弹窗
                showConfirmDialog()
            } else {
                //否则吐司提示已绑定
                Toaster.show(getString(R.string.bound))
            }
        }
    }

    /** 显示输入栏弹窗 */
    private fun showEditTextDialog(title: String) {
        DialogManager.with(this, R.style.EditTextDialogStyle)
            .useDialog()
            .setContentView(R.layout.layout_dialog_edittext) { v: View ->
                val edtDialog: EditText = v.findViewById(R.id.edt_dialog)
                val btCancel: Button = v.findViewById(R.id.bt_dialog_cancel)
                val btConfirm: Button = v.findViewById(R.id.bt_dialog_confirm)
                btCancel.setOnClickListener { DialogManager.dismiss() }
                btConfirm.setOnClickListener {
                    if (edtDialog.text.toString().isBlank()) {
                        Toaster.show(getString(R.string.edit_dialog_update_hint))
                        return@setOnClickListener
                    }
                    when (title) {
                        getString(R.string.edit_dialog_title_nickname) -> {
                            //更新昵称信息
                            updateUserInfo("1", edtDialog.text.toString())
                        }

                        getString(R.string.edit_dialog_title_signature) -> {
                            //更新签名信息
                            updateUserInfo("2", edtDialog.text.toString())
                        }

                        else -> {
                            //绑定邀请码
                            updateUserInfo("5", edtDialog.text.toString())
                        }
                    }
                }
            }
            .setTitle(title)
            .setCanceledOnTouchOutside(false)
            .setOnShowListener {
                val edtDialog: EditText? = DialogManager.contentView?.findViewById(R.id.edt_dialog)
                edtDialog?.isFocusable = true
                edtDialog?.requestFocus()
                edtDialog?.requestFocusFromTouch()
            }
            .show()
    }

    /** 显示性别选择器弹窗 */
    private fun showGenderOptionDialog() {
        DialogManager.with(this, R.style.EditTextDialogStyle)
            .useDialog()
            .setContentView(R.layout.layout_dialog_option_view) { v: View ->
                val optionView: OptionView = v.findViewById(R.id.optionView)
                optionView.obtain(
                    OptConfig(
                        //横向列表
                        itemLayoutResource = R.layout.item_option_horizontal_list,
                        //单选模式
                        setting = OptSetting(checkMode = MODE_CHECK_SINGLE),
                    ),
                    //弹窗列表数据源
                    data = listOf(
                        OptionItem(0, "女", ContextCompat.getDrawable(this, R.drawable.ic_female), false),
                        OptionItem(1, "男", ContextCompat.getDrawable(this, R.drawable.ic_male), false),
                    ), onItemViewCallBack = null,
                    onItemClickListener = object : OptionView.OnItemClickListener {
                        override fun onItemSelected(item: OptionItem) {
                            super.onItemSelected(item)
                            //更新性别信息
                            updateUserInfo("3", "${item.id}")
                        }
                    }
                )
            }
            .setTitle(getString(R.string.select_gender))
            .setCanceledOnTouchOutside(true)
            .show()
    }

    /** 显示日期选择器弹窗 */
    private fun showDatePickerDialog() {
        //日期/时间选择器(DateTimePicker)
        val dateTimeDialog = CustomDateTimePickerDialog()
        //支持三种类型: Y_M_D , Y_M_D_H_M , H_M
        dateTimeDialog.setType(CustomDateTimePickerDialog.Y_M_D)
        dateTimeDialog.setCallBack(object : CustomDateTimePickerDialog.CallBack {
            override fun onClick(originalTime: Date, dateTime: String) {
                //更新生日信息
                updateUserInfo("4", dateTime)
            }

            override fun onDateChanged(v: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {}

            override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {}
        })
        dateTimeDialog.show(this)
    }

    /**
     * 更新用户信息
     *
     * @param type 更新类型
     * @param content 更新内容
     */
    private fun updateUserInfo(type: String, content: String) {
        scopeDialog {
            if (type.toInt() < 5) {
                //更新用户信息数据
                Post<EmptyModel?>(UpdateUserInfoAPI) {
                    param("type", type)
                    param("content", content)
                }.await()
            } else {
                //绑定邀请码
                Post<EmptyModel?>(BindInvitationCodeAPI) {
                    param("code", content)
                }.await()
            }
            Toaster.show(R.string.save_succeed)
            scopeNetLife {
                //获取用户信息数据
                val userInfoData = Post<UserInfoModel>(UserInfoAPI).await()
                //保存用户个人信息数据
                UserPersonalInformationModel = userInfoData.user
                //重新展示页面数据
                showData()
                //关闭页面上的弹窗
                DialogManager.dismiss()
            }.catch {
                //获取数据出错，吐司错误信息
                Toaster.show(it.message)
            }
        }.catch {
            //请求失败，吐司错误信息
            Toaster.show(it.message)
        }
    }

    /** 显示确认弹窗 */
    private fun showConfirmDialog() {
        DialogManager.with(this, R.style.TransparentBgDialog)
            .useDialog()
            .setContentView(R.layout.layout_dialog_confirm) { v: View ->
                val tv: TextView = v.findViewById(R.id.dialog_tv)
                val cancel: TextView = v.findViewById(R.id.dialog_cancel)
                val sure: TextView = v.findViewById(R.id.dialog_sure)
                tv.text = getString(R.string.bind_hint)
                cancel.setOnClickListener { DialogManager.dismiss() }
                sure.setOnClickListener {
                    showEditTextDialog(getString(R.string.bind_invitation_code))
                }
            }
            .setCanceledOnTouchOutside(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }
}