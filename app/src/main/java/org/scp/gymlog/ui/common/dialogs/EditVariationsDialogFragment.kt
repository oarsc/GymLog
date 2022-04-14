package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.model.Variation
import java.util.function.Consumer

class EditVariationsDialogFragment(
    variations: List<Variation>,
    private val confirm: Consumer<List<Variation>>
) : DialogFragment() {

    private lateinit var input: EditText
    private var selectedIndex = 0
    private lateinit var adapter: EditVariationsRecyclerViewAdapter

    private val variations: MutableList<Variation> = variations
        .map { variation -> variation.clone() }
        .toMutableList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_variations, null)

        input = view.findViewById(R.id.dialogText)
        input.isEnabled = false
        input.onFocusChangeListener = OnFocusChangeListener { _,_ ->
            input.post {
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                adapter.updateText(selectedIndex, s.toString())
            }
        })

        adapter = EditVariationsRecyclerViewAdapter(variations, this::selectVariation)
        view.findViewById<RecyclerView>(R.id.variationsList).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EditVariationsDialogFragment.adapter
        }

        val addButton: ImageView = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val variation = Variation("New " + variations.size)
            variations.add(variation)
            adapter.notifyItemInserted(variations.size - 1)
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.text_variations)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ -> confirm.accept(variations) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> }

        return builder.create()
    }

    private fun selectVariation(index: Int, name: String) {
        selectedIndex = index
        input.isEnabled = true
        input.setText(name)
        input.setSelection(name.length)
        input.requestFocus()
    }
}