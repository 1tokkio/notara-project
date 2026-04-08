'use client';
import { useRouter } from 'next/navigation';

/**
 * SongCard — tarjeta de canción con imagen, título, artista y duración.
 */
export default function SongCard({ song }) {
  const router = useRouter();

  // Convertir duración de ms a mm:ss
  const formatDuration = (ms) => {
    if (!ms) return '--:--';
    const min = Math.floor(ms / 60000);
    const sec = Math.floor((ms % 60000) / 1000).toString().padStart(2, '0');
    return `${min}:${sec}`;
  };

  const handleClick = () => {
    router.push(`/lesson/${song.spotifyId}`);
  };

  return (
    <div
      onClick={handleClick}
      className="group flex items-center gap-4 bg-brand-card hover:bg-brand-hover rounded-xl p-4 cursor-pointer transition-all duration-200 hover:scale-[1.01]"
    >
      {/* Imagen del álbum */}
      <div className="relative flex-shrink-0 w-16 h-16">
        {song.imageUrl ? (
          <img
            src={song.imageUrl}
            alt={song.album || song.title}
            className="w-16 h-16 rounded-lg object-cover"
          />
        ) : (
          <div className="w-16 h-16 rounded-lg bg-brand-hover flex items-center justify-center">
            <span className="text-2xl">🎵</span>
          </div>
        )}
        {/* Play overlay */}
        <div className="absolute inset-0 bg-black/40 rounded-lg opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
          <span className="text-white text-xl">▶</span>
        </div>
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0">
        <p className="text-white font-medium truncate">{song.title}</p>
        <p className="text-brand-text text-sm truncate">{song.artist}</p>
        {song.album && (
          <p className="text-brand-text text-xs truncate mt-0.5 opacity-70">{song.album}</p>
        )}
      </div>

      {/* Duración */}
      <span className="text-brand-text text-sm flex-shrink-0">
        {formatDuration(song.duration)}
      </span>
    </div>
  );
}
