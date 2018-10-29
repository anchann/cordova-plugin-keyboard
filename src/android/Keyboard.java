package org.apache.cordova.labs.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;


public class Keyboard extends CordovaPlugin {

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		final CordovaWebView appView = webView;

		final View rootView = cordova.getActivity().getWindow().getDecorView().findViewById(android.R.id.content).getRootView();
		OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
			double lastHeightRatio = 0;

			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				//r will be populated with the coordinates of your view that area still visible.
				rootView.getWindowVisibleDisplayFrame(r);

				int currTotalHeight = rootView.getRootView().getHeight();
				int currUsableHeight = (r.bottom - r.top);
				double currHeightRatio = ((double)currUsableHeight) / currTotalHeight;

				if (currHeightRatio != lastHeightRatio) {
					if (currHeightRatio > 0.75 && lastHeightRatio <= 0.75) {
						appView.sendJavascript("window.Keyboard.fireOnHide();");
					}
					else if (currHeightRatio <= 0.75 && lastHeightRatio > 0.75) {
						appView.sendJavascript("window.Keyboard.fireOnShow();");
					}
				}

				lastHeightRatio = currHeightRatio;
			 }
		};

		// fire once to get the initial ratio
		listener.onGlobalLayout();

		rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Activity activity = this.cordova.getActivity();
		InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

		View view;
		try {
			view = (View)webView.getClass().getMethod("getView").invoke(webView);
		}
		catch (Exception e){
			view = (View)webView;
		}

		if ("show".equals(action)) {
			imm.showSoftInput(view, 0);
			callbackContext.success();
			return true;
		}
		else if ("hide".equals(action)) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			callbackContext.success();
			return true;
		}
		callbackContext.error(action + " is not a supported action");
		return false;
	}
}
