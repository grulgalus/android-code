package com.codedroid.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codedroid.app.plugins.PluginManager
import com.codedroid.app.ui.components.*
import com.codedroid.app.ui.theme.VSCodeTheme
import com.codedroid.app.viewmodel.MainViewModel
import com.codedroid.app.viewmodel.Panel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        PluginManager.registerLoader(com.codedroid.app.plugins.QuickJsLoader())
        PluginManager.registerLoader(com.codedroid.app.plugins.TermuxBashLoader(this))

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val viewModel: MainViewModel = viewModel()
                
                // Automatická kontrola práv na všechny soubory pro Android 11+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = android.net.Uri.parse("package:$packageName")
                        startActivity(intent)
                    } catch(e: Exception) {}
                }

                LaunchedEffect(intent) {
                    val incomingUri = intent.data
                    if (incomingUri != null) {
                        viewModel.loadFromUri(this@MainActivity, incomingUri)
                    }
                }
                
                VSCodeWorkspace(viewModel)
            }
        }
    }
}

@Composable
fun VSCodeWorkspace(viewModel: MainViewModel) {
    var sidebarWidth by remember { mutableStateOf(260.dp) }
    val density = LocalDensity.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(VSCodeTheme.bgDark)) {
        Row(modifier = Modifier.weight(1f)) {
            NavigationRail(modifier = Modifier.width(50.dp), containerColor = VSCodeTheme.bgActivityBar) {
                Spacer(modifier = Modifier.height(8.dp))
                ActivityIcon(Icons.Outlined.Folder, viewModel.activePanel == Panel.EXPLORER) { viewModel.updateActivePanel(Panel.EXPLORER) }
                ActivityIcon(Icons.Outlined.AccountTree, viewModel.activePanel == Panel.GIT) { viewModel.updateActivePanel(Panel.GIT) }
                ActivityIcon(Icons.Outlined.Extension, viewModel.activePanel == Panel.EXTENSIONS) { viewModel.updateActivePanel(Panel.EXTENSIONS) }
                ActivityIcon(Icons.Outlined.SmartToy, viewModel.activePanel == Panel.AI_AGENT) { viewModel.updateActivePanel(Panel.AI_AGENT) }
                Spacer(modifier = Modifier.weight(1f))
                ActivityIcon(Icons.Outlined.Settings, viewModel.activePanel == Panel.SETTINGS) { viewModel.updateActivePanel(Panel.SETTINGS) }
            }

            Column(modifier = Modifier.width(sidebarWidth).background(VSCodeTheme.bgSidebar).fillMaxHeight()) {
                Text(text = viewModel.activePanel.name, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp))
                Divider(color = VSCodeTheme.bgActivityBar)
                Box(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                    when (viewModel.activePanel) {
                        Panel.EXPLORER -> ExplorerPanel(viewModel)
                        Panel.GIT -> GitPanel(viewModel)
                        Panel.EXTENSIONS -> ExtensionsPanel(viewModel)
                        Panel.AI_AGENT -> AiAgentPanel(viewModel)
                        Panel.SETTINGS -> SettingsPanel(viewModel)
                    }
                }
            }
            
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color.Black.copy(alpha = 0.5f)).pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    with(density) { sidebarWidth = (sidebarWidth + dragAmount.toDp()).coerceIn(150.dp, 500.dp) }
                }
            })

            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(VSCodeTheme.bgDark)) {
                Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF2D2D2D)), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(VSCodeTheme.bgDark).padding(horizontal = 16.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(viewModel.currentFilePath.substringAfterLast("/"), color = VSCodeTheme.textHighlight, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.padding(end = 16.dp)) {
                        IconButton(onClick = { viewModel.saveCurrentFile(context) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Save, contentDescription = null, tint = Color.LightGray) }
                        IconButton(onClick = { viewModel.runCurrentFile(context) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF9EFF7A)) }
                    }
                }
                CodeEditor(code = viewModel.codeText, onCodeChange = { viewModel.updateCode(it); DiscordManager.updatePresence(viewModel) })
            }
        }

        // TADY JE OPRAVENÝ TERMINÁL
        Column(modifier = Modifier.fillMaxWidth().height(160.dp).background(VSCodeTheme.bgDark)) {
            Divider(color = VSCodeTheme.bgActivityBar, thickness = 1.dp)
            Text("KONZOLE / LOGY", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(16.dp, 8.dp), fontWeight = FontWeight.Bold)
            
            val listState = rememberLazyListState()
            val logs = viewModel.terminalLogs
            
            // KOUZLO PRO AUTO-SCROLL DOWN
            LaunchedEffect(logs.size) {
                if (logs.isNotEmpty()) {
                    listState.animateScrollToItem(logs.size - 1)
                }
            }
            
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(logs) { log -> 
                    // Nově používáme FontFamily.Monospace pro konzolový vzhled
                    Text(log, color = VSCodeTheme.terminalGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace) 
                    Spacer(Modifier.height(4.dp)) 
                }
            }
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
