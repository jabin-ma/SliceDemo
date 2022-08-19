/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sliceviewer.ui.list

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.slice.widget.SliceView
import androidx.slice.widget.SliceView.SliceMode
import com.example.sliceviewer.R
import com.example.sliceviewer.R.layout
import com.example.sliceviewer.util.bind

class SliceAdapter(
    val lifecycleOwner: LifecycleOwner,
    val selectedMode: LiveData<Int>
) : ListAdapter<Uri, SliceViewHolder>(
    SlicesDiff
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliceViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(layout.slice_row, parent, false)
        return SliceViewHolder(cardView, selectedMode, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: SliceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

class SliceViewHolder(
    cardView: View,
    @SliceMode val selectedMode: LiveData<Int>,
    private val lifecycleOwner: LifecycleOwner
) : ViewHolder(cardView) {
    private val context: Context = cardView.context
    private val sliceView: SliceView = cardView.findViewById(R.id.slice)
    private var needBind = true;

    fun bind(uri: Uri) {
        if (!needBind) return
        needBind = false
        sliceView.bind(
            context = context,
            lifecycleOwner = lifecycleOwner,
            uri = uri,
            scrollable = false
        )

        sliceView.isClickable=false
        selectedMode.observe(lifecycleOwner) {
            sliceView.mode = it ?: SliceView.MODE_LARGE
        }
    }
}

object SlicesDiff : DiffUtil.ItemCallback<Uri>() {
    override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem === newItem

    override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
}