package com.example.condominioparquemar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DeptoSalidaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosPasajeros: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depto_salida)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosPasajeros = ""

        val documentoId = intent?.getStringExtra("documentoId")
        Log.d("idDocumento", "$documentoId")

        if (documentoId != null) {
            buscarDocumentoFirestore(documentoId) // ✅ Llamamos a Firestore
        } else {
            Log.e("Firestore", "El documentoId es nulo")
        }

        val btnVolver = findViewById<MaterialButton>(R.id.btn_VolverSalida)

        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Volviste al menu principal", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }


    }

    private fun buscarDocumentoFirestore(documentoId: String) {
        db.collection("respuestasFormulario")
            .document(documentoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val datos = document.data
                    findViewById<TextView>(R.id.TextViewNombreConductor).text =
                        datos?.get("nombreConductorUno").toString()
                    findViewById<TextView>(R.id.TextViewRutConductor).text =
                        datos?.get("rutConductorUno").toString()


                    //Marca Ingreso
                    val marcaIngreso = datos?.get("marcaIngreso") as? Timestamp
                    val marcaIngresoFormateado = timestampAFechaLegible(marcaIngreso)
                    findViewById<TextView>(R.id.TextViewMarcaIngreso_Salida).text = "Ingreso: $marcaIngresoFormateado"

                    val marcaSalida = datos?.get("marcaSalida") as? Timestamp
                    val marcaSalidaFormateado = timestampAFechaLegible(marcaSalida)
                    findViewById<TextView>(R.id.TextViewMarcaSalida_Salida).text = "Ingreso: ${marcaSalidaFormateado}"


                    // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
                    datosPasajeros = datos?.get("nombrePasajerosUno").toString()
                    Log.d("DatosPasajeros", datosPasajeros)

                    // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
                    separarPersonas(datosPasajeros)
                } else {
                    Log.d("Firestore", "No se encontró el documento con ID: $documentoId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al buscar el documento", e)
            }
    }
    private fun obtenerTimestampActual(): Timestamp {
        val zonaChile = TimeZone.getTimeZone("America/Santiago")
        val calendar = Calendar.getInstance(zonaChile)
        return Timestamp(calendar.time)
    }

    private fun timestampAFechaLegible(timestamp: Timestamp?): String {
        if (timestamp == null) return "No disponible"

        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("America/Santiago")
        return formato.format(timestamp.toDate())
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
