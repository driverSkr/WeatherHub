package com.driverskr.lib.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Log
import kotlinx.coroutines.*

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 15:35
 * @Description: 自定义加载弹窗的图标$
 */
class LoadingDrawable(private val sun: Drawable, private val cloud: Drawable): Drawable(), Animatable {

    //用于管理用于动画的协程
    private var scope: CoroutineScope? = null

    //用于存储可绘制区域宽度的一半
    private var centerWidth = 0

    //用于追踪当前旋转角度
    private var currentAngel = 0f

    /**
     * 此方法在可绘制区域变化时被调用，用于基于新的可绘制区域更新 sun 和 cloud 的边界
     */
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        //计算绘制区域的中心宽度。这里通过取边界的差值并乘以 2/5 来确定中心宽度。
        centerWidth = (bounds.right - bounds.left) * 2 / 5

        //设置太阳 (sun) 对象的边界。setBounds 方法用于指定可绘制对象在画布上的绘制区域，
        // 这里将太阳绘制区域设置为一个以 (0, 0) 为中心的矩形
        sun.setBounds(-centerWidth, -centerWidth, centerWidth, centerWidth)

        //设置云 (cloud) 对象的边界。同样使用 setBounds 方法，将云的绘制区域设置为一个以 (0, 0) 为中心，宽度为 centerWidth * 3 / 2 的矩形。
        cloud.setBounds(-centerWidth, 0, centerWidth * 3 / 2, centerWidth * 3 / 2)
    }

    /**
     * 用于启动动画协程
     */
    private fun startAnim() {
        if (scope == null) {
            //使用 Job() 创建一个新的协程作业 (Job)。将该作业与 Dispatchers.Main 组合，表示在主线程上执行协程
            scope = CoroutineScope( Job() + Dispatchers.Main)
            scope?.launch {
                /**isActive 是协程内置的一个属性，表示协程当前是否处于活动状态**/
                while (isActive) {
                    //在默认的调度器 (Dispatchers.Default) 中暂停执行协程，相当于在后台线程执行。
                    withContext(Dispatchers.Default) {
                        /**指定了一个 20 毫秒的延迟，用于控制循环的速度**/
                        delay(20)
                    }
                    //用于更新旋转角度等动画状态
                    updatePosition()
                }
            }
        }
    }

    //用于更新旋转角度，每次增加 4 度。如果超过 360 度，重置为 0 度
    private fun updatePosition() {
        currentAngel += 4
        if (currentAngel > 360) {
            currentAngel = 0f
        }
        //调用 invalidateSelf() 触发重绘
        invalidateSelf()
    }

    /**
     * 用于实际绘制图形
     */
    override fun draw(canvas: Canvas) {
        //通过 translate 方法将画布平移到绘制区域的中心。这样，后续的绘制操作将以中心为原点。
        canvas.translate(centerWidth.toFloat(), centerWidth.toFloat())
        //使用 save 方法保存当前画布的状态。这样可以在后续的绘制操作中进行一些变换而不影响其他部分
        canvas.save()
        //使用 rotate 方法绕当前画布的原点旋转画布。currentAngel 表示旋转的角度，这个角度是动态变化的
        canvas.rotate(currentAngel)

        //将太阳绘制到画布上。注意，由于前面的平移和旋转操作，太阳会在经过这些变换后的位置绘制
        sun.draw(canvas)

        //使用 restore 方法恢复之前保存的画布状态。这样可以确保后续的绘制不会受到之前的变换的影响。
        canvas.restore()

        //将云绘制到画布上。同样，由于之前的平移和旋转操作，云会在经过这些变换后的位置绘制
        cloud.draw(canvas)
    }

    /**
     * 用于设置绘制的透明度
     * @param alpha : 范围是 0（完全透明）到 255（完全不透明）
     */
    override fun setAlpha(alpha: Int) {}

    /**
     * 用于设置颜色过滤器
     * @param colorFilter : 允许你使用不同的颜色过滤器来改变图形的颜色效果
     */
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    /**
     * 用于获取图形的不透明度
     * @return 表示图形的不透明度级别，可以是 PixelFormat 类中定义的常量之一
     *         (PixelFormat.OPAQUE, PixelFormat.TRANSLUCENT, PixelFormat.TRANSPARENT)
     */
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    fun setProgressRotation(rotation: Float) {
        currentAngel = rotation * 360
        invalidateSelf()
    }

    /**
     * 开始动画
     */
    override fun start() {
        Log.d(TAG,"start() ------------------->")
        startAnim()
    }

    /**
     * 停止动画
     */
    override fun stop() {
        scope?.cancel()
        scope = null
        Log.d(TAG,"ICancelable cancel --------------------------->")
    }

    /**
     * 动画是否在运行
     */
    override fun isRunning(): Boolean {
        //如果 scope 不为 null，则执行 let 块
        scope?.let {
            return it.isActive
        //如果 scope 为 null，执行 run 块。run 函数用于在对象上执行一组操作
        } ?: run {
            return false
        }
    }

    companion object {
        private val TAG = LoadingDrawable::class.simpleName
    }
}