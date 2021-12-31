package org.scp.gymlog.ui.main.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.scp.gymlog.R;

public class HistoryFragment extends Fragment {



	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_history, container, false);

		//CustomCalendarView cc = view.findViewById(R.id.calendarView);


		return view;
	}

}