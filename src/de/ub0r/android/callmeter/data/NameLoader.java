/*
 * Copyright (C) 2009-2012 Felix Bechstein
 * 
 * This file is part of CallMeter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.TextView;

/**
 * Load name from number in background.
 * 
 * @author flx
 */
public class NameLoader extends AsyncTask<Void, Void, String> {
	/** TAG for Log. */
	private static final String TAG = "NameLoader";

	/** {@link Context}. */
	private final Context ctx;
	/** {@link TextView}. */
	private final TextView tv;
	/** Number. */
	private final String num;
	/** {@link String} format. */
	private final String f;

	/**
	 * Get name for number synchronously.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param number
	 *            phone number
	 * @param format
	 *            format to format the {@link String} with
	 * @return name or formated {@link String}
	 */
	public static String getName(final Context context, final String number, final String format) {
		NameLoader loader = new NameLoader(context, number, format, null);
		String result = loader.doInBackground((Void) null);
		loader.onPostExecute(result);
		if (result == null) {
			return null;
		} else if (format == null) {
			NameCache.getInstance().put(number, result);
			return result;
		} else {
			NameCache.getInstance().put(number, result);
			return String.format(format, result);
		}
	}

	/**
	 * Default constructor.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param number
	 *            phone number
	 * @param format
	 *            format to format the {@link String} with
	 * @param view
	 *            {@link TextView} to set the result on
	 */
	public NameLoader(final Context context, final String number, final String format,
			final TextView view) {
		this.ctx = context;
		this.num = number;
		this.f = format;
		this.tv = view;
	}

	@Override
	protected String doInBackground(final Void... params) {
		String ret = null;
		try {
			Cursor c = this.ctx.getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.num),
					new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
			if (c.moveToFirst()) {
				ret = c.getString(0);
			}
			c.close();
		} catch (Exception e) {
			Log.e(TAG, "error loading name", e);
		}
		return ret;
	}

	@Override
	protected void onPostExecute(final String result) {
		if (result != null) {
			NameCache.getInstance().put(this.num, result);
			if (this.tv != null && !this.isCancelled()) {
				String s = result;
				if (this.f != null) {
					s = String.format(this.f, s);
				}
				this.tv.setText(s);
			}
		}
	}
}
