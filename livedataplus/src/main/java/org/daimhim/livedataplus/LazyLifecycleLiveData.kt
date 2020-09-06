package org.daimhim.livedataplus

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

open class LazyLifecycleLiveData<T> : LiveDataPlus<T>() {

    @MainThread
    open fun observe(owner: LifecycleOwner, observer: Observer<in T>){
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val wrapper = LazyLifecycleBoundObserver<T>(owner, observer, this)
        @SuppressLint("RestrictedApi") val existing = mObservers.putIfAbsent(observer,wrapper)
        require(!(existing != null && !existing.isAttachedTo(owner))) {
            ("Cannot add the same observer"
                    + " with different lifecycles")
        }
        if (existing != null) {
            return
        }
        owner.lifecycle.addObserver(wrapper)
    }

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }

    protected open class LazyLifecycleBoundObserver<T>(
        owner: LifecycleOwner,
        observer: Observer<in T>,
        liveDataPlus: LazyLifecycleLiveData<T>
    ) : ObserverWrapper<T>(observer, liveDataPlus), LifecycleEventObserver {
        var mOwner: LifecycleOwner = owner
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (mOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                liveDataPlus.removeObserver(mObserver)
                return
            }
            activeStateChanged(shouldBeActive())
        }

        override fun activeStateChanged(newActive: Boolean) {
            if (newActive == mActive) {
                return
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive
            val wasInactive = liveDataPlus.mActiveCount == 0
            liveDataPlus.mActiveCount += if (mActive) 1 else -1
            if (wasInactive && mActive) {
                liveDataPlus.onActive()
            }
            if (liveDataPlus.mActiveCount == 0 && !mActive) {
                liveDataPlus.onInactive()
            }
        }

        override fun detachObserver() {
            mOwner.lifecycle.removeObserver(this)
        }

        override fun shouldBeActive(): Boolean {
            return mOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        override fun isAttachedTo(owner: LifecycleOwner?): Boolean {
            return mOwner == owner
        }
    }
}