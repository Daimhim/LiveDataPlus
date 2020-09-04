package org.daimhim.livedataplus

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.internal.SafeIterableMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

abstract class LiveDataPlus<T> {
    companion object {
        const val START_VERSION = -1
        val NOT_SET = Any()
    }
    val mDataLock = Any()
    var mActiveCount = 0
    @SuppressLint("RestrictedApi")
    protected val mObservers = SafeIterableMap<Observer<in T>,ObserverWrapper<T>>()

    @Volatile
    private var mData: Any? = null

    @Volatile
    var mPendingData = NOT_SET
    private var mVersion = 0

    private var mDispatchingValue = false
    private var mDispatchInvalidated = false
    private val mPostValueRunnable = Runnable {
        var newValue: Any
        synchronized(mDataLock) {
            newValue = mPendingData
            mPendingData = NOT_SET
        }
        setValue(newValue as T)
    }

    constructor(value:T){
        mData = value
        mVersion = START_VERSION + 1
    }
    constructor(){
        mData = NOT_SET;
        mVersion = START_VERSION;
    }

    private fun considerNotify(observer: ObserverWrapper<T>) {
        if (!observer.mActive) {
            return
        }
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        if (observer.mLastVersion >= mVersion) {
            return
        }
        observer.mLastVersion = mVersion
        observer.mObserver.onChanged(mData as T)
    }


    fun dispatchingValue(initiator: ObserverWrapper<T>?) {
        if (mDispatchingValue) {
            mDispatchInvalidated = true
            return
        }
        mDispatchingValue = true
        do {
            mDispatchInvalidated = false
            if (initiator != null) {
                considerNotify(initiator)
            } else {
                @SuppressLint("RestrictedApi")
                val iterator:Iterator<Map.Entry<Observer<in T>,ObserverWrapper<T>>> = mObservers.iteratorWithAdditions()
                while (iterator.hasNext()) {
                    considerNotify(iterator.next().value)
                    if (mDispatchInvalidated) {
                        break
                    }
                }
            }
        } while (mDispatchInvalidated)
        mDispatchingValue = false
    }

    @MainThread
    open fun observe(owner:LifecycleOwner,observer:Observer<in T>){
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
    }
    @MainThread
    fun observeForever(observer: Observer<in T>) {
    }


    @MainThread
    fun removeObserver(observer: Observer<in T>) {
        @SuppressLint("RestrictedApi")
        val removed = mObservers.remove(observer) ?: return
        removed.detachObserver()
        removed.activeStateChanged(false)
    }

    @MainThread
    fun removeObservers(owner:LifecycleOwner){
        for ((key, value) in mObservers) {
            if (value.isAttachedTo(owner)) {
                removeObserver(key!!)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    protected fun postValue(value: T) {
        var postTask: Boolean
        synchronized(mDataLock) {
            postTask = mPendingData === NOT_SET
            mPendingData = value as Any
        }
        if (!postTask) {
            return
        }
        ArchTaskExecutor.getInstance().postToMainThread(mPostValueRunnable)
    }

    @MainThread
    protected fun setValue(value:T){
        mVersion++
        mData = value
        dispatchingValue(null)
    }

    fun getValue(): T? {
        val data = mData
        return if (data !== NOT_SET && data != null) {
            data as T
        } else null
    }

    fun getVersion(): Int {
        return mVersion
    }
    protected fun onActive() {}
    protected fun onInactive() {}

    @SuppressLint("RestrictedApi")
    fun hasObservers(): Boolean {
        return mObservers.size() > 0
    }

    fun hasActiveObservers(): Boolean {
        return mActiveCount > 0
    }
    abstract class ObserverWrapper<T> {
        val mObserver: Observer<in T>
        var mActive = false
        var mLastVersion = LiveDataPlus.START_VERSION;
        protected var mLiveDataPlus:LiveDataPlus<T>;
        constructor(observer:Observer<T>, liveDataPlus:LiveDataPlus<T>){
            mObserver = observer;
            mLiveDataPlus = liveDataPlus;
        }

        /**
         * 是否活跃
         */
        abstract fun shouldBeActive(): Boolean

        /**
         * 更新
         * newActive true 为最新数据
         * false 为老数据版本同步
         */
        fun activeStateChanged(newActive: Boolean){
            if (newActive == mActive) {
                return
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive
            val wasInactive = mLiveDataPlus.mActiveCount == 0
            mLiveDataPlus.mActiveCount += if (mActive) 1 else -1
            if (wasInactive && mActive) {
                mLiveDataPlus.onActive()
            }
            if (mLiveDataPlus.mActiveCount == 0 && !mActive) {
                mLiveDataPlus.onInactive()
            }
            if (mActive) {
                mLiveDataPlus.dispatchingValue(this)
            }
        }

        /**
         * 断开监听
         */
        fun detachObserver(){

        }

        open fun isAttachedTo(owner: LifecycleOwner?): Boolean {
            return false
        }
    }



}