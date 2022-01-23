package org.scp.gymlog.ui.main.history;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.ui.common.components.HistoryCalendarView;
import org.scp.gymlog.ui.common.components.HistoryCalendarView.PieDataInfo;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

	private HistoryCalendarView calendarView;
	private HistoryRecyclerViewAdapter historyAdapter;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_history, container, false);

		final RecyclerView legendRecyclerView = view.findViewById(R.id.legend);
		legendRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2) {
			@Override
			public boolean canScrollVertically() { return false; }
		});
		legendRecyclerView.setAdapter(new HistoryLegendRecyclerViewAdapter());

		final ImageView showLegendIcon = view.findViewById(R.id.showLegendIcon);
		view.findViewById(R.id.showLegend).setOnClickListener(v -> {
			if (legendRecyclerView.getVisibility() == View.VISIBLE) {
				legendRecyclerView.setVisibility(View.GONE);
				showLegendIcon.animate().rotation(0f).start();
			} else {
				legendRecyclerView.setVisibility(View.VISIBLE);
				showLegendIcon.animate().rotation(180f).start();
			}
		});

		final RecyclerView trainingRecyclerView = view.findViewById(R.id.trainingList);
		trainingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
			@Override
			public boolean canScrollVertically() { return false; }
		});
		trainingRecyclerView.setAdapter(historyAdapter = new HistoryRecyclerViewAdapter());

		calendarView = view.findViewById(R.id.calendarView);
		calendarView.setOnSelectDayListener(this::selectDay);
		calendarView.setOnChangeListener(this::updateData);

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
		return view;
	}

	private void selectDay(Calendar startDate) {
		Calendar endDate = (Calendar) startDate.clone();
		endDate.add(Calendar.DAY_OF_YEAR, 1);

		DBThread.run(getContext(), db -> {
			List<TrainingEntity> trainings =
					db.trainingDao().getTrainingByStartDate(startDate, endDate);

			int initialSize = historyAdapter.size();
			int endSize = trainings.size();
			historyAdapter.clear();

			trainings.forEach(training -> {
				List<BitEntity> bits = db.bitDao().getHistoryByTrainingId(training.trainingId);;
				TrainingData td = getTrainingData(training, bits);
				historyAdapter.add(td);
			});

			getActivity().runOnUiThread(() -> historyAdapter.notifyItemsChanged(initialSize, endSize));
		});
	}

	private void updateData(Calendar first, Calendar end) {
		final Resources resources = getResources();
		List<Muscle> allMuscles = Data.getInstance().getMuscles();
		DBThread.run(getContext(), db -> {
			List<BitEntity> bits = db.bitDao().getHistory(first, end);

			int i = 0;
			while (first.compareTo(end) < 0) {
				Map<Muscle, float[]> summary = allMuscles.stream()
						.collect(Collectors.toMap(muscle->muscle, s->new float[]{0}));

				for (; i<bits.size(); i++) {
					BitEntity bit = bits.get(i);

					if (first.compareTo(DateUtils.getFirstTimeOfDay(bit.timestamp)) == 0) {
						Exercise exercise = Data.getExercise(bit.exerciseId);

						int secondariesCount = exercise.getSecondaryMuscles().size();
						float primaryShare = secondariesCount>0? 7 : 9;
						float secondaryShare = secondariesCount>0? 3f/secondariesCount : 0;

						exercise.getPrimaryMuscles().stream()
								.map(summary::get)
								.forEach(f -> f[0]+=primaryShare);

						exercise.getSecondaryMuscles().stream()
								.map(summary::get)
								.forEach(f -> f[0]+=secondaryShare);
					} else break;
				}

				List<PieDataInfo> data = summary.entrySet().stream()
						.filter(entry -> entry.getValue()[0] > 0)
						.sorted(Comparator.comparing(entry -> entry.getValue()[0]))
						.map(entry -> {
							PieDataInfo dataInfo = new PieDataInfo();
							dataInfo.setValue(entry.getValue()[0]);
							dataInfo.setColor(
								ResourcesCompat.getColor(resources, entry.getKey().getColor(), null)
							);
							return dataInfo;
						})
						.collect(Collectors.toList());

				if (!data.isEmpty()) {
					long millis = first.getTimeInMillis();
					getActivity().runOnUiThread(() -> calendarView.setDayData(millis, data));
				}
				first.add(Calendar.DAY_OF_YEAR, 1);
			}

			getActivity().runOnUiThread(() -> calendarView.setEnabled(true));
		});
	}


	public static TrainingData getTrainingData(TrainingEntity training, List<BitEntity> bits) {
		List<MuscleCount> musclesCount = new ArrayList<>();

		bits.stream().map(bit -> bit.exerciseId)
				.map(Data::getExercise)
				.flatMap(exercise -> exercise.getPrimaryMuscles().stream())
				.forEach(muscle -> {
					Optional<MuscleCount> m = musclesCount.stream()
							.filter(mc -> mc.muscle == muscle)
							.findFirst();

					if (m.isPresent()) {
						m.get().count++;
					} else {
						musclesCount.add(new MuscleCount(muscle));
					}
				});

		musclesCount.sort((a,b) -> Integer.compare(b.count, a.count));

		int total = musclesCount.stream()
				.map(mc -> mc.count)
				.reduce(0, Integer::sum);
		int limit = (int)(musclesCount.get(0).count / (float)total - 7.5f);

		List<Muscle> mostUsedMuscles = musclesCount.stream()
				.filter(a -> a.count/total > limit)
				.map(a -> a.muscle)
				.collect(Collectors.toList());

		TrainingData td = new TrainingData();
		td.setId(training.trainingId);
		td.setStartDate(training.start);
		td.setMostUsedMuscles(mostUsedMuscles);

		return td;
	}

	static class MuscleCount {
		private final Muscle muscle;
		private int count = 1;

		public MuscleCount(Muscle muscle) {
			this.muscle = muscle;
		}

		@Override
		public boolean equals(Object o) {
			return muscle == ((MuscleCount) o).muscle;
		}
	}
}
