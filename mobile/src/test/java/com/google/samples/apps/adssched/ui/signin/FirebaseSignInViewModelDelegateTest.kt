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

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.samples.apps.adssched.androidtest.util.LiveDataTestUtil
import com.google.samples.apps.adssched.shared.data.signin.AuthenticatedUserInfo
import com.google.samples.apps.adssched.shared.domain.prefs.NotificationsPrefIsShownUseCase
import com.google.samples.apps.adssched.shared.result.Result
import com.google.samples.apps.adssched.test.util.SyncTaskExecutorRule
import com.google.samples.apps.adssched.test.util.fakes.FakePreferenceStorage
import com.google.samples.apps.adssched.ui.schedule.FakeObserveUserAuthStateUseCase
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [FirebaseSignInViewModelDelegate]
 */
class FirebaseSignInViewModelDelegateTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule var syncTaskExecutorRule = SyncTaskExecutorRule()

    @Test
    fun testSignedOut() {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(null)
            ),
            createNotificationsPrefIsShownUseCase()
        )

        val currentFirebaseUser = LiveDataTestUtil.getValue(
            subject.currentFirebaseUser
        ) as Result.Success<AuthenticatedUserInfo>?
        assertEquals(
            null,
            currentFirebaseUser?.data?.getUid()
        )
        assertEquals(
            null,
            LiveDataTestUtil.getValue(subject.currentUserImageUri)
        )
        assertFalse(subject.isSignedIn())
    }

    @Test
    fun testSignedInRegistered() {

        val user = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn("123")
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(user)
            ),
            createNotificationsPrefIsShownUseCase()
        )

        assertEquals(
            user.getUid(),
            (LiveDataTestUtil.getValue(subject.currentFirebaseUser) as Result.Success).data.getUid()
        )
        assertEquals(
            user.getPhotoUrl(),
            LiveDataTestUtil.getValue(subject.currentUserImageUri)
        )
        assertTrue(subject.isSignedIn())
    }

    @Test
    fun testSignedInNotRegistered() {

        val user = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn("123")
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(user)
            ),
            createNotificationsPrefIsShownUseCase()
        )

        assertEquals(
            user.getUid(),
            (LiveDataTestUtil.getValue(subject.currentFirebaseUser) as Result.Success).data.getUid()
        )
        assertEquals(
            user.getPhotoUrl(),
            LiveDataTestUtil.getValue(subject.currentUserImageUri)
        )
        assertTrue(subject.isSignedIn())
    }

    @Test
    fun testPostSignIn() {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(null)
            ),
            createNotificationsPrefIsShownUseCase()
        )

        subject.emitSignInRequest()

        // Check that the emitted event is a sign in request
        assertEquals(
            LiveDataTestUtil.getValue(subject.performSignInEvent)?.peekContent(),
            SignInEvent.RequestSignIn
        )
    }

    @Test
    fun testPostSignOut() {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(null)
            ),
            createNotificationsPrefIsShownUseCase()
        )

        subject.emitSignOutRequest()

        assertEquals(
            LiveDataTestUtil.getValue(subject.performSignInEvent)?.peekContent(),
            SignInEvent.RequestSignOut
        )
    }

    private fun createNotificationsPrefIsShownUseCase(): NotificationsPrefIsShownUseCase {
        return NotificationsPrefIsShownUseCase(FakePreferenceStorage())
    }
}
