package com.example.smile.widget.imageload;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.smile.R;
import com.example.smile.widget.ext.CustomViewExtKt;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class ShowImagesDialog extends Dialog {

    private View mView;
    private final Context mContext;
    private ShowImagesViewPager mViewPager;
    private TextView mIndexText;
    private final List<String> mImgUrls;
    private List<String> mTitles;
    private List<View> mViews;
    private final int position;

    public ShowImagesDialog(@NonNull Context context, List<String> imgUrls, int position) {
        super(context, R.style.transparentBgDialog);
        this.mContext = context;
        this.mImgUrls = imgUrls;
        this.position = position;
        initView();
        initData();
    }

    private void initView() {
        mView = View.inflate(mContext, R.layout.dialog_images_brower, null);
        mViewPager = mView.findViewById(R.id.vp_images);
        mIndexText = mView.findViewById(R.id.tv_image_index);
        mTitles = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        Window window = getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = 0;
        wl.height = CustomViewExtKt.getScreenHeight(mContext);
        wl.width = CustomViewExtKt.getScreenWidth(mContext);
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    private void initData() {
        for (int i = 0; i < mImgUrls.size(); i++) {
            final PhotoView photoView = new PhotoView(mContext);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            photoView.setLayoutParams(layoutParams);
            photoView.setOnPhotoTapListener((view, x, y) -> dismiss());
            Glide.with(mContext).load(mImgUrls.get(i)).placeholder(R.drawable.load_picture).error(R.drawable.load_picture).into(photoView);
            mViews.add(photoView);
            mTitles.add(i + "");
        }
        ShowImagesAdapter adapter = new ShowImagesAdapter(mViews, mTitles);
        mViewPager.setAdapter(adapter);
        mIndexText.setText(mContext.getString(R.string.page_num, position + 1, mImgUrls.size()));
        mViewPager.setCurrentItem(position);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mIndexText.setText(mContext.getString(R.string.page_num, position + 1, mImgUrls.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}