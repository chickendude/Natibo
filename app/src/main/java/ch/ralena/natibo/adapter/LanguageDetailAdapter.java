package ch.ralena.natibo.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Pack;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class LanguageDetailAdapter extends RecyclerView.Adapter<LanguageDetailAdapter.ViewHolder> {

	PublishSubject<Pack> packSubject = PublishSubject.create();

	public PublishSubject<Pack> asObservable() {
		return packSubject;
	}

	private RealmList<Pack> packs;

	public LanguageDetailAdapter(RealmList<Pack> packs) {
		this.packs = packs;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_detail, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount())
			holder.bindView(packs.get(position));
	}

	@Override
	public int getItemCount() {
		return packs.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView book;
		private TextView numSentences;
		private Pack pack;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			book = view.findViewById(R.id.packTitleLabel);
			numSentences = view.findViewById(R.id.numSentencesLabel);
			this.view.setOnClickListener(v -> packSubject.onNext(pack));
		}

		void bindView(Pack pack) {
			this.pack = pack;
			book.setText(pack.getBook());
			numSentences.setText("" + pack.getSentences().size());
		}
	}
}
