package org.scp.gymlog;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.scp.gymlog.databinding.ActivityMainBinding;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialLoading();

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_workout,
				R.id.navigation_history,
				R.id.navigation_exercises
			).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
		NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.navView, navController);
	}

	private void initialLoading() {
		List<MuscularGroup> groups = Data.getInstance().getGroups();

		groups.clear();
		int id = 0;
		Arrays.asList(
				new MuscularGroup(++id, R.string.group_pectoral, R.drawable.muscle_pectoral),
				new MuscularGroup(++id, R.string.group_upper_back, R.drawable.muscle_upper_back),
				new MuscularGroup(++id, R.string.group_lower_back, R.drawable.muscle_lower_back),
				new MuscularGroup(++id, R.string.group_deltoid, R.drawable.muscle_deltoid),
				new MuscularGroup(++id, R.string.group_trapezius, R.drawable.muscle_trapezius),
				new MuscularGroup(++id, R.string.group_biceps, R.drawable.muscle_biceps),
				new MuscularGroup(++id, R.string.group_triceps, R.drawable.muscle_triceps),
				new MuscularGroup(++id, R.string.group_forearm, R.drawable.muscle_forearm),
				new MuscularGroup(++id, R.string.group_quadriceps, R.drawable.muscle_quadriceps),
				new MuscularGroup(++id, R.string.group_hamstrings, R.drawable.muscle_hamstring),
				new MuscularGroup(++id, R.string.group_calves, R.drawable.muscle_calves),
				new MuscularGroup(++id, R.string.group_glutes, R.drawable.muscle_glutes),
				new MuscularGroup(++id, R.string.group_abdominals, R.drawable.muscle_abdominals),
				new MuscularGroup(++id, R.string.group_cardio, R.drawable.muscle_cardio)
		).forEach(groups::add);
	}
}