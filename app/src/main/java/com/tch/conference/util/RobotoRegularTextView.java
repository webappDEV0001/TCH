package com.tch.conference.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoRegularTextView extends TextView {

	public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RobotoRegularTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RobotoRegularTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "roboto_regular.ttf");
		setTypeface(tf);
	}

}
