package ch.rmy.android.framework.navigation

interface NavigationRequest {
    val route: String
}

data class NavigationRequestImpl(
    override val route: String,
) : NavigationRequest
