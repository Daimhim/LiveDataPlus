package org.daimhim.livedataplus

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class LifecycleLiveData<T>:LazyLifecycleLiveData<T>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val wrapper = LifecycleBoundObserver<T>(owner, observer, this)
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

    private class LifecycleBoundObserver<T>(
        owner: LifecycleOwner,
        observer: Observer<in T>,
        liveDataPlus: LazyLifecycleLiveData<T>
    ) : LazyLifecycleBoundObserver<T>(owner,observer, liveDataPlus){
        override fun activeStateChanged(newActive: Boolean) {
            super.activeStateChanged(newActive)
            if (mActive) {
                liveDataPlus.dispatchingValue(this)
            }
        }
    }
}