package org.oar.gymlog.ui.common.createForm

import android.graphics.drawable.Drawable
import android.view.View
import androidx.fragment.app.FragmentManager
import org.oar.gymlog.R
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import kotlin.reflect.KMutableProperty0

data class CreateFormElement(
	var title: Int,
	var value: Int = 0,
	var valueStr: String = "",
	var drawable: Drawable? = null,
	private val onClickListener: (View) -> Unit
) {
	private var updateListener: Runnable? = null

	fun onUpdateListener(updateListener: Runnable) {
		this.updateListener = updateListener
	}

	fun onClick(view: View) {
		onClickListener(view)
	}

	fun update() {
		updateListener?.run()
	}

	companion object {
		fun createStringFormElement(
			manager: FragmentManager,
			title: Int,
			property: KMutableProperty0<String>,
			drawable: Drawable? = null
		): CreateFormElement {
			lateinit var form: CreateFormElement
			form = CreateFormElement(
				title = title,
				valueStr = property.get(),
				drawable = drawable,
				onClickListener = {
					val dialog = EditTextDialogFragment(
						title = R.string.form_name,
						initialValue = property.get(),
						confirm = { result ->
							property.set(result)
							form.valueStr = result
							form.update()
						})
					dialog.show(manager, null)
				}
			)
			return form
		}
	}
}
