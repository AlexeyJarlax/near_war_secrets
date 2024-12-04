package com.pavlov.nearWarSecrets.ui.main

//@Composable
//fun MainScreen(navController: NavHostController) {
//    Scaffold(
//        bottomBar = { BottomNavigationBar(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = NavDestinations.AUTH,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//
//            composable(NavDestinations.LOADER) { ItemLoaderScreen() }
//            composable(NavDestinations.STORAGE_LOG) { StorageLogScreen(navController) }
//            composable(NavDestinations.SETTINGS) {
//                SettingsScreen(
//                    navController = navController,
//                    onNavigateBack = { navController.popBackStack() },
//                    onAboutClicked = { navController.navigate(NavDestinations.ABOUT) },
//                    onSecuritySettingsClicked = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
//                )
//            }
//            composable(NavDestinations.ABOUT) { AboutScreen(navController) }
//        }
//    }
//}