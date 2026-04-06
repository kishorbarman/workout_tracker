import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Typography,
  Chip,
  Box,
} from '@mui/material';

interface GoalDialogProps {
  open: boolean;
  year: number;
  currentGoalDays?: number;
  onClose: () => void;
  onSave: (goalDays: number) => void;
}

const SUGGESTIONS = [
  { days: 150, label: '150 days (41%)' },
  { days: 200, label: '200 days (55%)' },
  { days: 250, label: '250 days (68%)' },
];

export default function GoalDialog({ open, year, currentGoalDays, onClose, onSave }: GoalDialogProps) {
  const [goalDays, setGoalDays] = useState(currentGoalDays?.toString() || '200');
  const [error, setError] = useState('');

  function handleSave() {
    const value = parseInt(goalDays, 10);
    if (isNaN(value) || value < 1 || value > 366) {
      setError('Enter a number between 1 and 366');
      return;
    }
    onSave(value);
    onClose();
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Set {year} Goal</DialogTitle>
      <DialogContent>
        <TextField
          label="Goal (days)"
          type="number"
          value={goalDays}
          onChange={(e) => {
            setGoalDays(e.target.value);
            setError('');
          }}
          fullWidth
          margin="normal"
          error={!!error}
          helperText={error || 'Number of workout days this year'}
          inputProps={{ min: 1, max: 366 }}
        />

        <Typography variant="body2" color="text.secondary" sx={{ mt: 2, mb: 1 }}>
          Suggestions
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          {SUGGESTIONS.map((s) => (
            <Chip
              key={s.days}
              label={s.label}
              onClick={() => {
                setGoalDays(s.days.toString());
                setError('');
              }}
              variant={goalDays === s.days.toString() ? 'filled' : 'outlined'}
              color={goalDays === s.days.toString() ? 'primary' : 'default'}
            />
          ))}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={handleSave}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}
