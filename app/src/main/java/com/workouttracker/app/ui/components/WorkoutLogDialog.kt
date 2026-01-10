package com.workouttracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.model.WorkoutType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogDialog(
    selectedDate: LocalDate,
    existingWorkouts: List<Workout> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (LocalDateTime, String, String) -> Unit,
    onUpdate: (Workout) -> Unit = {},
    onDelete: (Workout) -> Unit = {}
) {
    // If there are existing workouts, show the first one for editing
    val editingWorkout = existingWorkouts.firstOrNull()

    var selectedWorkoutType by remember {
        mutableStateOf(
            editingWorkout?.let { WorkoutType.fromString(it.workoutType) } ?: WorkoutType.CARDIO
        )
    }
    var notes by remember { mutableStateOf(editingWorkout?.notes ?: "") }
    var selectedHour by remember {
        mutableStateOf(editingWorkout?.dateTime?.hour ?: LocalTime.now().hour)
    }
    var selectedMinute by remember {
        mutableStateOf(editingWorkout?.dateTime?.minute ?: LocalTime.now().minute)
    }
    var durationMinutes by remember {
        mutableStateOf(
            editingWorkout?.let {
                java.time.Duration.between(it.dateTime, it.endTime).toMinutes().toInt()
            } ?: 60
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation && editingWorkout != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Workout?") },
            text = { Text("Are you sure you want to delete this workout?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(editingWorkout)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (editingWorkout != null) "Edit Workout" else "Log Workout")
                if (editingWorkout != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete workout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Date display
                Text(
                    text = "Date: ${selectedDate}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Time picker
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Hour picker
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 0..23,
                        label = "Hour"
                    )

                    Text(
                        text = ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    // Minute picker
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59,
                        label = "Min"
                    )
                }

                // Duration picker
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        values = (5..240 step 5).toList(),
                        label = "Minutes",
                        modifier = Modifier.weight(0.4f)
                    )
                }

                // Workout type dropdown
                Text(
                    text = "Workout Type",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedWorkoutType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        WorkoutType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedWorkoutType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Notes
                Text(
                    text = "Notes (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Add any notes about your workout...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dateTime = LocalDateTime.of(
                        selectedDate,
                        LocalTime.of(selectedHour, selectedMinute)
                    )
                    val endTime = dateTime.plusMinutes(durationMinutes.toLong())

                    if (editingWorkout != null) {
                        // Update existing workout
                        val updatedWorkout = editingWorkout.copy(
                            dateTime = dateTime,
                            endTime = endTime,
                            workoutType = selectedWorkoutType.name,
                            notes = notes
                        )
                        onUpdate(updatedWorkout)
                    } else {
                        // Create new workout
                        onSave(dateTime, selectedWorkoutType.name, notes)
                    }
                    onDismiss()
                }
            ) {
                Text(if (editingWorkout != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
