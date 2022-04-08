package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import java.util.function.Consumer

class EditNotesDialogFragment(
    @StringRes title: Int,
    private val exerciseId: Int,
    override var initialValue: String,
    confirm: Consumer<String>
) : CustomDialogFragment<String>(title, confirm, Runnable{}) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_notes, null)
        val input: EditText = view.findViewById(R.id.dialogText)
        input.setText(initialValue)

        DBThread.run(requireContext()) { db: AppDatabase ->
            val notes = db.bitDao().getNotesHistory(exerciseId, 18)

            val recyclerView: RecyclerView = view.findViewById(R.id.historicNotes)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = EditNotesRecyclerViewAdapter(notes) { text: String? ->
                input.setText(text)
            }
        }

        val clearButton: ImageView = view.findViewById(R.id.clearButton)
        clearButton.setOnClickListener { input.text.clear() }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val text = input.text.toString().trim { it <= ' ' }
                confirm.accept(text)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create()
    }


}