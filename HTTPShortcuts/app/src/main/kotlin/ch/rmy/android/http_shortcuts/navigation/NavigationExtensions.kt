package ch.rmy.android.http_shortcuts.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.navigation.NavigationRequest
import ch.rmy.android.framework.navigation.NavigationRequestImpl
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.documentation.DocumentationUrlManager
import ch.rmy.android.http_shortcuts.components.EventHandler

private const val RESULT_KEY = "result"

@Composable
fun ResultHandler(savedStateHandle: SavedStateHandle, onResult: (result: Any) -> Unit) {
    val result by savedStateHandle.getStateFlow(RESULT_KEY, null as Any?).collectAsStateWithLifecycle()
    LaunchedEffect(result) {
        result?.let {
            onResult(it)
            savedStateHandle[RESULT_KEY] = null
        }
    }
}

@Composable
fun NavigationEventHandler(navController: NavController) {
    val focusManager = LocalFocusManager.current

    EventHandler { event ->
        when (event) {
            is ViewModelEvent.Navigate -> consume {
                focusManager.clearFocus()
                val route = event.navigationRequest.route
                logInfo("Navigation", "Navigating to $route")
                navController.navigate(route = route)
            }
            is ViewModelEvent.OpenURL -> {
                val uri = event.url.toUri()
                if (DocumentationUrlManager.canHandle(uri)) {
                    consume {
                        navController.navigate(route = NavigationDestination.Documentation.buildRequest(uri).route)
                    }
                } else {
                    false
                }
            }
            is ViewModelEvent.CloseScreen -> {
                focusManager.clearFocus()
                navController.previousBackStackEntry?.savedStateHandle?.set(RESULT_KEY, event.result)
                navController.popBackStack()
            }
            else -> false
        }
    }
}

fun NavGraphBuilder.composable(
    destination: NavigationDestination,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = destination.routePattern,
        arguments = destination.arguments,
        enterTransition = {
            fadeIn(
                animationSpec = tween(500),
            ) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(250, delayMillis = 250),
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(250),
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(500),
            )
        },
        content = content,
    )
}

private const val EMPTY_STRING_PLACEHOLDER = "___emPty___"

class RouteBuilder(
    private val basePath: String,
) {
    private val requiredArguments = mutableListOf<String>()
    private val optionalArguments = mutableListOf<Pair<String, String>>()

    fun pathPart(value: Any) {
        requiredArguments.add(value.toString())
    }

    fun parameter(key: String, value: Any?) {
        value?.toString()?.let {
            optionalArguments.add(key to it)
        }
    }

    fun build(): String =
        if (requiredArguments.isEmpty() && optionalArguments.isEmpty()) {
            basePath
        } else {
            buildString {
                append(basePath)
                requiredArguments.forEach { value ->
                    append("/")
                    append(Uri.encode(value.ifEmpty { EMPTY_STRING_PLACEHOLDER }))
                }
                if (optionalArguments.isNotEmpty()) {
                    append("?")
                    append(
                        optionalArguments
                            .joinToString(separator = "&") { (key, value) ->
                                "$key=${Uri.encode(value)}"
                            },
                    )
                }
            }
        }
}

fun NavigationDestination.buildNavigationRequest(builderAction: RouteBuilder.() -> Unit = {}): NavigationRequest =
    RouteBuilder(path)
        .apply(builderAction)
        .build()
        .let(::NavigationRequestImpl)

fun stringArg(key: String) =
    navArgument(key) {
        type = NavType.StringType
        nullable = false
    }

fun booleanArg(key: String) =
    navArgument(key) {
        type = NavType.BoolType
        nullable = false
    }

fun optionalStringArg(key: String) =
    navArgument(key) {
        type = NavType.StringType
        nullable = true
    }

fun optionalIntArg(key: String) =
    navArgument(key) {
        type = NavType.IntType
        defaultValue = -1
    }

fun optionalBooleanArg(key: String) =
    navArgument(key) {
        type = NavType.BoolType
        defaultValue = false
    }

fun Bundle.getEncodedString(key: String): String? =
    getString(key)?.let(Uri::decode)?.let { if (it == EMPTY_STRING_PLACEHOLDER) "" else it }

fun getRoute(path: String, arguments: List<NamedNavArgument>) =
    if (arguments.isEmpty()) {
        path
    } else {
        buildString {
            append(path)
            arguments.filter { !(it.argument.isNullable || it.argument.isDefaultValuePresent) }
                .forEach {
                    append("/{")
                    append(it.name)
                    append("}")
                }
            arguments
                .filter { it.argument.isNullable || it.argument.isDefaultValuePresent }
                .takeUnlessEmpty()
                ?.let { parameters ->
                    append("?")
                    append(
                        parameters.joinToString(separator = "&") {
                            "${it.name}={${it.name}}"
                        },
                    )
                }
        }
    }
