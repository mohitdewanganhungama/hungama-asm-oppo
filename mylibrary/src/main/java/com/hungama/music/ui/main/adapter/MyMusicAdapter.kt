import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.MyMusicDetailModel

class MyMusicAdapter(
    var context: Context,
    private val itemList: List<MyMusicDetailModel>
) : RecyclerView.Adapter<MyMusicAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(itemList[position].icon)
        holder.textView.text = itemList[position].title
        holder.rootLayout.background = ContextCompat.getDrawable(context, itemList[position].color)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iconView)
        val textView: TextView = itemView.findViewById(R.id.titleTv)
        val rootLayout: ConstraintLayout = itemView.findViewById(R.id.rootLayout)
    }
}
