package com.workouttracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun GoalSettingDialog(
    year: Int,
    currentGoal: Int?,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var goalDays by remember { mutableStateOf(currentGoal?.toString() ?: "200") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Set Goal for $year")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "How many days do you want to work out in $year?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = goalDays,
                    onValueChange = {
                        goalDays = it
                        error = null
                    },
                    label = { Text("Goal (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    supportingText = if (error != null) {
                        { Text(error!!) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Suggested goals: 150 days (41%), 200 days (55%), 250 days (68%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goal = goalDays.toIntOrNull()
                    when {
                        goal == null -> error = "Please enter a valid number"
                        goal < 1 -> error = "Goal must be at least 1 day"
                        goal > 366 -> error = "Goal cannot exceed 366 days"
                        else -> {
                            onSave(goal)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
