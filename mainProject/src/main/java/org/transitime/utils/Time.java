/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.transitime.gtfs.DbConfig;

/**
 * Contains convenience methods for dealing with time issues.
 * <p>
 * Note: To use the proper timezone should set
 * <code> TimeZone.setDefault(TimeZone.getTimeZone(timeZoneStr));</code> before
 * this class is initialized. Otherwise the SimpleDateFormat objects will
 * wrongly use the system default timezone.
 * 
 * @author SkiBu Smith
 * 
 */
public class Time {
	// Some handy constants for dealing with time.
	// MS_PER_SEC and MS_PER_MIN are declared as integers since they after
	// often used where just interested in a few minutes, which easily
	// fits within an int. By being an int instead of a long don't need
	// to cast to an int when using these values for things like config
	// parameters which are typically a IntegerConfigValue. But for the
	// big values, such as MS_PER_HOUR and longer then risk wrapping
	// around if just using an int. For example a month of 31 days *
	// MS_PER_DAY would wrap if MS_PER_DAY was an integer instead of a long.
	public static final int MS_PER_SEC = 1000;
	public static final int MS_PER_MIN = 60 * MS_PER_SEC;
	public static final long MS_PER_HOUR = 60 * MS_PER_MIN;
	public static final long MS_PER_DAY = 24 * MS_PER_HOUR;
	public static final long MS_PER_WEEK = 7 * MS_PER_DAY;
	public static final long MS_PER_YEAR = 365 * MS_PER_DAY;
	
	public static final int SEC_PER_MIN = 60;
	public static final int SEC_PER_HOUR = 60 * SEC_PER_MIN;
	public static final int SEC_PER_DAY = 24 * SEC_PER_HOUR;
	
	public static final long NSEC_PER_MSEC = 1000000;
	
	// These two are for reading in dates in various formats
	private static final DateFormat defaultDateFormat =
			SimpleDateFormat.getDateInstance(DateFormat.SHORT);
	private static final DateFormat dateFormatDashesShortYear =
			new SimpleDateFormat("MM-dd-yy");

	
	private static final DateFormat readableDateFormat =
			new SimpleDateFormat("MM-dd-yyyy");
	
	private static final DateFormat readableDateFormat24 = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
	
	private static final DateFormat readableDateFormat24NoSecs = 
		new SimpleDateFormat("MM-dd-yyyy HH:mm");

	private static final DateFormat readableDateFormat24Msec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z");
	
	private static final DateFormat readableDateFormat24NoTimeZoneMsec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");
	
	private static final DateFormat readableDateFormat24NoTimeZoneNoMsec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	private static final DateFormat timeFormat24 =
			new SimpleDateFormat("HH:mm:ss z");

	private static final DateFormat timeFormat24NoTimezone =
			new SimpleDateFormat("HH:mm:ss");
	
	private static final DateFormat timeFormat24Msec =
			new SimpleDateFormat("HH:mm:ss.SSS z");

	private static final DateFormat timeFormat24MsecNoTimeZone =
			new SimpleDateFormat("HH:mm:ss.SSS");

	// Note that this one is not static. It is for when need to include
	// timezone via a Time object.
	private final DateFormat readableDateFormat24MsecForTimeZone =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z");
	private final DateFormat readableTimeFormatForTimeZone =
			new SimpleDateFormat("HH:mm:ss");
	private final DateFormat readableDateFormatForTimeZone =
			new SimpleDateFormat("MM-dd-yyyy");
	
	// So can output headings and such with a consistent number of decimal places
	private static final DecimalFormat oneDigitFormat = new DecimalFormat("0.0");

	// Have a shared calendar so don't have to keep creating one
	private Calendar calendar;
	
	/******************* Methods ******************/
	
	public Time(DbConfig dbConfig) {
		this.calendar = new GregorianCalendar(dbConfig.getFirstAgency().getTimeZone());
	}
	
	/**
	 * Creates a Time object for the specified timezone. Useful for when have to
	 * frequently call members such as getSecondsIntoDay() that need an
	 * expensive calendar object.
	 * 
	 * @param timeZoneStr
	 *            Such as "America/Los_Angeles" . List of time zones can be found
	 *            at http://en.wikipedia.org/wiki/List_of_tz_database_time_zones
	 */
	public Time(String timeZoneStr) {
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
		this.calendar = new GregorianCalendar(timeZone);
		
		readableDateFormat24MsecForTimeZone.setCalendar(this.calendar);
		readableTimeFormatForTimeZone.setCalendar(this.calendar);
		readableDateFormatForTimeZone.setCalendar(this.calendar);
	}
	
	/**
	 * Converts the epoch time into number of seconds into the day.
	 * 
	 * @param epochTime
	 * @return seconds into the day
	 */
	public int getSecondsIntoDay(long epochTime) {
		// Since setting and then getting time and this method might be called
		// by multiple threads need to synchronize.
		synchronized (calendar) {
			// Get seconds into day
			calendar.setTimeInMillis(epochTime);
			return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
					calendar.get(Calendar.MINUTE) * 60          +
					calendar.get(Calendar.SECOND);
		}
	}
	
	/**
	 * Converts the epoch time into number of seconds into the day.
	 * 
	 * @param epochTime
	 * @return seconds into the day
	 */
	public int getSecondsIntoDay(Date epochDate) {
		return getSecondsIntoDay(epochDate.getTime());
	}
	
	/**
	 * Returns day of year. This method is not threadsafe in that it first sets
	 * the time of the calendar and then gets the day of the year without
	 * synchronizing the calendar. But this is a bit faster.
	 * 
	 * @param epochDate
	 * @return
	 */
	public int getDayOfYear(Date epochDate) {
		calendar.setTimeInMillis(epochDate.getTime());
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * Converts the epoch time into number of msec into the day.
	 * 
	 * @param epochTime
	 * @return msec into the day
	 */
	public int getMsecsIntoDay(Date epochTime) {
		// Since setting and then getting time and this method might be called
		// by multiple threads need to synchronize.
		synchronized (calendar) {
			// Get seconds into day
			calendar.setTime(epochTime);
			return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
					calendar.get(Calendar.MINUTE) * 60 * 1000          +
					calendar.get(Calendar.SECOND) * 1000               +
					calendar.get(Calendar.MILLISECOND);
		}
	}
	
	/**
	 * Returns the epoch time of the start of the current day for the specified
	 * timezone.
	 * 
	 * @param tz
	 * @return
	 */
	public static long getStartOfDay(TimeZone tz) {
		Calendar calendar = new GregorianCalendar(tz);

		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Get the epoch time
		long epochTime = calendar.getTimeInMillis();
		return epochTime;
	}
	
	/**
	 * Converts secondsIntoDay into an epoch time.
	 * 
	 * @param secondsIntoDay
	 *            To be converted into epoch time
	 * @param referenceDate
	 *            The approximate epoch time so that can handle times before and
	 *            after midnight.
	 * @return epoch time
	 */
	public long getEpochTime(int secondsIntoDay, Date referenceDate) {
		// Need to sync the calendar since reusing it.
		synchronized (calendar) {
			// Determine seconds, minutes, and hours
			int seconds = secondsIntoDay % 60;
			int minutesIntoDay = secondsIntoDay / 60;
			int minutes = minutesIntoDay % 60;
			int hoursIntoDay = minutesIntoDay / 60;
			int hours = hoursIntoDay % 24;
			
			// Set the calendar to use the reference time so that get the
			// proper date.
			calendar.setTime(referenceDate);
			
			// Set the seconds, minutes, and hours so that the calendar has
			// the proper time. Need to also set milliseconds because otherwise
			// would use milliseconds from the referenceDate.
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.HOUR_OF_DAY, hours);
			
			// Get the epoch time
			long epochTime = calendar.getTimeInMillis();
			
			// Need to make sure that didn't have a problem around midnight. 
			// For example, a vehicle is supposed to depart a layover at 
			// 00:05:00 right after midnight but the AVL time might be for
			// 23:57:13, which is actually for the previous day. If would
			// simply set the hours, minutes and seconds then would wrongly
			// get an epoch time for the previous day. Could have the same
			// problem if the AVL time is right after midnight but the 
			// secondsIntoDay is just before midnight. Therefore if the 
			// resulting epoch time is too far away then adjust the epoch
			// time by plus or minus day.
			if (epochTime > referenceDate.getTime() + 12 * MS_PER_HOUR) {
				// subtract a day
				epochTime -= MS_PER_DAY;
			} else if (epochTime < referenceDate.getTime() - 12 * MS_PER_HOUR) {
				// add a day
				epochTime += MS_PER_DAY;
			}
			
			// Get the results
			return epochTime;
		}
	}
	
	/**
	 * Converts secondsIntoDay into an epoch time.
	 * 
	 * @param secondsIntoDay
	 *            To be converted into epoch time
	 * @param referenceTime
	 *            The approximate epoch time so that can handle times before and
	 *            after midnight.
	 * @return epoch time
	 */
	public long getEpochTime(int secondsIntoDay, long referenceTime) {
		return getEpochTime(secondsIntoDay, new Date(referenceTime));
	}
	
	/**
	 * Returns time of day in msecs. But uses reference time to determine
	 * if interested in a time before midnight (a negative value) or a
	 * time past midnight (greater than 24 hours). This way when looking
	 * at schedule adherence and such it is much easier to deal with
	 * situations where have blocks that span midnight. Only need to
	 * get the time and do schedule adherence comparison once.
	 * 
	 * @param epochTime
	 * @param referenceTimeIntoDayMsecs
	 * @return
	 */
	public long getMsecsIntoDay(Date epochTime, long referenceTimeIntoDayMsecs) {
		int timeIntoDay = getMsecsIntoDay(epochTime);
		long delta = Math.abs(referenceTimeIntoDayMsecs - timeIntoDay);
		
		long deltaForBeforeMidnight = 
				Math.abs(referenceTimeIntoDayMsecs - (timeIntoDay - Time.MS_PER_DAY));
		if (deltaForBeforeMidnight < delta)
			return timeIntoDay - (int)Time.MS_PER_DAY;
		
		long deltaForAfterMidnight = 
				Math.abs(referenceTimeIntoDayMsecs - (timeIntoDay + Time.MS_PER_DAY));
		if (deltaForAfterMidnight < delta)
			return timeIntoDay + (int)Time.MS_PER_DAY;
		
		return timeIntoDay;
	}
	
	/**
	 * Parses the dateStr into a Date using the timezone for this Time object.
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public Date parseUsingTimezone(String dateStr) throws ParseException {
		return readableDateFormatForTimeZone.parse(dateStr);
	}
	
	/**
	 * Parses the datetimeStr and returns a Date object. Format is
	 * "MM-dd-yyyy HH:mm:ss z". Tries multiple formats including with
	 * milliseconds and with and without time zones.
	 * 
	 * @param datetimeStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String datetimeStr) throws ParseException {
		// First try with timezone and msec, the most complete form
		try {
			Date date = readableDateFormat24Msec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Got exception so try without timezone but still try msec
		try {
			Date date = readableDateFormat24NoTimeZoneMsec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try without seconds but with timezone
		try {
			Date date = readableDateFormat24.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try without msecs and without timezone
		try {
			Date date = readableDateFormat24NoTimeZoneNoMsec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try without seconds and without timezone
		try {
			Date date = readableDateFormat24NoSecs.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try date alone. This will ignore any time
		// specification so this attempt needs to be done after trying all
		// the other formats.
		try {
		    Date date = readableDateFormat.parse(datetimeStr);
		    return date;
		} catch (ParseException e) {}
		
		// As last resort try the default syntax. Will throw a ParseException
		// if can't parse.
		return new SimpleDateFormat().parse(datetimeStr);
	}
	
	/**
	 * Parses the dateStr and returns a Date object. Format of 
	 * date is "MM-dd-yyyy".
	 * 
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String dateStr) throws ParseException {
		try {
			return defaultDateFormat.parse(dateStr);
		} catch (ParseException e) {}

		// Try using "-" instead of "/" as separator. Having the date formatter
		// specify only two digits for the year means it also works when 4
		// digits are used, making it pretty versatile.
		return dateFormatDashesShortYear.parse(dateStr);		
	}
	
	/**
	 * Parses a time such as HH:MM:SS into seconds into the day.
	 * Instead of using SimpleDateFormat or such this function 
	 * does the conversion directly and simply in order to be quicker.
	 * This is useful for reading in large volumes of GTFS data and
	 * such. 
	 * @return
	 */
	public static int parseTimeOfDay(String timeStr) {
		// At some point GTFS will handle negative values
		// to indicate a time early in the morning before midnight.
		// Therefore might as well handle negative values now.
		boolean negative = timeStr.charAt(0) == '-';
		String positiveTimeStr = negative ? timeStr.substring(1) : timeStr;

		int firstColon = positiveTimeStr.indexOf(":");
		int secondColon = positiveTimeStr.lastIndexOf(":");
		int hours = Integer.parseInt(positiveTimeStr.substring(0, firstColon));  
		int minutes = Integer.parseInt(positiveTimeStr.substring(firstColon+1, secondColon));
		int seconds = Integer.parseInt(positiveTimeStr.substring(secondColon+1));
		
		int result = hours * 60 * 60 + minutes*60 + seconds;
		if (negative)
			return -result;
		else
			return result;
	}
	
	/**
	 * Converts seconds in day to a string HH:MM:SS.
	 * Note: secInDay can be negative.
	 * 
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayStr(long secInDay) {
		String timeStr = "";
		if (secInDay < 0) {
			timeStr="-";
			secInDay = -secInDay;
		}
		long hours = secInDay / (60*60);
		long minutes = (secInDay % (60*60)) / 60;
		long seconds = secInDay % 60;
		
		// Use StringBuilder instead of just concatenating strings since it
		// indeed is faster. Actually measured it and when writing out
		// GTFS stop_times file it was about 10% faster when using
		// StringBuilder.
		StringBuilder b = new StringBuilder(8);
		b.append(timeStr);
		if (hours<10) b.append("0");
		b.append(hours).append(":");
		if (minutes < 10) b.append("0");
		b.append(minutes).append(":");
		if (seconds<10) b.append("0");
		b.append(seconds);
		return b.toString();
	}
	
	/**
	 * Converts seconds in day to a string HH:MM:SS.
	 * If secInDay null then returns null.
	 * Note: secInDay can be negative.
	 * 
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayStr(Integer secInDay) {
		if (secInDay == null)
			return null;
		return timeOfDayStr(secInDay.intValue());
	}
	
	/**
	 * Outputs time in minutes with a single digit past the decimal point
	 * @param msec
	 * @return
	 */
	public static String minutesStr(long msec) {
		float minutes = (float) msec / Time.MS_PER_MIN;
		return oneDigitFormat.format(minutes);
	}
	
	/**
	 * Outputs time in seconds with a single digit past the decimal point
	 * @param msec
	 * @return
	 */
	public static String secondsStr(long msec) {
		float seconds = (float) msec / Time.MS_PER_SEC;
		return oneDigitFormat.format(seconds);
	}

	/**
	 * Returns the elapsed time in msec as a string. If the time is below
	 * 1 minute then it is displayed in seconds. If greater than 2 minutes
	 * then it is displayed in minutes. For both, 1 digit after the 
	 * decimal point is displayed. The units, either " sec" or " msec"
	 * are appended.
	 * 
	 * @param msec
	 * @return
	 */
	public static String elapsedTimeStr(long msec) {
		if (Math.abs(msec) < 2*Time.MS_PER_MIN) {
			return Time.secondsStr(msec) + " sec";
		} else {
			return Time.minutesStr(msec) + " min";
		}
	}
	
	/**
	 * Returns date in format "MM-dd-yyyy"
	 * @param epochTime
	 * @return
	 */
	public static String dateStr(long epochTime) {
		return readableDateFormat.format(epochTime);
	}
	
	/**
	 * Returns date in format "MM-dd-yyyy"
	 * @param epochTime
	 * @return
	 */
	public static String dateStr(Date epochTime) {
		return readableDateFormat.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(long epochTime) {
		return readableDateFormat24.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(Date epochTime) {
		return readableDateFormat24.format(epochTime.getTime());
	}	
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss.SSS z
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(long epochTime) {
		return readableDateFormat24Msec.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss.SSS z
	 * but does so for the Timezone specified by this Time object.
	 * 
	 * @param epochTime
	 * @return
	 */
	public String dateTimeStrMsecForTimezone(long epochTime) {
		return readableDateFormat24MsecForTimeZone.format(epochTime);
	}
	
	public String timeStrForTimezone(long epochTime) {
		return readableTimeFormatForTimeZone.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string, including msec, in the 
	 * format MM-dd-yyyy HH:mm:ss.SSS z
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(Date epochTime) {
		return readableDateFormat24Msec.format(epochTime.getTime());
	}	
	
	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(long epochTime) {
		return timeFormat24.format(epochTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(Date epochTime) {
		return timeStr(epochTime.getTime());
	}
	
	/**
	 * Returns just the time string in format "HH:mm:ss"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrNoTimeZone(long epochTime) {
		return timeFormat24NoTimezone.format(epochTime);
	}
	
	/**
	 * Returns just the time string in format "HH:mm:ss"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrNoTimeZone(Date epochTime) {
		return timeStrNoTimeZone(epochTime.getTime());
	}

	/**
	 * Returns just the time string. Includes msec.
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsec(Date epochTime) {
		return timeFormat24Msec.format(epochTime.getTime());
	}
	
	/**
	 * Returns just the time string. Includes msec.
	 * e.g. "HH:mm:ss.SSS z"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsec(long epochTime) {
		return timeFormat24Msec.format(epochTime);
	}

	/**
	 * Returns just the time string. Includes msec but no timezone.
	 * e.g. "HH:mm:ss.SSS"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsecNoTimeZone(long epochTime) {
		return timeFormat24MsecNoTimeZone.format(epochTime);
	}
	
	/**
	 * Returns just the time string. Includes msec but no timezone.
	 * e.g. "HH:mm:ss.SSS"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsecNoTimeZone(Date epochTime) {
		return timeFormat24MsecNoTimeZone.format(epochTime);
	}
	
	/**
	 * Returns the absolute value of the difference between the two times. If
	 * the difference is greater than 12 hours then 24hours-difference is
	 * returned. This is useful for when the times wrap around midnight. For
	 * example, if the times are 11:50pm and 12:05am then the difference will be
	 * 15 minutes instead of 23 hours and 45 minutes.
	 * 
	 * @param time1SecsIntoDay
	 * @param time2SecsIntoDay
	 * @return The absolute value of the difference between the two times
	 */
	public static int getTimeDifference(int time1SecsIntoDay,
			int time2SecsIntoDay) {
		int timeDiffSecs = Math.abs(time1SecsIntoDay - time2SecsIntoDay);
		if (timeDiffSecs > 12 * Time.SEC_PER_HOUR)
			return Time.SEC_PER_DAY - timeDiffSecs;
		else
			return timeDiffSecs;
	}
	
	/**
	 * Simply calls Thread.sleep() but catches the InterruptedException
	 * so that the calling function doesn't need to.
	 * @param msec
	 */
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	public static void main(String args[]) {
		try {
			// TODO make this a unit test
			Time time = new Time("America/Los_Angeles");
			Date referenceDate = parse("11-23-2013 23:55:00");
			int secondsIntoDay = 24 * SEC_PER_HOUR - 60;
			long epochTime = time.getEpochTime(secondsIntoDay, referenceDate);
			System.out.println(new Date(epochTime));
		} catch (ParseException e) {}
		
	}
}
