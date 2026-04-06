import {
  collection,
  doc,
  addDoc,
  updateDoc,
  deleteDoc,
  query,
  where,
  onSnapshot,
  setDoc,
} from 'firebase/firestore';
import { db } from '../firebase';
import type { Workout, YearlyGoal } from '../types';

function workoutsCollection(userId: string) {
  return collection(db, 'users', userId, 'workouts');
}

export function subscribeToWorkouts(
  userId: string,
  year: number,
  callback: (workouts: Workout[]) => void,
  onError?: (error: Error) => void
) {
  const q = query(workoutsCollection(userId), where('year', '==', year));
  return onSnapshot(
    q,
    (snapshot) => {
      const workouts: Workout[] = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Workout[];
      callback(workouts);
    },
    (error) => {
      console.error('Firestore workouts subscription error:', error);
      onError?.(error);
    }
  );
}

export async function addWorkout(userId: string, workout: Omit<Workout, 'id'>) {
  await addDoc(workoutsCollection(userId), workout);
}

export async function updateWorkout(userId: string, workoutId: string, data: Partial<Workout>) {
  const docRef = doc(db, 'users', userId, 'workouts', workoutId);
  await updateDoc(docRef, data);
}

export async function deleteWorkout(userId: string, workoutId: string) {
  const docRef = doc(db, 'users', userId, 'workouts', workoutId);
  await deleteDoc(docRef);
}

export function subscribeToGoal(
  userId: string,
  year: number,
  callback: (goal: YearlyGoal | null) => void,
  onError?: (error: Error) => void
) {
  const docRef = doc(db, 'users', userId, 'goals', year.toString());
  return onSnapshot(
    docRef,
    (snapshot) => {
      if (snapshot.exists()) {
        callback(snapshot.data() as YearlyGoal);
      } else {
        callback(null);
      }
    },
    (error) => {
      console.error('Firestore goal subscription error:', error);
      onError?.(error);
    }
  );
}

export async function setGoal(userId: string, year: number, goalDays: number) {
  const docRef = doc(db, 'users', userId, 'goals', year.toString());
  await setDoc(docRef, { year, goalDays });
}
