package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Day extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	RealmList<SentenceSet> sentenceSets;
	boolean isCompleted;

	public String getId() {
		return id;
	}

	public RealmList<SentenceSet> getSentenceSets() {
		return sentenceSets;
	}

	public void setSentenceSets(RealmList<SentenceSet> sentenceSets) {
		this.sentenceSets = sentenceSets;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean completed) {
		isCompleted = completed;
	}

	//	public String getFormattedDateCompleted() {
//		if (dateCompleted > 0) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM. yyyy");
//			return dateFormat.format(dateCompleted);
//		} else {
//			return "";
//		}
//	}
//
//	public String getFormattedDateShort() {
//		if (dateCompleted > 0) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
//			return dateFormat.format(dateCompleted);
//		} else {
//			return "";
//		}
//	}
//
//	public void updateDateCompleted() {
//		if (dateCompleted == 0) {
//			// check if all study items have been completed
//			boolean allCompleted = true;
//			for (StudyItem item : studyItems) {
//				allCompleted = allCompleted && item.isCompleted();
//			}
//			// if so, save today's date as the new completed date
//			if (allCompleted) {
//				dateCompleted = Calendar.getInstance().getTimeInMillis();
//			}
//		}
//	}
//
//	public boolean wasCompletedToday() {
//		if (dateCompleted > 0) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM. yyyy");
//			String today = dateFormat.format(Calendar.getInstance().getTimeInMillis());
//			return dateFormat.format(dateCompleted).equals(today);
//		} else {
//			return false;
//		}
//	}
}
