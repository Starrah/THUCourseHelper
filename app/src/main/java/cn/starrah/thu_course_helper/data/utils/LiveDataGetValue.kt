package cn.starrah.thu_course_helper.data.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 返回一个[LiveData]的非null的value。
 *
 * **严禁在主线程中以`runBlocking`方式调用本函数！否则主线程将永远阻塞。请务必以`launch`方式调用本函数。**
 *
 * Note: 该函数会检查这个[LiveData]当前是否为null，不为null则直接返回；否则，则监听该[LiveData]并挂起本函数，
 * 直到接受到不为null的数据后继续函数、返回value值。
 *
 * @return 该[LiveData]的value属性值，但是只有在value已经是或变为了非null值的情况下才返回。
 */
suspend fun <T> LiveData<T>.getNotNullValue(): T {
    var v: T? = this.value
    if (v != null) return v
    v = suspendCancellableCoroutine<T> { cont ->
        val observer = object: Observer<T> {
            override fun onChanged(t: T) {
                if (t != null){
                    this@getNotNullValue.removeObserver(this)
                    cont.resume(t)
                }
            }
        }
        this.observeForever(observer)
    }
    return v
}