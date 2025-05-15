package com.belajar.vigilanter

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.belajar.vigilanter.data.response.CreatedUserResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.google.android.material.textfield.TextInputLayout
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Register : AppCompatActivity() {
    private lateinit var namaDepan: EditText
    private lateinit var namaBelakang: EditText
    private lateinit var nik: EditText
    private lateinit var no_telepon: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var Confirmpassword: EditText
    private lateinit var checkBox: CheckBox


    private lateinit var calendar: Calendar
    private lateinit var inputTanggal: EditText
    private lateinit var selectedDate: String
    private var jenisKelamin: String = "null"
    private lateinit var adapterItems: ArrayAdapter<String>
    private lateinit var btnRegist: AppCompatButton

    var arrayJenisKelamin: Array<String> = arrayOf(
        "Laki-laki", "Perempuan"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi
        namaDepan = findViewById(R.id.namaDepan)
        namaBelakang = findViewById(R.id.namaBelakang)
        nik = findViewById(R.id.nik)
        no_telepon = findViewById(R.id.noHp)
        email = findViewById(R.id.email)
        password = findViewById(R.id.passwordBaru)
        Confirmpassword = findViewById(R.id.konfirmasiPasseword)
        checkBox = findViewById(R.id.rememberMeCheckBox)

        // Tanggal dan waktu
        inputTanggal = findViewById<EditText>(R.id.tanggalLahir)
        calendar = Calendar.getInstance()
        inputTanggal.setOnClickListener(View.OnClickListener { v: View? -> showDatePickerDialog() })

        persyaratanDanKebijakan()

        // Text masuk
        val masuk = findViewById<TextView>(R.id.masukText)
        masuk.setOnClickListener {
            onBackPressed()
        }

        jenisKelaminDropDown()

        btnRegist = findViewById(R.id.regist)
        btnRegist.setOnClickListener {
            cekInputan()
        }
    }

    private fun cekInputan(){
        if(namaDepan.text.isEmpty() || jenisKelamin == "null" || selectedDate.isEmpty() ||
            nik.text.isEmpty() || no_telepon.text.isEmpty() || email.text.isEmpty() ||
            password.text.isEmpty() || Confirmpassword.text.isEmpty() ){
            Toast.makeText(this, "Data tidak boleh kosong!", Toast.LENGTH_SHORT).show()
        }else if(password.text.toString() != Confirmpassword.text.toString()){
            Toast.makeText(this, "Konfirmasi password harus sama", Toast.LENGTH_SHORT).show()
        }else if(!checkBox.isChecked){
            Toast.makeText(this, "Setujui Persyaratan!", Toast.LENGTH_SHORT).show()
        }else{
            createUser(
                namaDepan.text.toString(), namaBelakang.text.toString(), jenisKelamin, selectedDate, nik.text.toString(),
                no_telepon.text.toString(), email.text.toString(), password.text.toString())
        }
    }

    private fun showDatePickerDialog() {
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this@Register,
            { view, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                updateDateInView()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        selectedDate =
            sdf.format(calendar.getTime()) // Simpan tanggal yang dipilih ke dalam variabel
        inputTanggal.setText(selectedDate) // Tampilkan tanggal yang diformat dalam EditText
        Toast.makeText(this@Register, selectedDate, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        val pg = findViewById<ProgressBar>(R.id.progressBar)
        val bg = findViewById<FrameLayout>(R.id.overlayLayout)
        if (isLoading) {
            pg.visibility = View.VISIBLE
            bg.visibility = View.VISIBLE
        } else {
            pg.visibility = View.GONE
            bg.visibility = View.GONE
        }
    }

    private fun createUser(n_d : String, n_b : String, J_l: String,tl : String,nik : String, n_t: String, email:String, password: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().createUser(n_d, n_b, J_l, tl, nik, n_t, email,password )
        client.enqueue(object : Callback<CreatedUserResponse> {
            override fun onResponse(
                call: Call<CreatedUserResponse>,
                response: Response<CreatedUserResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(this@Register, responseBody.message, Toast.LENGTH_SHORT).show()
                    onBackPressed()
                } else {
                    Log.e("ERROR", "onFailure: ${response.message()}")
                    Toast.makeText(this@Register, " Gagal di respon true", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CreatedUserResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(this@Register, "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun hashPassword(password: String): String {
        // Generate a hashed password using bcrypt
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }

    fun persyaratanDanKebijakan(){
        // Text persyaratan dan kebijakan bisa diklik
        val textView: TextView = findViewById(R.id.terms_and_privacy_text)
        val text = "Dengan membuat akun, Anda menyetujui Persyaratan Layanan dan Kebijakan Privasi"
        val spannableString = SpannableString(text)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Action when "Persyaratan Layanan" is clicked
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/terms"))
                startActivity(intent)
            }
        }

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Action when "Kebijakan Privasi" is clicked
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/privacy"))
                startActivity(intent)
            }
        }

        spannableString.setSpan(termsClickableSpan, 37, 56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyClickableSpan, 61, 78, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setTextColor(getResources().getColor(android.R.color.black)) // Set default text color

        textView.text = spannableString
        textView.setTextColor(Color.parseColor("#80FFFFFF")) // Default text color
        textView.setLinkTextColor(Color.parseColor("#FFE600"))
    }

    fun jenisKelaminDropDown(){
        // Menampilkan drop down untuk memilih jenis kelamin
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.jenisKelamin)
        val textInputLayout: TextInputLayout = findViewById(R.id.textInputLayout)
        adapterItems = ArrayAdapter<String>(this@Register, R.layout.list_item, arrayJenisKelamin)
        autoCompleteTextView.setAdapter<ArrayAdapter<String>>(adapterItems)

        autoCompleteTextView.onItemClickListener =
            OnItemClickListener { parent, view, position, id -> //Ketika item list di klik, datanya akan di simpan di variabel
                jenisKelamin = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@Register, "item : $jenisKelamin", Toast.LENGTH_SHORT).show()
                textInputLayout.hint = null
            }
    }

}