package ru.study.rundo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.util.PatternsCompat
import ru.study.rundo.interfaces.AuthorizationActivityNavigator
import ru.study.rundo.R
import ru.study.rundo.WorkWithServer
import ru.study.rundo.interfaces.ServerHandler
import ru.study.rundo.models.TokenInfo

class RegistrationFragment : Fragment(), ServerHandler<TokenInfo> {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    private lateinit var loginButton: Button
    private lateinit var registrationButton: Button
    private lateinit var email: EditText
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var password: EditText
    private lateinit var repeatPassword: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginButton = view.findViewById(R.id.loginButton)
        registrationButton = view.findViewById(R.id.registrationButton)
        email = view.findViewById(R.id.email)
        firstName = view.findViewById(R.id.firstName)
        lastName = view.findViewById(R.id.lastName)
        password = view.findViewById(R.id.password)
        repeatPassword = view.findViewById(R.id.repeatPassword)
        val act = activity
        loginButton.setOnClickListener {
            if (act is AuthorizationActivityNavigator) {
                act.switchToLoginFragment()
            }
        }
        registrationButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val repeatPasswordText = repeatPassword.text.toString()
            val firstNameText = firstName.text.toString()
            val lastNameText = lastName.text.toString()
            if (emailValidCheck(emailText) &&
                passwordsMatchCheck(passwordText, repeatPasswordText) &&
                WorkWithServer.isRegistrationFinished == null
            ) {
                WorkWithServer.registration(emailText, firstNameText, lastNameText, passwordText)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        WorkWithServer.addListenerRegistration(this)
    }

    override fun onStop() {
        super.onStop()
        WorkWithServer.addListenerRegistration(null)
    }

    private fun emailValidCheck(email: String): Boolean {
        val valid = email.isNotEmpty() && PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        if (!valid) {
            showInvalidEmailError()
        }
        return valid
    }

    private fun passwordsMatchCheck(password: String, repeatPassword: String): Boolean {
        val match = password.isNotEmpty() && repeatPassword == password
        if (!match) {
            showPasswordMatchError()
        }
        return match
    }

    private fun showInvalidEmailError() {
        email.error = resources.getString(R.string.invalidEmailError)
    }

    private fun showPasswordMatchError() {
        repeatPassword.error = resources.getString(R.string.repeatPasswordMatchError)
    }

    override fun onSuccess(result: TokenInfo) {
        val act = activity
        if (act is AuthorizationActivityNavigator) {
            act.goToMainActivity(result.token)
        }
    }

    override fun onError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }
}
