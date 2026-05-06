'use client';
import { useState, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { songs as songsApi } from '../../lib/api';
import Navbar from '../../components/ui/Navbar';
import SearchBar from '../../components/ui/SearchBar';
import SongCard from '../../components/ui/SongCard';

const styles = {
  page:        'min-h-screen bg-brand-dark',
  main:        'max-w-4xl mx-auto px-4 py-10',
  header:      'text-center mb-10',
  heading:     'text-4xl font-bold text-white mb-2',
  headingName: 'text-brand-green',
  subheading:  'text-brand-text text-lg',
  searchRow:   'flex justify-center mb-8',
  errorBox:    'bg-red-900/40 border border-red-500/50 text-red-300 rounded-lg px-4 py-3 mb-6 text-sm text-center',
  resultsCount:'text-brand-text text-sm mb-4',
  resultsList: 'space-y-2',
  emptyState:  'text-center py-16 animate-fadeIn',
  emptyTitle:  'text-white font-medium mt-4',
  emptySub:    'text-brand-text text-sm mt-1',
  initialState:'text-center py-16',
  initialDeco: 'grid grid-cols-3 gap-4 max-w-xs mx-auto mb-8 opacity-20',
  initialHint: 'text-brand-text',
};

const DECO_BARS = [1, 2, 3, 4, 5, 6];

export default function SearchPage() {
  const { user } = useAuth();

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
    } catch {
      setError('Error al buscar canciones. Intenta nuevamente.');
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <div className={styles.page}>
      <Navbar />

      <main className={styles.main}>

        <div className={styles.header}>
          <h1 className={styles.heading}>
            Hola, <span className={styles.headingName}>{user?.name || 'Estudiante'}</span>
          </h1>
          <p className={styles.subheading}>Busca una canción para empezar tu lección</p>
        </div>

        <div className={styles.searchRow}>
          <SearchBar onSearch={handleSearch} loading={loading} />
        </div>

        {error && <div className={styles.errorBox}>{error}</div>}

        {results.length > 0 && (
          <div className="animate-fadeIn">
            <p className={styles.resultsCount}>
              {results.length} resultado{results.length !== 1 ? 's' : ''} encontrado{results.length !== 1 ? 's' : ''}
            </p>
            <div className={styles.resultsList}>
              {results.map((song) => (
                <SongCard key={song.spotifyId} song={song} />
              ))}
            </div>
          </div>
        )}

        {searched && results.length === 0 && !loading && (
          <div className={styles.emptyState}>
            <p className={styles.emptyTitle}>No encontramos canciones</p>
            <p className={styles.emptySub}>Intenta con otro término de búsqueda</p>
          </div>
        )}

        {!searched && !loading && (
          <div className={styles.initialState}>
            <div className={styles.initialDeco}>
              {DECO_BARS.map((_, i) => (
                <div key={i} className="h-8 bg-brand-text/30 rounded" />
              ))}
            </div>
            <p className={styles.initialHint}>Escribe al menos 2 caracteres para buscar</p>
          </div>
        )}
      </main>
    </div>
  );
}
