'use client';
import { useState, useEffect, useRef } from 'react';

/**
 * SearchBar — barra de búsqueda con debounce.
 * Espera 500ms después del último keystroke antes de buscar
 * para no saturar el backend con cada letra.
 */
export default function SearchBar({ onSearch, loading }) {
  const [query, setQuery] = useState('');
  const debounceRef = useRef(null);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (query.trim().length >= 2) {
      debounceRef.current = setTimeout(() => {
        onSearch(query.trim());
      }, 500);
    }

    return () => clearTimeout(debounceRef.current);
  }, [query, onSearch]);

  const handleClear = () => {
    setQuery('');
    onSearch('');
  };

  return (
    <div className="relative w-full max-w-2xl">
      {/* Icono búsqueda */}
      <span className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text">
        🔍
      </span>

      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Busca una canción o artista..."
        className="w-full bg-white/10 border border-white/10 rounded-full px-12 py-4 text-white placeholder-brand-text focus:outline-none focus:border-brand-green focus:bg-white/15 transition-all"
      />

      {/* Spinner o botón limpiar */}
      <div className="absolute right-4 top-1/2 -translate-y-1/2">
        {loading ? (
          <div className="w-5 h-5 rounded-full border-2 border-brand-green border-t-transparent animate-spin" />
        ) : query ? (
          <button onClick={handleClear} className="text-brand-text hover:text-white transition-colors text-lg">
            ✕
          </button>
        ) : null}
      </div>
    </div>
  );
}
