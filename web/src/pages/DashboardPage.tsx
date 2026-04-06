import { useState, useMemo } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Typography,
  IconButton,
  LinearProgress,
  Chip,
  Button,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  Edit,
  LocalFireDepartment,
  EmojiEvents,
  CheckCircle,
  Warning,
} from '@mui/icons-material';
import { parseISO, getDayOfYear, isLeapYear } from 'date-fns';
import { useAuth } from '../contexts/AuthContext';
import { useWorkouts } from '../hooks/useWorkouts';
import { useGoal } from '../hooks/useGoal';
import { addWorkout, updateWorkout, deleteWorkout, setGoal } from '../services/workoutService';
import CalendarView from '../components/CalendarView';
import WorkoutDialog from '../components/WorkoutDialog';
import GoalDialog from '../components/GoalDialog';
import ImportDialog from '../components/ImportDialog';
import Layout from '../components/Layout';
import type { Workout } from '../types';

export default function DashboardPage() {
  const { user } = useAuth();
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState(currentYear);
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [showWorkoutDialog, setShowWorkoutDialog] = useState(false);
  const [selectedDate, setSelectedDate] = useState('');
  const [showGoalDialog, setShowGoalDialog] = useState(false);
  const [showImportDialog, setShowImportDialog] = useState(false);

  const { workouts, workoutDates, workoutDayCount, currentStreak, longestStreak, loading: workoutsLoading, error: workoutsError } = useWorkouts(
    user?.uid,
    selectedYear
  );
  const { goal, error: goalError } = useGoal(user?.uid, selectedYear);

  const selectedDateWorkouts = useMemo(() => {
    if (!selectedDate) return [];
    return workouts.filter((w) => w.dateTime.startsWith(selectedDate));
  }, [workouts, selectedDate]);

  function handleDateClick(dateStr: string) {
    setSelectedDate(dateStr);
    setShowWorkoutDialog(true);
  }

  async function handleSaveWorkout(dateTime: string, endTime: string, workoutType: string, notes: string) {
    if (!user) return;
    const year = parseISO(dateTime).getFullYear();
    await addWorkout(user.uid, { dateTime, endTime, workoutType, notes, year });
  }

  async function handleUpdateWorkout(workout: Workout) {
    if (!user) return;
    const { id, ...data } = workout;
    await updateWorkout(user.uid, id, data);
  }

  async function handleDeleteWorkout(workoutId: string) {
    if (!user) return;
    await deleteWorkout(user.uid, workoutId);
  }

  async function handleSetGoal(goalDays: number) {
    if (!user) return;
    await setGoal(user.uid, selectedYear, goalDays);
  }

  function handleYearChange(year: number) {
    setSelectedYear(year);
    setCurrentMonth(year === currentYear ? new Date().getMonth() + 1 : 1);
  }

  function handleMonthChange(month: number) {
    if (month === 0) {
      setSelectedYear((y) => y - 1);
      setCurrentMonth(12);
    } else if (month === 13) {
      setSelectedYear((y) => y + 1);
      setCurrentMonth(1);
    } else {
      setCurrentMonth(month);
    }
  }

  // Progress calculations
  const progressPercent = goal ? Math.round((workoutDayCount / goal.goalDays) * 100) : 0;
  const remaining = goal ? Math.max(0, goal.goalDays - workoutDayCount) : 0;

  const today = new Date();
  const dayOfYear = getDayOfYear(today);
  const totalDaysInYear = isLeapYear(today) ? 366 : 365;
  const expectedByNow = goal ? (goal.goalDays * dayOfYear) / totalDaysInYear : 0;
  const isOnTrack = workoutDayCount >= expectedByNow;
  const daysRemainingInYear = totalDaysInYear - dayOfYear;
  const workoutsNeeded = goal ? Math.max(0, goal.goalDays - workoutDayCount) : 0;
  const avgPerWeek = daysRemainingInYear > 0 ? (workoutsNeeded / daysRemainingInYear) * 7 : 0;

  const firestoreError = workoutsError || goalError;

  if (workoutsLoading) {
    return (
      <Layout>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress />
        </Box>
      </Layout>
    );
  }

  return (
    <Layout>
      {firestoreError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {firestoreError}
        </Alert>
      )}

      {/* Year Selector */}
      <Box display="flex" alignItems="center" justifyContent="center" my={1}>
        <IconButton onClick={() => handleYearChange(selectedYear - 1)}>
          <ChevronLeft />
        </IconButton>
        <Typography variant="h5" fontWeight={700} sx={{ mx: 3 }}>
          {selectedYear}
        </Typography>
        <IconButton onClick={() => handleYearChange(selectedYear + 1)}>
          <ChevronRight />
        </IconButton>
      </Box>

      {/* Progress Card */}
      <Card sx={{ mb: 2, bgcolor: 'primary.light' }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6" fontWeight={700}>
              {selectedYear} Goal Progress
            </Typography>
            <IconButton onClick={() => setShowGoalDialog(true)} size="small">
              <Edit />
            </IconButton>
          </Box>

          {goal ? (
            <>
              <LinearProgress
                variant="determinate"
                value={Math.min(100, progressPercent)}
                sx={{ my: 2, height: 10, borderRadius: 5 }}
              />
              <Box display="flex" justifyContent="space-between">
                <Box>
                  <Typography variant="subtitle1" fontWeight={700}>
                    {workoutDayCount} / {goal.goalDays} days
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {progressPercent}% complete
                  </Typography>
                </Box>
                <Box textAlign="right">
                  <Typography variant="subtitle1" fontWeight={700}>
                    {remaining} days
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    remaining
                  </Typography>
                </Box>
              </Box>

              {selectedYear === currentYear && (
                <Box display="flex" justifyContent="space-between" alignItems="center" mt={2}>
                  <Chip
                    icon={isOnTrack ? <CheckCircle /> : <Warning />}
                    label={isOnTrack ? 'On Track' : 'Off Track'}
                    color={isOnTrack ? 'success' : 'error'}
                    size="small"
                  />
                  <Typography variant="body2" fontWeight={600}>
                    {workoutsNeeded === 0
                      ? 'Goal achieved!'
                      : daysRemainingInYear <= 0
                        ? 'Year ended'
                        : avgPerWeek > 7
                          ? 'Goal unreachable'
                          : `${avgPerWeek.toFixed(1)} days/week needed`}
                  </Typography>
                </Box>
              )}

              {selectedYear < currentYear && (
                <Box mt={2}>
                  <Chip
                    icon={workoutDayCount >= goal.goalDays ? <CheckCircle /> : <Warning />}
                    label={workoutDayCount >= goal.goalDays ? 'Goal Achieved!' : 'Goal Not Met'}
                    color={workoutDayCount >= goal.goalDays ? 'success' : 'error'}
                    size="small"
                  />
                </Box>
              )}
            </>
          ) : (
            <Box mt={2}>
              <Typography variant="body1" gutterBottom>
                No goal set for {selectedYear}
              </Typography>
              <Button variant="contained" onClick={() => setShowGoalDialog(true)}>
                Set Goal
              </Button>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Calendar */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <CalendarView
            year={selectedYear}
            month={currentMonth}
            workoutDates={workoutDates}
            onDateClick={handleDateClick}
            onMonthChange={handleMonthChange}
          />
        </CardContent>
      </Card>

      {/* Streak Card */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-evenly" textAlign="center">
            <Box>
              <LocalFireDepartment sx={{ fontSize: 32, color: 'primary.main' }} />
              <Typography variant="h4" fontWeight={700}>
                {currentStreak}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Current Streak
              </Typography>
            </Box>
            <Box sx={{ borderLeft: 1, borderColor: 'divider', mx: 2 }} />
            <Box>
              <EmojiEvents sx={{ fontSize: 32, color: 'secondary.main' }} />
              <Typography variant="h4" fontWeight={700}>
                {longestStreak}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Longest Streak
              </Typography>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Workout Dialog */}
      {showWorkoutDialog && <WorkoutDialog
        open={showWorkoutDialog}
        dateStr={selectedDate}
        existingWorkouts={selectedDateWorkouts}
        onClose={() => setShowWorkoutDialog(false)}
        onSave={handleSaveWorkout}
        onUpdate={handleUpdateWorkout}
        onDelete={handleDeleteWorkout}
      />}

      {/* Import Card */}
      <Card sx={{ mb: 2, bgcolor: 'secondary.light' }}>
        <CardContent sx={{ textAlign: 'center' }}>
          <Typography variant="body1" gutterBottom>
            Have existing data from the Android app?
          </Typography>
          <Button variant="outlined" onClick={() => setShowImportDialog(true)}>
            Import from Google Drive Backup
          </Button>
        </CardContent>
      </Card>

      {/* Goal Dialog */}
      <GoalDialog
        open={showGoalDialog}
        year={selectedYear}
        currentGoalDays={goal?.goalDays}
        onClose={() => setShowGoalDialog(false)}
        onSave={handleSetGoal}
      />

      {/* Import Dialog */}
      {showImportDialog && user && (
        <ImportDialog
          open={showImportDialog}
          userId={user.uid}
          onClose={() => setShowImportDialog(false)}
        />
      )}
    </Layout>
  );
}
