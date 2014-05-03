package org.cmdbuild.dms.alfresco.webservice;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

class DateUtils {

	private static Calendar getCalendar(final String isodate) throws Exception {
		// YYYY-MM-DDThh:mm:ss.sTZD
		final StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

		final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.clear();
		try {
			// Year
			if (st.hasMoreTokens()) {
				final int year = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.YEAR, year);
			} else {
				return calendar;
			}
			// Month
			if (check(st, "-") && (st.hasMoreTokens())) {
				final int month = Integer.parseInt(st.nextToken()) - 1;
				calendar.set(Calendar.MONTH, month);
			} else {
				return calendar;
			}
			// Day
			if (check(st, "-") && (st.hasMoreTokens())) {
				final int day = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.DAY_OF_MONTH, day);
			} else {
				return calendar;
			}
			// Hour
			if (check(st, "T") && (st.hasMoreTokens())) {
				final int hour = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.HOUR_OF_DAY, hour);
			} else {
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				return calendar;
			}
			// Minutes
			if (check(st, ":") && (st.hasMoreTokens())) {
				final int minutes = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.MINUTE, minutes);
			} else {
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				return calendar;
			}

			//
			// Not mandatory now
			//

			// Secondes
			if (!st.hasMoreTokens()) {
				return calendar;
			}
			String tok = st.nextToken();
			if (tok.equals(":")) { // secondes
				if (st.hasMoreTokens()) {
					final int secondes = Integer.parseInt(st.nextToken());
					calendar.set(Calendar.SECOND, secondes);
					if (!st.hasMoreTokens()) {
						return calendar;
					}
					// frac sec
					tok = st.nextToken();
					if (tok.equals(".")) {
						// bug fixed, thx to Martin Bottcher
						String nt = st.nextToken();
						while (nt.length() < 3) {
							nt += "0";
						}
						nt = nt.substring(0, 3); // Cut trailing chars..
						final int millisec = Integer.parseInt(nt);
						// int millisec = Integer.parseInt(st.nextToken()) * 10;
						calendar.set(Calendar.MILLISECOND, millisec);
						if (!st.hasMoreTokens()) {
							return calendar;
						}
						tok = st.nextToken();
					} else {
						calendar.set(Calendar.MILLISECOND, 0);
					}
				} else {
					throw new Exception("No secondes specified");
				}
			} else {
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			}
			// Timezone
			if (!tok.equals("Z")) { // UTC
				if (!(tok.equals("+") || tok.equals("-"))) {
					throw new Exception("only Z, + or - allowed");
				}
				final boolean plus = tok.equals("+");
				if (!st.hasMoreTokens()) {
					throw new Exception("Missing hour field");
				}
				final int tzhour = Integer.parseInt(st.nextToken());
				int tzmin = 0;
				if (check(st, ":") && (st.hasMoreTokens())) {
					tzmin = Integer.parseInt(st.nextToken());
				} else {
					throw new Exception("Missing minute field");
				}
				if (plus) {
					calendar.add(Calendar.HOUR, -tzhour);
					calendar.add(Calendar.MINUTE, -tzmin);
				} else {
					calendar.add(Calendar.HOUR, tzhour);
					calendar.add(Calendar.MINUTE, tzmin);
				}
			}
		} catch (final NumberFormatException ex) {
			ex.printStackTrace();
		}
		return calendar;
	}

	private static boolean check(final StringTokenizer st, final String token) throws Exception {
		try {
			if (st.nextToken().equals(token)) {
				return true;
			} else {
				throw new Exception("Missing [" + token + "]");
			}
		} catch (final Exception ex) {
			return false;
		}
	}

	public static Date parse(final String isodate) {
		try {
			final Calendar calendar = getCalendar(isodate);
			return calendar.getTime();
		} catch (final Exception e) {
			return new Date();
		}
	}
}
