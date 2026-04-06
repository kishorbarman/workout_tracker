import { useState, useRef } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Box,
} from '@mui/material';
import { UploadFile } from '@mui/icons-material';
import { collection, addDoc, doc, setDoc } from 'firebase/firestore';
import { db } from '../firebase';

interface BackupWorkout {
  id: number;
  dateTime: string;
  endTime?: string;
  workoutType: string;
  notes: string;
  year: number;
}

interface BackupGoal {
  year: number;
  goalDays: number;
}

interface BackupData {
  workouts: BackupWorkout[];
  goals: BackupGoal[];
  lastBackupTime?: string;
}

interface ImportDialogProps {
  open: boolean;
  userId: string;
  onClose: () => void;
}

export default function ImportDialog({ open, userId, onClose }: ImportDialogProps) {
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  async function handleFileSelect(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    setImporting(true);
    setResult(null);

    try {
      const text = await file.text();
      const data: BackupData = JSON.parse(text);

      if (!data.workouts || !Array.isArray(data.workouts)) {
        throw new Error('Invalid backup file: missing workouts array');
      }

      const workoutsCol = collection(db, 'users', userId, 'workouts');

      // Import workouts
      let workoutCount = 0;
      for (const workout of data.workouts) {
        const endTime = workout.endTime || workout.dateTime;
        await addDoc(workoutsCol, {
          dateTime: workout.dateTime,
          endTime,
          workoutType: workout.workoutType,
          notes: workout.notes || '',
          year: workout.year,
        });
        workoutCount++;
      }

      // Import goals
      let goalCount = 0;
      if (data.goals && Array.isArray(data.goals)) {
        for (const goal of data.goals) {
          const goalRef = doc(db, 'users', userId, 'goals', goal.year.toString());
          await setDoc(goalRef, { year: goal.year, goalDays: goal.goalDays });
          goalCount++;
        }
      }

      setResult({
        success: true,
        message: `Imported ${workoutCount} workouts and ${goalCount} goals.`,
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Import failed';
      setResult({ success: false, message });
    } finally {
      setImporting(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  }

  function handleClose() {
    setResult(null);
    onClose();
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Import from Google Drive Backup</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Upload your <strong>workout_tracker_backup.json</strong> file from Google Drive
          (WorkoutTrackerBackup folder) to migrate your existing data.
        </Typography>

        <Box textAlign="center" py={2}>
          <input
            ref={fileInputRef}
            type="file"
            accept=".json"
            onChange={handleFileSelect}
            style={{ display: 'none' }}
            id="backup-file-input"
          />
          <label htmlFor="backup-file-input">
            <Button
              variant="outlined"
              component="span"
              startIcon={importing ? <CircularProgress size={20} /> : <UploadFile />}
              disabled={importing}
              size="large"
            >
              {importing ? 'Importing...' : 'Choose backup file'}
            </Button>
          </label>
        </Box>

        {result && (
          <Alert severity={result.success ? 'success' : 'error'} sx={{ mt: 2 }}>
            {result.message}
          </Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>
          {result?.success ? 'Done' : 'Cancel'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
