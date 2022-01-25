package org.scp.gymlog.ui.createexercise;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.function.Consumer;

public class FormElement {
	private Drawable drawable;
	private int title;
	private int value;
	private String valueStr;
	private Consumer<View> onClick;
	private Runnable update;

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getValueStr() {
		return valueStr;
	}

	public void setValueStr(String valueStr) {
		this.valueStr = valueStr;
	}

	public void onClick(View view) {
		onClick.accept(view);
	}

	public void setOnClickListener(Consumer<View> onClick) {
		this.onClick = onClick;
	}

	public void update() {
		if (update != null) {
			update.run();
		}
	}

	public void setUpdateListener(Runnable update) {
		this.update = update;
	}
}
