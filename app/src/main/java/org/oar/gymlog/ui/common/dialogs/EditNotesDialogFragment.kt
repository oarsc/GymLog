package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oar.gymlog.R
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Variation
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import java.util.function.Consumer

class EditNotesDialogFragment(
    @StringRes title: Int,
    private val variation: Variation,
    override var initialValue: String,
    confirm: Consumer<String>
) : CustomDialogFragment<String>(title, confirm, Runnable{}) {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_notes, null)
        val input = view.findViewById<EditText>(R.id.dialogText)
        input.setText(initialValue)

        view.findViewById<RecyclerView>(R.id.historicNotes).apply {
            layoutManager = LinearLayoutManager(context)

            requireContext().dbThread { db ->
                val notes = if (variation.gymRelation == GymRelation.NO_RELATION)
                        db.bitDao().getNotesHistory(variation.id, 18)
                    else
                        db.bitDao().getNotesHistory(Data.gym?.id ?: 0, variation.id, 18)

                runOnUiThread {
                    adapter = EditNotesRecyclerViewAdapter(notes) { input.setText(it) }
                }
            }
        }

        val clearButton = view.findViewById<ImageView>(R.id.clearButton)
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