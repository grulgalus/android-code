package com.codedroid.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.viewmodel.MainViewModel

@Composable
fun AiAgentPanel(viewModel: MainViewModel) {
    var apiKey by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    val providers = listOf("OpenRouter", "Google Gemini", "OpenAI", "Anthropic", "Ollama")
    var selectedProvider by remember { mutableStateOf(providers[0]) }

    Column {
        Text("VÝBĚR PROVIDERA", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        providers.forEach { provider ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                RadioButton(
                    selected = (provider == selectedProvider),
                    onClick = { selectedProvider = provider },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF67E8F9), unselectedColor = Color.Gray)
                )
                Text(provider, color = Color.LightGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Klíč pro $selectedProvider", fontSize = 11.sp) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(60.dp))
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, placeholder = { Text("Např: Napiš for cyklus", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth().height(100.dp))
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = {
            if (prompt.isNotEmpty()) {
                // Zavolá asynchronní funkci ve ViewModelu
                viewModel.askAi(selectedProvider, prompt, apiKey)
                prompt = ""
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { 
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generovat Kód", fontSize = 12.sp) 
        }
    }
}
