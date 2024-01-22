package com.lattice.facerecognition.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun RegisteredNamesScreen(
    navController: NavController,
    viewModel: RegisteredNameViewModel = hiltViewModel()
) {
    Column {
        viewModel.namesList.forEach {  name ->
            Text(text = name)
        }
    }
}