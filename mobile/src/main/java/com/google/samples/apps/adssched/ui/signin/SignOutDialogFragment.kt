/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.adssched.ui.signin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.samples.apps.adssched.databinding.DialogSignOutBinding
import com.google.samples.apps.adssched.shared.domain.internal.DefaultScheduler
import com.google.samples.apps.adssched.shared.result.EventObserver
import com.google.samples.apps.adssched.shared.util.viewModelProvider
import com.google.samples.apps.adssched.ui.signin.SignInEvent.RequestSignOut
import com.google.samples.apps.adssched.util.signin.SignInHandler
import com.google.samples.apps.adssched.widget.CustomDimDialogFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Dialog that confirms that a user wishes to sign out.
 */
class SignOutDialogFragment : CustomDimDialogFragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var signInHandler: SignInHandler

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var signOutViewModel: SignInViewModel

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        signOutViewModel = viewModelProvider(viewModelFactory)
        val binding = DialogSignOutBinding.inflate(inflater, container, false).apply {
            viewModel = signOutViewModel
        }

        signOutViewModel.performSignInEvent.observe(this, Observer { request ->
            if (request.peekContent() == RequestSignOut) {
                request.getContentIfNotHandled()
                context?.let {
                    DefaultScheduler.execute {
                        signInHandler.signOut(it) {
                            dismiss()
                        }
                    }
                }
            }
        })

        signOutViewModel.dismissDialogAction.observe(this, EventObserver {
            dismiss()
        })
        return binding.root
    }
}
