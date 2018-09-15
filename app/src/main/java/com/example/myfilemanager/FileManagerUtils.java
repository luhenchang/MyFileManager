package com.example.myfilemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 工具类
 *
 * @author spl
 */
public class FileManagerUtils {
    public static String KEY = "";//全局搜索的关键
    public String pathFile = "";
    public MyAdapter adapter;

    /**
     * 获取SDcard根路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    /**
     * 通过传入的路径,返回该路径下的所有的文件和文件夹列表
     *
     * @param path
     * @return
     */
    public static List<FileInfo> getListData(String path) {

        List<FileInfo> list = new ArrayList<FileInfo>();

        File pfile = new File(path);// 文件对象
        File[] files = null;// 声明了一个文件对象数组
        if (pfile.exists()) {// 判断路径是否存在
            files = pfile.listFiles();// 该文件对象下所属的所有文件和文件夹列表
        }

        if (files != null && files.length > 0) {// 非空验证
            for (File file : files) {// foreach循环遍历
                FileInfo item = new FileInfo();
                if (file.isHidden()) {
                    continue;// 跳过隐藏文件
                }
                if (file.isDirectory()// 文件夹
                        && file.canRead()//是否可读
                        ) {
                    file.isHidden();//  是否是隐藏文件
                    // 获取文件夹目录结构
                    item.icon = R.drawable.folder;//图标
                    item.bytesize = file.length();
                    item.size = getSize(item.bytesize);//大小
                    item.type = MainActivity.T_DIR;

                } else if (file.isFile()) {// 文件
                    Log.i("spl", file.getName());
                    String ext = getFileEXT(file.getName());
                    Log.i("spl", "ext=" + ext);

                    // 文件的图标
                    item.icon = getDrawableIcon(ext);// 根据扩展名获取图标
                    // 文件的大小
                    String size = getSize(file.length());
                    item.size = size;
                    item.type = MainActivity.T_FILE;

                } else {// 其它
                    item.icon = R.drawable.mul_file;
                }
                item.name = file.getName();// 名称
                item.lastModify = file.lastModified();
                item.path = file.getPath();// 路径
                // 最后修改时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(file.lastModified());
                String time = sdf.format(date);
                item.time = time;

                list.add(item);
            }
        }
        files = null;
        return list;
    }

    /**
     * 格式转换应用大小 单位"B,KB,MB,GB"
     */

    public static String getSize(float length) {

        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        if (length < kb) {
            return String.format("%dB", (int) length);
        } else if (length < mb) {
            return String.format("%.2fKB", length / kb);
        } else if (length < gb) {
            return String.format("%.2fMB", length / mb);
        } else {
            return String.format("%.2fGB", length / gb);
        }
    }

    public static List<FileInfo> getSearchResult(List<FileInfo> list, String keyword) {
//返回实体集合
        List<FileInfo> searchResultList = new ArrayList<FileInfo>();
        //循环遍历
        for (int i = 0; i < list.size(); i++) {
            FileInfo app = list.get(i);//拿到单个的实体类
//拿关键字和实体类比较
            if (app.name.toLowerCase().contains(keyword.toLowerCase())) {

                searchResultList.add(app);//添加到结果集
            }

        }
        return searchResultList;
    }

    /**
     * 截取文件的扩展名
     *
     * @param filename 文件全名
     * @return 扩展名(mp3, txt)
     */
    public static String getFileEXT(String filename) {
        if (filename.contains(".")) {
            int dot = filename.lastIndexOf(".");// 123.abc.txt
            String ext = filename.substring(dot + 1);
            return ext;
        } else {
            return "";
        }
    }

    /**
     * 检查扩展名end 是否在ends数组中
     *
     * @param end
     * @param ends
     * @return
     */
    public static boolean checkEndsInArray(String end, String[] ends) {
        for (String aEnd : ends) {
            if (end.equals(aEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得与扩展名对应的图标资源id
     *
     * @param end 扩展名
     * @return
     */
    public static int getDrawableIcon(String end) {

        int id = 0;
        if (end.equals("asf")) {
            id = R.drawable.asf;
        } else if (end.equals("avi")) {
            id = R.drawable.avi;
        } else if (end.equals("bmp")) {
            id = R.drawable.bmp;
        } else if (end.equals("doc")) {
            id = R.drawable.doc;
        } else if (end.equals("gif")) {
            id = R.drawable.gif;
        } else if (end.equals("html")) {
            id = R.drawable.html;
        } else if (end.equals("apk")) {
            id = R.drawable.iapk;
        } else if (end.equals("ico")) {
            id = R.drawable.ico;
        } else if (end.equals("jpg")) {
            id = R.drawable.jpg;
        } else if (end.equals("log")) {
            id = R.drawable.log;
        } else if (end.equals("mov")) {
            id = R.drawable.mov;
        } else if (end.equals("mp3")) {
            id = R.drawable.mp3;
        } else if (end.equals("mp4")) {
            id = R.drawable.mp4;
        } else if (end.equals("mpeg")) {
            id = R.drawable.mpeg;
        } else if (end.equals("pdf")) {
            id = R.drawable.pdf;
        } else if (end.equals("png")) {
            id = R.drawable.png;
        } else if (end.equals("ppt")) {
            id = R.drawable.ppt;
        } else if (end.equals("rar")) {
            id = R.drawable.rar;
        } else if (end.equals("txt") || end.equals("dat") || end.equals("ini")
                || end.equals("java")) {
            id = R.drawable.txt;
        } else if (end.equals("vob")) {
            id = R.drawable.vob;
        } else if (end.equals("wav")) {
            id = R.drawable.wav;
        } else if (end.equals("wma")) {
            id = R.drawable.wma;
        } else if (end.equals("wmv")) {
            id = R.drawable.wmv;
        } else if (end.equals("xls")) {
            id = R.drawable.xls;
        } else if (end.equals("xml")) {
            id = R.drawable.xml;
        } else if (end.equals("zip")) {
            id = R.drawable.zip;
        } else if (end.equals("3gp") || end.equals("flv")) {
            id = R.drawable.file_video;
        } else if (end.equals("amr")) {
            id = R.drawable.file_audio;
        } else {
            id = R.drawable.default_fileicon;
        }
        return id;
    }


    /**
     * 打开文件
     *
     * @param context
     * @param aFile
     */
    public static void openFile(Context context, File aFile) {

        // 实例化意图
        Intent intent = new Intent();
        // 添加动作(干什么?)
        intent.setAction(Intent.ACTION_VIEW);
        //取得文件名
        String fileName = aFile.getName();
        String end = getFileEXT(fileName).toLowerCase();
        if (aFile.exists()) {
            // 根据不同的文件类型来打开文件
            if (checkEndsInArray(end, new String[]{"png", "gif", "jpg", "bmp"})) {
                // 图片
                intent.setDataAndType(Uri.fromFile(aFile), "image/*");//MIME TYPE
            } else if (checkEndsInArray(end, new String[]{"apk"})) {
                // apk
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.android.package-archive");
            } else if (checkEndsInArray(end, new String[]{"mp3", "amr", "ogg", "mid", "wav"})) {
                // audio
                intent.setDataAndType(Uri.fromFile(aFile), "audio/*");
            } else if (checkEndsInArray(end, new String[]{"mp4", "3gp", "mpeg", "mov", "flv"})) {
                // video
                intent.setDataAndType(Uri.fromFile(aFile), "video/*");
            } else if (checkEndsInArray(end, new String[]{"txt", "ini", "log", "java", "xml", "html"})) {
                // text
                intent.setDataAndType(Uri.fromFile(aFile), "text/*");
            } else if (checkEndsInArray(end, new String[]{"doc", "docx"})) {
                // word
                intent.setDataAndType(Uri.fromFile(aFile), "application/msword");
            } else if (checkEndsInArray(end, new String[]{"xls", "xlsx"})) {
                // excel
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.ms-excel");
            } else if (checkEndsInArray(end, new String[]{"ppt", "pptx"})) {
                // ppt
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.ms-powerpoint");
            } else if (checkEndsInArray(end, new String[]{"chm"})) {
                // chm
                intent.setDataAndType(Uri.fromFile(aFile), "application/x-chm");
            } else {
                // 其他
                intent.setDataAndType(Uri.fromFile(aFile), "application/" + end);
            }
            try {
                // 发送意图
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "没有找到适合打开此文件的应用", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * 文件分组
     *
     * @param list
     * @return
     */
    public static List<FileInfo> getGroupList(List<FileInfo> list) {
        List<FileInfo> dirs = new ArrayList<FileInfo>();// 文件夹列表
        List<FileInfo> files = new ArrayList<FileInfo>();// 文件列表
        //拆分
        for (int i = 0; i < list.size(); i++) {
            FileInfo item = list.get(i);
            if (item.type == 0) {
                dirs.add(item);
            } else {
                files.add(item);
            }
        }
        // 合并
        dirs.addAll(files);// 文件夹+文件

        return dirs;
    }

    /**
     * 删除一个文件
     *
     * @param path
     */
    public static void deleteFile(String path) {
        // TODO Auto-generated method stub
        File file = new File(path);
        if (file.exists()) {
            file.delete();// 删除
        }
    }

    /**
     * 删除一个文件夹(递归调用)
     *
     * @param path
     */
    public static void deleteDir(String path) {
        // 1. 对自己的子元素进行遍历
        File dir = new File(path);// 目标文件夹
        File[] files = null;// 声明了一个文件对象数组
        if (dir.exists()) {// 判断路径是否存在
            files = dir.listFiles();// 该文件对象下所属的所有文件和文件夹列表
        }

        if (files != null && files.length > 0) {// 非空验证
            for (File file : files) {// foreach循环遍历
                // 2. 对每个子元素进行删除
                if (file.isFile()) {// 删除文件
                    deleteFile(file.getAbsolutePath());
                }
                if (file.isDirectory()) {// 删除文件夹!!!
                    deleteDir(file.getAbsolutePath());
                }
            }//for
        }// if
        // 3. 删掉自己
        dir.delete();
    }


    /**
     * 对单个文件的内容的拷贝
     *
     * @param from 待拷贝的原文件对象
     * @param to   目的地文件对象
     */
    public static void copyFile(File from, File to) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
            in.transferTo(0, in.size(), out);// 将文件通过out传输到目标文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null && fos != null && in != null && out != null) {

                try {
                    fis.close();
                    fos.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 在给定的文件夹目录下,拷入目标文件
     *
     * @param targetDir 目标文件夹的路径
     * @param file      待考的文件对象
     * @return
     */
    public static int pasteFile(String targetDir, File file) {
        // 生成目的地文件对象  新的文件夹/abc.txt
        File newFile = new File(targetDir, file.getName());
        if (newFile.exists()) {
            // Toast.makeText(this,newPath+"文件已存在",Toast.LENGTH_SHORT).show();
            return 1;// 没有成功
        } else {
            // 没有重名文件
            try {
                if (file.isFile()) {
                    newFile.createNewFile();// 新创建文件(空)

                } else if (file.isDirectory()) {

                    newFile.mkdirs();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        copyFile(file, newFile);//<------------------ call upper method

        return 0;// OK
    }

    /**
     * 在给定的文件夹目录下,粘贴上之前标记的文件夹中所有的内容(递归)
     * @param targetDir 目标文件夹的路径
     * @param dir 待考的文件夹对象
     * @return
     */
    public static int pasteDir(String targetDir, File dir) {
        // 生成目的地文件对象  targetDir=/temp, dir = abc
        //newDir = /temp/abc/....
        File newDir = new File(targetDir , dir.getName());
        // 生成这个newDir所对应的路径
        newDir.mkdirs();

        File[] files = null;// 声明了一个文件对象数组
        if (dir.exists()) {// 判断路径是否存在
            files = dir.listFiles();// 该文件对象下所属的所有文件和文件夹列表
        }

        if (files != null && files.length > 0) {// 非空验证
            for (File file : files) {// foreach循环遍历
                // 2. 对每个子元素进行
                if (file.isFile()){// 复制文件
                    pasteFile(newDir.getAbsolutePath(),file);
                }
                if (file.isDirectory()){// 复制文件夹!!!
                    pasteDir(newDir.getAbsolutePath(),file);// 递归调用
                }
            }//for
        }// if

        return 0;// OK
    }
}
