package ch.ralena.natibo.object;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
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
	private RealmList<Pack> basePacks;
	private RealmList<Pack> targetPacks;
	private Day currentDay;
	private int numReps;
	private int pauseMillis;
	private RealmList<Day> pastDays = new RealmList<>();
	private RealmList<Schedule> schedules = new RealmList<>();    // the different pieces that make up the study routine for each day

	// --- getters and setters ---

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

	public RealmList<Pack> getBasePacks() {
		return basePacks;
	}

	public void setBasePacks(RealmList<Pack> basePacks) {
		this.basePacks = basePacks;
	}

	public RealmList<Pack> getTargetPacks() {
		return targetPacks;
	}

	public void setTargetPacks(RealmList<Pack> targetPacks) {
		this.targetPacks = targetPacks;
	}

	public void setSchedules(RealmList<Schedule> schedules) {
		this.schedules = schedules;
	}

	public int getNumReps() {
		return numReps;
	}

	public void setNumReps(int numReps) {
		this.numReps = numReps;
	}

	public int getPauseMillis() {
		return pauseMillis;
	}

	public void setPauseMillis(int pauseMillis) {
		this.pauseMillis = pauseMillis;
		currentDay.setPauseMillis(pauseMillis);
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

	// --- helper methods ---

	public void addReps(int reps) {
		numReps += reps;
	}

	public void prepareNextDay(Realm realm) {
		// add current day to past days
		if (currentDay != null && currentDay.isCompleted()) {
			realm.executeTransaction(r -> pastDays.add(currentDay));
		}

		// create a new day
		realm.executeTransaction(r -> {
			Day day = r.createObject(Day.class, UUID.randomUUID().toString());

			// add the sentence sets from the current day to the next day
			if (currentDay != null) {
				day.getSentenceSets().addAll(currentDay.getSentenceSets());

				// move yesterday's new words to the front of the reviews
				SentenceSet lastSet = day.getSentenceSets().last();
				day.getSentenceSets().remove(lastSet);
				day.getSentenceSets().add(0, lastSet);
			}
			for (Schedule schedule : schedules) {
				RealmList<Integer> reviewPattern = schedule.getReviewPattern();
				int numSentences = schedule.getNumSentences();
				int sentenceIndex = schedule.getSentenceIndex();
				schedule.setSentenceIndex(sentenceIndex + numSentences);

				// create new set of sentences based off the schedule
				SentenceSet sentenceSet = new SentenceSet();
				sentenceSet.setBaseSentences(getSentences(sentenceIndex, numSentences, basePacks));
				sentenceSet.setTargetSentences(getSentences(sentenceIndex, numSentences, targetPacks));
				sentenceSet.setReviews(reviewPattern);
				sentenceSet.setFirstDay(true);
				sentenceSet.setOrder(schedule.getOrder());

				// add sentence set to list of sentencesets for the next day's studies
				day.getSentenceSets().add(sentenceSet);
			}
			day.setCompleted(false);
			day.setPauseMillis(pauseMillis);
			currentDay = day;
		});
		List<SentenceSet> emptySentenceSets = new ArrayList<>();
		for (SentenceSet set : currentDay.getSentenceSets()) {
			// create sentence set and mark it to be deleted if it is empty
			if (!set.buildSentences(realm)) {
				emptySentenceSets.add(set);
			}
		}

		realm.executeTransaction(r -> currentDay.getSentenceSets().removeAll(emptySentenceSets));
	}

	private RealmList<Sentence> getSentences(int index, int numSentences, RealmList<Pack> packs) {
		RealmList<Sentence> sentences = new RealmList<>();

		for (Pack pack : packs) {
			RealmList<Sentence> packSentences = pack.getSentences();
			if (index >= pack.getSentences().size())
				index -= packSentences.size();
			else {
				while (numSentences > 0) {
					if (index >= pack.getSentences().size())
						break;
					numSentences--;
					Sentence sentence = packSentences.get(index++);
					sentences.add(sentence);
				}
			}
		}
		return sentences;
	}

	public int getNumSentencesSeen() {
		int numSeen = 0;

		// count number of sentences we've studied in the past
		for (Day day : pastDays) {
			numSeen += day.getSentenceSets().get(0).getBaseSentences().size();
		}

		// if the current day has been completed, add those as well
		if (currentDay != null && currentDay.isCompleted())
			numSeen += currentDay.getSentenceSets().get(0).getBaseSentences().size();
		return numSeen;
	}

	public int getTotalReps() {
		int totalReps = numReps;
		if (currentDay != null)
			totalReps += currentDay.getTotalReviews() - currentDay.getNumReviewsLeft();
		return totalReps;
	}
}
