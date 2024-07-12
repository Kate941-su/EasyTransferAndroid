package com.kaitokitaya.easytransfer.mainScreen.model

sealed class ServerStatus {
    open val stateName: String = ""

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
}

// State transition of ServerStatus
// ServerStatus.Standby ---(Tap power on)---> ServerStatus.Launching ---(succeeded)---> ServerStatus.Working
//                     <---              ---- ServerStatus.Shutdown  <---(power off)---
