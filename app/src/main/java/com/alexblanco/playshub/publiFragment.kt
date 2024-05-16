package com.alexblanco.playshub

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

private const val TAG = "publiFragment"

class publiFragment : Fragment() {

    private lateinit var currentQuestionId: String
    private lateinit var currentCorrectAnswer: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_publi, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)

        // Cargar automáticamente una pregunta al entrar al fragmento
        loadRandomQuestion()

        // Configurar el botón de comprobar respuesta
        val buttonCheckAnswer = view.findViewById<Button>(R.id.buttonCheckAnswer)
        buttonCheckAnswer.setOnClickListener {
            checkAnswer()
            loadRandomQuestion()
        }

        // Configurar el botón de cambiar pregunta
        val buttonChangeQuestion = view.findViewById<Button>(R.id.buttonChangeQuestion)
        buttonChangeQuestion.setOnClickListener {
            // Cambiar la pregunta y deseleccionar los botones de opción
            loadRandomQuestion()
        }

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            // Inflar el diseño de la pantalla emergente
            val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_layout, null)

            // Crear y configurar la ventana emergente
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            // Mostrar la ventana emergente en la posición deseada
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            // Configurar el botón de guardar dentro del popup
            val buttonSave = popupView.findViewById<Button>(R.id.buttonSave)
            buttonSave.setOnClickListener {
                // Obtener los valores de los campos de texto
                val question = popupView.findViewById<EditText>(R.id.editTextQuestion).text.toString()
                val answer1 = popupView.findViewById<EditText>(R.id.editTextAnswer1).text.toString()
                val answer2 = popupView.findViewById<EditText>(R.id.editTextAnswer2).text.toString()
                val answer3 = popupView.findViewById<EditText>(R.id.editTextAnswer3).text.toString()
                val answerCorrect = popupView.findViewById<EditText>(R.id.editTextAnswerCorrect).text.toString()

                // Guardar los valores en Firebase Firestore
                saveQuestionToFirestore(question, answer1, answer2, answer3, answerCorrect)

                // Cerrar la ventana emergente después de guardar
                popupWindow.dismiss()
            }
        }
    }

    private fun saveQuestionToFirestore(
        question: String,
        answer1: String,
        answer2: String,
        answer3: String,
        answerCorrect: String
    ) {
        // Aquí deberías implementar la lógica para guardar la pregunta y sus respuestas en Firebase Firestore
        // Puedes usar la instancia de FirebaseFirestore para esto

        // Supongamos que tienes una referencia a la colección "preguntas" en Firestore.
        val db = FirebaseFirestore.getInstance()
        val questionsCollection = db.collection("preguntas")

        // Creamos un mapa con los datos de la pregunta y sus respuestas.
        val data = hashMapOf(
            "pregunta" to question,
            "respuesta1" to answer1,
            "respuesta2" to answer2,
            "respuesta3" to answer3,
            "respuesta_correcta" to answerCorrect
        )

        // Añadimos la pregunta a la colección en Firestore.
        questionsCollection.add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Pregunta guardada con ID: ${documentReference.id}")
                // Aquí puedes realizar acciones adicionales si la pregunta se guardó exitosamente
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al guardar la pregunta", e)
                // Aquí puedes manejar el caso en el que ocurra un error al guardar la pregunta
            }
    }

    private fun loadRandomQuestion() {
        val db = FirebaseFirestore.getInstance()
        val questionsCollection = db.collection("preguntas")

        questionsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val randomIndex = Random().nextInt(querySnapshot.size())
                    val randomDocument = querySnapshot.documents[randomIndex]
                    currentQuestionId = randomDocument.id
                    currentCorrectAnswer = randomDocument.getString("respuesta_correcta") ?: ""

                    val question = randomDocument.getString("pregunta") ?: ""
                    val answers = mutableListOf<String>()
                    answers.add(randomDocument.getString("respuesta1") ?: "")
                    answers.add(randomDocument.getString("respuesta2") ?: "")
                    answers.add(randomDocument.getString("respuesta3") ?: "")

                    val textViewQuestion = view?.findViewById<TextView>(R.id.textViewQuestion)
                    val radioButtonAnswer1 = view?.findViewById<TextView>(R.id.radioButtonAnswer1)
                    val radioButtonAnswer2 = view?.findViewById<TextView>(R.id.radioButtonAnswer2)
                    val radioButtonAnswer3 = view?.findViewById<TextView>(R.id.radioButtonAnswer3)



                    if (textViewQuestion != null) {
                        textViewQuestion.text = question
                    }
                    if (radioButtonAnswer1 != null) {
                        radioButtonAnswer1.text = answers[0]
                    }
                    if (radioButtonAnswer2 != null) {
                        radioButtonAnswer2.text = answers[1]
                    }
                    if (radioButtonAnswer3 != null) {
                        radioButtonAnswer3.text = answers[2]
                    }

                    val radioGroupAnswers = view?.findViewById<RadioGroup>(R.id.radioGroupAnswers)
                    radioGroupAnswers?.clearCheck()
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun checkAnswer() {
        val radioGroupAnswers = view?.findViewById<RadioGroup>(R.id.radioGroupAnswers)


        val selectedRadioButtonId = radioGroupAnswers?.checkedRadioButtonId
        val selectedRadioButton = selectedRadioButtonId?.let { view?.findViewById<RadioButton>(it) }
        val selectedAnswer = selectedRadioButton?.text.toString()

        if (selectedAnswer == currentCorrectAnswer) {
            Toast.makeText(requireContext(), "¡Respuesta correcta!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Respuesta incorrecta, intenta de nuevo.", Toast.LENGTH_SHORT).show()
        }
    }
}