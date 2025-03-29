import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.FirebaseModel
import com.example.yadshniya.Model.Model
import com.example.yadshniya.Model.Post
import com.example.yadshniya.PostsListViewModel
import com.example.yadshniya.R
import com.squareup.picasso.Picasso

class PostAdapter(
    var posts: MutableList<Post>,
    private val isProfileScreen: Boolean) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private var viewModel: PostsListViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.ownerName.text = post.ownerName
        holder.itemLocation.text = post.location
        holder.itemDescription.text = post.description
        holder.itemPrice.text = "${post.price}₪"

        Picasso.get().load(post.imageUrl).into(holder.postImage)

        if (!post.ownerImageUrl.isNullOrEmpty()) {
            Picasso.get().load(post.ownerImageUrl).into(holder.ownerImage)
        } else {
            holder.ownerImage.setImageResource(R.drawable.sample_profile)
        }

        if (isProfileScreen) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }

        setUI(holder, position)

    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts.toMutableList()
        notifyDataSetChanged()
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerName: TextView = itemView.findViewById(R.id.owner_name)
        val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val ownerImage: ImageView = itemView.findViewById(R.id.profile_image)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_post)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_post)
    }

    private fun setUI(holder: PostViewHolder,  position: Int) {
        var isEditing = false

        val editButton = holder.itemView.findViewById<ImageButton>(R.id.btn_edit_post)
        val deleteButton = holder.itemView.findViewById<ImageButton>(R.id.btn_delete_post)
        val description = holder.itemView.findViewById<TextView>(R.id.item_description)
        val price = holder.itemView.findViewById<TextView>(R.id.item_price)
        val postImage = holder.itemView.findViewById<ImageView>(R.id.post_image)

        val context = holder.itemView.context
        val editDescription = EditText(context)
        val editPrice = EditText(context)

        editDescription.setText(description.text)
        editDescription.textSize =
            description.textSize / context.resources.displayMetrics.scaledDensity
        editDescription.setTextColor(description.currentTextColor)
        editDescription.setTypeface(description.typeface)
        editDescription.visibility = View.GONE
        editDescription.id = View.generateViewId()

        editPrice.setText(price.text)
        editPrice.textSize = price.textSize / context.resources.displayMetrics.scaledDensity
        editPrice.setTextColor(price.currentTextColor)
        editPrice.setTypeface(price.typeface, Typeface.BOLD)
        editPrice.visibility = View.GONE
        editPrice.id = View.generateViewId()

        val layoutParamsDesc = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = postImage.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(0, 8, 0, 0)
        }

        val layoutParamsPrice = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = editDescription.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(0, 8, 0, 0)
        }

        editDescription.layoutParams = layoutParamsDesc
        editPrice.layoutParams = layoutParamsPrice

        val parentLayout = description.parent as ConstraintLayout
        parentLayout.addView(editDescription)
        parentLayout.addView(editPrice)

        editButton.setOnClickListener {
            Log.d("DEBUG", "Edit button clicked! isEditing: $isEditing")

            if (isEditing) {
                description.text = editDescription.text.toString()
                price.text = editPrice.text.toString()
                description.visibility = View.VISIBLE
                price.visibility = View.VISIBLE
                editDescription.visibility = View.GONE
                editPrice.visibility = View.GONE
            } else {
                editDescription.setText(description.text.toString())
                editPrice.setText(price.text.toString())
                description.visibility = View.GONE
                price.visibility = View.GONE
                editDescription.visibility = View.VISIBLE
                editPrice.visibility = View.VISIBLE
                editDescription.requestFocus()
            }

            isEditing = !isEditing
        }

        deleteButton.setOnClickListener {
            Log.d("Delete", "delete button in post clicked!")
            val postToDelete = posts[position]
            Log.d("Delete", "post to delete: $postToDelete")

            Model.instance().deletePost(postToDelete){
                viewModel?.removePost(postToDelete)
            }

            posts.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, posts.size)
        }
    }
}
