package com.example.condominioparquemar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if(auth.currentUser != null){
            // Inicio de sesión exitoso
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            startMainActivity()  // Navegar a la siguiente actividad
        }

        val usernameEditText = findViewById<EditText>(R.id.editTextNombreUsuario)
        val passwordEditText = findViewById<EditText>(R.id.editTextPasswordUsuario)
        val loginButton = findViewById<Button>(R.id.btn_IngresarUsuario)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validación simple de campos vacíos
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  // Salir si los campos están vacíos
            }

            // Buscar en Firestore el correo asociado al nombre de usuario
            db.collection("Usuario")
                .whereEqualTo("nombreUsuario", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Usuario no encontrado en Firestore
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si el usuario existe, obtenemos su correo y autenticamos con FirebaseAuth
                        val email = documents.first().getString("email") ?: ""

                        // Autenticación con Firebase
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { authResult ->
                                if (authResult.isSuccessful) {
                                    // Inicio de sesión exitoso
                                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                    startMainActivity()  // Navegar a la siguiente actividad

                                } else {
                                    // Error de autenticación
                                    Toast.makeText(this, "Error en el inicio de sesión: ${authResult.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Manejo de errores de Firestore
                    Toast.makeText(this, "Error al buscar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun startMainActivity() {
        // Implementa la navegación a tu actividad principal
         Intent(this, MainActivity::class.java).also {
             startActivity(it)
             finish()
         }
    }

}

