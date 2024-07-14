package com.kaitokitaya.easytransfer.router

sealed class AppRouter {
    open val path get() = ""
    open val name get() = ""

    data object Splash : AppRouter() {
        override val path get() = "splash"
        override val name get() = "Splash"
    }

    data object Main : AppRouter() {
        override val path get() = "main"
        override val name get() = "Main"
    }
    data object HowToUseRouter: AppRouter() {
        override val path get() = "how_to_use"
        override val name get() = "How To Use"
    }
    data object InformationRouter: AppRouter() {
        override val path get() = "information"
        override val name get() = "Information"
    }

    data object PrivacyPolicyRouter: AppRouter() {
        override val path get() = "privacy_policy"
        override val name get() = "Privacy policy"
    }

    data object TermsOfUseRouter: AppRouter() {
        override val path get() = "terms_of_use"
        override val name get() = "Terms of Use"
    }
}