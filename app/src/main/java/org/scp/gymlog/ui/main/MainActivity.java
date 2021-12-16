package org.scp.gymlog.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.scp.gymlog.R;
import org.scp.gymlog.SplashActivity;
import org.scp.gymlog.databinding.ActivityMainBinding;
import org.scp.gymlog.util.Data;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Data.getInstance().getMuscles().isEmpty()) {
			Intent intent = new Intent(this, SplashActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_workout,
				R.id.navigation_exercises,
				R.id.navigation_history,
				R.id.navigation_preferences
			).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
		//NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.navView, navController);
	}
}