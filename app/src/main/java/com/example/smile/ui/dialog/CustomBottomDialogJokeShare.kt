package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.example.smile.R
import com.example.smile.util.PhotoUtils
import com.example.smile.util.decrypt
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.copyJoke
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.pressRightClose
import com.example.smile.widget.view.DrawableTextView
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.io.File

/**
 * 自定义底部弹出对话框(分享段子)
 *
 * @param context 上下文对象
 * @property lifecycleOwner 生命周期管理器
 * @property jokeId 段子ID
 * @property type 段子类型(0:文本;1:图片;2:视频)
 * @property text 段子文本内容
 * @property url 段子图片/视频地址
 */
class CustomBottomDialogJokeShare(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val jokeId: String,
    private val type: Int = 0,
    private val text: String,
    private val url: String = "",
) :
    BottomDialog(context, R.style.AndoLoadingDialog) {

    private val commentTitle: DrawableTextView by lazy { findViewById(R.id.comment_title) }
    private val collect: ShapeableImageView by lazy { findViewById(R.id.collect) }
    private val copy: ShapeableImageView by lazy { findViewById(R.id.copy) }
    private val flSave: FrameLayout by lazy { findViewById(R.id.fl_save) }
    private val save: ShapeableImageView by lazy { findViewById(R.id.save) }
    private val report: ShapeableImageView by lazy { findViewById(R.id.report) }
    private val detail: ShapeableImageView by lazy { findViewById(R.id.detail) }
    private val tvSave: TextView by lazy { findViewById(R.id.tv_save) }
    private val cancel: TextView by lazy { findViewById(R.id.cancel) }

    /** 等待加载框 */
    private val waitDialog by lazy { WaitDialog.Builder(context).setMessage(R.string.wait) }

    /** 完成提示框 */
    private val finishDialog by lazy { TipsDialog.Builder(context).setIcon(TipsDialog.ICON_FINISH).setMessage(R.string.save_succeed) }

    /** 错误提示框 */
    private val errorDialog by lazy {
        TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.save_failed).setDuration(2000)
    }

    override fun initView() {
        //按下标题右侧图标关闭弹窗
        commentTitle.pressRightClose()
        //如果是文本段子，则隐藏保存选项
        if (type == 0) {
            flSave.gone()
            tvSave.gone()
        }
        //todo 收藏段子
        collect.clickNoRepeat { Toaster.show(jokeId) }
        //复制段子文本内容
        copy.clickNoRepeat { context.copyJoke(text) }
        //保存段子图片/视频
        save.clickNoRepeat {
            if (type == 1) {
                //请求权限
                XXPermissions.with(context).permission(Permission.WRITE_EXTERNAL_STORAGE).request { _, all ->
                    if (all) {
                        //保存图片，需在子线程
                        scopeNet(dispatcher = Dispatchers.IO) {
                            url.split(",").map { it.decrypt() }.forEach {
                                if (PhotoUtils.saveFile2Gallery(context, it)) {
                                    Toaster.show(R.string.save_succeed)
                                } else {
                                    Toaster.show(R.string.save_failed)
                                }
                            }
                            //保存完成后取消协程
                            cancel()
                        }
                        //顺便震动一下
                        context.vibration()
                    }
                }
            } else if (type == 2) {
                //请求权限
                XXPermissions.with(context).permission(Permission.WRITE_EXTERNAL_STORAGE).request { _, all ->
                    if (all) {
                        //显示加载中对话框
                        waitDialog.show()
                        scopeNet(dispatcher = Dispatchers.IO) {
                            val file = Post<File>(url.decrypt()) {
                                //生成的中间文件名称（标题_当前时间.mp4）
                                setDownloadFileName("${text}_${System.currentTimeMillis()}.mp4")
                                //存储路径
                                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.let { setDownloadDir(it) }
                                    ?: setDownloadDir(context.filesDir)
                                setDownloadMd5Verify()
                                setDownloadFileNameDecode()
                                setDownloadTempFile()
                            }.await()
                            Toaster.show(file.path)
                            //请求完成后关闭等待加载框
                            waitDialog.dismiss()
                            //显示完成对话框
                            finishDialog.show()
                            //下载完成后取消协程
                            cancel()
                        }.catch {
                            //请求完成后关闭等待加载框
                            waitDialog.dismiss()
                            //请求失败，吐司错误信息，显示错误对话框
                            Toaster.show(it.message)
                            errorDialog.show()
                        }
                        //顺便震动一下
                        context.vibration()
                    }
                }
            }
            //关闭弹窗
            cancel.clickNoRepeat { dismiss() }
        }
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
    }

    override fun initWindow(window: Window) {
        super.initWindow(window)
        window.setBackgroundDrawableResource(R.drawable.rectangle_ando_dialog_bottom)
        window.setWindowAnimations(R.style.AndoBottomDialogAnimation)
    }

    override fun getLayoutId(): Int = R.layout.layout_dialog_bottom_joke_share
}