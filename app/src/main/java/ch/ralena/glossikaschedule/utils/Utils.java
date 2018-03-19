package ch.ralena.glossikaschedule.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

public class Utils {
	public static void alert(Context context, String title, String message) {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.show();
	}
}
