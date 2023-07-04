package com.example.smile.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.smile.R
import com.gyf.immersionbar.ktx.immersionBar

/**
 * Created by 咸鱼至尊 on 2023/7/3
 *
 * desc: Fragment基类
 */
open class AppFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //状态栏沉浸
        when (AppConfig.DarkTheme) {
            false -> immersionBar {
                navigationBarColor(R.color.white_smoke)
                statusBarDarkFont(true, 0.2f)
                navigationBarDarkIcon(true, 0.2f)
            }

            true -> immersionBar {}
        }
    }
}