package com.alexblanco.playshub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setup()
    }

    private fun setup(){
        title = "Autenticación"

        val signButton = findViewById<Button>(R.id.singButton)
        val signEditText = findViewById<EditText>(R.id.emailSignEditText)
        val coEditText = findViewById<EditText>(R.id.passSignEditText)

        signButton.setOnClickListener {
            if (signEditText.text.isNotEmpty() && coEditText.text.isNotEmpty()) {

                val password = coEditText.text.toString().trim()

                if (!isPasswordValid(password)) {
                    showPasswordErrorAlert()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(signEditText.text.toString(), coEditText.text.toString()).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            addUserToFirestore(user.uid, signEditText.text.toString())
                        }
                        showHome(task.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun addUserToFirestore(userId: String, email: String) {
        val user = hashMapOf(
            "email" to email,
            "telefono" to "",
            "sexo" to "",
            "direccion" to "",
            "codi_postal" to ""
        )

        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                // User added successfully
            }
            .addOnFailureListener { e ->
                // Error adding user
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Error")
                builder.setMessage("Error al agregar usuario a Firestore: ${e.message}")
                builder.setPositiveButton("Aceptar", null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, AuthActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider)
        }
        startActivity(homeIntent)
    }

    private fun isPasswordValid(password: String): Boolean {
        var hasUppercase = false
        var hasDigit = false

        for (char in password) {
            if (char.isUpperCase()) {
                hasUppercase = true
            }
            if (char.isDigit()) {
                hasDigit = true
            }
            if (hasUppercase && hasDigit) {
                return true
            }
        }

        return false
    }

    private fun showPasswordErrorAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("La contraseña debe contener al menos una mayúscula, un número y tener al menos 8 caracteres.")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
