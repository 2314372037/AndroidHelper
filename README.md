# zpermission
#**一个android权限请求辅助类，activity和fragment通用**

###回调在UI线程，使用ViewModel和LiveData，自动生命周期管理

####使用方法:
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
