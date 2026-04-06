import { useEffect, useState } from 'react';
import { subscribeToGoal } from '../services/workoutService';
import type { YearlyGoal } from '../types';

export function useGoal(userId: string | undefined, year: number) {
  const [goal, setGoal] = useState<YearlyGoal | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!userId) {
      setGoal(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    const unsubscribe = subscribeToGoal(
      userId,
      year,
      (data) => {
        setGoal(data);
        setLoading(false);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );
    return unsubscribe;
  }, [userId, year]);

  return { goal, loading, error };
}
