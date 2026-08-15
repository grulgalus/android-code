package com.codedroid.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.DiscordManager
import com.codedroid.app.GitHelper
import com.codedroid.app.plugins.PluginManager
import com.codedroid.app.viewmodel.MainViewModel
import java.io.File

@Composable
fun GitPanel(viewModel: MainViewModel) {
    var repoUrl by remember { mutableStateOf("https://github.com/user/repo.git") }
    Column {
        OutlinedTextField(
            value = repoUrl, onValueChange = { repoUrl = it }, 
            label = { Text("URL repozitáře", fontSize = 11.sp) }, 
            modifier = Modifier.fillMaxWidth().height(60.dp), 
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            viewModel.logToTerminal("Začínám klonování $repoUrl ...")
            GitHelper.cloneRepo(repoUrl, File("/data/data/com.termux/files/home/workspace")) { success, msg ->
                viewModel.logToTerminal(if(success) "[GIT ÚSPĚCH] $msg" else "[GIT CHYBA] $msg")
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))) { 
            Text("Klonovat", fontSize = 12.sp) 
        }
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
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636))) { 
            Text("Test: QuickJS", fontSize = 12.sp) 
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel) {
    Column {
        Text("NASTAVENÍ", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        // --- OPRÁVNĚNÍ SOUBORŮ --- 
        val context = androidx.compose.ui.platform.LocalContext.current
        Spacer(modifier = Modifier.height(16.dp))
        Text("Systémová Práva", color = Color.White, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            } catch(e: Exception) {
                viewModel.logToTerminal("Nelze otevřít nastavení. Zkuste to ručně v nastavení Androidu.")
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
            Text("Povolit přístup k Úložišti", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Vzhled Editoru", color = Color.White, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { viewModel.logToTerminal("Settings: Téma zatím nelze změnit.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
            Text("Změnit téma (WIP)", fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Integrace", color = Color.White, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { 
            DiscordManager.isEnabled = !DiscordManager.isEnabled 
            viewModel.logToTerminal("Discord RPC: ${DiscordManager.isEnabled}")
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2))) {
            Text("Zapnout Discord Status", fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("O aplikaci", color = Color.White, fontSize = 13.sp)
        Text("CodeDroid X - Verze 1.0\nBěží na Jetpack Compose", color = Color.Gray, fontSize = 12.sp)
    }
}
