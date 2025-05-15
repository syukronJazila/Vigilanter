package com.belajar.vigilanter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belajar.vigilanter.data.response.CreatedUserResponse
import com.belajar.vigilanter.data.response.LoginResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.emailLogin)
        password = findViewById(R.id.passwordLogin)

        val createAccount = findViewById<TextView>(R.id.createNewAccount)
        createAccount.setOnClickListener {
            val registerIntent = Intent(this, Register::class.java)
            startActivity(registerIntent)
        }

        val loginBtn = findViewById<AppCompatButton>(R.id.login)
        loginBtn.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            val view = currentFocus
            if (imm != null && view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            cekLogin()
        }

    }

    private fun cekLogin(){
        if(email.text.isEmpty() && password.text.isEmpty()){
            Toast.makeText(this, "Email dan Password Tidak Boleh Kosong",Toast.LENGTH_SHORT).show()
        }else if(email.text.isEmpty()){
            Toast.makeText(this, "Email Tidak Boleh Kosong",Toast.LENGTH_SHORT).show()
        }else if(password.text.isEmpty()){
            Toast.makeText(this, "Password Tidak Boleh Kosong",Toast.LENGTH_SHORT).show()
        }else{
            loginUser(email.text.toString(), password.text.toString())
        }
    }

    private fun loginUser(email:String, password: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().login(email,password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null && responseBody.status) {
                    Toast.makeText(this@Login, responseBody.message, Toast.LENGTH_SHORT).show()
                    val mainIntent = Intent(this@Login, MainActivity::class.java)

                    mainIntent.putExtra("user_id", responseBody.user_id)
                    mainIntent.putExtra("username", responseBody.username)
                    startActivity(mainIntent)
                    finish()
                } else {
                    Log.e("ERROR", "onFailure setelah berhasil: ${response.message()}")
                    Toast.makeText(this@Login, responseBody?.message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(this@Login, "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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



}