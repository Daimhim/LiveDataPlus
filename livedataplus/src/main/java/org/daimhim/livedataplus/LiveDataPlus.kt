package org.daimhim.livedataplus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class LiveDataPlus<T> {


    fun observe(
        owner: LifecycleOwner,
        observer: Observer<T>
    ){

    }

    abstract class ObserverWrapper<T> {
        /**
         * 是否活跃
         */
        abstract fun shouldBeActive(): Boolean

        /**
         * 更新
         * newActive true 为最新数据
         * false 为老数据版本同步
         */
        abstract fun activeStateChanged(newActive: Boolean)

        /**
         * 断开监听
         */
        abstract fun detachObserver()

        abstract fun isAttachedTo(owner: LifecycleOwner?): Boolean
    }

}