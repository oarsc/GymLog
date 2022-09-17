package org.scp.gymlog.ui.create

import android.graphics.drawable.Drawable
import android.view.View
import java.util.function.Consumer

class CreateFormElement(
	var title: Int,
	var value: Int = 0,
	var valueStr: String = "",
	var drawable: Drawable?,
	private val onClickListener: Consumer<View>
) {

	var updateListener: Runnable? = null

	fun onClick(view: View) {
		onClickListener.accept(view)
	}

	fun update() {
		updateListener?.run()
	}
}
