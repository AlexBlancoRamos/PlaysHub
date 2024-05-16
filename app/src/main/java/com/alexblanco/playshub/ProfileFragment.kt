package com.alexblanco.playshub

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setup(view)
        return view
    }

    private fun setup(view: View) {
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val phoneEditText = view.findViewById<EditText>(R.id.phoneEditText)
        val genderEditText = view.findViewById<EditText>(R.id.genderEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val postalCodeEditText = view.findViewById<EditText>(R.id.postalCodeEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        emailEditText.setText(document.getString("email"))
                        phoneEditText.setText(document.getString("telefono"))
                        genderEditText.setText(document.getString("sexo"))
                        addressEditText.setText(document.getString("direccion"))
                        postalCodeEditText.setText(document.getString("codi_postal"))
                    }
                }
        }

        saveButton.setOnClickListener {
            val userUpdates = hashMapOf<String, Any>(
                "telefono" to phoneEditText.text.toString(),
                "sexo" to genderEditText.text.toString(),
                "direccion" to addressEditText.text.toString(),
                "codi_postal" to postalCodeEditText.text.toString()
            )

            if (currentUser != null) {
                db.collection("users").document(currentUser.uid)
                    .update(userUpdates)
                    .addOnSuccessListener {
                        showAlert("Éxito", "Los cambios se han guardado correctamente.")
                    }
                    .addOnFailureListener { e ->
                        showAlert("Error", "Error al guardar los cambios: ${e.message}")
                    }
            }
        }

        closeButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, AuthActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
