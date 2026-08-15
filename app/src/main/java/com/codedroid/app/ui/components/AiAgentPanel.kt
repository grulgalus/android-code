package com.codedroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.viewmodel.MainViewModel

@Composable
fun AiAgentPanel(viewModel: MainViewModel) {
    var apiKey by remember { mutableStateOf(viewModel.settings.apiKey) }
    var prompt by remember { mutableStateOf("") }
    var isSettingsOpen by remember { mutableStateOf(apiKey.isBlank()) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // HORNÍ LIŠTA S NASTAVENÍM KLÍČE (Rozbalovací)
        if (isSettingsOpen) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("OpenRouter API Klíč", fontSize = 11.sp) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isSettingsOpen = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                Text("Uložit a zavřít", fontSize = 12.sp)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AI CHAT", color = Color.Gray, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                IconButton(onClick = { isSettingsOpen = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Nastavení", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // HISTORIE CHATU (Kurzor styl - bubliny zpráv)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(viewModel.chatHistory) { msg ->
                val isUser = msg.role == "user"
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if(isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Column(
                        modifier = Modifier
                            .background(
                                color = if (isUser) Color(0xFF2B3A42) else Color(0xFF1E1E1E),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                            .fillMaxWidth(0.9f)
                    ) {
                        Text(if (isUser) "Ty" else "CodeDroid AI", color = if (isUser) Color(0xFF67E8F9) else Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.content, color = Color.White, fontSize = 12.sp)
                        
                        // Tlačítko pro aplikaci kódu do editoru (zobrazí se jen u AI)
                        if (!isUser && msg.content != "Generuji odpověď...") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.appendCode("\n" + msg.content) }, 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Vložit do editoru", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        
        // SPODNÍ POLE PRO ZADÁVÁNÍ DOTAZŮ
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Zeptej se AI...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).height(50.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (prompt.isNotEmpty()) {
                        viewModel.askAi("OpenRouter", prompt, apiKey)
                        prompt = ""
                    }
                },
                modifier = Modifier.size(40.dp).background(Color(0xFF7C3AED), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Odeslat", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
