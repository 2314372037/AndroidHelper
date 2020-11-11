package com.xx.xx.ext

import android.graphics.Color
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.xx.xx.R

/***
 * created by zhanghao
 * email zh2314372037@outlook.com
 * @time 2020/9/1 16:02
 * @desc 这个是fragment状态view帮助类，主要用于单activity多fragment，可以显示和切换各种fragment页面状态
 * @desc 注意：需要操作的viewGroup，tag = "statusView"，推荐父布局为FrameLayout
 */
class ViewHelper {
    enum class CURRENT_STATUS {
        NORMAL,//正常状态
        EMPTY,//空状态
        LOADING,//加载中
        ERROR,//加载失败
        INIT//初始化hide视图
    }

    companion object {
        var currentStatus = CURRENT_STATUS.NORMAL
        val VIEW_TAG = "statusView"//要替换(操作)的view tag
        val STATUS_VIEW_TAG = "zh2314372037"//状态view的view tag
        val childViewsVisibility = SparseArray<Int>()//保存contentView的子view原本的显示状态
    }
}

/***
 * 没有数据时
 * @param resId imageView资源id
 * @param text 文本
 */
fun Fragment.showEmpty(
    resId: Int = R.drawable.pic_empty,
    text: String = "没有数据",
    backgroundColor: Int = Color.TRANSPARENT
) {
    val contentView = view?.findViewWithTag<View>(ViewHelper.VIEW_TAG)
    if (contentView is ViewGroup && contentView !is ScrollView) {//这里排除不可操作的view
        if (contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG) != null) {//如果存在其他状态view，则删除，准备显示当前状态
            contentView.removeView(contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG))
        }

        val childCount = contentView.childCount
        for (i in 0 until childCount) {
            val tmpView = contentView.getChildAt(i)
            if (ViewHelper.childViewsVisibility.size() < childCount) {//如果每个子view没有保存状态
                ViewHelper.childViewsVisibility.put(i,tmpView.visibility)//保存contentView的子view原本的显示状态
            }
            tmpView.visibility = View.INVISIBLE//保存子view状态后，全部隐藏掉
        }

        val imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(400, 400)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        imageView.setImageResource(resId)

        val textView = TextView(requireContext())
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setText(text)
        textView.setTextColor(Color.GRAY)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)

        val linearLayout = LinearLayout(requireContext())
        linearLayout.setBackgroundColor(backgroundColor)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
        linearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL)
        linearLayout.gravity = Gravity.CENTER
        linearLayout.addView(imageView)
        linearLayout.addView(textView)
        linearLayout.tag = ViewHelper.STATUS_VIEW_TAG

        contentView.addView(linearLayout)
        ViewHelper.currentStatus = ViewHelper.CURRENT_STATUS.EMPTY
    } else {
        Log.d("zzz调试", "无法切换状态到${ViewHelper.currentStatus.name}，tag:${ViewHelper.VIEW_TAG}")
    }
}

/***
 * 加载中
 * @param resId imageView资源id
 * @param text 文本
 */
fun Fragment.showLoading(
    resId: Int = R.drawable.pic_empty,
    text: String = "加载中",
    backgroundColor: Int = Color.TRANSPARENT
) {
    val contentView = view?.findViewWithTag<View>(ViewHelper.VIEW_TAG)
    if (contentView is ViewGroup && contentView !is ScrollView) {//这里排除不可操作的view
        if (contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG) != null) {//如果存在其他状态view，则删除，准备显示当前状态
            contentView.removeView(contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG))
        }

        val childCount = contentView.childCount
        for (i in 0 until childCount) {
            val tmpView = contentView.getChildAt(i)
            if (ViewHelper.childViewsVisibility.size() < childCount) {//如果每个子view没有保存状态
                ViewHelper.childViewsVisibility.put(i,tmpView.visibility)//保存contentView的子view原本的显示状态
            }
            tmpView.visibility = View.INVISIBLE//保存子view状态后，全部隐藏掉
        }

//        val imageView = ImageView(requireContext())
//        imageView.layoutParams = ViewGroup.LayoutParams(200, 200)
//        imageView.scaleType = ImageView.ScaleType.FIT_XY
//        imageView.setImageResource(resId)
        val progressBar = ProgressBar(requireContext())
        progressBar.layoutParams = ViewGroup.LayoutParams(200, 200)

        val textView = TextView(requireContext())
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setText(text)
        textView.setTextColor(Color.GRAY)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)

        val linearLayout = LinearLayout(requireContext())
        linearLayout.setBackgroundColor(backgroundColor)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
        linearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL)
        linearLayout.gravity = Gravity.CENTER
        linearLayout.addView(progressBar)
        linearLayout.addView(textView)
        linearLayout.tag = ViewHelper.STATUS_VIEW_TAG

        contentView.addView(linearLayout)
        ViewHelper.currentStatus = ViewHelper.CURRENT_STATUS.LOADING
    } else {
        Log.d("zzz调试", "无法切换状态到${ViewHelper.currentStatus.name}，tag:${ViewHelper.VIEW_TAG}")
    }
}

/***
 * 加载错误
 * @param reTry 点击重试时的监听
 * @param resId imageView资源id
 * @param text 文本
 */
fun Fragment.showError(
    reTry: (View) -> Unit,
    resId: Int = R.drawable.pic_empty,
    text: String = "加载失败，点击重试",
    backgroundColor: Int = Color.TRANSPARENT
) {
    val contentView = view?.findViewWithTag<View>(ViewHelper.VIEW_TAG)
    if (contentView is ViewGroup && contentView !is ScrollView) {//这里排除不可操作的view
        if (contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG) != null) {//如果存在其他状态view，则删除，准备显示当前状态
            contentView.removeView(contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG))
        }

        val childCount = contentView.childCount
        for (i in 0 until childCount) {
            val tmpView = contentView.getChildAt(i)
            if (ViewHelper.childViewsVisibility.size() < childCount) {//如果每个子view没有保存状态
                ViewHelper.childViewsVisibility.put(i,tmpView.visibility)//保存contentView的子view原本的显示状态
            }
            tmpView.visibility = View.INVISIBLE//保存子view状态后，全部隐藏掉
        }
        val imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(400, 400)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        imageView.setImageResource(resId)

        val textView = TextView(requireContext())
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setText(text)
        textView.setTextColor(Color.GRAY)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)

        val linearLayout = LinearLayout(requireContext())
        linearLayout.setBackgroundColor(backgroundColor)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
        linearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL)
        linearLayout.gravity = Gravity.CENTER
        linearLayout.addView(imageView)
        linearLayout.addView(textView)
        linearLayout.tag = ViewHelper.STATUS_VIEW_TAG
        linearLayout.setOnClickListener {
            reTry.invoke(it)
        }

        contentView.addView(linearLayout)
        ViewHelper.currentStatus = ViewHelper.CURRENT_STATUS.ERROR
    } else {
        Log.d("zzz调试", "无法切换状态到${ViewHelper.currentStatus.name}，tag:${ViewHelper.VIEW_TAG}")
    }
}

/***
 * 显示正常的页面
 */
fun Fragment.showSuccess() {
    if (ViewHelper.currentStatus != ViewHelper.CURRENT_STATUS.NORMAL) {
        val contentView = view?.findViewWithTag<View>(ViewHelper.VIEW_TAG)
        if (contentView is ViewGroup && contentView !is ScrollView) {//这里排除不可操作的view
            if (contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG) != null) {//如果存在其他状态view，则删除，准备显示当前状态
                contentView.removeView(contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG))
            }

            val childCount = contentView.childCount
            for (i in 0 until childCount) {
                val tmpView = contentView.getChildAt(i)
                tmpView.visibility = ViewHelper.childViewsVisibility.get(i)//获取保存的子view状态，还原原状态
            }
            ViewHelper.childViewsVisibility.clear()
            ViewHelper.currentStatus = ViewHelper.CURRENT_STATUS.NORMAL
        } else {
            Log.d("zzz调试", "无法切换状态到${ViewHelper.currentStatus.name}，tag:${ViewHelper.VIEW_TAG}")
        }
    }
}

/***
 * 初始化指定页面(android:tag标记)为hide状态，避免某些情况下的闪屏
 * 注意：在BaseFragment onCreateView()方法处调用
 * 例如return inflater.inflate(layoutId(), container, false)，改为return initStatusView(inflater.inflate(layoutId(), container, false))
 */
fun Fragment.initStatusView(view: View):View{
    val contentView = view.findViewWithTag<View>(ViewHelper.VIEW_TAG)
    if (contentView is ViewGroup && contentView !is ScrollView) {//这里排除不可操作的view
        if (contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG) != null) {//如果存在其他状态view，则删除，准备显示当前状态
            contentView.removeView(contentView.findViewWithTag<ViewGroup>(ViewHelper.STATUS_VIEW_TAG))
        }

        val childCount = contentView.childCount
        for (i in 0 until childCount) {
            val tmpView = contentView.getChildAt(i)
            if (ViewHelper.childViewsVisibility.size() < childCount) {//如果每个子view没有保存状态
                ViewHelper.childViewsVisibility.put(i,tmpView.visibility)//保存contentView的子view原本的显示状态
            }
            tmpView.visibility = View.INVISIBLE//保存子view状态后，全部隐藏掉
        }
        ViewHelper.currentStatus = ViewHelper.CURRENT_STATUS.INIT
    }
    return view
}
