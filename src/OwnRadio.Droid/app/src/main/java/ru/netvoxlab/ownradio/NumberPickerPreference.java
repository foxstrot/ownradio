package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

/**
 * Created by a.polunina on 25.07.2017.
 */

public class NumberPickerPreference extends DialogPreference {
	// Namespaces to read attributes
	private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/ru.netvoxlab.ownradio";
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	
	// Attribute names
	private static final String ATTR_DEFAULT_VALUE = "defaultValue";
	private static final String ATTR_MIN_VALUE = "minValue";
	private static final String ATTR_MAX_VALUE = "maxValue";
	
	// Default values for defaults
	private static final int DEFAULT_CURRENT_VALUE = 1;
	private static final int DEFAULT_MIN_VALUE = 1;
	private static final int DEFAULT_MAX_VALUE = 64;
	
	// Real defaults
	private final int mDefaultValue;
	private final int mMaxValue;
	private final int mMinValue;
	
	// enable or disable the 'circular behavior'
	public static final boolean WRAP_SELECTOR_WHEEL = false;
	
	private NumberPicker picker;
	private int value;
	
	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
		mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
		mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
	}
	
	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
		mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
		mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
	}
	
	@Override
	protected View onCreateDialogView() {
		value = getPersistedInt(mDefaultValue);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		
		picker = new NumberPicker(getContext());
		picker.setLayoutParams(layoutParams);
		
		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(picker);
		
		return dialogView;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		picker.setMinValue(mMinValue);
		picker.setMaxValue(mMaxValue);
		picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
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
		return a.getInt(index, mMinValue);
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue ? getPersistedInt(mMinValue) : (Integer) defaultValue);
	}
	
	public void setValue(int value) {
		this.value = value;
		persistInt(this.value);
	}
	
	public int getValue() {
		return this.value;
	}
}