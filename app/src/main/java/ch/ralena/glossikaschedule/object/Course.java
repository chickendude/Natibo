package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;


/*
 *
 * Sentence sets, each set will hold it's review schedule
 * The schedule will create the sentence sets, ie. will create a new sentence set each time you study
 * a new set of words.
 *
 * Creating a schedule:
 * OPTIONS:
 * 0.1 - select base/target language
 * 0.2 - select packs
 * 1. Select pre-packaged schedule
 * 2. New Glossika
 *	a. # sentences/day
 *	b. # days to review/reviews per day
 *		+ pre-set
 *		+ set manually
 * 3. Create your own
 *	a. Add sentence pack type
 *		+ # sentences
 *		+ # days to review (1 = GMS style) / reviews per day (similar to 2.b)
 *		+ add languages (e.g. base + target + target, target + base, target, etc.)
 *		+ starting sentence (<= 0 means not started yet)
 *		+ study pattern: X = X + 1
 * 4? Temporary schedule (eg. quick run-through of all sentences)
 *
 *
 * Course:
 * - realm list of sentence packs
 *
 */


public class Course extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private String title;
	private Language baseLanguage;
	private Language targetLanguage;
	private Day currentDay;
	private int numReps;
	private RealmList<Day> pastDays = new RealmList<>();
	private RealmList<Schedule> schedules = new RealmList<>();

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Language getBaseLanguage() {
		return baseLanguage;
	}

	public void setBaseLanguage(Language baseLanguage) {
		this.baseLanguage = baseLanguage;
	}

	public Language getTargetLanguage() {
		return targetLanguage;
	}

	public void setTargetLanguage(Language targetLanguage) {
		this.targetLanguage = targetLanguage;
	}

	public int getNumReps() {
		return numReps;
	}

	public void setNumReps(int numReps) {
		this.numReps = numReps;
	}

	public RealmList<Day> getPastDays() {
		return pastDays;
	}

	public void setPastDays(RealmList<Day> pastDays) {
		this.pastDays = pastDays;
	}

	public Day getCurrentDay() {
		return currentDay;
	}

	public void setCurrentDay(Day currentDay) {
		this.currentDay = currentDay;
	}

	public RealmList<Schedule> getSchedules() {
		return schedules;
	}
}
