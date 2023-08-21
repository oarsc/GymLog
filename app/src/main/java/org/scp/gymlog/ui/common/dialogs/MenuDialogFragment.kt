package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.MenuRes
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import java.util.function.Consumer

class MenuDialogFragment(
    @param:MenuRes private val menuId: Int,
    private val callback: Consumer<Int>
) : DialogFragment() {

    companion object {
        const val DIALOG_CLOSED = -1
    }

    private var callbackCalled = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_menu, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.parentLayout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MenuRecyclerViewAdapter(requireContext(), menuId) { menuItemId ->
            onMenuElementClicked(menuItemId)
        }

        val builder = AlertDialog.Builder(activity).setView(view)
        return builder.create()
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
}
