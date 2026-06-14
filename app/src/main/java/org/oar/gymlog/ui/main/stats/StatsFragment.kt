package org.oar.gymlog.ui.main.stats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.FragmentStatsBinding
import org.oar.gymlog.ui.LoadActivity
import org.oar.gymlog.ui.common.ResultLauncherFragment

class StatsFragment : ResultLauncherFragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentStatsBinding.inflate(inflater, container, false)
		.apply {
			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			toolbar.setOnMenuItemClickListener { item: MenuItem ->
				when (item.itemId) {
					R.id.testButton -> {
						requireActivity().apply {
							val intent = Intent(this, LoadActivity::class.java)
							intent.action = "keep"
							startActivity(intent)
						}
//                        val seconds = 10
//                        val date = NOW.plusSeconds(seconds.toLong())
//                        NotificationService(requireContext())
//                            .startNewNotification(date, seconds, Data.exercises[0].variations[0])
					}
				}
				false
			}
		}
		.root
}
