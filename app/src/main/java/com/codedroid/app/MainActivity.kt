package com.codedroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Registrace ofiko Plugin Loaderů
        com.codedroid.app.plugins.PluginManager.registerLoader(com.codedroid.app.plugins.QuickJsLoader())
        com.codedroid.app.plugins.PluginManager.registerLoader(com.codedroid.app.plugins.TermuxBashLoader(this))
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VSCodeScreen()
            }
        }
    }
}

enum class Panel { EXPLORER, SEARCH, GIT, RUN, EXTENSIONS, AI_AGENT }

@Composable
fun VSCodeScreen() {
    var activePanel by remember { mutableStateOf(Panel.EXPLORER) }
    
    // Barvy ve stylu VS Code Dark+
    val bgDark = Color(0xFF1E1E1E)
    val bgSidebar = Color(0xFF252526)
    val bgActivityBar = Color(0xFF333333)
    val bgPanel = Color(0xFF1E1E1E)
    val accentColor = Color(0xFF007ACC)
    val textColor = Color(0xFFCCCCCC)

    Column(modifier = Modifier.fillMaxSize().background(bgDark)) {
        // Hlavní rozvržení (Activity Bar + Sidebar + Editor)
        Row(modifier = Modifier.weight(1f)) {
            
            // 1. ACTIVITY BAR (Zcela vlevo)
            NavigationRail(
                modifier = Modifier.width(50.dp),
                containerColor = bgActivityBar
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                ActivityIcon(Icons.Outlined.Folder, activePanel == Panel.EXPLORER) { activePanel = Panel.EXPLORER }
                ActivityIcon(Icons.Outlined.Search, activePanel == Panel.SEARCH) { activePanel = Panel.SEARCH }
                ActivityIcon(Icons.Outlined.AccountTree, activePanel == Panel.GIT) { activePanel = Panel.GIT }
                ActivityIcon(Icons.Outlined.PlayArrow, activePanel == Panel.RUN) { activePanel = Panel.RUN }
                ActivityIcon(Icons.Outlined.Extension, activePanel == Panel.EXTENSIONS) { activePanel = Panel.EXTENSIONS }
                Spacer(modifier = Modifier.weight(1f))
                ActivityIcon(Icons.Outlined.SmartToy, activePanel == Panel.AI_AGENT) { activePanel = Panel.AI_AGENT }
                ActivityIcon(Icons.Outlined.Settings, false) { /* Settings */ }
            }

            // 2. SIDEBAR (Dynamický podle výběru)
            Column(modifier = Modifier.width(260.dp).background(bgSidebar).fillMaxHeight()) {
                Text(
                    text = activePanel.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = FontFamily.Monospace
                )
                
                when (activePanel) {
                    Panel.EXPLORER -> FileExplorer()
                    Panel.GIT -> GitAndBuildPanel()
                    Panel.AI_AGENT -> AiAgentPanel()
                    else -> Text("WIP...", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }

            // 3. EDITOR AREA
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(bgDark)) {
                // Taby (Tabs)
                Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF2D2D2D))) {
                    EditorTab("MainActivity.kt", isActive = true)
                    EditorTab("build.gradle.kts", isActive = false)
                }
                
                // Editor kódu
                var codeText by remember { mutableStateOf("package com.codedroid.app\n\n// TODO: Implement JGit clone here\nfun main() {\n    println(\"GitHub Build & AI Agent Ready!\")\n}") }
                TextField(
                    value = codeText,
                    onValueChange = { codeText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFD4D4D4), fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        // 4. BOTTOM PANEL (Terminal, Output, Problems)
        Column(modifier = Modifier.fillMaxWidth().height(180.dp).background(bgPanel)) {
            Divider(color = Color(0xFF333333), thickness = 1.dp)
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("PROBLEMS", color = textColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("OUTPUT", color = textColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("TERMINAL", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("GIT LOG", color = textColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Text(
                text = "user@lenovo-tab:~/CodeDroidX$ ./gradlew assembleDebug\n> Task :app:compileDebugKotlin\nBUILD SUCCESSFUL in 4s",
                color = Color(0xFF9EFF7A),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 5. STATUS BAR (Úplně dole)
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).background(accentColor).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("main*", color = Color.White, fontSize = 11.sp)
                Text("0 ⚠ 0 ❌", color = Color.White, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Kotlin", color = Color.White, fontSize = 11.sp)
                Text("UTF-8", color = Color.White, fontSize = 11.sp)
                Text("AI Agent: Ready", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ActivityIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(50.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color(0xFF007ACC)).align(Alignment.CenterStart))
        }
        Icon(icon, contentDescription = null, tint = if (isActive) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun EditorTab(title: String, isActive: Boolean) {
    Box(
        modifier = Modifier.fillMaxHeight().background(if (isActive) Color(0xFF1E1E1E) else Color(0xFF2D2D2D)).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isActive) Color(0xFF67E8F9) else Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun FileExplorer() {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text("🗂 CodeDroidX", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📄 build.gradle.kts", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("  📂 app", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Text("    📄 MainActivity.kt", color = Color(0xFF67E8F9), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun GitAndBuildPanel() {
    Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
        Text("SOURCE CONTROL", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = "", onValueChange = {},
            label = { Text("Commit message", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF333333), focusedContainerColor = Color(0xFF333333))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))) { Text("Commit & Push") }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("GITHUB ACTIONS BUILD", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636))) { Text("▶ Trigger Build (CI/CD)") }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("↓ Pull Latest APK") }
    }
}

@Composable
fun AiAgentPanel() {
    LazyColumn(modifier = Modifier.padding(12.dp).fillMaxSize()) {
        item {
            Text("AI AGENT SETTINGS", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            ApiKeyField("OpenAI API Key", "sk-proj-...")
            ApiKeyField("Anthropic API Key", "sk-ant-...")
            ApiKeyField("Google Gemini Key", "AIzaSy...")
            ApiKeyField("OpenRouter Key", "sk-or-v1-...")
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("AGENT CHAT", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "", onValueChange = {},
                placeholder = { Text("Ask Agent to write code...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF333333), focusedContainerColor = Color(0xFF333333))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { 
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Code") 
            }
        }
    }
}

@Composable
fun ApiKeyField(label: String, placeholder: String) {
    var key by remember { mutableStateOf("") }
    OutlinedTextField(
        value = key, onValueChange = { key = it },
        label = { Text(label, fontSize = 11.sp, color = Color.Gray) },
        placeholder = { Text(placeholder, fontSize = 11.sp) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFF1E1E1E), 
            focusedContainerColor = Color(0xFF1E1E1E),
            focusedIndicatorColor = Color(0xFF007ACC)
        ),
        singleLine = true
    )
}
