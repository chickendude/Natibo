package ch.ralena.glossikaschedule.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Pack;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class CourseDetailAdapter extends RecyclerView.Adapter<CourseDetailAdapter.ViewHolder> {

	PublishSubject<Pack> packSubject = PublishSubject.create();

	public PublishSubject<Pack> asObservable() {
		return packSubject;
	}

	private RealmList<Pack> targetPacks;
	private RealmList<Pack> packs;

	public CourseDetailAdapter(RealmList<Pack> targetPacks, RealmList<Pack> packs) {
		this.targetPacks = targetPacks;
		this.packs = packs;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_detail, parent, false);
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
		private TextView book;
		private Pack pack;

		ViewHolder(View view) {
			super(view);
			book = view.findViewById(R.id.bookLabel);
			view.setOnClickListener(v -> packSubject.onNext(pack));
		}

		void bindView(Pack pack) {
			this.pack = pack;
			book.setText(pack.getBook());
			if (targetPacks.contains(pack))
				book.setText(pack.getBook() + "-");
		}
	}
}
