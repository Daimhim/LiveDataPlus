package org.daimhim.livedataplus

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.arch.core.executor.ArchTaskExecutor

class LiveData<T> {
    @Volatile
    private var mData: Any? = null


//    @MainThread
//    protected fun setValue(value: T) {
//        CopyLiveData.assertMainThread("setValue")
//        mVersion++
//        mData = value
//        dispatchingValue(null)
//    }

}