package org.scp.gymlog.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (intent.extras?.getBoolean("loaded", false) != true) {
			val intent = Intent(this, SplashActivity::class.java)
			startActivity(intent)
			finish()
			return
		}

		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navView = findViewById<BottomNavigationView>(R.id.navView)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration.Builder(
			R.id.navigationWorkout,
			R.id.navigationExercises,
			R.id.navigationHistory,
			R.id.navigationPreferences
		).build()
		val navController = Navigation.findNavController(this, R.id.navHostFragmentActivityMain)
		//NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.navView, navController)
	}
}
