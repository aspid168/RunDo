package ru.study.rundo.interfaces

interface AuthorizationActivityNavigator {
    fun switchToLoginFragment()
    fun switchToRegistrationFragment()
    fun goToMainActivity(token: String)
}
