package com.kaii.trainspotter.domain

enum class LoginEvent {
    RealtimeKeyInvalid,
    TrafikverketKeyInvalid,
    LoginSuccessful,
    RequestNotificationPermission
}