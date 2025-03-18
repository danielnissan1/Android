import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Post
import com.example.yadshniya.R
import com.squareup.picasso.Picasso

class PostAdapter(private val postList: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ownerName: TextView = view.findViewById(R.id.owner_name)
        val postImage: ImageView = view.findViewById(R.id.post_image)
        val itemDescription: TextView = view.findViewById(R.id.item_description)
        val itemPrice: TextView = view.findViewById(R.id.item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.ownerName.text = post.userId ?: "Unknown User"
        holder.itemDescription.text = post.description
        holder.itemPrice.text = "$${post.id}" // Update this if needed

        // ✅ Load image with Picasso
        Picasso.get()
            .load(post.imageUrl) // URL from Firestore
            .placeholder(R.drawable.sample_post) // Image while loading
            .error(R.drawable.sample_post) // Image if loading fails
            .into(holder.postImage)
    }

    override fun getItemCount() = postList.size
}
