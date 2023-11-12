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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

private const val LOG_TAG = "_SIGNUP PAGE"
private const val LOG_TAG_FB = "$LOG_TAG FIREBASE"

class SignupActivity : AppCompatActivity() {

    private lateinit var backBtn : ImageButton
    private lateinit var emailInput : EditText
    private lateinit var nameInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var confirmPasswordInput : EditText
    private lateinit var createAccountBtn : Button
    private lateinit var errorIcon : ImageView
    private lateinit var errorText : TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Init
        backBtn = findViewById(R.id.backBtn)
        emailInput = findViewById(R.id.emailInput)
        nameInput = findViewById(R.id.nameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        errorIcon = findViewById(R.id.errorIcon)
        errorText = findViewById(R.id.errorText)
        firebaseAuth = FirebaseAuth.getInstance()

        // TextWatcher
        emailInput.addTextChangedListener(textWatcher)
        nameInput.addTextChangedListener(textWatcher)
        passwordInput.addTextChangedListener(textWatcher)
        confirmPasswordInput.addTextChangedListener(textWatcher)

        // Back Button
        backBtn.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }

        // Create Account Button
        createAccountBtn.setOnClickListener {
            Log.d(LOG_TAG, "createAccountBtn clicked")

            val email : String = emailInput.text.toString().trim()
            val password : String = passwordInput.text.toString().trim()
            val name : String = nameInput.text.toString().trim()

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        Log.d(LOG_TAG_FB, "Create user success")

                        firebaseAuth.currentUser!!.updateProfile(userProfileChangeRequest{displayName = name})
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.d(LOG_TAG_FB, "User name updated: $name.")
                                    Toast.makeText(this, "Created user : ${firebaseAuth.currentUser!!.displayName}", Toast.LENGTH_LONG).show()

                                    val loginActivity = Intent(this@SignupActivity, LoginActivity::class.java)
                                    loginActivity.putExtra("EMAIL", email).putExtra("PASSWORD", password)
                                    startActivity(loginActivity)
                                }
                            }
                    } else {
                        val exception = task.exception
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage(exception!!.message.toString())
                            .setPositiveButton("OK", null)
                            .show()
                        Log.d(LOG_TAG_FB, exception.toString())
                    }
                }
        }
    }


    private val textWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val email : String = emailInput.text.toString()
            val name : String = nameInput.text.toString()
            val password : String = passwordInput.text.toString()
            val confirmPassword : String = confirmPasswordInput.text.toString()

            errorIcon.isVisible = password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword
            errorText.isVisible = errorIcon.isVisible

            createAccountBtn.isEnabled = email.isNotBlank()
                                      && name.isNotBlank()
                                      && password.isNotBlank()
                                      && confirmPassword.isNotBlank()
                                      && password == confirmPassword
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}
