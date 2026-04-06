import { useEffect, useState, useMemo } from 'react';
import { subscribeToWorkouts } from '../services/workoutService';
import type { Workout } from '../types';
import { parseISO, differenceInCalendarDays, format, isToday, isYesterday } from 'date-fns';

export function useWorkouts(userId: string | undefined, year: number) {
  const [workouts, setWorkouts] = useState<Workout[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!userId) {
      setWorkouts([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    const unsubscribe = subscribeToWorkouts(
      userId,
      year,
      (data) => {
        setWorkouts(data);
        setLoading(false);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );
    return unsubscribe;
  }, [userId, year]);

  const workoutDates = useMemo(() => {
    const dates = new Set<string>();
    workouts.forEach((w) => {
      dates.add(format(parseISO(w.dateTime), 'yyyy-MM-dd'));
    });
    return dates;
  }, [workouts]);

  const workoutDayCount = workoutDates.size;

  const { currentStreak, longestStreak } = useMemo(() => {
    if (workoutDates.size === 0) return { currentStreak: 0, longestStreak: 0 };

    const sortedDates = Array.from(workoutDates).sort();
    let longest = 1;
    let temp = 1;

    for (let i = 1; i < sortedDates.length; i++) {
      const diff = differenceInCalendarDays(
        parseISO(sortedDates[i]),
        parseISO(sortedDates[i - 1])
      );
      if (diff === 1) {
        temp++;
      } else {
        longest = Math.max(longest, temp);
        temp = 1;
      }
    }
    longest = Math.max(longest, temp);

    let current = 0;
    const lastDate = parseISO(sortedDates[sortedDates.length - 1]);
    if (isToday(lastDate) || isYesterday(lastDate)) {
      current = 1;
      for (let i = sortedDates.length - 2; i >= 0; i--) {
        const diff = differenceInCalendarDays(
          parseISO(sortedDates[i + 1]),
          parseISO(sortedDates[i])
        );
        if (diff === 1) {
          current++;
        } else {
          break;
        }
      }
    }

    return { currentStreak: current, longestStreak: longest };
  }, [workoutDates]);

  return { workouts, workoutDates, workoutDayCount, currentStreak, longestStreak, loading, error };
}
