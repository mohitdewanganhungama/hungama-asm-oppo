package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.CountryModel
import com.hungama.music.utils.ImageLoader
import kotlinx.android.synthetic.main.row_country.view.*
import kotlinx.android.synthetic.main.row_language.view.*

class CountryListAdapter(var context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listOfCountry = listOf<CountryModel>()
    var itemClick: ((CountryModel) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_language, parent, false))
        return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_country, parent, false), context).apply {
            itemClick = { countryModel ->
                this@CountryListAdapter.itemClick?.invoke(countryModel)
            }
        }
    }

    override fun getItemCount(): Int = listOfCountry.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val movieViewHolder = viewHolder as LanguageViewHolder
        val item = listOfCountry[position]
        movieViewHolder.bindView(item)
    }

    fun setCountryList(listOfCountry: List<CountryModel>) {
        this.listOfCountry = listOfCountry
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    class LanguageViewHolder(itemView: View, var context: Context) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((CountryModel) -> Unit)? = null

        fun bindView(countryModel: CountryModel) {
            //itemView.imageFlag.setImageResource(countryModel.imageURL)
            if (!TextUtils.isEmpty(countryModel.imageURL)){
                ImageLoader.loadImage(
                    context,
                    itemView.imageFlag,
                    countryModel.imageURL,
                    R.drawable.bg_gradient_placeholder
                )
            }
            itemView.txtCode.text = countryModel.code
            itemView.txtName.text = countryModel.country
            if(countryModel.isSelected){
                itemView.imageSelect.visibility = View.VISIBLE
            }else{
                itemView.imageSelect.visibility = View.GONE
            }

            itemView?.setOnClickListener {

                //invoke() function will pass the value to receiver function.
                itemClick?.invoke(countryModel)
            }
            //Glide.with(itemView.context).load(movieModel.moviePicture!!).into(itemView.imageMovie)
        }

    }

}