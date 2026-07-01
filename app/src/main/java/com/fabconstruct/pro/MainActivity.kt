package com.fabconstruct.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialisation de la base de données locale Room
        val database = ProjectDatabase.getDatabase(this)
        val projectDao = database.projectDao()

        setContent {
            // Lancement de l'interface graphique principale
            MainAppScreen(dao = projectDao)
        }
    }
}

