package org.oar.gymlog.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

abstract class BindingAppCompatActivity<T: ViewBinding>(
    private val inflater: (LayoutInflater) -> T
) : ResultLauncherAppCompatActivity() {
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflater(layoutInflater)
        setContentView(binding.root)
    }
}