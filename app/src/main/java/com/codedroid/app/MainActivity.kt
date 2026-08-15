package com.codedroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.codedroid.app.ui.components.*
import com.codedroid.app.ui.theme.VSCodeTheme
import com.codedroid.app.viewmodel.MainViewModel
import com.codedroid.app.viewmodel.Panel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val viewModel: MainViewModel = viewModel()
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
        
        // HLAVNÍ PROSTOR
        Row(modifier = Modifier.weight(1f)) {
            // VS CODE ACTIVITY BAR
            NavigationRail(modifier = Modifier.width(50.dp), containerColor = VSCodeTheme.bgActivityBar) {
                Spacer(modifier = Modifier.height(8.dp))
                ActivityIcon(Icons.Outlined.Folder, viewModel.activePanel == Panel.EXPLORER) { viewModel.updateActivePanel(Panel.EXPLORER) }
                ActivityIcon(Icons.Outlined.Search, false) {}
                ActivityIcon(Icons.Outlined.AccountTree, viewModel.activePanel == Panel.GIT) { viewModel.updateActivePanel(Panel.GIT) }
                ActivityIcon(Icons.Outlined.AutoAwesome, viewModel.activePanel == Panel.AI_AGENT) { viewModel.updateActivePanel(Panel.AI_AGENT) }
                Spacer(modifier = Modifier.weight(1f))
                ActivityIcon(Icons.Outlined.Settings, viewModel.activePanel == Panel.SETTINGS) { viewModel.updateActivePanel(Panel.SETTINGS) }
            }

            // VS CODE SIDEBAR
            Column(modifier = Modifier.width(sidebarWidth).background(VSCodeTheme.bgSidebar).fillMaxHeight()) {
                Text(text = viewModel.activePanel.name, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp))
                Box(modifier = Modifier.fillMaxSize()) {
                    when (viewModel.activePanel) {
                        Panel.EXPLORER -> ExplorerPanel(viewModel)
                        Panel.GIT -> GitPanel(viewModel)
                        Panel.AI_AGENT -> AiAgentPanel(viewModel)
                        Panel.SETTINGS -> SettingsPanel(viewModel)
                        else -> {}
                    }
                }
            }
            
            // DRAG BAR
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color(0xFF2B2B2B)).pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    with(density) { sidebarWidth = (sidebarWidth + dragAmount.toDp()).coerceIn(150.dp, 500.dp) }
                }
            })

            // PRAVÁ ČÁST (EDITOR + TERMINÁL)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                
                // VS CODE TABS (Záložky)
                LazyRow(modifier = Modifier.fillMaxWidth().height(35.dp).background(Color(0xFF252526)), verticalAlignment = Alignment.CenterVertically) {
                    itemsIndexed(viewModel.openTabs) { index, tab ->
                        val isActive = index == viewModel.activeTabIndex
                        Row(
                            modifier = Modifier.fillMaxHeight().background(if (isActive) VSCodeTheme.bgDark else Color.Transparent).clickable { viewModel.activeTabIndex = index }.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tab.name + if(tab.isModified) " •" else "", color = if (isActive) Color(0xFF569CD6) else Color.Gray, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.Gray, modifier = Modifier.size(14.dp).clickable { viewModel.closeTab(index) })
                        }
                    }
                    // Ovládací prvky vpravo
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
                            IconButton(onClick = { viewModel.isInlineAiVisible = !viewModel.isInlineAiVisible }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFA78BFA)) }
                            IconButton(onClick = { viewModel.saveCurrentFile(context) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Save, null, tint = Color.LightGray) }
                            IconButton(onClick = { viewModel.runCurrentFile(context) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF89D185)) }
                        }
                    }
                }
                
                // BREADCRUMBS
                Row(modifier = Modifier.fillMaxWidth().height(22.dp).background(VSCodeTheme.bgDark).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("CodeDroid X > ${viewModel.getActiveTab()?.name ?: "..."}", color = Color.Gray, fontSize = 11.sp)
                }

                // EDITOR
                Box(modifier = Modifier.weight(1f)) {
                    CodeEditor(viewModel)
                }

                // TERMINAL PANEL
                Column(modifier = Modifier.fillMaxWidth().height(140.dp).background(VSCodeTheme.bgSidebar)) {
                    Divider(color = Color(0xFF2B2B2B), thickness = 1.dp)
                    Text("TERMINAL", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(12.dp, 6.dp))
                    val listState = rememberLazyListState()
                    val logs = viewModel.terminalLogs
                    LaunchedEffect(logs.size) { if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1) }
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        items(logs) { Text(it, color = VSCodeTheme.terminalGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace); Spacer(Modifier.height(2.dp)) }
                    }
                }
            }
        }

        // VS CODE STATUS BAR (Modrá lišta dole)
        Row(modifier = Modifier.fillMaxWidth().height(22.dp).background(Color(0xFF007ACC)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("main*", color = Color.White, fontSize = 11.sp)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.Sync, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ln 1, Col 1", color = Color.White, fontSize = 11.sp)
                Spacer(Modifier.width(16.dp))
                Text("UTF-8", color = Color.White, fontSize = 11.sp)
                Spacer(Modifier.width(16.dp))
                Text(viewModel.getActiveTab()?.name?.substringAfterLast(".")?.uppercase() ?: "TXT", color = Color.White, fontSize = 11.sp)
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
