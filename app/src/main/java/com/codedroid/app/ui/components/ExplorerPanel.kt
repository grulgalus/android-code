package com.codedroid.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.FileHelper
import com.codedroid.app.viewmodel.MainViewModel

@Composable
fun ExplorerPanel(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentDir by remember { mutableStateOf(context.filesDir.absolutePath) }
    var files by remember { mutableStateOf(FileHelper.getFilesInDir(currentDir)) }

    Column {
        Text("PRŮZKUMNÍK", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
        
        Button(
            onClick = { 
                currentDir = "/storage/emulated/0"
                files = FileHelper.getFilesInDir(currentDir)
            }, 
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
        ) {
            Text("Otevřít Interní Úložiště", fontSize = 11.sp)
        }

        Text("Složka: ${currentDir.substringAfterLast("/")}", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(modifier = Modifier.fillMaxWidth().clickable {
                    val parent = java.io.File(currentDir).parent
                    if (parent != null) {
                        currentDir = parent
                        files = FileHelper.getFilesInDir(currentDir)
                    }
                }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(".. (Zpět)", color = Color.LightGray, fontSize = 13.sp)
                }
            }

            items(files) { file ->
                Row(modifier = Modifier.fillMaxWidth().clickable {
                    if (file.isDirectory) {
                        currentDir = file.path
                        files = FileHelper.getFilesInDir(currentDir)
                    } else {
                        viewModel.loadFile(file.path)
                    }
                }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = if (file.isDirectory) Color(0xFFFFCA28) else Color(0xFF67E8F9),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(file.name, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}
