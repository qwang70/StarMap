package edu.stanford.qiwen.starmap

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.stanford.qiwen.starmap.models.UserMap
import java.util.*

private const val TAG = "MapsAdapter"

class MapsAdapter(
    private val context: Context,
    val userMapsAll: List<UserMap>,
    private val onClickListener: OnClickListener
) :
    RecyclerView.Adapter<MapsAdapter.ViewHolder>(),
    Filterable {

    private var mUserMap: List<UserMap> = userMapsAll

    interface OnClickListener {
        fun onItemClick(userMap: UserMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_map, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount() = mUserMap.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userMap = mUserMap[position]
        holder.itemView.setOnClickListener {
            Log.i(TAG, "Tapped on position $position")
            onClickListener.onItemClick(userMap)
        }
        // Set up the view for each map.
        val textViewTitle = holder.itemView.findViewById<TextView>(R.id.tvMapTitle)
        val textViewNumPlaces = holder.itemView.findViewById<TextView>(R.id.tvNumPlaces)
        textViewTitle.text = userMap.title
        textViewNumPlaces.text = "${userMap.places.size} starred places"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Search Item
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                mUserMap = filterResults.values as List<UserMap>
                notifyDataSetChanged()
            }

            /*
                Perform the filtering based on the user search query.
                The search query maps to any substring in the map name, or the place name or
                description.
             */
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ROOT)

                val filterResults = FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty())
                    userMapsAll
                else
                    userMapsAll.filter {
                        it.title.toLowerCase(Locale.ROOT).contains(queryString) ||
                                it.places.any {
                                    it.title.toLowerCase(Locale.ROOT).contains(queryString)
                                } ||
                                it.places.any {
                                    it.description.toLowerCase(Locale.ROOT).contains(queryString)
                                }
                    }
                return filterResults
            }
        }
    }
}
