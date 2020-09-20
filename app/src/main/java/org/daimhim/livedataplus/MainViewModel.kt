package org.daimhim.livedataplus

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel{
    var liveData = MutableLiveData<String>()

    fun upData(){
        GlobalScope.launch {
            delay(1000)

        }
    }
}