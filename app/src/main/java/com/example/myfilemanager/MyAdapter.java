package com.example.myfilemanager;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.HashMap;
import java.util.List;

/**
 * Created by uilubo on 2015/9/11.
 */
public class MyAdapter extends BaseAdapter {

    List<FileInfo> list;
    LayoutInflater inflater;


    //声明一个哈希表:用来记录用户的item
    public HashMap<Integer, Integer> selectMap = new HashMap<Integer, Integer>();
    //声明接口

    public void setProxy(OnclickInterfaceFile proxy) {
        this.proxy = proxy;

    }

    OnclickInterfaceFile proxy;

    public MyAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setList(List<FileInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return (list == null) ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.size = (TextView) convertView.findViewById(R.id.desc);
            holder.lineOnClick = (RelativeLayout) convertView.findViewById(R.id.lineOnClick);
            holder.imgOnClick = (ImageView) convertView.findViewById(R.id.imgOnClick);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FileInfo map = list.get(position);
        String w = (String) map.name;// 原始应用名
        String key =FileManagerUtils.KEY;
        int start = w.toLowerCase().indexOf(key.toLowerCase());//高亮文字的起始位置
        if (start > -1) {// 有
            int end = start + key.length();//高亮文字的终止位置
            // 字符串样式对象
            SpannableStringBuilder style
                    = new SpannableStringBuilder((String) map.name);
            style.setSpan(// 设定样式
                    new ForegroundColorSpan(Color.BLUE),// 前景样式
                    start,// 起始坐标
                    end,// 终止坐标
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE// 旗标
            );

            holder.name.setText(style);// 将样式设置给TextView
        } else {
            holder.name.setText(w);// 将原来的文字赋值
        }

        FileInfo item = list.get(position);
        holder.icon.setImageResource(item.icon);

        // holder.name.setText(item.name);
        holder.size.setText(item.size + " " + item.time);

//点击事件结果
        if (selectMap.containsKey(position)) {
            holder.imgOnClick.setImageResource(R.drawable.blue_selected);

        } else {

            holder.imgOnClick.setImageResource(R.drawable.blue_unselected);
        }
        //点击多选框事件
        holder.lineOnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proxy.itemClick(position);
            }
        });
        return convertView;
    }


    public class ViewHolder {
        ImageView icon;
        TextView name;
        TextView size;
        RelativeLayout lineOnClick;
        ImageView imgOnClick;
    }
}
