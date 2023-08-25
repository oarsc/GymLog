package org.scp.gymlog.ui.create

import android.graphics.drawable.Drawable
import android.view.View
import java.util.function.Consumer

data class CreateFormElement(
	var title: Int,
	var value: Int = 0,
	var valueStr: String = "",
	var drawable: Drawable?,
	private val onClickListener: Consumer<View>
) {

	private var updateListener: Runnable? = null

	fun onUpdateListener(updateListener: Runnable) {
		this.updateListener = updateListener
	}


	fun onClick(view: View) {
		onClickListener.accept(view)
	}

	fun update() {
		updateListener?.run()
	}
}
