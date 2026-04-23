'use client';
import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';

import { songs as songsApi } from '../../lib/api';
import Navbar from '../../components/ui/Navbar';
import SearchBar from '../../components/ui/SearchBar';
import SongCard from '../../components/ui/SongCard';

export default function SearchPage() {
  const { user } = useAuth();
  const router   = useRouter();

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError]     = useState('');

  const handleSearch = useCallback(async (query) => {
    if (!query) {
      setResults([]);
      setSearched(false);
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await songsApi.search(query);
      setResults(data.results || []);
      setSearched(true);
    } catch (err) {
      setError('Error al buscar canciones. Intenta nuevamente.');
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <div className="min-h-screen bg-brand-dark">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 py-10">

        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-4xl font-bold text-white mb-2">
            ¡Hola, <span className="text-brand-green">{user?.name || 'Estudiante'}</span>! 👋
          </h1>
          <p className="text-brand-text text-lg">
            Busca una canción para empezar tu lección de inglés
          </p>
        </div>

        {/* Search */}
        <div className="flex justify-center mb-8">
          <SearchBar onSearch={handleSearch} loading={loading} />
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-900/40 border border-red-500/50 text-red-300 rounded-lg px-4 py-3 mb-6 text-sm text-center">
            {error}
          </div>
        )}

        {/* Resultados */}
        {results.length > 0 && (
          <div className="animate-fadeIn">
            <p className="text-brand-text text-sm mb-4">
              {results.length} resultado{results.length !== 1 ? 's' : ''} encontrado{results.length !== 1 ? 's' : ''}
            </p>
            <div className="space-y-2">
              {results.map((song) => (
                <SongCard key={song.spotifyId} song={song} />
              ))}
            </div>
          </div>
        )}

        {/* Estado vacío post-búsqueda */}
        {searched && results.length === 0 && !loading && (
          <div className="text-center py-16 animate-fadeIn">
            <span className="text-6xl">🔍</span>
            <p className="text-white font-medium mt-4">No encontramos canciones</p>
            <p className="text-brand-text text-sm mt-1">Intenta con otro término de búsqueda</p>
          </div>
        )}

        {/* Estado inicial */}
        {!searched && !loading && (
          <div className="text-center py-16">
            <div className="grid grid-cols-3 gap-4 max-w-xs mx-auto mb-8 opacity-30">
              {['🎸','🎹','🎺','🥁','🎷','🎻'].map((e, i) => (
                <span key={i} className="text-4xl">{e}</span>
              ))}
            </div>
            <p className="text-brand-text">Escribe al menos 2 caracteres para buscar</p>
          </div>
        )}
      </main>
    </div>
  );
}
