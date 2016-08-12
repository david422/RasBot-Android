package pl.dp.rasbot.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import pl.dp.rasbot.R;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public class PreferencePicker extends DialogPreference {

    // allowed range
    public static final int MAX_VALUE = 100;
    public static final int MIN_VALUE = 0;
    // enable or disable the 'circular behavior'
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker picker;
    private int value;

    private int minValue = MAX_VALUE;
    private int maxValue = MIN_VALUE;
    private boolean wrapSelectorWheel = WRAP_SELECTOR_WHEEL;

    public PreferencePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
    }

    public PreferencePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreferencePicker(Context context) {
        super(context);
    }

    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    private void initAttr(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PickerPreference);

        final int iCount = typedArray.getIndexCount();
/*
        for (int i = 0; i < iCount; i++) {

            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.PickerPreference_preference_picker_max_value
            }
        }*/

        maxValue = typedArray.getInt(R.styleable.PickerPreference_preference_picker_max_value, MAX_VALUE);
        minValue = typedArray.getInt(R.styleable.PickerPreference_preference_picker_min_value, MIN_VALUE);
        wrapSelectorWheel = typedArray.getBoolean(R.styleable.PickerPreference_preference_picker_wrap_wheel, WRAP_SELECTOR_WHEEL);

        typedArray.recycle();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setWrapSelectorWheel(wrapSelectorWheel);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, MIN_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(MIN_VALUE) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }

}
