package ru.study.rundo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import ru.study.rundo.interfaces.AuthorizationActivityNavigator
import ru.study.rundo.R
import ru.study.rundo.WorkWithServer
import ru.study.rundo.interfaces.ServerHandler
import ru.study.rundo.models.TokenInfo

class LoginFragment : Fragment(), ServerHandler<TokenInfo> {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    private lateinit var loginButton: Button
    private lateinit var registrationButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        loginButton = view.findViewById(R.id.loginButton)
        registrationButton = view.findViewById(R.id.registrationButton)
        email = view.findViewById(R.id.email)
        password = view.findViewById(R.id.password)

        val act = activity
        registrationButton.setOnClickListener {
            if (act is AuthorizationActivityNavigator) {
                act.switchToRegistrationFragment()
            }
        }

        loginButton.setOnClickListener {
            if (emailValidCheck() && WorkWithServer.isLoginFinished == null) {
                WorkWithServer.login(getEmailText(), getPasswordText())
            }
        }
    }

    private fun getEmailText(): String {
        return email.text.toString()
    }

    private fun getPasswordText(): String {
        return password.text.toString()
    }

    private fun emailValidCheck(): Boolean {
        val isValid = getEmailText().isNotEmpty() && EMAIL_ADDRESS.matcher(getEmailText()).matches()
        if (!isValid) {
            this.email.error = resources.getString(R.string.invalidEmailError)
        }
        return isValid
    }

    override fun onStart() {
        super.onStart()
        WorkWithServer.addListenerLogin(this)
    }

    override fun onStop() {
        super.onStop()
        WorkWithServer.addListenerLogin(null)
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
