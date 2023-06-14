package com.hungama.music.ui.main.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.main.view.fragment.SearchAllTabFragment
import com.hungama.music.data.model.SuggestionTextModel
import com.hungama.music.utils.CommonUtils.setLog

class SuggestionAdapter(private val mList: List<SuggestionTextModel>,
						var searchItem: SearchAllTabFragment
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

	// create new views
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		// inflates the card_view_design view
		// that is used to hold list item
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.search_suggestion_list_item, parent, false)

		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {

		val ItemsViewModel = mList[position]

		holder.textView.text = ItemsViewModel.search
		holder.textView2.text = ItemsViewModel.suggestion

		setLog("TAG","${holder.position}")
	}

	// return the number of the items in the list
	override fun getItemCount(): Int {
		return mList.size
	}

	// Holds the views for adding it to image and text
	inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),View.OnClickListener{
		val textView: TextView = itemView.findViewById(R.id.tvSearchItem)
		val textView2: TextView = itemView.findViewById(R.id.tvSearchItem2)

		init {
		    itemView.setOnClickListener(this)
			textView.setOnClickListener(this)
			textView2.setOnClickListener(this)
		}

		override fun onClick(p0: View?) {
			searchItem.SearchBarItemClick(position = position)
		}
	}
	interface SearchItem{
		fun SearchBarItemClick(position: Int)
	}
}
