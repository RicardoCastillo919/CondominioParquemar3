package com.example.condominioparquemar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        db = FirebaseFirestore.getInstance()

        //Texto de los departamentos
        val textDecena = findViewById<TextView>(R.id.Text_Decena)
        val textUnidad = findViewById<TextView>(R.id.Text_Unidad)

        //Botones de aumentar y bajar
        val btnAumentarDecena = findViewById<ImageButton>(R.id.btn_AumentarDecena)
        val btnAumentarUnidad = findViewById<ImageButton>(R.id.btn_AumentarUnidad)
        val btnbajarDecena = findViewById<ImageButton>(R.id.btn_bajarDecena)
        val btnbajarUnidad = findViewById<ImageButton>(R.id.btn_bajarUnidad)

        //Botones Torre
        val btnTorreA = findViewById<TextView>(R.id.btn_TorreA)
        val btnTorreB = findViewById<TextView>(R.id.btn_TorreB)
        val btnTorreC = findViewById<TextView>(R.id.btn_TorreC)
        val btnTorreD = findViewById<TextView>(R.id.btn_TorreD)

        // Variables para controlar los valores actuales de decena y unidad
        var decena = 0
        var unidad = 0
        var numeroDepartamento = ""
        var torreSeleccionada = ""


        // Actualiza el valor mostrado en los TextViews
        fun actualizarTexto() {
            textDecena.text = decena.toString()
            textUnidad.text = unidad.toString()

            numeroDepartamento ="${textDecena.text}${textUnidad.text}"
        }

    // Configura los botones de aumentar y bajar
        btnAumentarDecena.setOnClickListener {
            if (decena < 5) decena++ else decena = 0
            actualizarTexto()
        }

        btnbajarDecena.setOnClickListener {
            if (decena > 0) decena-- else decena = 5
            actualizarTexto()
        }

        btnAumentarUnidad.setOnClickListener {
            if (unidad < 9) unidad++ else unidad = 0
            actualizarTexto()
        }

        btnbajarUnidad.setOnClickListener {
            if (unidad > 0) unidad-- else unidad = 9
            actualizarTexto()
        }


        // Configura los botones de torre
        btnTorreA.setOnClickListener {
            torreSeleccionada = "A"
            actualizarTexto()
            buscarDocumentoFirestore(numeroDepartamento,torreSeleccionada)
        }
        btnTorreB.setOnClickListener {
            torreSeleccionada = "B"
            actualizarTexto()
            buscarDocumentoFirestore(numeroDepartamento,torreSeleccionada)

        }
        btnTorreC.setOnClickListener {
            torreSeleccionada = "C"
            actualizarTexto()
            buscarDocumentoFirestore(numeroDepartamento,torreSeleccionada)
        }
        btnTorreD.setOnClickListener {
            torreSeleccionada = "D"
            actualizarTexto()
            buscarDocumentoFirestore(numeroDepartamento,torreSeleccionada)
        }

    }

    //Obtener la fecha actual
    /**
     * fecha en formato string
    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("America/Santiago")

        val fechaActual = Date()
        val fechaFormateada = formato.format(fechaActual)
        Log.d("Fecha", fechaFormateada)
        return fechaFormateada// Devuelve la fecha actual en formato yyyy_MM_dd
    }
    **/

    fun obtenerTimestampExactoDeHoy(): Timestamp {
        val zonaChile = TimeZone.getTimeZone("America/Santiago")
        val calendar = Calendar.getInstance(zonaChile).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return Timestamp(calendar.time)
    }



    private fun buscarDocumentoFirestore(numeroDepartamento: String, torreDepartamento: String) {
        // fecha en formato string
        // val fecha = obtenerFechaActual()


        val timestampHoy = obtenerTimestampExactoDeHoy()

        db.collection("respuestasFormulario")
            .whereEqualTo("numeroDepartamento", numeroDepartamento)
            .whereEqualTo("torreDepartamento", torreDepartamento)
            .whereEqualTo("fechaIngreso", timestampHoy)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val primerDocumento = querySnapshot.documents.first()
                    val datos = primerDocumento.data
                    val idDocumento = primerDocumento.id
                    Log.d("datosBaseDeDatos", datos.toString())

                    when (datos?.get("estadoVisita")) {
                        "Disponible" -> {
                            val intent = Intent(this, VisitaDisponibleActivity::class.java)
                            intent.putExtra("documentoId", idDocumento)
                            startActivity(intent)
                            finish()
                        }
                        "En Visita" -> {
                            val intent = Intent(this, DeptoEnVisitaActivity::class.java)
                            intent.putExtra("documentoId", idDocumento)
                            startActivity(intent)
                            finish()
                        }
                        "Salida" -> {
                            db.collection("respuestasFormulario")
                                .whereEqualTo("numeroDepartamento", numeroDepartamento)
                                .whereEqualTo("torreDepartamento", torreDepartamento)
                                .whereEqualTo("fechaIngreso", timestampHoy)
                                .get()
                                .addOnSuccessListener { nuevosDocs ->
                                    // Filtramos los documentos relevantes
                                    val documentosValidos = nuevosDocs.documents
                                        .filter { doc ->
                                            doc.id != idDocumento &&
                                                    (doc.getString("estadoVisita") == "Disponible" ||
                                                            doc.getString("estadoVisita") == "En Visita" ||
                                                            doc.getString("estadoVisita") == "Salida")
                                        }

                                    val documentoElegido = documentosValidos.firstOrNull()

                                    val intent = if (documentoElegido != null) {
                                        val nuevoId = documentoElegido.id
                                        val estadoNuevo = documentoElegido.getString("estadoVisita")
                                        when (estadoNuevo) {
                                            "Disponible" -> Intent(this, VisitaDisponibleActivity::class.java)
                                            "En Visita" -> Intent(this, DeptoEnVisitaActivity::class.java)
                                            else -> Intent(this, DeptoSalidaActivity::class.java)
                                        }.apply {
                                            putExtra("documentoId", nuevoId)
                                        }
                                    } else {
                                        Intent(this, DeptoSalidaActivity::class.java).apply {
                                            putExtra("documentoId", idDocumento)
                                        }
                                    }

                                    startActivity(intent)
                                    finish()
                                }

                        }
                    }
                } else {
                    val intent = Intent(this, DeptoSinDisponibilidadActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }
}



