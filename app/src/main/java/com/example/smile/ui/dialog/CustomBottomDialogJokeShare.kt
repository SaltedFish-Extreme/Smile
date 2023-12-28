package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.example.smile.R
import com.example.smile.http.NetApi
import com.example.smile.model.EmptyModel
import com.example.smile.model.JokeCollectStateModel
import com.example.smile.util.PhotoUtils
import com.example.smile.util.copyText
import com.example.smile.util.decrypt
import com.example.smile.util.getVideoDirectory
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.pressRightClose
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.RevealViewCollect
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
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
    private val ivCollect: RevealViewCollect by lazy { findViewById(R.id.iv_collect) }
    private val copy: ShapeableImageView by lazy { findViewById(R.id.copy) }
    private val flSave: FrameLayout by lazy { findViewById(R.id.fl_save) }
    private val save: ShapeableImageView by lazy { findViewById(R.id.save) }
    private val report: ShapeableImageView by lazy { findViewById(R.id.report) }
    private val detail: ShapeableImageView by lazy { findViewById(R.id.detail) }
    private val tvCollect: TextView by lazy { findViewById(R.id.tv_collect) }
    private val tvSave: TextView by lazy { findViewById(R.id.tv_save) }
    private val cancel: TextView by lazy { findViewById(R.id.cancel) }

    /** 等待加载框 */
    private val waitDialog by lazy { WaitDialog.Builder(context).setMessage(R.string.wait) }

    override fun initView() {
        //按下标题右侧图标关闭弹窗
        commentTitle.pressRightClose()
        //如果是文本段子，则隐藏保存选项
        if (type == 0) {
            flSave.gone()
            tvSave.gone()
        }
        //获取段子收藏状态
        scopeNet {
            val data = Post<JokeCollectStateModel>(NetApi.JokeCollectStateAPI) { param("jokeId", jokeId) }.await()
            data.isCollect.apply {
                //设置收藏控件选中状态
                ivCollect.setChecked(this, false)
                //设置是否收藏文本
                tvCollect.text = if (this) context.getString(R.string.collected) else context.getString(R.string.collect)
            }
        }.catch {
            //请求失败，吐司错误信息
            Toaster.show(it.message)
        }.finally {
            //完成后取消协程
            cancel()
        }
        //设置收藏控件选中监听
        ivCollect.setOnClickListener(object : RevealViewCollect.OnClickListener {
            override fun onClick(v: RevealViewCollect) {
                scopeNet {
                    Post<EmptyModel?>(NetApi.JokeCollectOrCancelAPI) {
                        param("jokeId", jokeId)
                        param("status", v.isChecked)
                    }.await()
                    if (v.isChecked) {
                        tvCollect.text = context.getString(R.string.collected)
                        Toaster.show(R.string.collect_success)
                    } else {
                        tvCollect.text = context.getString(R.string.collect)
                        Toaster.show(R.string.collect_cancel)
                    }
                }.catch {
                    //请求失败，吐司错误信息
                    Toaster.show(it.message)
                }.finally {
                    //完成后取消协程
                    cancel()
                }
            }
        })
        //复制段子文本内容
        copy.clickNoRepeat {
            context.copyText(text)
        }
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
                        }.finally {
                            //完成后取消协程
                            cancel()
                        }
                        //顺便震动一下
                        context.vibration()
                    }
                }
            } else if (type == 2) {
                //请求权限
                XXPermissions.with(context).permission(Permission.MANAGE_EXTERNAL_STORAGE).request { _, all ->
                    if (all) {
                        //显示加载中对话框
                        waitDialog.show()
                        //保存视频，需在子线程
                        scopeNet(dispatcher = Dispatchers.IO) {
                            Get<File>(url.decrypt()) {
                                //设置存储路径
                                setDownloadDir(getVideoDirectory())
                                //context.getExternalFilesDir(DIRECTORY_MOVIES)?.let { setDownloadDir(it) } ?: setDownloadDir(context.filesDir)
                                setDownloadMd5Verify()
                                setDownloadFileNameDecode()
                                setDownloadTempFile()
                            }.await()
                            Toaster.show(R.string.save_succeed)
                        }.catch {
                            //请求失败，吐司错误信息
                            Toaster.show(it.message)
                        }.finally {
                            //请求完成后关闭等待加载框
                            waitDialog.dismiss()
                            //完成后取消协程
                            cancel()
                        }
                        //顺便震动一下
                        context.vibration()
                    }
                }
            }
        }
        //关闭弹窗
        cancel.clickNoRepeat { dismiss() }
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