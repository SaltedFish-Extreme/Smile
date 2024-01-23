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
import com.drake.channel.sendTag
import com.example.smile.R
import com.example.smile.ui.adapter.UploadPictureAdapter.MainVH
import com.example.smile.widget.ext.clickNoRepeat
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
    private lateinit var rv: RecyclerView

    init {
        mInflater = LayoutInflater.from(activity)
        mGlide = Glide.with(activity)
        mActivity = activity
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //赋值rv
        rv = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainVH {
        val ivPhoto = mInflater.inflate(R.layout.item_add_picture, parent, false)
        return MainVH(ivPhoto)
    }

    override fun onBindViewHolder(holder: MainVH, position: Int) {
        val photo = dataList[position].path
        //加载显示图片
        mGlide.load(photo).apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
            .placeholder(R.drawable.ic_loading).error(R.drawable.ic_load_error).into(holder.ivPhoto)
        //长按图片删除
        holder.ivPhoto.setOnLongClickListener {
            Toaster.show(R.string.delete_success)
            dataList.removeAt(position) //从数据源删除
            notifyItemRemoved(position) //通知移除item
            notifyItemRangeChanged(position, itemCount - position) //刷新数据（不加偶尔会删除 item 的位置错误）
            sendTag(mActivity.getString(R.string.channel_tag_remove_photo)) //发送消息标签，删除图片
            true
        }
        //单击图片预览
        holder.ivPhoto.clickNoRepeat {
            PhotoPreview.with(mActivity).defaultShowPosition(position).sources(dataList.map { it.path })
                .shapeTransformType(ShapeTransformType.ROUND_RECT).shapeCornerRadius(10).build().show { pos ->
                    val viewByPosition: View? = rv.layoutManager?.findViewByPosition(pos)
                    return@show viewByPosition?.findViewById<View>(R.id.upload_image)
                }// 指定缩略图
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