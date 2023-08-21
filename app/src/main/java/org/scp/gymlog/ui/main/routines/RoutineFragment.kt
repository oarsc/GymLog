package org.scp.gymlog.ui.main.routines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R

/**
 * A fragment representing a list of Items.
 */
class RoutineFragment : Fragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_list_routines, container, false)
		val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
		toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

		// Set the adapter
		if (view is RecyclerView) {
			val context = view.getContext()
			view.layoutManager = LinearLayoutManager(context)
			view.adapter = RoutineRecyclerViewAdapter()
		}
		return view
	}
}
