import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Post
import com.example.yadshniya.R
import com.squareup.picasso.Picasso

class PostAdapter(private val posts: List<Post>, private val isProfileScreen: Boolean) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.ownerName.text = post.userId
        holder.itemLocation.text = post.location
        holder.itemDescription.text = post.description
        holder.itemPrice.text = "$${post.price}"

        // Load image using Picasso
        Picasso.get().load(post.imageUrl).into(holder.postImage)

        // Show or hide buttons based on the screen
        if (isProfileScreen) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerName: TextView = itemView.findViewById(R.id.owner_name)
        val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_post)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_post)
    }
}
