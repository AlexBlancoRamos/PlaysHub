package com.alexblanco.playshub

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "HomeFragment"
private const val PICK_IMAGE_REQUEST = 1

class HomeFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private var imageViewSelected: ImageView? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showCreatePostPopup()
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter()
        recyclerView.adapter = postAdapter

        loadPosts() // Llamar a loadPosts() después de inicializar el adaptador y el RecyclerView
    }

    private fun loadPosts() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val postsList = mutableListOf<Post>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let { postsList.add(it) }
                }
                postAdapter.submitList(postsList)
            }
            .addOnFailureListener { exception ->
                // Manejar errores aquí
            }
    }

    private fun showCreatePostPopup() {
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_create_post, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        val textViewUsername = popupView.findViewById<TextView>(R.id.textViewEmail)
        val editTextPost = popupView.findViewById<EditText>(R.id.editTextPost)
        imageViewSelected = popupView.findViewById<ImageView>(R.id.imageViewSelected)
        val buttonSelectImage = popupView.findViewById<Button>(R.id.buttonSelectImage)
        val buttonCreatePost = popupView.findViewById<Button>(R.id.buttonSavePost)

        val user = FirebaseAuth.getInstance().currentUser
        textViewUsername.text = user?.email

        buttonSelectImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST)
        }

        buttonCreatePost.setOnClickListener {
            val postText = editTextPost.text.toString().trim()
            if (postText.isNotEmpty()) {
                createPost(postText, selectedImageUri)
                popupWindow.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor, escribe algo en tu post.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPost(postText: String, imageUri: Uri?) {
        val db = FirebaseFirestore.getInstance()
        val postsCollection = db.collection("posts")

        val user = FirebaseAuth.getInstance().currentUser
        val postId = postsCollection.document().id

        val post = hashMapOf(
            "userId" to user?.uid,
            "username" to user?.email,
            "postText" to postText,
            "imageUrl" to ""
        )

        if (imageUri != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("post_images/$postId.jpg")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    post["imageUrl"] = uri.toString()
                    savePostToFirestore(postId, post)
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error al subir la imagen", e)
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            savePostToFirestore(postId, post)
        }
    }

    private fun savePostToFirestore(postId: String, post: HashMap<String, String?>) {
        val postsCollection = FirebaseFirestore.getInstance().collection("posts")
        postsCollection.document(postId).set(post)
            .addOnSuccessListener {
                Log.d(TAG, "Post creado con éxito")
                Toast.makeText(requireContext(), "Post creado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al crear el post", e)
                Toast.makeText(requireContext(), "Error al crear el post", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageViewSelected?.setImageURI(selectedImageUri)
            imageViewSelected?.visibility = View.VISIBLE
        }
    }
}
