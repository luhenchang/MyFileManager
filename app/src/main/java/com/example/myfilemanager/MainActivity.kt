package com.example.myfilemanager
import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.storage.StorageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.myfilemanager.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.text.Collator
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.Locale


class MainActivity : AppCompatActivity(), OnclickInterfaceFile, SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    internal lateinit var list: List<FileInfo>// 数据
    internal lateinit var lv: ListView
    internal var allList: MutableList<FileInfo> = ArrayList<FileInfo>()
    internal lateinit var listView: ListView
    internal lateinit var layout: LinearLayout
    internal lateinit var relativeLayout1: RelativeLayout
    internal var name = ""
    internal lateinit var text: EditText
    internal lateinit var img: ImageView
    internal lateinit var img1: ImageView
    internal lateinit var tv_path: TextView
    internal lateinit var view: View
    internal lateinit var currPath: String // 当前目录
    internal lateinit var parentPath: String // 上级目录


    internal val ROOT = FileUtils.sdCardPath + "/amap1" //SDCard根目录  File.separator

    internal val sorts = arrayOf("名称", "日期", "大小")

    companion object {
        const val T_DIR = 0// 文件夹
        const val T_FILE = 1// 文件

        const val SORT_NAME = 0//按名称排序
        const val SORT_DATE = 1//按日期排序
        const val SORT_SIZE = 2//按大小排序
        internal var currSort = SORT_DATE//当前排序
        internal var comparator: Comparator<FileInfo>? = null// 当前所使用的比较器
        const private val REQUEST_WRITE_EXTERNAL_STORAGE = 99
    }

    //声明变量3
    internal var sv: SearchView? = null
    internal lateinit var search: MenuItem


    internal var asc = 1 // 可以帮助在正序和倒序之间进行切换
    // 日期比较器
    internal var dateComparator: Comparator<FileInfo> = Comparator<FileInfo> { lhs, rhs ->
        if (rhs.lastModify > lhs.lastModify) {
            -1 * asc
        } else if (rhs.lastModify === lhs.lastModify) {
            0
        } else {
            1 * asc
        }
    }

    // 大小比较器
    internal var sizeComparator: Comparator<FileInfo> = Comparator<FileInfo> { lhs, rhs ->
        if (rhs.bytesize > lhs.bytesize) {
            -1 * asc
        } else if (rhs.bytesize === lhs.bytesize) {
            0
        } else {
            1 * asc
        }
    }

    // 应用名比较器
    internal var nameComparator: Comparator<FileInfo> = Comparator<FileInfo> { lhs, rhs ->
        // 为了适应汉字的比较
        val c = Collator.getInstance(Locale.CHINA)
        if (asc == 1)
            c.compare(lhs.name, rhs.name)
        else
            c.compare(rhs.name, lhs.name)
    }

    internal lateinit var mAdapter: FileAdapter // 适配器

    // 1声明进度框对象
    internal lateinit var pd: ProgressDialog

    internal var handler: Handler = object : Handler() {// 内部类

        override fun handleMessage(msg: Message) {
            // 重写方法
            if (msg.what == 1) {// UI 线程的回调处理
                pd.dismiss()
                // 更新列表
                mAdapter.notifyDataSetChanged()

                Toast.makeText(this@MainActivity, "文件数:" + list.size, Toast.LENGTH_LONG).show()
                update_sort()
            }
        }
    }
    internal lateinit var iv_asc: ImageView
    internal lateinit var sort: TextView
    internal lateinit var count: TextView
    internal lateinit var size: TextView

    /**
     * 遍历数据集合,累加全部的Size
     *
     * @return
     */
    private// 总和
    // foreach
    val listSize: String
        get() {
            var sum: Long = 0
            for (app in list) {
                sum += app.bytesize
            }
            return FileUtils.getSize(sum.toFloat())
        }


    //复制文件结构
    internal var copyMap = HashMap<Int, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );*/
        setContentView(R.layout.activity_main)
        checkPermission()
        initView()// 初始化
        Log.e("path_erro", getStoragePath(this, false)!! + "/amap1")
        updateData(getStoragePath(this, false)!! + "/amap1")//"/storage/emulated/0/amap/file_locks"
        FileUtils.KEY = ""//初始化
        // 初始化控件
        lv = findViewById(R.id.list) as ListView
        mAdapter = FileAdapter(this)
        mAdapter.setList(list)
        mAdapter.setProxy(this)
        lv.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


        sort = findViewById(R.id.sort) as TextView
        count = findViewById(R.id.count) as TextView
        size = findViewById(R.id.size) as TextView
        iv_asc = findViewById(R.id.iv_asc) as ImageView
        layout = findViewById(R.id.pathclick) as LinearLayout
        img = findViewById(R.id.imgpath) as ImageView
        relativeLayout1 = findViewById(R.id.bottom) as RelativeLayout

        layout.isEnabled = false
        updateData()// 子线程--拿数据
    }

    private fun initView() {
        tv_path = findViewById(R.id.path) as TextView
        listView = findViewById(R.id.list) as ListView
        mAdapter = FileAdapter(this)
        listView.adapter = mAdapter
        listView.onItemClickListener = this

    }

    private fun updateData(path: String) {
        currPath = path// 记录当前的目录
        val file = File(path)
        parentPath = file.parent// 更新了上级目录
        mAdapter.selectMap.clear()
        list = FileUtils.getListData(path)// 数据'

        Log.e("listdata", list.toString())
        list = FileUtils.getGroupList(list)//2次排序
        mAdapter.setList(list)
        mAdapter.notifyDataSetChanged()// 刷新视图
        tv_path.text = submitPath(path)

    }

    /**
     * 截取字符串
     *
     * @return
     */

    fun submitPath(path: String): String {
        return if (path == ROOT) {
            "/"
        } else {
            currPath.substring(ROOT.length)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        search = menu.findItem(R.id.search)//容器
        sv = search.actionView as SearchView//真正的搜索对象'
        if (sv != null) {
            sv!!.setIconifiedByDefault(false)//图标显示在外侧
            sv!!.isSubmitButtonEnabled = true//让提交按钮可用
            sv!!.queryHint = "请输入文件名"//提示用户信息
            sv!!.setOnQueryTextListener(this)//关联提交事件
        }
        return true
    }

    private fun checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                            .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show()
            }
            //申请权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.sort_name) {
            currSort = SORT_NAME// 给排序状态赋值
        } else if (id == R.id.sort_date) {
            currSort = SORT_DATE
        } else if (id == R.id.sort_size) {
            currSort = SORT_SIZE
        } else if (id == R.id.c1) {
            val inflater = this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout, null)
            //显示确认框
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("系统提示")
                    .setView(view)
                    .setPositiveButton("确定") { dialog, which ->
                        text = view.findViewById(R.id.name1) as EditText
                        name = text.text.toString()
                        val destDir = File("$currPath/$name")
                        if (!destDir.exists()) {
                            destDir.mkdirs()
                        }
                        updateData(currPath)
                        mAdapter.notifyDataSetChanged()
                    }.setNegativeButton("取消", null)
                    .create().show()


        } else if (id == R.id.y1) {


            val inflater = this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout, null)
            img1 = view.findViewById(R.id.icon1) as ImageView
            img1.setImageResource(R.drawable.txt)
            //显示确认框
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("系统提示")
                    .setView(view)
                    .setPositiveButton("确定") { dialog, which ->
                        text = view.findViewById(R.id.name1) as EditText
                        name = text.text.toString()
                        val destDir = File("$currPath/$name.txt")
                        if (!destDir.exists()) {
                            try {
                                destDir.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        updateData1(currPath)
                    }.setNegativeButton("取消", null)
                    .create().show()


        } else if (id == R.id.y2) {


            val inflater = this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout, null)
            img1 = view.findViewById(R.id.icon1) as ImageView
            img1.setImageResource(R.drawable.xml)
            //显示确认框
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("系统提示")
                    .setView(view)
                    .setPositiveButton("确定") { dialog, which ->
                        text = view.findViewById(R.id.name1) as EditText
                        name = text.text.toString()
                        val destDir = File("$currPath/$name.xml")
                        if (!destDir.exists()) {
                            try {
                                destDir.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        updateData1(currPath)
                    }.setNegativeButton("取消", null)
                    .create().show()

        } else if (id == R.id.y3) {

            val inflater = this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout, null)
            img1 = view.findViewById(R.id.icon1) as ImageView
            img1.setImageResource(R.drawable.doc)
            //显示确认框
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("系统提示")
                    .setView(view)
                    .setPositiveButton("确定") { dialog, which ->
                        text = view.findViewById(R.id.name1) as EditText
                        name = text.text.toString()
                        val destDir = File("$currPath/$name.doc")
                        if (!destDir.exists()) {
                            try {
                                destDir.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        updateData1(currPath)
                    }.setNegativeButton("取消", null)
                    .create().show()


        } else if (id == R.id.y4) {

            val inflater = this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout, null)
            img1 = view.findViewById(R.id.icon1) as ImageView
            img1.setImageResource(R.drawable.xls)
            //显示确认框
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("系统提示")
                    .setView(view)
                    .setPositiveButton("确定") { dialog, which ->
                        text = view.findViewById(R.id.name1) as EditText
                        name = text.text.toString()
                        val destDir = File("$currPath/$name.xls")
                        if (!destDir.exists()) {
                            try {
                                destDir.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        updateData1(currPath)
                    }.setNegativeButton("取消", null)
                    .create().show()
        }
        update_sort()// 调用统一的排序方法

        asc *= -1//负数,正数

        return super.onOptionsItemSelected(item)
    }

    private fun update_sort() {
        if (currSort == SORT_NAME) {
            comparator = nameComparator// 选择不同的比较器
        }
        if (currSort == SORT_DATE) {
            comparator = dateComparator
        }
        if (currSort == SORT_SIZE) {
            comparator = sizeComparator
        }
        Collections.sort<FileInfo>(list, comparator)// 这里才是排序的操作
        list = FileUtils.getGroupList(list)//2次排序
        mAdapter.setList(list)
        mAdapter.notifyDataSetChanged()// 刷新视图
        update_infobar()
    }

    // 显示一个环形进度框
    fun showProgressDialog() {
        // 实例化
        pd = ProgressDialog(this)
        // "旋转"风格
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setTitle("系统信息")
        pd.setMessage("正在加载文件列表,请耐心等待...")
        pd.show()// 显示
    }


    private fun updateData1(path: String) {

        // (1)--启动新线程,处理耗时操作
        object : Thread() {
            override fun run() {
                // 获得数据(所有的应用)
                list = FileUtils.getListData(path)
                allList.clear()// 清空
                allList.addAll(list)// 复制集合
                list = FileUtils.getGroupList(list)//2次排序
                mAdapter.setList(list)
                // 给主线程发消息

                val msg = handler.obtainMessage()
                msg.what = 1
                handler.sendMessage(msg)// msg.what=1
            }
        }.start()
        // (2) --
        showProgressDialog()// 显示进度框
    }


    //3.子线程
    private fun updateData() {

        // (1)--启动新线程,处理耗时操作
        object : Thread() {
            override fun run() {
                //TODO 获得数据(所有的应用)TODO
                list = FileUtils.getListData(getStoragePath(this@MainActivity, false)!! + "/amap1")
                allList.clear()// 清空
                allList.addAll(list)// 复制集合
                list = FileUtils.getGroupList(list)//2次排序
                mAdapter.setList(list)
                // 给主线程发消息

                val msg = handler.obtainMessage()
                msg.what = 1
                handler.sendMessage(msg)// msg.what=1
            }
        }.start()
        // (2) --
        showProgressDialog()// 显示进度框
    }

    // 更新顶部信息栏中内容
    private fun update_infobar() {


        if (asc == 1) {
            iv_asc.setImageResource(R.drawable.top_icon)
        } else {
            iv_asc.setImageResource(R.drawable.down_icon)
        }
        sort.text = "排序: " + sorts[currSort]

        //sort.setOnClickListener(this);
        count.text = "文件数: " + list.size
        size.text = "大小: $listSize"
    }

    //long time = System.currentTimeMillis();

    override fun onBackPressed() {
        // 点击"回退"键

        // 计算和上次点击的时间差
        //        long delta = System.currentTimeMillis() - time;
        //        if (delta < 1000) {// 小于1秒
        //            // 用户真的要退出
        //            finish();
        //        } else {// 不让用户退出
        //            Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
        //            time = System.currentTimeMillis();// 让time更新为当前时间
        //        }

        // 返回 --> 打开上级
        if (currPath == ROOT) {
            // 退出流程
            getExit()
        } else {
            //打开上目录
            updateData(parentPath)
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        //提交关键字
        // Utils.toast(this, "您查询的关键字是 ：" + query.trim());
        FileUtils.KEY = query.trim { it <= ' ' }
        list = FileUtils.getSearchResult(allList, query)//根据关键字生成结果
        update_sort()//重新排序事件

        return true//消化事件
    }

    fun clickImg(v: View) {

        update_sort()
        //切换正负倒序
        asc *= -1


    }

    override fun onQueryTextChange(newText: String): Boolean {
        FileUtils.KEY = newText.trim { it <= ' ' }
        list = FileUtils.getSearchResult(allList, newText.trim { it <= ' ' })//根就关键字生成结果
        update_sort()//重新排序更新

        return true
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position) as FileInfo
        // 判断文件/文件夹
        if (item.type === T_DIR) {
            // 进入
            updateData(item.path)


        } else {
            // 文件: 打开
            FileUtils.openFile(this, File(item.path))
        }

    }


    private fun getExit() {
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setMessage("确定退出吗?")
                .setPositiveButton("确定") { dialog, which -> finish() }
                .setNegativeButton("取消", null)
                .show()
    }


    override fun itemClick(position: Int) {

        //点击多选的实现
        if (mAdapter.selectMap.containsKey(position)) {
            //删除key
            mAdapter.selectMap.remove(position)
            if (mAdapter.selectMap.size === 0) {

                relativeLayout1.visibility = View.GONE
            } else {

                relativeLayout1.visibility = View.VISIBLE
            }
        } else {

            relativeLayout1.visibility = View.VISIBLE
            mAdapter.selectMap.put(position, position)
        }
        mAdapter.notifyDataSetChanged()
    }
    //复制
    fun cope(view: View) {

        if (mAdapter.selectMap.size === 0) {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show()
        } else {

            //把用户信息保存到一个合理的数据结构中
            copyMap.clear()
            for (position in mAdapter.selectMap.keys) {

                copyMap[position] = list[position!!].path
            }
            Toast.makeText(this, copyMap.size.toString() + "个项目已保存", Toast.LENGTH_SHORT).show()
            //切换粘贴为激活状态
            //切换粘贴为激活状态
            layout.isEnabled = true

            img.setImageResource(R.drawable.ic_menu_paste_holo_light)

        }

    }


    fun delete(view: View) {

        if (mAdapter.selectMap.size === 0) {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show()

        } else {


            //显示确认框
            AlertDialog.Builder(this)
                    .setTitle("系统提示")
                    .setMessage("您是否要删除这" + mAdapter.selectMap.size + "个项目")
                    .setPositiveButton("确定") { dialog, which ->
                        //遍历
                        for (position in mAdapter.selectMap.keys) {
                            val path = list[position!!].path
                            val file = File(path)
                            //根据path路径删除文件
                            if (file.isFile()) {
                                FileUtils.deleteFile(path)
                            }
                            if (file.isDirectory()) {
                                FileUtils.deleteDir(path)
                            }

                        }
                        //更新
                        mAdapter.selectMap.clear()
                        updateData(currPath)
                    }
                    .setNegativeButton("取消", null)
                    .create().show()
        }
    }


    fun path(view: View) {

        if (copyMap.size > 0) {
            for (path in copyMap.values) {
                val file = File(path)

                if (file.isFile) {
                    val res = FileUtils.pasteFile(currPath, File(path))
                    if (res == 1) {
                        Toast.makeText(this, "该文件已存在", Toast.LENGTH_SHORT).show()

                    } else {

                        Toast.makeText(this, path + "文件复制成功", Toast.LENGTH_SHORT).show()
                    }

                }
                //如果是剪切那么这里必须要删除文件夹和文件，之前剪切的如果是复制那就不进行处理了
                if (isCut) {
                    deletePath(view)
                    isCut = false
                }
                if (file.isDirectory) {

                    FileUtils.pasteDir(currPath, File(path))
                }
            }

            copyMap.clear()

            updateData(currPath)
            layout.isEnabled = false
            img.setImageResource(R.drawable.ic_menu_paste_holo_dark)


        } else {

            Toast.makeText(this, "没有可粘帖的项目", Toast.LENGTH_SHORT).show()
        }

    }

    //全选操作
    fun selectAll(v: View) {
        mAdapter.selectMap.clear()
        for (i in list.indices) {
            mAdapter.selectMap.put(i, i)
        }
        mAdapter.notifyDataSetChanged()
    }

    //全不选操作
    fun selectNone(v: View) {
        mAdapter.selectMap.clear()

        mAdapter.notifyDataSetChanged()
    }


    //剪切

    private var isCut: Boolean = false

    fun pathDelete(v: View) {
        if (mAdapter.selectMap.size> 0) {
            copyMap.clear()
            for (position in mAdapter.selectMap.keys) {

                copyMap[position] = list[position!!].path
            }
            for (path in copyMap.values) {
                val file = File(path)

                if (file.isFile) {

                    val res = FileUtils.pasteFile(currPath, File(path))
                    isCut=true
                    Toast.makeText(this, path + "文件剪切成功", Toast.LENGTH_SHORT).show()
                    //切换粘贴为激活状态
                    layout.isEnabled = true
                    img.setImageResource(R.drawable.ic_menu_paste_holo_light)
                    Toast.makeText(this, path + "文件剪切成功", Toast.LENGTH_SHORT).show()
                }

                if (file.isDirectory) {

                    FileUtils.pasteDir(currPath, File(path))
                }
            }

        } else {

            Toast.makeText(this, "没有可粘帖的项目", Toast.LENGTH_SHORT).show()
        }

    }

    fun deletePath(view: View) {


        //遍历
        for (path in copyMap.values) {
            val file = File(path)

            if (file.isFile) {
                FileUtils.deleteFile(path)
            }
            if (file.isDirectory) {
                FileUtils.deleteDir(path)
            }
        }
        //更新
        mAdapter.selectMap.clear()
        updateData(currPath)


    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储，true返回外置sd卡
     * @return
     */
    private fun getStoragePath(mContext: Context, is_removale: Boolean): String? {

        val mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        var storageVolumeClazz: Class<*>? = null
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz!!.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(mStorageManager)
            val length = Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = Array.get(result, i)
                val path = getPath.invoke(storageVolumeElement) as String
                val removable = isRemovable.invoke(storageVolumeElement) as Boolean
                if (is_removale == removable) {
                    return path
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return null
    }


}
