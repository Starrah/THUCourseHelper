package cn.starrah.thu_course_helper

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView


/*
可以绑定的scrollview,用于同步左右的滑动

 */
class BindableScrollView : HorizontalScrollView {
    private var bindedScrollView: BindableScrollView? = null

    constructor(context: Context?) : super(context) {}
    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    fun bindView(bindedScrollView: BindableScrollView?) {
        this.bindedScrollView = bindedScrollView
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        if (bindedScrollView != null) {
            bindedScrollView!!.scrollTo(x,y);
        }
    }
}
