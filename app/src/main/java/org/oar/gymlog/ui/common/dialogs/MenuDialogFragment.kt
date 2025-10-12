package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.MenuRes
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oar.gymlog.databinding.DialogMenuBinding
import java.util.function.Consumer

class MenuDialogFragment(
    @param:MenuRes private val menuId: Int,
    private val removedActions: List<Int> = listOf(),
    private val callback: Consumer<Int>
) : DialogFragment() {

    private var callbackCalled = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogMenuBinding.inflate(layoutInflater)

        binding.parentLayout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MenuRecyclerViewAdapter(
                context = requireContext(),
                menuId = menuId,
                removedActions = removedActions,
                onClick = ::onMenuElementClicked
            )
        }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .create()
    }

    private fun onMenuElementClicked(menuItemId: Int) {
        callback.accept(menuItemId)
        callbackCalled = true
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!callbackCalled) {
            callback.accept(DIALOG_CLOSED)
        }
        super.onDismiss(dialog)
    }

    companion object {
        const val DIALOG_CLOSED = -1
    }
}
