package ch.ralena.natibo.ui.language.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.*
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

		val id = arguments?.getLong(TAG_LANGUAGE_ID)
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

	override fun onLanguageLoaded(languageWithPacks: LanguageWithPacks) {
		val language = languageWithPacks.language
		val packs = languageWithPacks.packs
		binding.languageLabel.text = language.name
		binding.flagImage.setImageResource(language.flagDrawable)
		rvAdapter.loadLanguagePacks(packs)
	}

	override fun onLanguageNotFound() {
		Toast.makeText(context, getString(R.string.no_language_found), Toast.LENGTH_SHORT).show()
	}

	override fun onLanguagePackClicked(pack: PackRoom) {
		viewModel.languagePackSelected(pack)
	}
}