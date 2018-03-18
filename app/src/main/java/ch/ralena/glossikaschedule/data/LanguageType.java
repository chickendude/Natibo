package ch.ralena.glossikaschedule.data;

import android.os.Parcel;
import android.os.Parcelable;

public class LanguageType implements Parcelable {
	String name;
	int drawable;

	public LanguageType(String name, int drawable) {
		this.name = name;
		this.drawable = drawable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDrawable() {
		return drawable;
	}

	public void setDrawable(int drawable) {
		this.drawable = drawable;
	}

	protected LanguageType(Parcel in) {
		name = in.readString();
		drawable = in.readInt();
	}

	public static final Creator<LanguageType> CREATOR = new Creator<LanguageType>() {
		@Override
		public LanguageType createFromParcel(Parcel in) {
			return new LanguageType(in);
		}

		@Override
		public LanguageType[] newArray(int size) {
			return new LanguageType[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(name);
		parcel.writeInt(drawable);
	}
}
