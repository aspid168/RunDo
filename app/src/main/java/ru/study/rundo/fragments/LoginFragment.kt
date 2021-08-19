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
import ru.study.rundo.SingletonClass
import ru.study.rundo.WorkWithServer
import ru.study.rundo.activities.AuthorizationActivity
import ru.study.rundo.interfaces.Handler
import ru.study.rundo.models.TokenInfo

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    lateinit var loginButton: Button
    lateinit var registrationButton: Button
    lateinit var email: EditText
    lateinit var password: EditText

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
//            (act as AuthorizationActivityNavigator).goToMainActivity("117:1f9b389df8dae29af45b8465d2a85705c9ba0afa")
            if (emailValidCheck()) {
                val workWithServer = WorkWithServer(requireContext())
                workWithServer.login(getEmailText(), getPasswordText())
                workWithServer.addListenerLogin(object : Handler<TokenInfo> {
                    override fun onSuccess(result: TokenInfo) {
                        if (act is AuthorizationActivityNavigator) {
                            act.goToMainActivity(result.token)
                        }
                    }

                    override fun onError(error: String) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    }
                })

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
}
