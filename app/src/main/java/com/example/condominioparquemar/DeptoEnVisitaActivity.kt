package com.example.condominioparquemar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DeptoEnVisitaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosPasajeros: String
    private var datos: Map<String, Any>? = null
    private var botonSeleccionado: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depto_en_visita)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosPasajeros = ""

        val documentoId = intent?.getStringExtra("documentoId")
        Log.d("idDocumento", "$documentoId")

        buscarDocumentoFirestore(documentoId.toString())

        val btnMarcarSalida = findViewById<MaterialButton>(R.id.btn_MarcarSalida_EnVisita)
        val btnVolver = findViewById<MaterialButton>(R.id.btn_VolverEnVisita)
        // Esta configurados para que funcione de manera interruptor
        val btnVehiculoUno = findViewById<MaterialButton>(R.id.btn_Vehiculo_Uno_EnVisita)
        val btnVehiculoDos = findViewById<MaterialButton>(R.id.btn_Vehiculo_Dos_EnVisita)



        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Volviste al menu principal", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }

        btnMarcarSalida.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("respuestasFormulario")
                .document(documentoId.toString())
                .update(mapOf(
                    "marcaSalida" to obtenerTimestampActual(),
                    "estadoVisita" to "Salida"
                )
                )
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    Toast.makeText(this, "Las visitas han salido exitosamente del condominio", Toast.LENGTH_SHORT).show()
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
            findViewById<TextView>(R.id.TextViewNombreConductor_EnVisita).text = datos?.get("nombreConductorUno").toString()
            findViewById<TextView>(R.id.TextViewRutConductor_EnVisita).text = datos?.get("rutConductorUno").toString()
            findViewById<MaterialButton>(R.id.TextViewPatente_EnVisita).text = datos?.get("patenteVehiculoUno").toString()
            // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
            datosPasajeros = datos?.get("nombrePasajerosUno").toString()
            Log.d("DatosPasajeros", datosPasajeros)
            // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
            separarPersonas(datosPasajeros)
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
                findViewById<TextView>(R.id.TextViewNombreConductor_EnVisita).text = datos?.get("nombreConductorDos").toString()
                findViewById<TextView>(R.id.TextViewRutConductor_EnVisita).text = datos?.get("rutConductorDos").toString()
                findViewById<MaterialButton>(R.id.TextViewPatente_EnVisita).text = datos?.get("patenteVehiculoDos").toString()
                // 🔥 Actualizamos `datosPasajeros` con la información de Firestore
                datosPasajeros = datos?.get("nombrePasajerosDos").toString()
                Log.d("DatosPasajeros", datosPasajeros)
                // ✅ Llamamos a `separarPersonas()` después de actualizar `datosPasajeros`
                separarPersonas(datosPasajeros)
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

    private fun timestampAFechaLegible(timestamp: Timestamp?): String {
        if (timestamp == null) return "No disponible"

        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("America/Santiago")
        return formato.format(timestamp.toDate())
    }

    private fun buscarDocumentoFirestore(documentoId: String) {
        db.collection("respuestasFormulario")
            .document(documentoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    datos = document.data
                    findViewById<TextView>(R.id.TextViewNombreConductor_EnVisita).text =
                        datos?.get("nombreConductorUno").toString()
                    findViewById<TextView>(R.id.TextViewRutConductor_EnVisita).text =
                        datos?.get("rutConductorUno").toString()
                    findViewById<MaterialButton>(R.id.TextViewPatente_EnVisita).text =
                        datos?.get("patenteVehiculoUno").toString()

                    //Marca Ingreso
                    val marcaIngresoTimestamp = datos?.get("marcaIngreso") as? Timestamp
                    val marcaIngresoFormateado = timestampAFechaLegible(marcaIngresoTimestamp)
                    findViewById<TextView>(R.id.TextViewMarcaIngreso).text = "Ingreso: $marcaIngresoFormateado"

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
                        findViewById<TextView>(R.id.TextViewNombrePasajeroUno_EnVisita).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroUno_EnVisita).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroUno_EnVisita).visibility = VISIBLE
                    }
                    2 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroDos_EnVisita).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroDos_EnVisita).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroDos_EnVisita).visibility = VISIBLE
                    }
                    3 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroTres_EnVisita).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroTres_EnVisita).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroTres_EnVisita).visibility = VISIBLE
                    }
                    4 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroCuatro_EnVisita).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroCuatro_EnVisita).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroCuatro_EnVisita).visibility = VISIBLE
                    }
                    5 -> {
                        findViewById<TextView>(R.id.TextViewNombrePasajeroCinco_EnVisita).text = nombre
                        findViewById<TextView>(R.id.TextViewRutPasajeroCinco_EnVisita).text = rut
                        findViewById<LinearLayout>(R.id.layoutPasajeroCinco_EnVisita).visibility = VISIBLE
                    }
                }
            }
            contador++
        }
    }
}


