package com.example.smile.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.drake.brv.PageRefreshLayout
import com.drake.net.NetConfig
import com.drake.net.interceptor.RequestInterceptor
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDialogFactory
import com.drake.net.okhttp.setRequestInterceptor
import com.drake.net.request.BaseRequest
import com.drake.statelayout.StateConfig
import com.example.smile.R
import com.example.smile.http.NetApi.BaseURL
import com.example.smile.http.SerializationConverter
import com.example.smile.ui.dialog.WaitDialog
import com.example.smile.util.DynamicTimeFormat
import com.google.android.material.color.DynamicColors
import com.hjq.toast.Toaster
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import com.wgw.photo.preview.PhotoPreview
import org.litepal.LitePal
import per.goweii.swipeback.SwipeBack
import per.goweii.swipeback.SwipeBackDirection
import per.goweii.swipeback.transformer.ShrinkSwipeBackTransformer
import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager

/**
 * Created by 咸鱼至尊 on 2021/12/9
 *
 * desc: 全局Application对象
 */
class AppApplication : Application() {
    companion object {
        /** 全局context对象 */
        @SuppressLint("StaticFieldLeak")
        internal lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        //延迟初始化全局context对象
        context = applicationContext
        //初始化数据序列化框架
        MMKV.initialize(this)
        //启用动态主题颜色
        DynamicColors.applyToActivitiesIfAvailable(this)
        //Activity栈管理初始化
        ActivityManager.getInstance().init(this)
        //本地异常捕捉
        CrashHandler.register(this)
        //初始化数据库框架
        LitePal.initialize(this)
        //初始化Toast框架
        Toaster.init(this)
        //设置吐司调试模式
        Toaster.setDebugMode(AppConfig.isDebug())
        //全局配置侧滑返回activity(有各种骚操作的奇怪BUG，不影响正常使用)
        SwipeBack.getInstance().run {
            //初始化侧滑返回
            init(this@AppApplication)
            //栈底不可滑动返回
            isRootSwipeBackEnable = false
            //强制边缘侧滑返回(横向列表适配器)
            isSwipeBackForceEdge = true
            //下滑方向
            swipeBackDirection = SwipeBackDirection.BOTTOM
            //底部activity联动视差效果
            swipeBackTransformer = ShrinkSwipeBackTransformer(0.94f, 1f, 0.5f, 0.5f)
        }
        //网络请求配置全局根路径
        NetConfig.initialize(host = BaseURL, context = this) {
            //设置Gson解析方式
            setConverter(SerializationConverter("200", "code", "msg"))
            //添加日志拦截器
            if (AppConfig.isDebug()) addInterceptor(OkHttpProfilerInterceptor())
            //全局加载中对话框配置(使用自定义对话框)
            setDialogFactory {
                WaitDialog.Builder(it).apply {
                    setMessage(getString(R.string.load))
                }
            }
            //设置请求拦截器
            setRequestInterceptor(object : RequestInterceptor {
                override fun interceptor(request: BaseRequest) {
                    //添加请求头信息
                    request.apply {
                        addHeader("project_token", AppConfig.project_token)
                        addHeader("token", AppConfig.token)
                        addHeader("uk", AppConfig.uk)
                        addHeader("channel", AppConfig.channel)
                        addHeader("app", AppConfig.app)
                        addHeader("device", AppConfig.device)
                    }
                }
            })
        }
        //全局缺省页配置 [https://github.com/liangjingkanji/StateLayout]
        StateConfig.apply {
            emptyLayout = R.layout.layout_empty
            loadingLayout = R.layout.layout_loading
            errorLayout = R.layout.layout_error
            //点击重试，会触发[onRefresh]
            setRetryIds(R.id.msg)
        }
        //全局分页初始索引(注意：每次调用智能刷新扩展布局的Refresh刷新方法都会重置索引，若请求分页初始索引不同，需在每个页面设置单例初始索引)
        PageRefreshLayout.startIndex = 1
        //全局预加载索引
        PageRefreshLayout.preloadIndex = 3
        //初始化SmartRefreshLayout构建器
        //设置全局RefreshHeader构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            ClassicsHeader(context).setTimeFormat(DynamicTimeFormat("更新于 %s"))//指定为经典Header
        }
        //设置全局RefreshFooter构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context).setDrawableSize(20F)//指定为经典Footer
        }
        //设置全局图片加载器
        PhotoPreview.setGlobalImageLoader { _: Int, source: Any?, imageView: ImageView ->
            //加载原图
            Glide.with(imageView.context).load(source as String?).override(Target.SIZE_ORIGINAL).into(imageView)
        }
        //视频播放器初始化配置
        VideoViewManager.setConfig(
            VideoViewConfig.newBuilder()
                //使用MediaPlayer解码
                .setPlayerFactory(AndroidMediaPlayerFactory.create())
                .build()
        )
    }
}