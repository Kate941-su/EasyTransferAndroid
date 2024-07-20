package com.kaitokitaya.easytransfer.screen.mainScreen.model

// State transition of ServerStatus
// ServerStatus.Standby ---(Tap power on)---> ServerStatus.Launching ---(succeeded)---> ServerStatus.Working
//                     <---              ---- ServerStatus.Shutdown  <---(power off)---
sealed class ServerStatus {
    open val stateName: String = ""

    data object Unavailable : ServerStatus() {
        override val stateName: String
            get() = "Unavailable"
    }

    data object Standby : ServerStatus() {
        override val stateName: String
            get() = "Standby"
    }

    data object Launching : ServerStatus() {
        override val stateName: String
            get() = "Launching..."
    }

    data object Shutdown : ServerStatus() {
        override val stateName: String
            get() = "Shutting down..."
    }

    data object Working : ServerStatus() {
        override val stateName: String
            get() = "Working"
    }

    data object Refresh : ServerStatus() {
        override val stateName: String
            get() = "Refreshing..."
    }
}