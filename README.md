# zpermission

**一个 android 权限请求辅助类，activity 和 fragment 通用**

回调在 UI 线程，自动生命周期管理

使用方法:

```
ZPermission.get(requireActivity())?.req(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )?.listener({
                //这是全部授权的回调
            },{
                //返回一个ArrayList<String>，包含拒绝的权限
                //这是拒绝一个或多个权限的回调
            })

```

# ViewHelper

**一个 fragment 多状态 view 帮助 kotlin 扩展函数**

使用超级简单！！！

两行代码实现指定 view 状态切换，不需要创建任何变量，不需要继承，可以在任意 fragment 主线程调用 showXXX()

使用方法:

```
xml:
    给一个view设置tag：android:tag="statusView"
    <FrameLayout
        android:tag="statusView"
        android:background="#407853"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!--你的内容-->
    </FrameLayout>

fragment:
      showEmpty(R.drawable.pic_empty,"没有数据")//显示到空视图，可以传入图片资源id和文本内容，可不传
      showLoading()//显示到正在加载视图，可以传入图片资源id和文本内容，可不传
      showError({//显示到错误视图，可以传入图片资源id和文本内容，可不传，点击页面重新加载监听，必传
         //重新加载数据
      })
      showSuccess()//显示到正常（原）视图

```
