package ch.ralena.glossikaschedule.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ScheduleType implements Parcelable {
	private String title;
	private String[][] schedule;
	private int minutesDay;
	private int totalReps;
	private String courseLength;
	private String courseLengthSmall;
	private String summary;
	private String description;

	public ScheduleType(String title, String[][] schedule, int minutesDay, int totalReps, String courseLength, String courseLengthSmall, String summary, String description) {
		this.title = title;
		this.schedule = schedule;
		this.minutesDay = minutesDay;
		this.totalReps = totalReps;
		this.courseLength = courseLength;
		this.courseLengthSmall = courseLengthSmall;
		this.summary = summary;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[][] getSchedule() {
		return schedule;
	}

	public void setSchedule(String[][] schedule) {
		this.schedule = schedule;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMinutesDay() {
		return minutesDay;
	}

	public String getRepsAsString() {
		return String.format("%,d", totalReps);
	}

	public int getTotalReps() {
		return totalReps;
	}

	public String getCourseLength() {
		return courseLength;
	}

	public String getCourseLengthSmall() {
		return courseLengthSmall;
	}

	protected ScheduleType(Parcel in) {
		title = in.readString();
		minutesDay = in.readInt();
		totalReps = in.readInt();
		courseLength = in.readString();
		courseLengthSmall = in.readString();
		summary = in.readString();
		description = in.readString();
	}

	public static final Creator<ScheduleType> CREATOR = new Creator<ScheduleType>() {
		@Override
		public ScheduleType createFromParcel(Parcel in) {
			return new ScheduleType(in);
		}

		@Override
		public ScheduleType[] newArray(int size) {
			return new ScheduleType[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(title);
		parcel.writeInt(minutesDay);
		parcel.writeInt(totalReps);
		parcel.writeString(courseLength);
		parcel.writeString(courseLengthSmall);
		parcel.writeString(summary);
		parcel.writeString(description);
	}
}
