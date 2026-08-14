package com.codedroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.plugins.PluginManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// === HLAVNÍ STRUKTURA ===
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Registrace Plugin Engine
        PluginManager.registerLoader(com.codedroid.app.plugins.QuickJsLoader())
        PluginManager.registerLoader(com.codedroid.app.plugins.TermuxBashLoader(this))

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VSCodeWorkspace()
            }
        }
    }
}

// Stavy pro levý panel
enum class Panel { EXPLORER, GIT, EXTENSIONS, AI_AGENT }

@Composable
fun VSCodeWorkspace() {
    // Globální stavy aplikace
    var activePanel by remember { mutableStateOf(Panel.EXPLORER) }
    var codeText by remember { mutableStateOf("// Vítejte v CodeDroid X\n// \n// Zkus levé menu:\n// 1. Git (Klonování)\n// 2. Extensions (Test QuickJS)\n// 3. AI Agent (Generování)\n\nfun main() {\n    println(\"CodeDroid X Ready!\")\n}") }
    var terminalLogs by remember { mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]")) }

    // Funkce pro bezpečné přidání logu do terminálu
    val logToTerminal: (String) -> Unit = { msg ->
        terminalLogs = terminalLogs + "[${time()}] $msg"
    }

    // Barvy VSCode
    val bgDark = Color(0xFF1E1E1E)
    val bgSidebar = Color(0xFF252526)
    val bgActivityBar = Color(0xFF333333)

    Column(modifier = Modifier.fillMaxSize().background(bgDark)) {
        // --- HORNÍ ČÁST (Sidebar + Editor) ---
        Row(modifier = Modifier.weight(1f)) {
            
            // 1. ACTIVITY BAR (Levý pruh s ikonami)
            NavigationRail(modifier = Modifier.width(50.dp), containerColor = bgActivityBar) {
                Spacer(modifier = Modifier.height(8.dp))
                ActivityIcon(Icons.Outlined.Folder, activePanel == Panel.EXPLORER) { activePanel = Panel.EXPLORER }
                ActivityIcon(Icons.Outlined.AccountTree, activePanel == Panel.GIT) { activePanel = Panel.GIT }
                ActivityIcon(Icons.Outlined.Extension, activePanel == Panel.EXTENSIONS) { activePanel = Panel.EXTENSIONS }
                Spacer(modifier = Modifier.weight(1f))
                ActivityIcon(Icons.Outlined.SmartToy, activePanel == Panel.AI_AGENT) { activePanel = Panel.AI_AGENT }
                ActivityIcon(Icons.Outlined.Settings, false) { logToTerminal("Settings: WIP") }
            }

            // 2. DYNAMICKÝ SIDEBAR
            Column(modifier = Modifier.width(260.dp).background(bgSidebar).fillMaxHeight()) {
                Text(
                    text = activePanel.name, color = Color.White, fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp)
                )
                Divider(color = Color(0xFF333333))
                
                Box(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                    when (activePanel) {
                        Panel.EXPLORER -> ExplorerPanel()
                        Panel.GIT -> GitPanel(logToTerminal)
                        Panel.EXTENSIONS -> ExtensionsPanel(logToTerminal)
                        Panel.AI_AGENT -> AiAgentPanel(logToTerminal) { generatedCode -> 
                            codeText += "\n\n// AI Vygenerováno:\n$generatedCode"
                        }
                    }
                }
            }

            // 3. HLAVNÍ EDITOR
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(bgDark)) {
                // Taby
                Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF2D2D2D))) {
                    Box(modifier = Modifier.background(bgDark).padding(horizontal = 16.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text("MainActivity.kt", color = Color(0xFF67E8F9), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                
                TextField(
                    value = codeText, onValueChange = { codeText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFD4D4D4), fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        // --- SPODNÍ ČÁST (Terminal) ---
        Column(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF1E1E1E))) {
            Divider(color = Color(0xFF333333), thickness = 1.dp)
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("PROBLEMS", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("OUTPUT", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("TERMINAL", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp)) {
                items(terminalLogs) { log ->
                    Text(log, color = Color(0xFF9EFF7A), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
        
        // --- STATUS BAR ---
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).background(Color(0xFF007ACC)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("main*", color = Color.White, fontSize = 11.sp)
            Text("CodeDroid X - AI Ready", color = Color.White, fontSize = 11.sp)
        }
    }
}

// === KOMPONENTY PRO PANELY ===

@Composable
fun ExplorerPanel() {
    Column {
        Text("🗂 CodeDroidX", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📄 build.gradle.kts", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📂 app", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("    📄 MainActivity.kt", color = Color(0xFF67E8F9), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun GitPanel(logToTerminal: (String) -> Unit) {
    var repoUrl by remember { mutableStateOf("https://github.com/user/repo.git") }
    Column {
        OutlinedTextField(
            value = repoUrl, onValueChange = { repoUrl = it },
            label = { Text("URL repozitáře", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth().height(60.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            logToTerminal("Začínám klonování $repoUrl ...")
            GitHelper.cloneRepo(repoUrl, File("/data/data/com.termux/files/home/workspace")) { success, msg ->
                logToTerminal(if(success) "[GIT ÚSPĚCH] $msg" else "[GIT CHYBA] $msg")
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))) {
            Text("Klonovat", fontSize = 12.sp)
        }
    }
}

@Composable
fun ExtensionsPanel(logToTerminal: (String) -> Unit) {
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
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            logToTerminal("Spouštím JS Plugin přes QuickJS...")
            val jsCode = "var sum = 0; for(var i=1; i<=5; i++) sum+=i; 'Součet 1 až 5 je: ' + sum;"
            val result = PluginManager.runPlugin("test_plugin", "test.js", jsCode, "quickjs")
            logToTerminal("Výsledek pluginu: $result")
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636))) {
            Text("Test: QuickJS (Matematika)", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = {
            logToTerminal("Posílám Termux Plugin do pozadí...")
            val bashCode = "mkdir -p ~/CodeDroidX_Test && echo 'Ahoj z Termuxu' > ~/CodeDroidX_Test/test.txt"
            val result = PluginManager.runPlugin("termux_plugin", "test.sh", bashCode, "termux")
            logToTerminal("Výsledek pluginu: $result")
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
            Text("Test: Termux Bash (Vytvoř složku)", fontSize = 12.sp)
        }
    }
}

@Composable
fun AiAgentPanel(logToTerminal: (String) -> Unit, onCodeGenerated: (String) -> Unit) {
    var apiKey by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    Column {
        Text("AI NASTAVENÍ", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = apiKey, onValueChange = { apiKey = it },
            label = { Text("API Klíč (OpenAI/Anthropic)", fontSize = 11.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("AI AGENT CHAT", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = prompt, onValueChange = { prompt = it },
            placeholder = { Text("Např: Napiš for cyklus", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = {
            if (prompt.isNotEmpty()) {
                logToTerminal("AI Agent: Generuji odpověď na '$prompt'...")
                // Simulace AI odpovědi
                val vysledek = "for(i in 1..10) {\n    println(\"Cislo: \$i\")\n}"
                onCodeGenerated(vysledek)
                logToTerminal("AI Agent: Kód byl úspěšně vložen do editoru.")
                prompt = ""
            } else {
                logToTerminal("AI Agent: Napiš mi prosím, co chceš vygenerovat.")
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generovat Kód", fontSize = 12.sp)
        }
    }
}

@Composable
fun ActivityIcon(icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(50.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isActive) Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color(0xFF007ACC)).align(Alignment.CenterStart))
        Icon(icon, contentDescription = null, tint = if (isActive) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
    }
}

fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
