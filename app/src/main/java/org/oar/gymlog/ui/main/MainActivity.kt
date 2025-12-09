package org.oar.gymlog.ui.main

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.oar.gymlog.Init.FilePickerHandler
import org.oar.gymlog.Init.firstLoad
import org.oar.gymlog.Init.startUp
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityMainBinding
import org.oar.gymlog.ui.common.ResultLauncherAppCompatActivity
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation

class MainActivity : ResultLauncherAppCompatActivity() {

	private var ready = false
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.Theme_GymLog_NoActionBar)
		super.onCreate(savedInstanceState)

		val splashScreen = installSplashScreen()
		splashScreen.setKeepOnScreenCondition { !ready }

		// Launcher should be instantiated inside of "onCreate"
		val filePickerHandler = FilePickerHandler(this)

		startUp(lifecycleScope) { firstOpen ->
			ready = true
			if (firstOpen) {
				// Should not be executed when the splash screen is active, because the notification will not appear
				firstLoad(filePickerHandler) {
					init()
				}
			} else {
				init()
			}
		}
	}

	private fun init() {
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navController = findNavController(R.id.navHostFragmentActivityMain)
		NavigationUI.setupWithNavController(binding.navView, navController)

		val variationId = intent.extras?.getInt("variationId", -1) ?: -1
		if (variationId >= 0) {
			goToVariation(Data.getVariation(variationId))
		}
	}
}
