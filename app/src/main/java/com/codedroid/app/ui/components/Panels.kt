package com.codedroid.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.GitHelper
import com.codedroid.app.plugins.PluginManager
import com.codedroid.app.viewmodel.MainViewModel
import java.io.File

@Composable
fun ExplorerPanel() {
    Column {
        Text("🗂 CodeDroidX", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📄 build.gradle.kts", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📂 app", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun GitPanel(viewModel: MainViewModel) {
    var repoUrl by remember { mutableStateOf("https://github.com/user/repo.git") }
    Column {
        OutlinedTextField(value = repoUrl, onValueChange = { repoUrl = it }, label = { Text("URL repozitáře", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth().height(60.dp), textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            viewModel.logToTerminal("Začínám klonování $repoUrl ...")
            GitHelper.cloneRepo(repoUrl, File("/data/data/com.termux/files/home/workspace")) { success, msg ->
                viewModel.logToTerminal(if(success) "[GIT ÚSPĚCH] $msg" else "[GIT CHYBA] $msg")
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))) { Text("Klonovat", fontSize = 12.sp) }
    }
}

@Composable
fun ExtensionsPanel(viewModel: MainViewModel) {
    Column {
        Text("Aktivní Enginy:", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        PluginManager.getAvailableEngines().forEach { engine ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(engine, color = Color.White, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.logToTerminal("Spouštím JS Plugin přes QuickJS...")
            val result = PluginManager.runPlugin("test_plugin", "test.js", "var sum = 0; for(var i=1; i<=5; i++) sum+=i; 'Součet je: ' + sum;", "quickjs")
            viewModel.logToTerminal("Výsledek: $result")
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636))) { Text("Test: QuickJS", fontSize = 12.sp) }
    }
}

@Composable
fun AiAgentPanel(viewModel: MainViewModel) {
    var apiKey by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Klíč", fontSize = 11.sp) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(60.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, placeholder = { Text("Např: Napiš for cyklus", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth().height(100.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (prompt.isNotEmpty()) {
                viewModel.logToTerminal("AI Agent: Generuji odpověď...")
                viewModel.appendCode("// AI: $prompt\nfor(i in 1..10) { println(i) }")
                prompt = ""
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { 
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generovat Kód", fontSize = 12.sp) 
        }
    }
}
