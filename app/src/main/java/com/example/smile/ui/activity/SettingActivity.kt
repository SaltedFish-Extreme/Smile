package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.drake.net.utils.scopeLife
import com.drake.serialize.intent.bundle
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.ActivityManager
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig
import com.example.smile.ui.dialog.TipsDialog
import com.example.smile.ui.dialog.WaitDialog
import com.example.smile.util.CacheDataUtil
import com.example.smile.util.isNotificationEnabled
import com.example.smile.util.jumpNotificationSettings
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.settingbar.SettingBar
import com.example.smile.widget.view.PressAlphaTextView
import com.example.smile.widget.view.SwitchButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

/** 设置页 */
class SettingActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val userRelated: LinearLayout by lazy { findViewById(R.id.user_related) }
    private val userInfo: SettingBar by lazy { findViewById(R.id.user_info) }
    private val accountSecurity: SettingBar by lazy { findViewById(R.id.account_security) }
    private val pushSwitch: SettingBar by lazy { findViewById(R.id.push_switch) }
    private val vibrationSwitch: SwitchButton by lazy { findViewById(R.id.vibration_switch) }
    private val mobileNetSwitch: SwitchButton by lazy { findViewById(R.id.mobile_net_switch) }
    private val clearCache: SettingBar by lazy { findViewById(R.id.clear_cache) }
    private val score: SettingBar by lazy { findViewById(R.id.score) }
    private val checkUpdate: SettingBar by lazy { findViewById(R.id.check_update) }
    private val servicesAgreement: SettingBar by lazy { findViewById(R.id.services_agreement) }
    private val privacyPolicy: SettingBar by lazy { findViewById(R.id.privacy_policy) }
    private val aboutUs: SettingBar by lazy { findViewById(R.id.about_us) }
    private val logout: TextView by lazy { findViewById(R.id.logout) }

    /** Serialize界面传递参数: loginOrNo */
    private val loginOrNo: Boolean by bundle()

    /** 等待加载框 */
    private val waitDialog by lazy { WaitDialog.Builder(this).setMessage(R.string.wait) }

    /** 完成提示框 */
    private val finishDialog by lazy { TipsDialog.Builder(this).setIcon(TipsDialog.ICON_FINISH).setMessage(R.string.exit_succeed) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        //设置标题
        titleBar.title = getString(R.string.setting)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //如果未登录，则隐藏用户相关信息
        if (!loginOrNo) {
            userRelated.gone()
            logout.gone()
        }
        //显示缓存大小
        clearCache.setRightText(CacheDataUtil.getTotalCacheSize(this))
        //点击清除缓存
        clearCache.clickNoRepeat {
            Toaster.show(R.string.clear_cache_success)
            CacheDataUtil.clearAllCache(this)
            clearCache.setRightText(CacheDataUtil.getTotalCacheSize(this))
        }
        //显示版本号
        checkUpdate.setRightText(getString(R.string.version_name, AppConfig.getVersionName()))
        //点击推送开关跳转通知设置
        pushSwitch.clickNoRepeat { jumpNotificationSettings() }
        //设置震动开关选中状态
        vibrationSwitch.setChecked(AppConfig.vibrationOrNo)
        //震动开关切换监听
        vibrationSwitch.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
                //保存震动开关切换状态
                AppConfig.vibrationOrNo = checked
                //如果选中，震动提示一下
                if (checked) vibration()
            }
        })
        //设置流量开关选中状态
        mobileNetSwitch.setChecked(AppConfig.mobileNetLoadingPicturesOrNo)
        //流量开关切换监听
        mobileNetSwitch.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
                //保存流量开关切换状态
                AppConfig.mobileNetLoadingPicturesOrNo = checked
                //如果选中，震动提示一下
                if (checked) vibration()
            }
        })
        //点击退出登录
        logout.clickNoRepeat {
            //清除token
            AppConfig.token = ""
            //清除用户ID
            AppConfig.userId = ""
            //因为要对加载中对话框进行隐藏显示操作，不使用scopeDialog作用域
            scopeLife {
                //显示加载中对话框
                waitDialog.show()
                //销毁主页
                ActivityManager.getInstance().finishActivity(MainActivity::class.java)
                //延迟1s
                delay(1000)
                //关闭加载中对话框
                waitDialog.dismiss()
                //显示成功对话框
                finishDialog.show()
                //延迟0.5s
                delay(500)
                //跳转主页
                openActivity<MainActivity>()
                //关闭页面
                finish()
            }.catch {
                //操作失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
        //跳转公告页，传递标题：用户服务协议
        servicesAgreement.clickNoRepeat {
            openActivity<AnnouncementActivity>("title" to getString(R.string.services_agreement_title))
        }
        //跳转公告页，传递标题：隐私政策
        privacyPolicy.clickNoRepeat {
            openActivity<AnnouncementActivity>("title" to getString(R.string.privacy_policy_title))
        }
        //打开关于我们弹窗片段
        aboutUs.clickNoRepeat {
            DialogManager.with(this, R.style.TransparentBgDialog).useDialogFragment().setContentView(R.layout.fragment_dialog_about).show()
                .apply {
                    //点击链接，打开浏览器跳转网页
                    contentView?.findViewById<PressAlphaTextView>(R.id.tv_link)?.clickNoRepeat {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://${(it as PressAlphaTextView).text}")))
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
        //设置是否开启通知推送
        pushSwitch.setRightText(
            if (isNotificationEnabled()) {
                R.string.turned_on
            } else {
                R.string.turned_off
            }
        )
    }
}