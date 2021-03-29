package ch.ralena.natibo.ui.adapter;

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

public class CourseBookAdapter extends RecyclerView.Adapter<CourseBookAdapter.ViewHolder> {

	PublishSubject<Pack> packSubject = PublishSubject.create();

	public PublishSubject<Pack> asObservable() {
		return packSubject;
	}

	private RealmList<Pack> packs;

	public CourseBookAdapter(RealmList<Pack> packs) {
		this.packs = packs;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_book_list, parent, false);
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
		private TextView packTitle;
		private TextView progress;
		private Pack pack;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			packTitle = view.findViewById(R.id.packTitleLabel);
			progress = view.findViewById(R.id.progressLabel);
			this.view.setOnClickListener(v -> packSubject.onNext(pack));
		}

		void bindView(Pack pack) {
			this.pack = pack;
			packTitle.setText(pack.getBook());
			progress.setText("");
		}
	}
}
