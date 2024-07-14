package com.kaitokitaya.easytransfer.router

sealed class AppRouter {
    open val path get() = ""

    data object Splash : AppRouter() {
        override val path get() = "splash"
    }

    data object Main : AppRouter() {
        override val path get() = "main"
    }
    data object HowToUseRouter: AppRouter() {
        override val path get() = "how_to_use"
    }
    data object Information: AppRouter() {
        override val path get() = "information"
    }
}