package com.zh.mvvmcore

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

/****
 * 基于viewModel和liveData的权限请求
 * 自动管理生命周期
 *  created by zhanghao
 */
class ZPermission private constructor(private val activity: AppCompatActivity) : Fragment() {
    private val requestCode = 9990
    private var permissions: Array<out String>? = null
    var refusePermissions: ArrayList<String> = arrayListOf()
    var allowPermissions: ArrayList<String> = arrayListOf()
    var allow = MutableLiveData<Boolean>()
    private val TAG = this::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View(context)
        view.setBackgroundColor(Color.TRANSPARENT)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allow.observe(this.activity, Observer {
            if (it) {
                allowListener.invoke()
            } else {
                refuseListener.invoke(refusePermissions)
            }
        })

        if (!permissions.isNullOrEmpty()) {
            requestPermissions(permissions!!, requestCode)
        }
    }

    override fun onDestroyView() {
        Log.w(TAG, "onDestroyView")
        super.onDestroyView()
    }

    companion object {
        private var zPermission: ZPermission? = null
        private lateinit var allowListener: () -> Unit
        private lateinit var refuseListener: (list: ArrayList<String>?) -> Unit

        fun get(activity: Activity): ZPermission? {
            if (zPermission == null) {
                if (activity is AppCompatActivity) {
                    zPermission = ZPermission(activity)
                    activity.supportFragmentManager.beginTransaction()
                        .add(zPermission!!, "permissionF").commit()
                } else {
                    Log.w("ZPermission", "警告：传入的Activity不是一个AppCompatActivity类，取消本次请求")
                    return null
                }
            }
            return zPermission
        }
    }


    /***
     * 请求权限
     */
    fun req(vararg permissions: String): ZPermission {
        this.permissions = permissions
        return this
    }

    fun listener(allow: () -> Unit, refuse: ((list: ArrayList<String>?) -> Unit)? = null) {
        allowListener = allow

        if (refuse!=null){
            refuseListener = refuse
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode) {
            for (i in permissions) {
                //如果授予权限
                if (ContextCompat.checkSelfPermission(
                        activity,
                        i
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    allowPermissions.add(i)
                } else {
                    refusePermissions.add(i)
                }
            }
            allow.value = permissions.size == allowPermissions.size

            activity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

}