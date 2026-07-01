package com.fabconstruct.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Thème de couleurs Fabconstruct (Noir, Rouge vif et accents gris foncé)
val FabBlack = Color(0xFF121212)
val FabDarkGray = Color(0xFF1E1E1E)
val FabRed = Color(0xFFD32F2F)
val FabWhite = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(dao: ProjectDao) {
    val coroutineScope = rememberCoroutineScope()
    val projects by dao.getAllProjects().collectAsState(initial = emptyList())
    
    var currentScreen by remember { mutableStateOf("home") } // home, new_project, calc
    
    // Variables pour le formulaire de calcul de maçonnerie
    var projectName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var perimeter by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FABCONSTRUCT PRO", color = FabWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FabBlack),
                actions = {
                    if (currentScreen != "home") {
                        IconButton(onClick = { currentScreen = "home" }) {
                            Icon(Icons.Default.Home, contentDescription = "Accueil", tint = FabRed)
                        }
                    }
                }
            )
        },
        containerColor = FabBlack
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(FabBlack)) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    projects = projects,
                    onNavigateToNewProject = { currentScreen = "new_project" },
                    onDeleteProject = { id -> coroutineScope.launch { dao.deleteProject(id) } }
                )
                "new_project" -> NewProjectScreen(
                    projectName = projectName,
                    onProjectNameChange = { projectName = it },
                    clientName = clientName,
                    onClientNameChange = { clientName = it },
                    perimeter = perimeter,
                    onPerimeterChange = { perimeter = it },
                    height = height,
                    onHeightChange = { height = it },
                    duration = duration,
                    onDurationChange = { duration = it },
                    onCalculate = {
                        val p = perimeter.toDoubleOrNull() ?: 0.0
                        val h = height.toDoubleOrNull() ?: 0.0
                        
                        // Formule de calcul technique simplifiée pour blocs et ciment
                        val area = p * h
                        val blocks = (area * 9.5).toInt() // Exemple basé sur vos standards de dimension
                        val cement = (blocks / 20.5).toInt() // Ratio de sacs de ciment requis
                        val days = duration.toIntOrNull() ?: 0

                        coroutineScope.launch {
                            dao.insertProject(
                                Project(
                                    name = projectName,
                                    clientName = clientName,
                                    perimeterMeter = p,
                                    wallHeightMeter = h,
                                    totalBlocksNeeded = blocks,
                                    cementBagsNeeded = cement,
                                    projectDurationDays = days
                                )
                            )
                            // Réinitialisation
                            projectName = ""
                            clientName = ""
                            perimeter = ""
                            height = ""
                            duration = ""
                            currentScreen = "home"
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    projects: List<Project>,
    onNavigateToNewProject: () -> GanttState = {},
    onNavigateToNewProject: () -> Unit,
    onDeleteProject: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tableau de bord des chantiers", color = FabWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
        
        if (projects.isEmpty()) {
            Box(modifier = Modifier.weight(1fr).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Aucun projet enregistré pour le moment.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(projects) { project ->
                    ProjectCard(project = project, onDelete = { onDeleteProject(project.id) })
                }
            }
        }
        
        Button(
            onClick = onNavigateToNewProject,
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FabRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = FabWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("NOUVEAU CALCUL & PROJET", color = FabWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProjectCard(project: Project, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FabDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(project.name, color = FabWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Client: ${project.clientName}", color = Color.LightGray, fontSize = 14.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = FabRed)
                }
            }
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Matériaux estimés", color = FabRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Blocs : ${project.totalBlocksNeeded} pcs", color = FabWhite, fontSize = 14.sp)
                    Text("• Ciment : ${project.cementBagsNeeded} sacs", color = FabWhite, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Périmètre : ${project.perimeterMeter} m", color = Color.Gray, fontSize = 13.sp)
                    Text("Hauteur : ${project.wallHeightMeter} m", color = Color.Gray, fontSize = 13.sp)
                    Text("Durée : ${project.projectDurationDays} jours", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    projectName: String, onProjectNameChange: (String) -> Unit,
    clientName: String, onClientNameChange: (String) -> Unit,
    perimeter: String, onPerimeterChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    duration: String, onDurationChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Calculateur de Devis & Métré", color = FabWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        item {
            OutlinedTextField(
                value = projectName, onValueChange = onProjectNameChange,
                label = { Text("Nom du Projet (ex: Clôture Orphelinat)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FabRed, focusedLabelColor = FabRed, unfocusedLabelColor = Color.Gray, focusedTextColor = FabWhite, unfocusedTextColor = FabWhite)
            )
        }
        item {
            OutlinedTextField(
                value = clientName, onValueChange = onClientNameChange,
                label = { Text("Nom du Client") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FabRed, focusedLabelColor = FabRed, unfocusedLabelColor = Color.Gray, focusedTextColor = FabWhite, unfocusedTextColor = FabWhite)
            )
        }
        item {
            OutlinedTextField(
                value = perimeter, onValueChange = onPerimeterChange,
                label = { Text("Périmètre total (en mètres)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FabRed, focusedLabelColor = FabRed, unfocusedLabelColor = Color.Gray, focusedTextColor = FabWhite, unfocusedTextColor = FabWhite)
            )
        }
        item {
            OutlinedTextField(
                value = height, onValueChange = onHeightChange,
                label = { Text("Hauteur du mur (en mètres)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FabRed, focusedLabelColor = FabRed, unfocusedLabelColor = Color.Gray, focusedTextColor = FabWhite, unfocusedTextColor = FabWhite)
            )
        }
        item {
            OutlinedTextField(
                value = duration, onValueChange = onDurationChange,
                label = { Text("Durée estimée des travaux (en jours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FabRed, focusedLabelColor = FabRed, unfocusedLabelColor = Color.Gray, focusedTextColor = FabWhite, unfocusedTextColor = FabWhite)
            )
        }
        item {
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FabRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("GÉNÉRER L'ESTIMATION ET SAUVEGARDER", color = FabWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
