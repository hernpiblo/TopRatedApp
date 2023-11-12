package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

private const val LOG_TAG = "_LOGIN PAGE"
private const val LOG_TAG_FB = "$LOG_TAG FIREBASE"

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var rememberMeCheckbox : CheckBox
    private lateinit var loginBtn : Button
    private lateinit var signupBtn : Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Init
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        loginBtn = findViewById(R.id.loginBtn)
        signupBtn = findViewById(R.id.signupBtn)
        firebaseAuth = FirebaseAuth.getInstance()
        val sharedPrefs = getSharedPreferences("LoginActivity", MODE_PRIVATE)

        // Email Input
        emailInput.addTextChangedListener(textWatcher)
        var savedEmail = intent.getStringExtra("EMAIL")
        if (savedEmail.isNullOrBlank()) {
            savedEmail = sharedPrefs.getString("EMAIL", "")
        }
        emailInput.setText(savedEmail.toString())

        // Password Input
        passwordInput.addTextChangedListener(textWatcher)
        var savedPassword = intent.getStringExtra("PASSWORD")
        if (savedPassword.isNullOrBlank()) {
            savedPassword = sharedPrefs.getString("PASSWORD", "")
        }
        passwordInput.setText(savedPassword.toString())

        // Checkbox
        rememberMeCheckbox.isChecked = sharedPrefs.getBoolean("REMEMBERME", false)

        // Login Button
        loginBtn.isEnabled = emailInput.text.toString().isNotBlank() && passwordInput.text.toString().isNotBlank()
        loginBtn.setOnClickListener {
            Log.d(LOG_TAG, "loginBtn clicked")
            val email : String = emailInput.text.toString().trim()
            val password : String = passwordInput.text.toString().trim()

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(LOG_TAG_FB, "Login user success")

                        if (rememberMeCheckbox.isChecked) {
                            Log.d(LOG_TAG, "rememberMe Checked")
                            sharedPrefs.edit().putString("EMAIL", email).apply()
                            sharedPrefs.edit().putString("PASSWORD", password).apply()
                            sharedPrefs.edit().putBoolean("REMEMBERME", true).apply()
                        } else {
                            Log.d(LOG_TAG, "rememberMe Unchecked")
                            sharedPrefs.edit().putString("EMAIL", "").apply()
                            sharedPrefs.edit().putString("PASSWORD", "").apply()
                            sharedPrefs.edit().putBoolean("REMEMBERME", false).apply()
                        }
                        val user = firebaseAuth.currentUser
                        Toast.makeText(this, "Login success : ${user!!.displayName}", Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    } else {
                        val exception = it.exception
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage(exception!!.message.toString())
                            .setPositiveButton("OK", null)
                            .show()
                        Log.d(LOG_TAG_FB, exception.toString())
                    }
                }
        }

        // Signup Button
        signupBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))

//            Log.d(LOG_TAG, "signupBtn clicked")
//            val signupActivity = Intent(this@LoginActivity, SignupActivity::class.java)
//            startActivity(signupActivity)
        }
    }

    private val textWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val email : String = emailInput.text.toString()
            val password : String = passwordInput.text.toString()
            loginBtn.isEnabled = email.isNotBlank() && password.isNotBlank()
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}