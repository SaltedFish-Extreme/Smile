package com.example.smile.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.smile.R
import com.example.smile.ui.adapter.UploadPictureAdapter.MainVH
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.wgw.photo.preview.PhotoPreview
import com.wgw.photo.preview.ShapeTransformType

/**
 * 上传图片适配器(适用带有脚布局的RecyclerView)
 *
 * @param activity Activity对象
 * @property dataList 照片对象集合
 */
class UploadPictureAdapter(activity: FragmentActivity, private val dataList: ArrayList<Photo>) : RecyclerView.Adapter<MainVH>() {

    private val mInflater: LayoutInflater
    private val mGlide: RequestManager
    private val mActivity: FragmentActivity

    init {
        mInflater = LayoutInflater.from(activity)
        mGlide = Glide.with(activity)
        mActivity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainVH {
        val ivPhoto = mInflater.inflate(R.layout.item_add_image, parent, false)
        return MainVH(ivPhoto)
    }

    override fun onBindViewHolder(holder: MainVH, position: Int) {
        val photo = dataList[position].path
        //加载显示图片
        mGlide.load(photo).apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
            .placeholder(R.drawable.load_picture).into(holder.ivPhoto)
        //长按图片删除
        holder.ivPhoto.setOnLongClickListener {
            Toaster.show(mActivity.getString(R.string.delete_success))
            dataList.removeAt(position) //从数据源删除
            notifyItemRemoved(position) //通知移除item
            notifyItemRangeChanged(position, itemCount - position) //刷新数据（不加偶尔会删除 item 的位置错误）
            true
        }
        //单击图片预览
        holder.ivPhoto.setOnClickListener {
            PhotoPreview.with(mActivity).defaultShowPosition(position).sources(dataList.map { it.path })
                .shapeTransformType(ShapeTransformType.ROUND_RECT).shapeCornerRadius(10).build().show(it)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class MainVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivPhoto: ImageView

        init {
            ivPhoto = itemView.findViewById(R.id.upload_image)
        }
    }
}