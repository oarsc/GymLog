package org.scp.gymlog.ui.main.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

	private static final int[] COLORS = new int[] {
			Color.rgb(150,117,206), //pectoral
			Color.rgb(16 ,115,174), //upper back
			Color.rgb(77 ,208,226), //lower back
			Color.rgb(224,94 ,85 ), //deltoid
			Color.rgb(240,98 ,146), //trapezius
			Color.rgb(115,183,122), //biceps
			Color.rgb(23 ,208,46 ), //triceps
			Color.rgb(1  ,191,165), //forearm
			Color.rgb(255,211,63 ), //quadriceps
			Color.rgb(249,168,37 ), //hamstrings
			Color.rgb(188,154,20 ), //calves
			Color.rgb(249,140,37 ), //glutes
			Color.rgb(240,98 ,146), //abdominals
			Color.rgb(170,170,170), //cardio
	};

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_history, container, false);

		calendarView = view.findViewById(R.id.calendarView);
		calendarView.setOnChangeListener(this::updateData);
		return view;
	}

	private void updateData(Calendar first, Calendar end) {
		List<Muscle> allMuscles = Data.getInstance().getMuscles();
		DBThread.run(getContext(), db -> {

			List<BitEntity> bits = db.bitDao().getHistory(first, end);

			getActivity().runOnUiThread(() -> {
				while (first.compareTo(end) < 0) {
					Map<Muscle, int[]> summary = allMuscles.stream()
							.collect(Collectors.toMap(muscle->muscle, s->new int[]{0}));

					bits.stream()
							.filter(bit -> first.compareTo(DateUtils.getFirstTimeOfDay(bit.timestamp)) == 0)
							.map(bit -> Data.getExercise(bit.exerciseId))
							.flatMap(exercise -> exercise.getBelongingMuscles().stream())
							.map(summary::get)
							.forEach(i -> i[0]++);

					List<PieDataInfo> data = summary.entrySet().stream()
							.filter(entry -> entry.getValue()[0] > 0)
							.sorted(Comparator.comparing(entry -> entry.getValue()[0]))
							.map(entry -> {
								PieDataInfo dataInfo = new PieDataInfo();
								dataInfo.setValue(entry.getValue()[0]);
								dataInfo.setColor(COLORS[entry.getKey().getId()-1]);
								return dataInfo;
							})
							.collect(Collectors.toList());

					calendarView.setData(first.getTimeInMillis(), data);
					first.add(Calendar.DAY_OF_YEAR, 1);
				}
			});
		});
	}
}