package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import ch.ralena.glossikaschedule.data.LanguageData;
import ch.ralena.glossikaschedule.data.LanguageType;
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
*/


public class Schedule extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	String title;    // later switch to Schedule type?
	String language;
	RealmList<Day> schedule;

	// constructor
	public Schedule() { super(); }
	public Schedule(String title, String language) {
		this.title = title;
		this.language = language;
		schedule = new RealmList<Day>();
	}

	// getters and setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public RealmList<Day> getSchedule() {
		return schedule;
	}

	public void setSchedule(RealmList<Day> schedule) {
		this.schedule = schedule;
	}

	public void addDay(Day day) {
		schedule.add(day);
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public LanguageType getLanguageType() {
		LanguageType languageType = null;
		for (LanguageType language : LanguageData.languages) {
			if (this.language.equals(language.getName())) {
				return language;
			}
		}
		return languageType;
	}
}
