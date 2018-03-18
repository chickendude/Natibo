package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class StudyItem extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	String mTitle;
	boolean mIsCompleted;

	public StudyItem() {
		super();
	}

	public StudyItem(String title) {
		mTitle = title;
		mIsCompleted = false;
	}

	public StudyItem(String title, boolean isCompleted) {
		mTitle = title;
		mIsCompleted = isCompleted;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public boolean isCompleted() {
		return mIsCompleted;
	}

	public void setCompleted(boolean completed) {
		mIsCompleted = completed;
	}

	public String getId() {
		return id;
	}

	public void setId(long id) {
		id = id;
	}
}
