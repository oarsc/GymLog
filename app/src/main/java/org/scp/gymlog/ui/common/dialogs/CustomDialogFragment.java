package org.scp.gymlog.ui.common.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import org.scp.gymlog.util.Function;

import java.util.function.Consumer;

public abstract class CustomDialogFragment<T> extends DialogFragment {

    protected int title;
    protected Consumer<T> confirm;
    protected Function cancel;
    protected T initialValue;

    public CustomDialogFragment(@StringRes int title, Consumer<T> confirm, Function cancel) {
        this.title = title;
        this.confirm = confirm;
        this.cancel = cancel;
    }

    public void setInitialValue(T initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public abstract Dialog onCreateDialog(Bundle savedInstanceState);
}
