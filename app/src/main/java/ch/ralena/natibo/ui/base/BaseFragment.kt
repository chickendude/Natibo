package ch.ralena.natibo.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The base class all fragments extend.
 *
 * This provides some convenience methods to inject your ViewModel, simplify view binding and
 * dependency injection, and handles [onCreate] and [onCreateView] for you.
 *
 * You should override [setupViews] in your Fragment. [setupViews] is where you can do any work
 * with your layout's views.
 *
 * @param inflate this should be your viewbinding's inflate method, e.g.
 * `MyFragmentBinding::inflate`
 */
abstract class BaseFragment<VB : ViewBinding, LISTENER, VM : BaseViewModel<LISTENER>>(
		private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {
	@Inject
	lateinit var viewModel: VM

	lateinit var binding: VB

	override fun onCreateView(
			inflater: LayoutInflater,
			container: ViewGroup?,
			savedInstanceState: Bundle?
	): View? {
		binding = inflate(inflater, container, false)
		val view: View = binding.root
		setupViews(view)
		return view
	}

	abstract fun setupViews(view: View)
}