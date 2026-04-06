import { Box, Button, Card, CardContent, Typography, CircularProgress } from '@mui/material';
import { Google as GoogleIcon, FitnessCenter } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { Navigate } from 'react-router-dom';

export default function LoginPage() {
  const { user, loading, signInWithGoogle } = useAuth();

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  if (user) {
    return <Navigate to="/" replace />;
  }

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
      sx={{ background: 'linear-gradient(135deg, #EADDFF 0%, #FFFBFE 100%)' }}
    >
      <Card sx={{ maxWidth: 400, width: '100%', mx: 2, p: 2 }} elevation={4}>
        <CardContent sx={{ textAlign: 'center' }}>
          <FitnessCenter sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Workout Tracker
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Track your workouts, set goals, and build consistency.
          </Typography>
          <Button
            variant="contained"
            size="large"
            startIcon={<GoogleIcon />}
            onClick={signInWithGoogle}
            fullWidth
            sx={{ py: 1.5 }}
          >
            Sign in with Google
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
