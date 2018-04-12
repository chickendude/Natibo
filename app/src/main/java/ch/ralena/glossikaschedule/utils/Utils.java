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

	public static boolean isNumeric(String string) {
		if (string == null || string.equals(""))
			return false;
		for (char c : string.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}
}
