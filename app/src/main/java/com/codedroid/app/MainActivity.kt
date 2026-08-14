package com.codedroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.plugins.PluginManager
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Registrace ofiko Plugin Loaderů při startu
        PluginManager.registerLoader(com.codedroid.app.plugins.QuickJsLoader())
        PluginManager.registerLoader(com.codedroid.app.plugins.TermuxBashLoader(this))

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VSCodeScreen()
            }
        }
    }
}

enum class Panel { EXPLORER, GIT, EXTENSIONS }

@Composable
fun VSCodeScreen() {
    var activePanel by remember { mutableStateOf(Panel.EXPLORER) }
    var terminalOutput by remember { mutableStateOf("> CodeDroid X inicializováno...\n") }
    
    val bgDark = Color(0xFF1E1E1E)
    val bgSidebar = Color(0xFF252526)
    val bgActivityBar = Color(0xFF333333)

    Column(modifier = Modifier.fillMaxSize().background(bgDark)) {
        Row(modifier = Modifier.weight(1f)) {
            
            // 1. ACTIVITY BAR
            NavigationRail(modifier = Modifier.width(50.dp), containerColor = bgActivityBar) {
                Spacer(modifier = Modifier.height(8.dp))
                ActivityIcon(Icons.Outlined.Folder, activePanel == Panel.EXPLORER) { activePanel = Panel.EXPLORER }
                ActivityIcon(Icons.Outlined.AccountTree, activePanel == Panel.GIT) { activePanel = Panel.GIT }
                ActivityIcon(Icons.Outlined.Extension, activePanel == Panel.EXTENSIONS) { activePanel = Panel.EXTENSIONS }
            }

            // 2. SIDEBAR
            Column(modifier = Modifier.width(260.dp).background(bgSidebar).fillMaxHeight().padding(12.dp)) {
                Text(text = activePanel.name, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                
                when (activePanel) {
                    Panel.EXPLORER -> Text("📂 workspace\n  📄 MainActivity.kt\n  📄 build.gradle.kts", color = Color.LightGray, fontFamily = FontFamily.Monospace)
                    Panel.GIT -> GitPanel { log -> terminalOutput += "$log\n" }
                    Panel.EXTENSIONS -> ExtensionsPanel { log -> terminalOutput += "$log\n" }
                }
            }

            // 3. EDITOR
            var codeText by remember { mutableStateOf("// Vitejte v CodeDroid X\n// Vyberte panel nalevo pro praci s Gitem nebo Pluginy.") }
            TextField(
                value = codeText, onValueChange = { codeText = it },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFD4D4D4), fontSize = 14.sp),
                colors = TextFieldDefaults.colors(focusedContainerColor = bgDark, unfocusedContainerColor = bgDark)
            )
        }

        // 4. TERMINÁL
        Column(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Black).padding(8.dp)) {
            Text("TERMINAL", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
            LazyColumn {
                item { Text(terminalOutput, color = Color(0xFF9EFF7A), fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun GitPanel(logToTerminal: (String) -> Unit) {
    var repoUrl by remember { mutableStateOf("https://github.com/user/repo.git") }
    
    OutlinedTextField(value = repoUrl, onValueChange = { repoUrl = it }, label = { Text("URL Repozitáře", color = Color.Gray, fontSize = 12.sp) })
    Spacer(modifier = Modifier.height(8.dp))
    
    Button(onClick = {
        logToTerminal("> Klonuji $repoUrl...")
        // Zde voláme náš reálný JGit kód!
        GitHelper.cloneRepo(repoUrl, File("/data/data/com.termux/files/home/workspace")) { success, msg ->
            logToTerminal(if (success) "[OK] $msg" else "[ERR] $msg")
        }
    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)), modifier = Modifier.fillMaxWidth()) {
        Text("Klonovat (Clone)")
    }
}

@Composable
fun ExtensionsPanel(logToTerminal: (String) -> Unit) {
    Text("Aktivní Loadery:", color = Color.Gray, fontSize = 12.sp)
    LazyColumn(modifier = Modifier.height(100.dp)) {
        items(PluginManager.getAvailableEngines()) { engine ->
            Text("⚙️ $engine", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        logToTerminal("> Hledám nové loadery na GitHubu (WIP)...")
    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)), modifier = Modifier.fillMaxWidth()) {
        Text("Přidat Loader")
    }

    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
        logToTerminal("> Spouštím testovací JS plugin...")
        // Voláme reálný Plugin Manager!
        val vysledek = PluginManager.runPlugin("test_1", "plugin.js", "var a = 5; var b = 10; 'Vysledek z QuickJS: ' + (a+b);", "quickjs")
        logToTerminal(vysledek)
    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)), modifier = Modifier.fillMaxWidth()) {
        Text("Spustit Test JS Plugin")
    }
}

@Composable
fun ActivityIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(50.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isActive) Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color(0xFF007ACC)).align(Alignment.CenterStart))
        Icon(icon, contentDescription = null, tint = if (isActive) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
    }
}
