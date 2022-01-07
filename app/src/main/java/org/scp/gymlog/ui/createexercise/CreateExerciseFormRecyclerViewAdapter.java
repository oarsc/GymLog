package org.scp.gymlog.ui.createexercise;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentFormBinding;

import java.util.List;

public class CreateExerciseFormRecyclerViewAdapter extends RecyclerView.Adapter<CreateExerciseFormRecyclerViewAdapter.ViewHolder> {

	private final List<FormElement> formElements;

	public CreateExerciseFormRecyclerViewAdapter(List<FormElement> formElements) {
		this.formElements = formElements;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				ListElementFragmentFormBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false
				)
		);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.formElement = formElements.get(position);
		holder.mTitleView.setText(holder.formElement.getTitle());
		holder.updateValue();
		holder.formElement.setUpdateListener(holder::updateValue);
	}

	@Override
	public int getItemCount() {
		return formElements.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public FormElement formElement;
		public final TextView mTitleView;
		public final TextView mContentView;
		public final ImageView mImageView;

		public ViewHolder(ListElementFragmentFormBinding binding) {
			super(binding.getRoot());
			mTitleView = binding.title;
			mContentView = binding.content;
			mImageView = binding.image;

			itemView.setOnClickListener(v ->
				formElement.onClick(v)
			);
		}

		public void updateValue() {
			mImageView.setImageDrawable(formElement.getDrawable());

			if (formElement.getValueStr() != null && !formElement.getValueStr().isEmpty())
				mContentView.setText(formElement.getValueStr());
			else if (formElement.getValue() == 0)
				mContentView.setText("-");
			else
				mContentView.setText(formElement.getValue());
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}