package ch.ralena.natibo.ui.language.detail

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.databinding.FragmentLanguageDetailBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.sentences.SentenceListFragment
import ch.ralena.natibo.ui.language.detail.adapter.LanguageDetailAdapter
import javax.inject.Inject

class LanguageDetailFragment :
		BaseFragment<FragmentLanguageDetailBinding,
				LanguageDetailViewModel.Listener,
				LanguageDetailViewModel>(FragmentLanguageDetailBinding::inflate),
		LanguageDetailViewModel.Listener,
		LanguageDetailAdapter.Listener {

	companion object {
		val TAG: String = LanguageDetailFragment::class.java.simpleName
		const val TAG_LANGUAGE_ID = "language_id"
	}

	@Inject
	lateinit var rvAdapter: LanguageDetailAdapter

	override fun setupViews(view: View) {
		viewModel.registerListener(this)

		val id = arguments?.getString(TAG_LANGUAGE_ID)
		viewModel.loadLanguage(id)

		binding.recyclerView.apply {
			adapter = rvAdapter
			layoutManager = LinearLayoutManager(context)
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onStart() {
		super.onStart()
		viewModel.registerListener(this)
		rvAdapter.registerListener(this)
	}

	override fun onStop() {
		super.onStop()
		viewModel.unregisterListener(this)
		rvAdapter.unregisterListener(this)
	}

	// ViewModel and Adapter listener functions

	override fun onLanguageLoaded(language: Language) {
		binding.languageLabel.text = language.longName
		binding.flagImageView.setImageResource(language.languageType.drawable)
		rvAdapter.loadLanguagePacks(language.packs.toList())
	}

	override fun onLanguagePackClicked(pack: Pack) {
		viewModel.languagePackSelected(pack)
	}
}