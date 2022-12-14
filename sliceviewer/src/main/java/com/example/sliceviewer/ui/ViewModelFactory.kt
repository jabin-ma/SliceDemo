/*
 *  Copyright 2018 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.sliceviewer.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sliceviewer.domain.LocalUriDataSource
import com.example.sliceviewer.domain.UriDataSource
import com.example.sliceviewer.ui.list.SliceViewModel
import com.example.sliceviewer.ui.single.SingleSliceViewModel

class ViewModelFactory private constructor(
    private val uriDataSource: UriDataSource
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SliceViewModel::class.java)) {
            return SliceViewModel(uriDataSource) as T
        } else if (modelClass.isAssignableFrom(SingleSliceViewModel::class.java)) {
            return SingleSliceViewModel(uriDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory? {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    if (INSTANCE == null) {
                        val sharedPrefs = context.getSharedPreferences(
                            LocalUriDataSource.SHARED_PREFS_NAME,
                            Context.MODE_PRIVATE
                        )
                        INSTANCE = ViewModelFactory(LocalUriDataSource(sharedPrefs))
                    }
                }
            }
            return INSTANCE
        }
    }
}