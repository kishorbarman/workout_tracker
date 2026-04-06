import { useState, useEffect } from 'react';
import { Box, Button, IconButton, Paper, Typography } from '@mui/material';
import { Close, GetApp } from '@mui/icons-material';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export default function InstallPrompt() {
  const [installEvent, setInstallEvent] = useState<BeforeInstallPromptEvent | null>(null);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    const handler = (e: Event) => {
      e.preventDefault();
      setInstallEvent(e as BeforeInstallPromptEvent);
    };

    window.addEventListener('beforeinstallprompt', handler);

    const onInstalled = () => setInstallEvent(null);
    window.addEventListener('appinstalled', onInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handler);
      window.removeEventListener('appinstalled', onInstalled);
    };
  }, []);

  const handleInstall = async () => {
    if (!installEvent) return;
    await installEvent.prompt();
    const { outcome } = await installEvent.userChoice;
    if (outcome === 'accepted') {
      setInstallEvent(null);
    }
  };

  if (!installEvent || dismissed) return null;

  return (
    <Paper
      elevation={4}
      sx={{
        position: 'fixed',
        bottom: 16,
        left: '50%',
        transform: 'translateX(-50%)',
        width: { xs: 'calc(100% - 32px)', sm: 400 },
        px: 2,
        py: 1.5,
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        zIndex: 1400,
        borderRadius: 2,
      }}
    >
      <GetApp color="primary" />
      <Box sx={{ flex: 1 }}>
        <Typography variant="body2" fontWeight={600}>
          Install Workout Tracker
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Add to your home screen for quick access
        </Typography>
      </Box>
      <Button size="small" variant="contained" onClick={handleInstall} sx={{ flexShrink: 0 }}>
        Install
      </Button>
      <IconButton size="small" onClick={() => setDismissed(true)}>
        <Close fontSize="small" />
      </IconButton>
    </Paper>
  );
}
