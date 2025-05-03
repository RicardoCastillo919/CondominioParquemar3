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
import com.google.firebase.firestore.DocumentSnapshot
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


    private fun buscarDocumentoFirestore(numeroDepartamento: String, torreDepartamento: String) {
        val hoy = Timestamp.now()
        val db = FirebaseFirestore.getInstance()

        db.collection("respuestasFormulario")
            .whereEqualTo("numeroDepartamento", numeroDepartamento)
            .whereEqualTo("torreDepartamento", torreDepartamento)
            .get()
            .addOnSuccessListener { snapshot ->
                val documentos = snapshot.documents

                val documentosDelDia = documentos.filter { doc ->
                    val ingreso = doc.getTimestamp("fechaIngreso")
                    val salida = doc.getTimestamp("fechaSalida")
                    val estado = doc.getString("estadoVisita")

                    when (estado) {
                        "Disponible" -> ingreso?.let { esMismoDia(it.toDate(), hoy.toDate()) } ?: false
                        "En Visita" -> true
                        "Salida" -> salida?.let { esMismoDia(it.toDate(), hoy.toDate()) } ?: false
                        else -> false
                    }
                }.sortedBy { it.getTimestamp("marcaTemporal") }

                if (documentosDelDia.isEmpty()) {
                    irASinDisponibilidad()
                    return@addOnSuccessListener
                }

                val primero = documentosDelDia.first()
                val estadoPrimero = primero.getString("estadoVisita")
                val idPrimero = primero.id

                when (estadoPrimero) {
                    "Disponible" -> irAActividad("Disponible", idPrimero)
                    "En Visita" -> irAActividad("En Visita", idPrimero)
                    "Salida" -> {
                        val siguientes = documentosDelDia.filter { it.id != idPrimero &&
                                listOf("Disponible", "En Visita", "Salida").contains(it.getString("estadoVisita")) }

                        val siguiente = siguientes.firstOrNull()
                        if (siguiente != null) {
                            val estadoSiguiente = siguiente.getString("estadoVisita")
                            val idSiguiente = siguiente.id
                            irAActividad(estadoSiguiente, idSiguiente)
                        } else {
                            irAActividad("Salida", idPrimero)
                        }
                    }
                    else -> irASinDisponibilidad()
                }
            }
    }

    private fun irAActividad(estado: String?, docId: String) {
        val intent = when (estado) {
            "Disponible" -> Intent(this, VisitaDisponibleActivity::class.java)
            "En Visita" -> Intent(this, DeptoEnVisitaActivity::class.java)
            "Salida" -> Intent(this, DeptoSalidaActivity::class.java)
            else -> Intent(this, DeptoSinDisponibilidadActivity::class.java)
        }
        intent.putExtra("documentoId", docId)
        startActivity(intent)
        finish()
    }

    private fun irASinDisponibilidad() {
        val intent = Intent(this, DeptoSinDisponibilidadActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun esMismoDia(fecha1: Date, fecha2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = fecha1 }
        val cal2 = Calendar.getInstance().apply { time = fecha2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

}



