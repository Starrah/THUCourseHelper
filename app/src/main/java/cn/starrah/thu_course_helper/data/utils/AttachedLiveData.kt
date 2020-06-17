package cn.starrah.thu_course_helper.data.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class AttachedLiveData<T, P>(
    /**
     * 要监视的主要的LiveData数据。
     */
    private val mainLiveData: LiveData<P>,

    /**
     * 如果被监视的LiveData的数据类型和本LiveData提供的数据类型不同，则必须提供一个函数以用于数据类型的转换。
     *
     * 函数接受两个参数，第一个为P类型的、[mainLiveData]提供的新数据；第二个为T?类型的、表示之前的[value]的值。
     * 函数应当返回一个T类型的数据，将被本[AttachedLiveData]作为新的value，分发给监听者。
     */
    private val transformFunc: (P, T?) -> T = { a, _ -> a as T}
): LiveData<T>() {
    private var mainData: T? = mainLiveData.value?.let{ transformFunc(it, null) }

//    init {
//        value = mainData
//    }

    private val observer = Observer<P> { t ->
        mainData = transformFunc(t, mainData)
        value = mainData
    }

    override fun onActive() {
        super.onActive()
        mainLiveData.observeForever(observer)
    }

    override fun onInactive() {
        super.onInactive()
        mainLiveData.removeObserver(observer)
    }
}

class AttachedListedLiveData<T, P>(
    /**
     * 要监视的主要的LiveData数据。
     */
    mainLiveData: LiveData<List<P>>,

    /**
     * 如果被监视的LiveData的数据类型和本LiveData提供的数据类型不同，则必须提供一个函数以用于数据类型的转换。
     *
     * 函数接受两个参数，第一个为P类型的、[mainLiveData]提供的新数据；第二个为T?类型的、表示之前的[value]的值。
     * 函数应当返回一个T类型的数据，将被本[AttachedLiveData]作为新的value，分发给监听者。
     */
    transformFunc: (P, T?) -> T = { a, _ -> a as T},

    /**
     * 可以提供函数用于判断两个元素的相等性，以便于转换时重用[transformFunc]的old参数。若不提供此参数，
     * 则调用[transformFunc]的old参数总为null。
     */
    equalFunc: ((P, T) -> Boolean)? = null
): AttachedLiveData<List<T>, List<P>>(mainLiveData, { listP, listT ->
    listP.map { p -> transformFunc(p, equalFunc?.let { listT?.find { equalFunc(p, it) } }) }
})