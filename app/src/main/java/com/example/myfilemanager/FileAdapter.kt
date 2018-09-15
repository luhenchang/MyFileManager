package com.example.myfilemanager

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView




/**
 * Created by uilubo on 2015/9/11.
 */
class FileAdapter(context: Context) : BaseAdapter() {

    internal var list: List<FileInfo>? = null
    internal var inflater: LayoutInflater


    //声明一个哈希表:用来记录用户的item
    var selectMap = HashMap<Int, Int>()

    internal lateinit var proxy: OnclickInterfaceFile
    //声明接口

    fun setProxy(proxy: OnclickInterfaceFile) {
        this.proxy = proxy

    }

    init {
        this.inflater = LayoutInflater.from(context)
    }

    fun setList(list: List<FileInfo>) {
        this.list = list
    }

    override fun getCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    override fun getItem(position: Int): Any {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, null)
            holder = ViewHolder()
            holder.icon = convertView!!.findViewById(R.id.icon)
            holder.name = convertView!!.findViewById(R.id.name) as TextView
            holder.size = convertView!!.findViewById(R.id.desc) as TextView
            holder.lineOnClick = convertView!!.findViewById(R.id.lineOnClick) as RelativeLayout
            holder.imgOnClick = convertView!!.findViewById(R.id.imgOnClick)
            convertView!!.setTag(holder)
        } else {
            holder = convertView.getTag() as ViewHolder?
        }
        val map = list!![position]
        val w = map.name as String// 原始应用名
        val key = FileManagerUtils.KEY
        val start = w.toLowerCase().indexOf(key.toLowerCase())//高亮文字的起始位置
        if (start > -1) {// 有
            val end = start + key.length//高亮文字的终止位置
            // 字符串样式对象
            val style = SpannableStringBuilder(map.name as String)
            style.setSpan(// 设定样式
                    ForegroundColorSpan(Color.BLUE), // 前景样式
                    start, // 起始坐标
                    end, // 终止坐标
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE// 旗标
            )

            holder!!.name!!.setText(style)// 将样式设置给TextView
        } else {
            holder!!.name!!.setText(w)// 将原来的文字赋值
        }

        val item = list!![position]
        holder.icon!!.setImageResource(item.icon)

        // holder.name.setText(item.name);
        holder.size!!.setText(item.size + " " + item.time)

        //点击事件结果
        if (selectMap.containsKey(position)) {
            holder.imgOnClick!!.setImageResource(R.drawable.blue_selected)

        } else {

            holder.imgOnClick!!.setImageResource(R.drawable.blue_unselected)
        }
        //点击多选框事件
        holder.lineOnClick!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                proxy.itemClick(position)
            }
        })
        return convertView
    }


    inner class ViewHolder {
        internal var icon: ImageView? = null
        internal var name: TextView? = null
        internal var size: TextView? = null
        internal var lineOnClick: RelativeLayout? = null
        internal var imgOnClick: ImageView? = null
    }
}

