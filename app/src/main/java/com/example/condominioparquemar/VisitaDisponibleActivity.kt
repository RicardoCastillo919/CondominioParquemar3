package com.example.condominioparquemar

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.TimeZone
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class VisitaDisponibleActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosPasajerosUno: String
    private var datos: Map<String, Any>? = null
    private var botonSeleccionado: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visita_disponible)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosPasajerosUno = ""

        val documentoId = intent?.getStringExtra("documentoId")
        Log.d("idDocumento", "$documentoId")

        if (documentoId != null) {
            buscarDocumentoFirestore(documentoId) // ✅ Llamamos a Firestore
        } else {
            Log.e("Firestore", "El documentoId es nulo")
        }

        val btnMarcarIngreso = findViewById< MaterialButton>(R.id.btnMarcarIngreso)
        val btnVolver = findViewById<MaterialButton>(R.id.btn_VolverDisponible)
        // Esta configurados para que funcione de manera interruptor
        val btnVehiculoUno = findViewById<MaterialButton>(R.id.btn_Vehiculo_Uno_Disponible)
        val btnVehiculoDos = findViewById<MaterialButton>(R.id.btn_Vehiculo_Dos_Disponible)


        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Volviste al menu principal", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }

        btnMarcarIngreso.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("respuestasFormulario")
                .document(documentoId.toString())
                .update(mapOf(
                    "marcaIngreso" to obtenerTimestampActual(),
                    "estadoVisita" to "En Visita"
                    )
                )
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    Toast.makeText(this, "Las visitas han ingresado exitosamente", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al actualizar campo", e)
                }
        }

        fun animarBotonPresionado(boton: MaterialButton) {
            boton.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(50)
                .withEndAction {
                    boton.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(50)
                        .start()
                }
                .start()
        }

        btnVehiculoUno.setOnClickListener {
            if (botonSeleccionado != 1) {
                animarBotonPresionado(btnVehiculoUno)
                btnVehiculoUno.setBackgroundColor("#a40101".toColorInt()) // rojo
                btnVehiculoDos.setBackgroundColor("#A6A6A6".toColorInt()) // gris
                botonSeleccionado = 1
            }
            //datos Vehiculo Uno
            findViewById<TextView>(R.id.TextViewNombreConductor).text = datos?.get("nombreConductorUno").toString()
            findViewById<TextView>(R.id.TextViewRutConductor).text = datos?.get("rutConductorUno").toString()
            findViewById<MaterialButton>(R.id.TextViewPatente_Disponible).text = datos?.get("patenteVehiculoUno").toString()
            // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
            datosPasajerosUno = datos?.get("nombrePasajerosUno").toString()
            Log.d("DatosPasajeros", datosPasajerosUno)
            // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
            separarPersonas(datosPasajerosUno)
        }

        btnVehiculoDos.setOnClickListener {

            //datos Vehiculo Dos
            if(datos?.get("patenteVehiculoDos").toString().isNotEmpty()){
                if (botonSeleccionado != 2) {
                    animarBotonPresionado(btnVehiculoDos)
                    btnVehiculoDos.setBackgroundColor("#a40101".toColorInt()) // rojo
                    btnVehiculoUno.setBackgroundColor("#A6A6A6".toColorInt()) // gris
                    botonSeleccionado = 2
                }
                findViewById<TextView>(R.id.TextViewNombreConductor).text = datos?.get("nombreConductorDos").toString()
                findViewById<TextView>(R.id.TextViewRutConductor).text = datos?.get("rutConductorDos").toString()
                findViewById<MaterialButton>(R.id.TextViewPatente_Disponible).text = datos?.get("patenteVehiculoDos").toString()
                // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
                datosPasajerosUno = datos?.get("nombrePasajerosDos").toString()
                Log.d("DatosPasajeros", datosPasajerosUno)
                // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
                separarPersonas(datosPasajerosUno)
            }else{
                Toast.makeText(this, "No hay segundo auto registrado", Toast.LENGTH_SHORT).show()
            }


        }

    }
    private fun obtenerTimestampActual(): Timestamp {
        val zonaChile = TimeZone.getTimeZone("America/Santiago")
        val calendar = Calendar.getInstance(zonaChile)
        return Timestamp(calendar.time)
    }

    private fun buscarDocumentoFirestore(documentoId: String) {
        db.collection("respuestasFormulario")
            .document(documentoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    datos = document.data
                    //datos Vehiculo Uno
                    findViewById<TextView>(R.id.TextViewNombreConductor).text =
                        datos?.get("nombreConductorUno").toString()
                    findViewById<TextView>(R.id.TextViewRutConductor).text =
                        datos?.get("rutConductorUno").toString()
                    findViewById<MaterialButton>(R.id.TextViewPatente_Disponible).text =
                        datos?.get("patenteVehiculoUno").toString()
                    // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
                    datosPasajerosUno = datos?.get("nombrePasajerosUno").toString()
                    Log.d("DatosPasajeros", datosPasajerosUno)
                    // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
                    separarPersonas(datosPasajerosUno)
                } else {
                    Log.d("Firestore", "No se encontró el documento con ID: $documentoId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al buscar el documento", e)
            }
    }

    private fun separarPersonas(datos: String) {
        var contador = 1
        val lineas = datos.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        for (linea in lineas) {
            val partes = linea.split(" ") // Separa por espacios
            if (partes.size >= 2) { // Asegura que haya al menos nombre y RUT
                val rut = partes.last() // Última parte es el RUT
                val nombre = partes.dropLast(1).joinToString(" ") // Todo lo demás es el nombre
                Log.d("nombre", nombre)
                Log.d("rut", rut)

                when (contador) {
                    1 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroUno).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroUno).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroUno).visibility = VISIBLE
                    }
                    2 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroDos).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroDos).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroDos).visibility = VISIBLE
                    }
                    3 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroTres).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroTres).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroTres).visibility = VISIBLE
                    }
                    4 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroCuatro).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroCuatro).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroCuatro).visibility = VISIBLE
                    }
                    5 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroCinco).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroCinco).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroCinco).visibility = VISIBLE
                    }
                }
            }
            contador++
        }
    }
}
