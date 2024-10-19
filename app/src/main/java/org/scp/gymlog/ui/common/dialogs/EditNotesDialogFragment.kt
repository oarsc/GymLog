package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.model.GymRelation
import org.scp.gymlog.model.Variation
import org.scp.gymlog.util.Constants.NOTES_SEPARATOR
import org.scp.gymlog.util.Constants.NOTES_VISUAL_SEPARATOR
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.scp.gymlog.util.extensions.ComponentsExts.setOnChangeListener
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import java.util.function.Consumer

class EditNotesDialogFragment(
    @StringRes title: Int,
    private val variation: Variation,
    override var initialValue: String,
    confirm: Consumer<String>
) : CustomDialogFragment<String>(title, confirm, Runnable{}) {

    private lateinit var listAdapter: EditNotesRecyclerViewAdapter

    private val notes: MutableList<String?> = initialValue
        .split(NOTES_SEPARATOR)
        .map { it.trim() }
        .filter(String::isNotEmpty)
        .distinct()
        .toMutableList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_notes, null)
        val input = view.findViewById<EditText>(R.id.dialogText)

        input.setOnChangeListener {
            if (it.isBlank()) {
                notes.remove(null)
            } else if (!notes.contains(null)) {
                notes.add(null)
            }
        }

        view.findViewById<RecyclerView>(R.id.historicNotes).apply {
            layoutManager = LinearLayoutManager(context)

            requireContext().dbThread { db ->
                val notesHistory = if (variation.gymRelation == GymRelation.NO_RELATION)
                        db.noteDao().getNotesHistory(variation.id, 18)
                    else
                        db.noteDao().getNotesHistory(Data.gym?.id ?: 0, variation.id, 18)

                val preselected = notes
                    //.toList()
                    .filterIndexed { idx, it ->
                        if (notesHistory.contains(it)) true
                        else {
                            notes[idx] = null
                            input.setText(it)
                            false
                        }
                    }
                    .filterNotNull()

                runOnUiThread {
                    listAdapter = EditNotesRecyclerViewAdapter(notesHistory, preselected) { it, sel ->
                        if (!sel) {
                            notes.remove(it)
                        } else if (!notes.contains(it)) {
                            notes.add(it)
                        }
                    }
                    adapter = listAdapter
                }
            }
        }

        view.findViewById<ImageView>(R.id.clearButton).setOnClickListener {
            input.text.clear()
            listAdapter.clearSelects()
            notes.clear()
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val text = notes.joinToString(NOTES_VISUAL_SEPARATOR) { note ->
                    note ?: input.text.toString().trim { it <= ' ' }
                }

                confirm.accept(text)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create()
    }
}