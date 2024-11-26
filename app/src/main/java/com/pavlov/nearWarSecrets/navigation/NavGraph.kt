package com.pavlov.nearWarSecrets.navigation

import android.app.Activity
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.pavlov.nearWarSecrets.ui.auth.AuthScreen
import com.pavlov.nearWarSecrets.ui.itemLoader.ItemLoaderScreen
import com.pavlov.nearWarSecrets.ui.keyinput.KeyInputScreen
import com.pavlov.nearWarSecrets.ui.main.MainScreen
import com.pavlov.nearWarSecrets.ui.setpassword.SetPasswordScreen
import com.pavlov.nearWarSecrets.ui.twosteps.TwoStepsForSaveScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    activity: Activity,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = modifier
    ) {
        composable("auth") {
            AuthScreen(
                onNavigateToMain = { navController.navigate("main") },
                onNavigateToTwoStepsForSave = { navController.navigate("two_steps_for_save") }
            )
        }
        composable("set_password") {
            SetPasswordScreen(
                onPasswordSet = { navController.navigate("two_steps_for_save") }
            )
        }
        composable("two_steps_for_save") {
            TwoStepsForSaveScreen(
                onNavigateToSetPassword = { navController.navigate("set_password") },
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("two_steps_for_save") { inclusive = true }
                    }
                },
                onNavigateToKeyInput = { navController.navigate("key_input") }
            )
        }
        composable("key_input") {
            KeyInputScreen(
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("key_input") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}