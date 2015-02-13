package com.app.chasebank.fragment;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.app.chasebank.R;
import com.app.chasebank.framework.DialogScreen;

public class CompanyDetailsPopup extends DialogScreen {
	private View root;
	private TextView textTitle;
	private View btn_close;
	private TextView textDetails;
	
	/**
	 * Return an instance of this class
	 * @param title The title to be used in this Dialog
	 * @param details The details to be shown here
	 * @return CompanyDetailsPopup
	 */
	public static CompanyDetailsPopup newInstance(String title, String details) {
		CompanyDetailsPopup f = new CompanyDetailsPopup();
		Bundle bun = new Bundle();
		bun.putString("TITLE", title);
		bun.putString("DETAILS", details);
		f.setArguments(bun);
		return f;		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getDialog() != null) {
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		}
		
		root = inflater.inflate(R.layout.company_details_popup, container, false);
		
		initUI();
		
		Bundle bun = getArguments();
		String title = bun.getString("TITLE");
		String details = bun.getString("DETAILS");
		
		textTitle.setText(title);
		textDetails.setText(details);
		
		return root;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getDialog().getWindow()
		.getAttributes().windowAnimations = R.style.DialogAnimation;
	}

	public void initUI() {
		textTitle = (TextView) root.findViewById(R.id.textTitle); 
		textDetails = (TextView) root.findViewById(R.id.textDetails); 
		btn_close = root.findViewById(R.id.imageClose);
		
		getParent().setFontRegular(textDetails);
		getParent().setFontSemiBold(textTitle);
		
		/**
		 * Onclick Listener
		 */
		OnClickListener notilist = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CompanyDetailsPopup.this.dismiss();
			}
		};

		btn_close.setOnClickListener(notilist);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart() {
		super.onStart();

		// change dialog width
		if (getDialog() != null) {

			int fullWidth = getDialog().getWindow().getAttributes().width;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				fullWidth = size.x;
			} else {
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				fullWidth = display.getWidth();
			}

			final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
					.getDisplayMetrics());

			int w = fullWidth - padding;
			int h = getDialog().getWindow().getAttributes().height;

			getDialog().getWindow().setLayout(w, h);
		}
	}
}
