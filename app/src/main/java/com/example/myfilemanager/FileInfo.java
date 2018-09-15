package com.example.myfilemanager;

/**
 * Created by ZPC on 2016/5/26.
 */
public class FileInfo {
    public int icon;
    public String name;
    public String size;
    public String time;
    public String path;
    public int type;
    public long lastModify;
    public long bytesize;

    @Override
    public String toString() {
        return "FileInfo{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", size='" + size + '\'' +
                ", time='" + time + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                ", lastModify=" + lastModify +
                ", bytesize=" + bytesize +
                '}';
    }
}
