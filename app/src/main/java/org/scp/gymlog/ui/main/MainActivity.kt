package org.scp.gymlog.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.databinding.ActivityMainBinding
import org.scp.gymlog.util.Data

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (Data.exercises.isEmpty() || intent.extras?.getBoolean("loaded", false) != true) {
			val intent = Intent(this, SplashActivity::class.java)
			startActivity(intent)
			finish()
			return
		}

		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navController = Navigation.findNavController(this, R.id.navHostFragmentActivityMain)
		NavigationUI.setupWithNavController(binding.navView, navController)
	}
}
