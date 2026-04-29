'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const [name, setName]         = useState('');
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm]   = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);
  const { register } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirm) {
      setError('Las contraseñas no coinciden');
      return;
    }
    if (password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    setLoading(true);
    try {
      await register(name, email, password);
      router.push('/search');
    } catch (err) {
      setError(err.message || 'Error al crear la cuenta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-brand-dark flex items-center justify-center px-4">
      <div className="w-full max-w-md animate-fadeIn">

        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-brand-green mb-4">
            <span className="text-3xl">🎵</span>
          </div>
          <h1 className="text-3xl font-bold text-white">LinguaFlow</h1>
          <p className="text-brand-text mt-1">Crea tu cuenta gratuita</p>
        </div>

        <div className="bg-brand-card rounded-2xl p-8 shadow-xl">
          <h2 className="text-xl font-semibold text-white mb-6">Crear cuenta</h2>

          {error && (
            <div className="bg-red-900/40 border border-red-500/50 text-red-300 rounded-lg px-4 py-3 mb-4 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm text-brand-text mb-1">Nombre</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Tu nombre"
                required
                className="w-full bg-brand-hover border border-white/10 rounded-lg px-4 py-3 text-white placeholder-brand-text focus:outline-none focus:border-brand-green transition-colors"
              />
            </div>

            <div>
              <label className="block text-sm text-brand-text mb-1">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="tu@email.com"
                required
                className="w-full bg-brand-hover border border-white/10 rounded-lg px-4 py-3 text-white placeholder-brand-text focus:outline-none focus:border-brand-green transition-colors"
              />
            </div>

            <div>
              <label className="block text-sm text-brand-text mb-1">Contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Mínimo 6 caracteres"
                required
                className="w-full bg-brand-hover border border-white/10 rounded-lg px-4 py-3 text-white placeholder-brand-text focus:outline-none focus:border-brand-green transition-colors"
              />
            </div>

            <div>
              <label className="block text-sm text-brand-text mb-1">Confirmar contraseña</label>
              <input
                type="password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                placeholder="Repite tu contraseña"
                required
                className="w-full bg-brand-hover border border-white/10 rounded-lg px-4 py-3 text-white placeholder-brand-text focus:outline-none focus:border-brand-green transition-colors"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-brand-green text-black font-semibold py-3 rounded-lg hover:bg-green-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed mt-2"
            >
              {loading ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>

          <p className="text-center text-brand-text text-sm mt-6">
            ¿Ya tienes cuenta?{' '}
            <Link href="/login" className="text-brand-green hover:underline">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
