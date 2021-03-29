package ch.ralena.natibo.ui.adapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Pack;
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
		private CheckedTextView book;
		private Pack pack;

		ViewHolder(View view) {
			super(view);
			book = view.findViewById(R.id.packTitleLabel);
			view.setOnClickListener(v -> packSubject.onNext(pack));
		}

		void bindView(Pack pack) {
			this.pack = pack;
			book.setText(pack.getBook());
			book.setChecked(targetPacks.contains(pack));
			int color = book.isChecked() ? R.color.colorPrimaryDark : R.color.colorPrimaryLight;
			book.setTextColor(ContextCompat.getColor(book.getContext(), color));
		}
	}
}
