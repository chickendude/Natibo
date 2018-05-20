package ch.ralena.natibo.utils;

import android.content.Context;
import android.content.SharedPreferences;
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

	public static class Storage {
		private static final String PREFERENCES = "ch.ralena.natibo.PREFERENCES";
		private static final String KEY_DAY_ID = "key_day_id";

		private Context context;

		public Storage(Context context) {
			this.context = context;
		}

		public void putDayId(String dayId) {
			SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			preferences.edit().putString(KEY_DAY_ID, dayId).apply();
		}

		public String getDayId() {
			SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			return preferences.getString(KEY_DAY_ID, "");
		}
	}
}
