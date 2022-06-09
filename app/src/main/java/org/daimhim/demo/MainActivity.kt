package org.daimhim.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.daimhim.livedataplus.LazyLifecycleLiveData

class MainActivity : AppCompatActivity() {
    val lazyLifecycleLiveData = LazyLifecycleLiveData<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val value = Observer<String> {  }
        lazyLifecycleLiveData.observe(this, observer = value)
        val findViewById = findViewById<TextView>(R.id.tv_content)
        findViewById.setOnClickListener {
            findViewById.text = "${lazyLifecycleLiveData.hasObservers(observer = value)}"
        }
    }
}