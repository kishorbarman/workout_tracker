import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Slider,
  Typography,
  Box,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
} from '@mui/material';
import { Delete, Edit } from '@mui/icons-material';
import { WorkoutType, workoutTypeDisplayNames } from '../types';
import type { Workout } from '../types';
import { format, parseISO } from 'date-fns';

interface WorkoutDialogProps {
  open: boolean;
  dateStr: string;
  existingWorkouts: Workout[];
  onClose: () => void;
  onSave: (dateTime: string, endTime: string, workoutType: string, notes: string) => void;
  onUpdate: (workout: Workout) => void;
  onDelete: (workoutId: string) => void;
}

export default function WorkoutDialog({
  open,
  dateStr,
  existingWorkouts,
  onClose,
  onSave,
  onUpdate,
  onDelete,
}: WorkoutDialogProps) {
  const [workoutType, setWorkoutType] = useState<string>(WorkoutType.STRENGTH);
  const [time, setTime] = useState(() => {
    const now = new Date();
    return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  });
  const [duration, setDuration] = useState(60);
  const [notes, setNotes] = useState('');
  const [editingWorkout, setEditingWorkout] = useState<Workout | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  function resetForm() {
    setWorkoutType(WorkoutType.STRENGTH);
    const now = new Date();
    setTime(`${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`);
    setDuration(60);
    setNotes('');
    setEditingWorkout(null);
  }

  function handleSave() {
    const dateTime = `${dateStr}T${time}:00`;
    const endDate = new Date(`${dateStr}T${time}:00`);
    endDate.setMinutes(endDate.getMinutes() + duration);
    const endTime = endDate.toISOString().slice(0, 19);

    if (editingWorkout) {
      onUpdate({
        ...editingWorkout,
        dateTime,
        endTime,
        workoutType,
        notes,
      });
    } else {
      onSave(dateTime, endTime, workoutType, notes);
    }
    resetForm();
    onClose();
  }

  function handleEdit(workout: Workout) {
    setEditingWorkout(workout);
    setWorkoutType(workout.workoutType);
    const dt = parseISO(workout.dateTime);
    setTime(format(dt, 'HH:mm'));
    const start = parseISO(workout.dateTime);
    const end = parseISO(workout.endTime);
    const durationMins = Math.round((end.getTime() - start.getTime()) / 60000);
    setDuration(Math.min(120, Math.max(5, durationMins)));
    setNotes(workout.notes);
  }

  function handleDelete(workoutId: string) {
    if (confirmDeleteId === workoutId) {
      onDelete(workoutId);
      setConfirmDeleteId(null);
    } else {
      setConfirmDeleteId(workoutId);
    }
  }

  function handleClose() {
    resetForm();
    setConfirmDeleteId(null);
    onClose();
  }

  const formatDuration = (mins: number) => {
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    if (h === 0) return `${m} min`;
    if (m === 0) return `${h} hr`;
    return `${h} hr ${m} min`;
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {editingWorkout ? 'Edit Workout' : `Log Workout — ${format(parseISO(dateStr), 'MMM d, yyyy')}`}
      </DialogTitle>
      <DialogContent>
        {existingWorkouts.length > 0 && !editingWorkout && (
          <>
            <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 1, mb: 1 }}>
              Existing workouts
            </Typography>
            <List dense disablePadding>
              {existingWorkouts.map((w) => (
                <ListItem key={w.id} disableGutters>
                  <ListItemText
                    primary={workoutTypeDisplayNames[w.workoutType as WorkoutType] || w.workoutType}
                    secondary={format(parseISO(w.dateTime), 'h:mm a')}
                  />
                  <ListItemSecondaryAction>
                    <IconButton size="small" onClick={() => handleEdit(w)}>
                      <Edit fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      color={confirmDeleteId === w.id ? 'error' : 'default'}
                      onClick={() => handleDelete(w.id)}
                    >
                      <Delete fontSize="small" />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
            <Divider sx={{ my: 2 }} />
            <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
              Add new workout
            </Typography>
          </>
        )}

        <TextField
          select
          label="Workout Type"
          value={workoutType}
          onChange={(e) => setWorkoutType(e.target.value)}
          fullWidth
          margin="normal"
        >
          {Object.entries(workoutTypeDisplayNames).map(([value, label]) => (
            <MenuItem key={value} value={value}>
              {label}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Time"
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
          fullWidth
          margin="normal"
          InputLabelProps={{ shrink: true }}
        />

        <Box sx={{ mt: 2, px: 1 }}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Duration: {formatDuration(duration)}
          </Typography>
          <Slider
            value={duration}
            onChange={(_, v) => setDuration(v as number)}
            min={5}
            max={120}
            step={5}
            marks={[
              { value: 5, label: '5m' },
              { value: 60, label: '1h' },
              { value: 120, label: '2h' },
            ]}
          />
        </Box>

        <TextField
          label="Notes"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          fullWidth
          margin="normal"
          multiline
          rows={3}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button variant="contained" onClick={handleSave}>
          {editingWorkout ? 'Update' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
