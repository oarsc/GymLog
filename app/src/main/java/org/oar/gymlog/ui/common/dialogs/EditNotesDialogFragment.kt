package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditNotesBinding
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
        val binding = DialogEditNotesBinding.inflate(layoutInflater)

        binding.dialogText.setText(initialValue)

        binding.historicNotes.apply {
            layoutManager = LinearLayoutManager(context)

            dbThread { db ->
                val notes = if (variation.gymRelation == GymRelation.NO_RELATION)
                        db.bitDao().getNotesHistory(variation.id, 18)
                    else
                        db.bitDao().getNotesHistory(Data.gym?.id ?: 0, variation.id, 18)

                runOnUiThread {
                    adapter = EditNotesRecyclerViewAdapter(notes) { binding.dialogText.setText(it) }
                }
            }
        }

        binding.clearButton.setOnClickListener { binding.dialogText.text.clear() }

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val text = binding.dialogText.text.toString().trim { it <= ' ' }
                confirm.accept(text)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
            .create()
    }
}