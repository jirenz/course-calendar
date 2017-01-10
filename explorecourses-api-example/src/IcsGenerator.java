import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.jdom2.JDOMException;
import java.text.*;

import edu.stanford.services.explorecourses.Course;
import edu.stanford.services.explorecourses.Department;
import edu.stanford.services.explorecourses.School;
import edu.stanford.services.explorecourses.Section;
import edu.stanford.services.explorecourses.ExploreCoursesConnection;
import edu.stanford.services.explorecourses.MeetingSchedule;

/*
 * Jiren Zhu
 * jirenz@stanford.edu
 * Sep 24 2016
 * */

/** Prints a list of all courses offered at Stanford in the current academic year **/
public class IcsGenerator
{
	final static DateFormat TARGET_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	final static DateFormat SOURCE_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyyhh:mm:ss a");
	final static String PRODID = "explore_courses";
	final static String NEW_LINE = "\r\n";
	final static String[] ALLOWED_COMPONENTS_ARRAY = new String[] { "SEM", "PRC", "LEC","ISF","DIS","LAB","PRA","CAS","ISS","COL","LNG","API","ACT","LBS","RES","CLB","SIM"};
	final static HashSet<String> ALLOWED_COMPONENTS = new HashSet<String>(Arrays.asList(ALLOWED_COMPONENTS_ARRAY));
	final static String TIME_ZONE = "BEGIN:VTIMEZONE" + NEW_LINE
			+ "TZID:America/Los_Angeles" + NEW_LINE
			+ "X-LIC-LOCATION:America/Los_Angeles" + NEW_LINE
			+ "BEGIN:DAYLIGHT" + NEW_LINE
			+ "TZOFFSETFROM:-0800" + NEW_LINE
			+ "TZOFFSETTO:-0700" + NEW_LINE
			+ "TZNAME:PDT" + NEW_LINE
			+ "DTSTART:19700308T020000" + NEW_LINE
			+ "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU" + NEW_LINE
			+ "END:DAYLIGHT" + NEW_LINE
			+ "BEGIN:STANDARD" + NEW_LINE
			+ "TZOFFSETFROM:-0700" + NEW_LINE
			+ "TZOFFSETTO:-0800" + NEW_LINE
			+ "TZNAME:PST" + NEW_LINE
			+ "DTSTART:19701101T020000" + NEW_LINE
			+ "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU" + NEW_LINE
			+ "END:STANDARD" + NEW_LINE
			+ "END:VTIMEZONE" + NEW_LINE;
	final static String TZID = "America/Los_Angeles";
	
	public static String generate_file_name_for_section(Section sec, Course c) 
	{
		return c.getSubjectCodePrefix() + c.getSubjectCodeSuffix() + "-"+ sec.getTerm().split(" ")[1] + "-" + sec.getSectionNumber();//  + ".ics";
	}
	
	public static String generate_ics_for_section(Section sec, Course c)
	{
		String result = "BEGIN:VCALENDAR" + NEW_LINE
		+ "CALSCALE:GREGORIAN" + NEW_LINE
		+ "VERSION:2.0" + NEW_LINE
		// + "X-WR-CALNAME:" + title + NEW_LINE
		+ "METHOD:PUBLISH" + NEW_LINE
		+ "PRODID:" + PRODID + NEW_LINE // Need changing
		+ TIME_ZONE;
		Boolean success = false;
		/// ADD VEVENT
		for (MeetingSchedule sche : sec.getMeetingSchedules())
		{
			Date start_time_date;
			Date end_time_date;
			Date until_date;
			try
			{
				// System.out.println(sche.getStartDate() + sche.getStartTime());
				start_time_date = SOURCE_DATE_FORMAT.parse(sche.getStartDate() + sche.getStartTime());
				end_time_date = SOURCE_DATE_FORMAT.parse(sche.getStartDate() + sche.getEndTime());
				until_date = SOURCE_DATE_FORMAT.parse(sche.getEndDate() + sche.getEndTime());
			}
			catch(ParseException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
				/*System.out.println(sche.getStartDate() + sche.getStartTime());
				System.out.println(sche.getStartDate() + sche.getEndTime());
				System.out.println(sche.getEndDate() + sche.getEndTime());
				System.out.println(sec.getComponent());
				System.out.println(c.getTitle());*/
				continue;
			}
			String by_day = get_by_day(sche.getDays());
			String start_time = get_actual_time(start_time_date, by_day);
			String end_time = get_actual_time(end_time_date, by_day);
			String until = TARGET_DATE_FORMAT.format(until_date);
			String title = c.getSubjectCodePrefix()+c.getSubjectCodeSuffix() 
			+ " " + sec.getComponent();
			String description = c.getDescription();
			String location = sche.getLocation();
			result += generate_vevent(start_time, end_time, by_day, until, title, description, location);
			success = true;
		}
		result += "END:VCALENDAR";
		if (success) {
			return result;	
		} else {
			return "";
		}
	}
	
	private static String get_actual_time(Date rough_date, String by_day_in_week) {
		int first_day_in_week = 1; 
		// On unexpected cases, the first class would be defaulted on Monday
		// The date returned would be the first day of class that is possible 
		// after the rough date
		// NOTE: Requires that the days in week field is written in increasing format, Sun, Mon ... Sat
		if (by_day_in_week.length() >= 2) {
			switch (by_day_in_week.substring(0, 2)) {
				case "TU":
					first_day_in_week = 2;
					break;
				case "WE":
					first_day_in_week = 3;
					break;
				case "TH":
					first_day_in_week = 4;
					break;
				case "FR":
					first_day_in_week = 5;
					break;
				case "SA":
					first_day_in_week = 6;
					break;
				case "SU":
					first_day_in_week = 0;
					break;
			}
		}
		int date_dif = (first_day_in_week + 7 - rough_date.getDay()) % 7;
		// System.out.println(date_dif);
		Date actual_date = new Date(date_dif * 24 * 3600 * 1000 + rough_date.getTime());
		return TARGET_DATE_FORMAT.format(actual_date);
	}
	
	private static String generate_vevent(String start_time, String end_time, 
			String by_day, String until, String title, String description, String location)
	{
		String now = TARGET_DATE_FORMAT.format(new Date());
		String uid = "explore_courses" + UUID.randomUUID().toString();
		String result = "BEGIN:VEVENT" + NEW_LINE  
				+ "DTEND;TZID=" + TZID + ":" + end_time + NEW_LINE
				+ "LAST-MODIFIED:" + now + NEW_LINE
				+ "UID:" + uid + NEW_LINE
				+ "DTSTAMP:" + now + NEW_LINE
				+ "LOCATION:" + location + NEW_LINE
				+ "DESCRIPTION:" + description + NEW_LINE
				+ "STATUS:CONFIRMED" + NEW_LINE
				+ "SEQUENCE:0" + NEW_LINE
				+ "SUMMARY:" + title + NEW_LINE
				+ "DTSTART;TZID=" + TZID + ":" + start_time + NEW_LINE
				+ "CREATED:" + now + NEW_LINE
				+ "RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=" + until + ";BYDAY=" + by_day + NEW_LINE // Change 3, 4
				+ "END:VEVENT" + NEW_LINE;
		return result;
	}
	
	private static String get_by_day(String input)
	{
		String[] days = input.trim().split("[\t\n]+");
		String by_day = "";
		for (String day : days)
		{
			String formatted_day = "";
			switch (day) {
				case "Monday":
					formatted_day = "MO";
					break;
				case "Tuesday":
					formatted_day = "TU";
					break;
				case "Wednesday":
					formatted_day = "WE";
					break;
				case "Thursday":
					formatted_day = "TH";
					break;
				case "Friday":
					formatted_day = "FR";
					break;
				case "Saturday":
					formatted_day = "SA";
					break;
				case "Sunday":
					formatted_day = "SU";
					break;
				default:
					continue;
			}
			if (by_day != "") {
				by_day += ",";
			}
			by_day += formatted_day;
		}
		return by_day;
	}

	public static boolean can_generate_ics_for_section(Section sec)
	{
		return ALLOWED_COMPONENTS.contains(sec.getComponent());
	}
	
}

