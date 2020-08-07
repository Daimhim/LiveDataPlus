package org.daimhim.livedataplus

import androidx.annotation.MainThread

class LiveDataPlus<T> {
    @Volatile
    private var mData: Any? = null


    @MainThread
    protected fun setValue(value: T) {
        CopyLiveData.assertMainThread("setValue")
        mData = value
        dispatchingValue(null)
    }
    private fun dispatchingValue(initiator: CopyLiveData.ObserverWrapper<T>?) {
        initiator?.let {
            considerNotify(it)
        }
    }

    private fun considerNotify(observer: CopyLiveData.ObserverWrapper<T>) {
        observer.mObserver.onChanged(mData as T)
    }

    public abstract class ObserverWrapperPlus<T>{

    }
}