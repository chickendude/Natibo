package ch.ralena.natibo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Language;
import ch.ralena.natibo.object.Sentence;
import ch.ralena.natibo.object.SentenceGroup;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class StudySessionAdapter extends RecyclerView.Adapter<StudySessionAdapter.ViewHolder> {

	PublishSubject<Sentence> languageSubject = PublishSubject.create();

	public PublishSubject<Sentence> asObservable() {
		return languageSubject;
	}

	private List<SentenceGroup> sentenceGroups;
	private RealmList<Language> languageIds;

	public StudySessionAdapter(RealmList<Language> languageIds, List<SentenceGroup> sentenceGroups) {
		this.sentenceGroups = sentenceGroups;
		this.languageIds = languageIds;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_session_container, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount()) {
			holder.bindView(sentenceGroups.get(position));

			// set up nested recycler view to show all the sentenceGroups
			StudySessionSentenceAdapter adapter = new StudySessionSentenceAdapter(languageIds, sentenceGroups.get(position).getSentences());
			holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
			holder.recyclerView.setAdapter(adapter);
		}
	}

	@Override
	public int getItemCount() {
		return sentenceGroups.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		TextView index;
		RecyclerView recyclerView;

		ViewHolder(View view) {
			super(view);
			index = view.findViewById(R.id.indexLabel);
			recyclerView = view.findViewById(R.id.recyclerView);
		}

		void bindView(SentenceGroup sentencePair) {
			index.setText("" + sentencePair.getSentences().first().getIndex());
		}
	}
}
