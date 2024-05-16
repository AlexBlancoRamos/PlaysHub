package com.alexblanco.playshub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var postList = mutableListOf<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun submitList(posts: List<Post>) {
        postList.clear()
        postList.addAll(posts)
        notifyDataSetChanged()
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
        private val textViewPostText: TextView = itemView.findViewById(R.id.textViewPostText)
        private val imageViewPost: ImageView = itemView.findViewById(R.id.imageViewPost)

        fun bind(post: Post) {
            // Configura otros datos de la publicación
            textViewUsername.text = post.username
            textViewPostText.text = post.postText

            // Carga la imagen utilizando Picasso
            Picasso.get().load(post.imageUrl).into(imageViewPost)
        }
    }
}