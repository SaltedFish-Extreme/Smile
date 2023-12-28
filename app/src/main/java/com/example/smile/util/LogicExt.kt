package com.example.smile.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationManagerCompat
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppConfig
import com.example.smile.ui.activity.LoginActivity
import com.example.smile.ui.adapter.UploadPictureAdapter
import com.example.smile.widget.recycler.WrapRecyclerView
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo

/**
 * 相册上传图片(九宫格)
 *
 * @param photoList 照片对象集合
 * @param adapter 数据适配器
 * @param rv rv对象
 */
fun Activity.albumUploadImage(photoList: ArrayList<Photo>, adapter: UploadPictureAdapter, rv: WrapRecyclerView) {
    GlideEngine.instance?.let {
        //参数说明：上下文，是否显示相机按钮，是否使用宽高数据（false时宽高数据为0，扫描速度更快），[配置Glide为图片加载引擎]
        EasyPhotos.createAlbum(this, true, false, it)
            //参数说明：见下方`FileProvider的配置`
            .setFileProviderAuthority("${AppConfig.getPackageName()}.provider")
            //无拼图功能
            .setPuzzleMenu(false)
            //参数说明：最大可选数，默认1
            .setCount(9)
            //参数说明:用户上一次勾选过的图片地址集合，ArrayList<String>类型;上次用户选择图片时是否选中了原图选项，如不用原图选项功能直接传false即可。
            .setSelectedPhotos(photoList, false)
            //相册回调
            .start(object : SelectCallback() {
                //photos:返回对象集合：如果你需要了解图片的宽、高、大小、用户是否选中原图选项等信息，可以用这个
                @SuppressLint("NotifyDataSetChanged")
                override fun onResult(photos: ArrayList<Photo>, isOriginal: Boolean) {
                    //将图片对象集合重新添加，刷新数据列表
                    photoList.clear()
                    photoList.addAll(photos)
                    adapter.notifyDataSetChanged()
                    //图片未全部添加完
                    if (photoList.size < 9) {
                        //列表滚动到最后面(因为有脚布局，所以不用-1，否则需要-1)
                        rv.scrollToPosition(adapter.itemCount)
                    } else {
                        //添加完所有图片后，移除脚布局
                        rv.removeAllFooterViews()
                        //列表滚动到最后面(此时没有脚布局，位置-1)
                        rv.scrollToPosition(adapter.itemCount - 1)
                    }
                }

                override fun onCancel() {}
            })
    }
}

/**
 * 复制文本内容
 *
 * @param text 要复制的字符串
 */
fun Context.copyText(text: String) {
    val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(getString(R.string.tag_copy_joke_content), text)
    clipboard.setPrimaryClip(clip)
    Toaster.show(R.string.copy_succeed)
    //顺便震动一下
    vibration()
}

/**
 * 获取外部存储器上的公共视频目录
 *
 * desc : 这将返回类似于“/storage/emulated/0/Movies”的路径
 */
fun getVideoDirectory(): String {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
}

fun Any?.notNull(notNullAction: (value: Any) -> Unit, nullAction1: () -> Unit) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction1.invoke()
    }
}

/**
 * 判断登录状态，未登录吐司提示登录，跳转登录页面，否则执行后续逻辑操作
 *
 * @param invoke 已登录状态要执行的操作
 */
fun Context.judgeLoginOperation(invoke: () -> Unit) {
    if (AppConfig.token.isBlank()) {
        Toaster.show(R.string.please_login)
        openActivity<LoginActivity>()
    } else {
        invoke()
    }
}

/** 是否允许程序使用通知 */
fun Context.isNotificationEnabled(): Boolean {
    return try {
        NotificationManagerCompat.from(this).areNotificationsEnabled()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/** 跳转通知设置 */
fun Context.jumpNotificationSettings() {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= 26) {
        // android 8.0引导
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
    } else {
        // 其他
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", packageName, null)
    }
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}