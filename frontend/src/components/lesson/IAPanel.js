'use client';
import { useState, useEffect } from 'react';
import { ia, progress as progressApi } from '@/lib/api';
import ExerciseCard from './ExerciseCard';

/**
 * IAPanel — panel lateral que muestra la explicación y ejercicios del LLM
 * cuando el usuario selecciona una frase de la letra.
 *
 * @param {string}   songId   — ID Spotify de la canción
 * @param {string}   phrase   — frase seleccionada por el usuario
 * @param {function} onClose  — callback para cerrar el panel
 */
export default function IAPanel({ songId, phrase, onClose }) {
  const [explanation, setExplanation] = useState(null);
  const [exercises, setExercises]     = useState([]);
  const [loadingExp, setLoadingExp]   = useState(true);
  const [loadingEx, setLoadingEx]     = useState(false);
  const [error, setError]             = useState('');
  const [tab, setTab]                 = useState('explain'); // 'explain' | 'exercises' | 'chat'
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput]     = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  // Cargar explicación al montar o cambiar frase
  useEffect(() => {
    if (!phrase) return;
    setExplanation(null);
    setExercises([]);
    setError('');
    setLoadingExp(true);
    setTab('explain');

    ia.explain(songId, phrase)
      .then((data) => setExplanation(data))
      .catch(() => setError('No se pudo obtener la explicación. Intenta más tarde.'))
      .finally(() => setLoadingExp(false));
  }, [songId, phrase]);

  // Cargar ejercicios cuando el usuario cambia a ese tab
  const handleExercisesTab = async () => {
    setTab('exercises');
    if (exercises.length > 0) return;

    setLoadingEx(true);
    try {
      const data = await ia.getExercises(songId, phrase);
      setExercises(data.exercises || []);
    } catch {
      setError('No se pudieron cargar los ejercicios.');
    } finally {
      setLoadingEx(false);
    }
  };

  const handleWordLearned = async (word) => {
    try {
      await progressApi.saveWord(word, songId, phrase);
    } catch {
      // silencioso
    }
  };

  const handleChatSend = async () => {
    if (!chatInput.trim() || chatLoading) return;
    const message = chatInput.trim();
    setChatInput('');
    setChatMessages((prev) => [...prev, { role: 'user', content: message }]);
    setChatLoading(true);

    try {
      const data = await ia.chat(songId, message, chatMessages);
      setChatMessages((prev) => [...prev, { role: 'assistant', content: data.reply }]);
    } catch {
      setChatMessages((prev) => [...prev, { role: 'assistant', content: '⚠️ Error al responder. Intenta de nuevo.' }]);
    } finally {
      setChatLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full bg-brand-card border-l border-white/10">

      {/* Header */}
      <div className="flex items-start justify-between p-4 border-b border-white/10">
        <div className="flex-1 min-w-0">
          <p className="text-brand-text text-xs mb-1">Frase seleccionada</p>
          <p className="text-brand-green font-medium text-sm italic truncate">"{phrase}"</p>
        </div>
        <button onClick={onClose} className="text-brand-text hover:text-white ml-3 text-xl flex-shrink-0">✕</button>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-white/10">
        {[
          { id: 'explain',   label: '📖 Explicar', action: () => setTab('explain') },
          { id: 'exercises', label: '✏️ Ejercicios', action: handleExercisesTab },
          { id: 'chat',      label: '💬 Chat', action: () => setTab('chat') },
        ].map((t) => (
          <button
            key={t.id}
            onClick={t.action}
            className={`flex-1 py-3 text-xs font-medium transition-colors ${
              tab === t.id
                ? 'text-brand-green border-b-2 border-brand-green'
                : 'text-brand-text hover:text-white'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Contenido */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">

        {/* Error global */}
        {error && (
          <div className="bg-red-900/30 border border-red-500/40 rounded-lg px-3 py-2 text-red-300 text-sm">
            {error}
          </div>
        )}

        {/* Tab: Explicación */}
        {tab === 'explain' && (
          <>
            {loadingExp ? (
              <div className="space-y-3 animate-pulse">
                <div className="h-4 bg-brand-hover rounded w-3/4" />
                <div className="h-4 bg-brand-hover rounded w-full" />
                <div className="h-4 bg-brand-hover rounded w-5/6" />
                <div className="h-4 bg-brand-hover rounded w-2/3" />
              </div>
            ) : explanation ? (
              <div className="animate-fadeIn space-y-4">
                {/* Traducción */}
                {explanation.translation && (
                  <div>
                    <p className="text-brand-text text-xs font-medium uppercase mb-1">Traducción</p>
                    <p className="text-white text-sm">{explanation.translation}</p>
                  </div>
                )}
                {/* Explicación */}
                {explanation.explanation && (
                  <div>
                    <p className="text-brand-text text-xs font-medium uppercase mb-1">Explicación</p>
                    <p className="text-white text-sm leading-relaxed">{explanation.explanation}</p>
                  </div>
                )}
                {/* Gramática */}
                {explanation.grammar && (
                  <div>
                    <p className="text-brand-text text-xs font-medium uppercase mb-1">Nota gramatical</p>
                    <p className="text-white text-sm leading-relaxed">{explanation.grammar}</p>
                  </div>
                )}
                {/* Vocabulario clave */}
                {explanation.keywords?.length > 0 && (
                  <div>
                    <p className="text-brand-text text-xs font-medium uppercase mb-2">Vocabulario clave</p>
                    <div className="flex flex-wrap gap-2">
                      {explanation.keywords.map((kw, i) => (
                        <button
                          key={i}
                          onClick={() => handleWordLearned(kw.word)}
                          className="bg-brand-green/20 text-brand-green text-xs rounded-full px-3 py-1 hover:bg-brand-green/30 transition-colors"
                          title={kw.meaning}
                        >
                          {kw.word}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ) : null}
          </>
        )}

        {/* Tab: Ejercicios */}
        {tab === 'exercises' && (
          <>
            {loadingEx ? (
              <div className="text-center py-8">
                <div className="w-8 h-8 rounded-full border-2 border-brand-green border-t-transparent animate-spin mx-auto" />
                <p className="text-brand-text text-sm mt-3">Generando ejercicios...</p>
              </div>
            ) : exercises.length > 0 ? (
              <div className="space-y-4 animate-fadeIn">
                {exercises.map((ex, i) => (
                  <div key={i}>
                    <p className="text-brand-text text-xs mb-2">Ejercicio {i + 1}</p>
                    <ExerciseCard
                      exercise={ex}
                      onCorrect={() => handleWordLearned(ex.targetWord)}
                    />
                  </div>
                ))}
              </div>
            ) : (
              !error && <p className="text-brand-text text-sm text-center py-8">No hay ejercicios disponibles.</p>
            )}
          </>
        )}

        {/* Tab: Chat */}
        {tab === 'chat' && (
          <div className="flex flex-col h-full">
            {chatMessages.length === 0 && (
              <p className="text-brand-text text-sm text-center py-4">
                Hazme preguntas sobre esta frase o sobre la canción.
              </p>
            )}
            <div className="space-y-3 flex-1">
              {chatMessages.map((msg, i) => (
                <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[85%] rounded-xl px-3 py-2 text-sm ${
                    msg.role === 'user'
                      ? 'bg-brand-green text-black'
                      : 'bg-brand-hover text-white'
                  }`}>
                    {msg.content}
                  </div>
                </div>
              ))}
              {chatLoading && (
                <div className="flex justify-start">
                  <div className="bg-brand-hover rounded-xl px-3 py-2">
                    <div className="flex gap-1">
                      {[0,1,2].map(i => (
                        <div key={i} className="w-2 h-2 bg-brand-text rounded-full animate-bounce" style={{ animationDelay: `${i*0.15}s` }} />
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Input de chat */}
      {tab === 'chat' && (
        <div className="p-4 border-t border-white/10 flex gap-2">
          <input
            type="text"
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleChatSend()}
            placeholder="Pregunta algo..."
            className="flex-1 bg-brand-hover border border-white/10 rounded-lg px-3 py-2 text-white text-sm placeholder-brand-text focus:outline-none focus:border-brand-green"
          />
          <button
            onClick={handleChatSend}
            disabled={!chatInput.trim() || chatLoading}
            className="bg-brand-green text-black font-bold px-4 py-2 rounded-lg text-sm hover:bg-green-400 disabled:opacity-40 transition-colors"
          >
            ➤
          </button>
        </div>
      )}
    </div>
  );
}
