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
    data object Information: AppRouter() {
        override val path get() = "information"
        override val name get() = "Information"
    }
}