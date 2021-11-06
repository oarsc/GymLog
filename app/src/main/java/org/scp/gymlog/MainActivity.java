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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		final int[] texts = new int[]{
				R.string.group_pectoral,
				R.string.group_upper_back,
				R.string.group_lower_back,
				R.string.group_deltoid,
				R.string.group_trapezius,
				R.string.group_biceps,
				R.string.group_triceps,
				R.string.group_forearm,
				R.string.group_quadriceps,
				R.string.group_hamstrings,
				R.string.group_glutes,
				R.string.group_calves,
				R.string.group_abdominals,
				R.string.group_cardio
		};

		groups.clear();
		IntStream.range(0, texts.length)
				.mapToObj(idx -> new MuscularGroup(idx, texts[idx]))
				.forEach(groups::add);
	}
}