package com.driverskr.lib.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.driverskr.lib.databinding.CustomToastBinding
import com.google.gson.Gson

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 13:36
 * @Description: $
 */
fun Context.toast(content: String) {
    showToast(this, content)
}

fun Fragment.toast(content: String) {
    showToast(requireContext(), content)
}

fun Fragment.toastCenter(content: String) {
    val context = requireContext()
    val toast = Toast(context)
    toast.duration = Toast.LENGTH_LONG
    val inflate = CustomToastBinding.inflate(layoutInflater)
    inflate.tvContent.text = content
    toast.view = inflate.root
    toast.setGravity(Gravity.CENTER,0,0)
    toast.show()
}

private fun showToast(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_LONG).show()
}

/**
 * @inline : 用于告诉编译器，在调用该函数时，将其内部的代码插入到调用处，而不是实际调用函数。这样可以减少函数调用的开销
 * @reified : 表明函数可以在运行时获取泛型的实际类型
 */
inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T: Activity> Activity.startActivity(pair: Pair<String, Int>) {
    val intent = Intent(this, T::class.java)
    intent.putExtra(pair.first, pair.second)
    startActivity(intent)
}

fun CharSequence?.notEmpty(): Boolean {
    return (this != null) && this.isNotEmpty()
}

inline fun <reified T : Any> String.fromJson(): T {
    return Gson().fromJson(this, T::class.java)
}