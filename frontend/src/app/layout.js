import './globals.css';
<<<<<<< HEAD
import { AuthProvider } from '@/context/AuthContext';
=======
import { AuthProvider } from '../context/AuthContext';
>>>>>>> origin/panxo

export const metadata = {
  title: 'LinguaFlow — Aprende inglés con música',
  description: 'Plataforma de aprendizaje de inglés usando canciones de Spotify e IA',
};

export default function RootLayout({ children }) {
  return (
    <html lang="es">
      <body>
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
