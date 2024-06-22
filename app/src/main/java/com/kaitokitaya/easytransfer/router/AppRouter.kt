package com.kaitokitaya.easytransfer.router

sealed class AppRouter {
    class Splash: AppRouter() {
        companion object {
            const val PATH: String = "splash"
        }
    }
    class Main: AppRouter() {
        companion object {
            const val PATH: String = "main"
        }
    }
    class  ServerDetail: AppRouter() {
        companion object {
           const val PATH: String = "server_detail"
        }
    }
}