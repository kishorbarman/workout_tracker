import {
  Box,
  Card,
  CardContent,
  Typography,
  LinearProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  DirectionsRun,
  DirectionsWalk,
  FitnessCenter,
  SelfImprovement,
  DirectionsBike,
  Pool,
  SportsHandball,
} from '@mui/icons-material';
import { format, parseISO, getWeek, subWeeks, startOfWeek, subMonths, getDaysInMonth } from 'date-fns';
import { useAuth } from '../contexts/AuthContext';
import { useWorkouts } from '../hooks/useWorkouts';
import Layout from '../components/Layout';
import { WorkoutType, workoutTypeDisplayNames } from '../types';
import { useState } from 'react';

function getWorkoutIcon(type: string) {
  switch (type) {
    case WorkoutType.RUNNING:
    case WorkoutType.CARDIO:
      return <DirectionsRun />;
    case WorkoutType.WALKING:
      return <DirectionsWalk />;
    case WorkoutType.STRENGTH:
      return <FitnessCenter />;
    case WorkoutType.YOGA:
      return <SelfImprovement />;
    case WorkoutType.CYCLING:
      return <DirectionsBike />;
    case WorkoutType.SWIMMING:
      return <Pool />;
    case WorkoutType.SPORTS:
      return <SportsHandball />;
    default:
      return <FitnessCenter />;
  }
}

export default function HistoryPage() {
  const { user } = useAuth();
  const [selectedYear] = useState(new Date().getFullYear());
  const { workouts } = useWorkouts(user?.uid, selectedYear);

  const sortedWorkouts = [...workouts].sort(
    (a, b) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime()
  );

  // Weekly trends: last 4 weeks
  const now = new Date();
  const weeklyData = [];
  for (let i = 3; i >= 0; i--) {
    const weekStart = startOfWeek(subWeeks(now, i));
    const weekNum = getWeek(weekStart);
    const daysInWeek = new Set<string>();
    workouts.forEach((w) => {
      const d = parseISO(w.dateTime);
      if (getWeek(d) === weekNum && d.getFullYear() === weekStart.getFullYear()) {
        daysInWeek.add(format(d, 'yyyy-MM-dd'));
      }
    });
    weeklyData.push({ weekNum, days: daysInWeek.size });
  }

  // Monthly trends: last 3 months
  const monthlyData = [];
  for (let i = 2; i >= 0; i--) {
    const monthDate = subMonths(now, i);
    const monthName = format(monthDate, 'MMMM');
    const totalDays = getDaysInMonth(monthDate);
    const daysInMonth = new Set<string>();
    workouts.forEach((w) => {
      const d = parseISO(w.dateTime);
      if (d.getMonth() === monthDate.getMonth() && d.getFullYear() === monthDate.getFullYear()) {
        daysInMonth.add(format(d, 'yyyy-MM-dd'));
      }
    });
    monthlyData.push({ monthName, days: daysInMonth.size, totalDays });
  }

  return (
    <Layout>
      {/* Weekly Trends */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Weekly Trends
          </Typography>
          {weeklyData.map((w) => (
            <Box key={w.weekNum} mb={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2">Week {w.weekNum}</Typography>
                <Typography variant="body2" fontWeight={700} color="primary">
                  {w.days} days
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={(w.days / 7) * 100}
                sx={{ height: 6, borderRadius: 3 }}
              />
            </Box>
          ))}
        </CardContent>
      </Card>

      {/* Monthly Trends */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Monthly Trends
          </Typography>
          {monthlyData.map((m) => (
            <Box key={m.monthName} mb={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2">{m.monthName}</Typography>
                <Typography variant="body2" fontWeight={700} color="primary">
                  {m.days} / {m.totalDays} days
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={(m.days / m.totalDays) * 100}
                sx={{ height: 6, borderRadius: 3 }}
              />
            </Box>
          ))}
        </CardContent>
      </Card>

      {/* Workout List */}
      <Typography variant="h6" fontWeight={700} sx={{ mb: 1 }}>
        All Workouts ({workouts.length})
      </Typography>
      {sortedWorkouts.length === 0 ? (
        <Typography color="text.secondary" textAlign="center" py={4}>
          No workouts logged yet
        </Typography>
      ) : (
        <List disablePadding>
          {sortedWorkouts.map((workout) => (
            <Card key={workout.id} sx={{ mb: 1 }}>
              <ListItem>
                <ListItemIcon sx={{ color: 'primary.main' }}>
                  {getWorkoutIcon(workout.workoutType)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Typography fontWeight={600}>
                      {workoutTypeDisplayNames[workout.workoutType as WorkoutType] || workout.workoutType}
                    </Typography>
                  }
                  secondary={
                    <>
                      {format(parseISO(workout.dateTime), 'MMM dd, yyyy')} at{' '}
                      {format(parseISO(workout.dateTime), 'h:mm a')}
                      {workout.notes && (
                        <Typography variant="body2" component="span" display="block" sx={{ mt: 0.5 }}>
                          {workout.notes}
                        </Typography>
                      )}
                    </>
                  }
                />
              </ListItem>
            </Card>
          ))}
        </List>
      )}
    </Layout>
  );
}
