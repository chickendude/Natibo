package ch.ralena.natibo.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import javax.inject.Inject

abstract class BaseFragment<LISTENER, VM: BaseViewModel<LISTENER>>: Fragment() {
	@Inject
	lateinit var viewModel: VM

	val injector: PresentationComponent by lazy {
		(requireActivity() as MainActivity).activityComponent.newPresentationComponent()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		injectDependencies(injector)
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
			inflater: LayoutInflater,
			container: ViewGroup?,
			savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(provideLayoutId(), container, false)
		setupViews(view)
		return view
	}

	@LayoutRes
	abstract fun provideLayoutId(): Int
	abstract fun setupViews(view: View)
	abstract fun injectDependencies(injector: PresentationComponent)
}