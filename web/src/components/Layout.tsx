import { ReactNode } from 'react';
import { AppBar, Toolbar, Typography, IconButton, Box, Avatar, Tooltip } from '@mui/material';
import { History, Logout, FitnessCenter, Dashboard } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Layout({ children }: { children: ReactNode }) {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={1} sx={{ bgcolor: 'background.paper', color: 'text.primary' }}>
        <Toolbar>
          <FitnessCenter sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" fontWeight={700} sx={{ flexGrow: 1 }}>
            Workout Tracker
          </Typography>

          {location.pathname === '/history' ? (
            <Tooltip title="Dashboard">
              <IconButton onClick={() => navigate('/')}>
                <Dashboard />
              </IconButton>
            </Tooltip>
          ) : (
            <Tooltip title="History">
              <IconButton onClick={() => navigate('/history')}>
                <History />
              </IconButton>
            </Tooltip>
          )}

          {user?.photoURL && (
            <Avatar src={user.photoURL} sx={{ width: 32, height: 32, ml: 1 }} />
          )}

          <Tooltip title="Sign out">
            <IconButton onClick={signOut} sx={{ ml: 0.5 }}>
              <Logout />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <Box component="main" sx={{ maxWidth: 600, mx: 'auto', px: 2, py: 2 }}>
        {children}
      </Box>
    </Box>
  );
}
