package org.scp.gymlog.ui.main.history;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.ui.common.components.HistoryCalendarView;
import org.scp.gymlog.ui.common.components.HistoryCalendarView.PieDataInfo;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.DateUtils;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

	private HistoryCalendarView calendarView;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_history, container, false);

		calendarView = view.findViewById(R.id.calendarView);
		calendarView.setOnSelectDayListener(this::selectDay);
		calendarView.setOnChangeListener(this::updateData);
		return view;
	}

	private void selectDay(Calendar calendar) {

	}

	private void updateData(Calendar first, Calendar end) {
		final Resources resources = getResources();
		List<Muscle> allMuscles = Data.getInstance().getMuscles();
		DBThread.run(getContext(), db -> {

			List<BitEntity> bits = db.bitDao().getHistory(first, end);

			getActivity().runOnUiThread(() -> {
				while (first.compareTo(end) < 0) {
					Map<Muscle, float[]> summary = allMuscles.stream()
							.collect(Collectors.toMap(muscle->muscle, s->new float[]{0}));

					bits.stream()
							.filter(bit -> first.compareTo(DateUtils.getFirstTimeOfDay(bit.timestamp)) == 0)
							.map(bit -> Data.getExercise(bit.exerciseId))
							.forEach(exercise -> {
								int secondariesCount = exercise.getSecondaryMuscles().size();
								float primaryShare = secondariesCount>0? 7f : 9;
								float secondaryShare = secondariesCount>0? 3f/secondariesCount : 0;

								exercise.getPrimaryMuscles().stream()
										.map(summary::get)
										.forEach(i -> i[0]+=primaryShare);

								exercise.getSecondaryMuscles().stream()
										.map(summary::get)
										.forEach(i -> i[0]+=secondaryShare);
							});

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
						calendarView.setDayData(first.getTimeInMillis(), data);
					}
					first.add(Calendar.DAY_OF_YEAR, 1);
				}
				calendarView.setEnabled(true);
			});
		});
	}
}