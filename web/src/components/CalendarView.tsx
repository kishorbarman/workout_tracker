import { Box, IconButton, Typography, Grid } from '@mui/material';
import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import {
  format,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  addDays,
  isSameMonth,
  isToday,
} from 'date-fns';

interface CalendarViewProps {
  year: number;
  month: number;
  workoutDates: Set<string>;
  onDateClick: (dateStr: string) => void;
  onMonthChange: (month: number) => void;
}

const DAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export default function CalendarView({
  year,
  month,
  workoutDates,
  onDateClick,
  onMonthChange,
}: CalendarViewProps) {
  const currentDate = new Date(year, month - 1, 1);
  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const calendarStart = startOfWeek(monthStart);
  const calendarEnd = endOfWeek(monthEnd);

  const weeks: Date[][] = [];
  let day = calendarStart;
  while (day <= calendarEnd) {
    const week: Date[] = [];
    for (let i = 0; i < 7; i++) {
      week.push(day);
      day = addDays(day, 1);
    }
    weeks.push(week);
  }

  function handlePrevMonth() {
    onMonthChange(month === 1 ? 12 : month - 1);
  }

  function handleNextMonth() {
    onMonthChange(month === 12 ? 1 : month + 1);
  }

  return (
    <Box sx={{ px: 1 }}>
      <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
        <IconButton onClick={handlePrevMonth} size="small">
          <ChevronLeft />
        </IconButton>
        <Typography variant="subtitle1" fontWeight={600}>
          {format(currentDate, 'MMMM yyyy')}
        </Typography>
        <IconButton onClick={handleNextMonth} size="small">
          <ChevronRight />
        </IconButton>
      </Box>

      <Grid container columns={7} spacing={0}>
        {DAY_LABELS.map((label) => (
          <Grid item xs={1} key={label}>
            <Typography
              variant="caption"
              color="text.secondary"
              textAlign="center"
              display="block"
              fontWeight={600}
            >
              {label}
            </Typography>
          </Grid>
        ))}

        {weeks.map((week, wi) =>
          week.map((d, di) => {
            const dateStr = format(d, 'yyyy-MM-dd');
            const inMonth = isSameMonth(d, currentDate);
            const hasWorkout = workoutDates.has(dateStr);
            const today = isToday(d);

            return (
              <Grid item xs={1} key={`${wi}-${di}`}>
                <Box
                  onClick={() => inMonth && onDateClick(dateStr)}
                  sx={{
                    width: '100%',
                    aspectRatio: '1',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '50%',
                    cursor: inMonth ? 'pointer' : 'default',
                    bgcolor: hasWorkout && inMonth ? 'primary.main' : 'transparent',
                    color: hasWorkout && inMonth
                      ? 'primary.contrastText'
                      : inMonth
                        ? 'text.primary'
                        : 'text.disabled',
                    border: today ? '2px solid' : 'none',
                    borderColor: today ? 'primary.main' : 'transparent',
                    fontWeight: today || hasWorkout ? 700 : 400,
                    fontSize: '0.85rem',
                    transition: 'background-color 0.15s',
                    '&:hover': inMonth
                      ? {
                          bgcolor: hasWorkout ? 'primary.dark' : 'action.hover',
                        }
                      : {},
                  }}
                >
                  {format(d, 'd')}
                </Box>
              </Grid>
            );
          })
        )}
      </Grid>
    </Box>
  );
}
