export enum WorkoutType {
  CARDIO = 'CARDIO',
  STRENGTH = 'STRENGTH',
  YOGA = 'YOGA',
  SPORTS = 'SPORTS',
  WALKING = 'WALKING',
  RUNNING = 'RUNNING',
  CYCLING = 'CYCLING',
  SWIMMING = 'SWIMMING',
  OTHER = 'OTHER',
}

export const workoutTypeDisplayNames: Record<WorkoutType, string> = {
  [WorkoutType.CARDIO]: 'Cardio',
  [WorkoutType.STRENGTH]: 'Strength',
  [WorkoutType.YOGA]: 'Yoga',
  [WorkoutType.SPORTS]: 'Sports',
  [WorkoutType.WALKING]: 'Walking',
  [WorkoutType.RUNNING]: 'Running',
  [WorkoutType.CYCLING]: 'Cycling',
  [WorkoutType.SWIMMING]: 'Swimming',
  [WorkoutType.OTHER]: 'Other',
};

export interface Workout {
  id: string;
  dateTime: string; // ISO 8601
  endTime: string; // ISO 8601
  workoutType: string;
  notes: string;
  year: number;
}

export interface YearlyGoal {
  year: number;
  goalDays: number;
}
