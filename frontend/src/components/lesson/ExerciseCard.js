'use client';
import { useState } from 'react';

/**
 * ExerciseCard — ejercicio interactivo de fill-in-the-blank.
 * Muestra la oración con un hueco y valida la respuesta del usuario.
 *
 * @param {object} exercise  — { question, answer, hint }
 * @param {function} onCorrect — callback al responder correctamente
 */
export default function ExerciseCard({ exercise, onCorrect }) {
  const [input, setInput]     = useState('');
  const [status, setStatus]   = useState('idle'); // idle | correct | wrong
  const [attempts, setAttempts] = useState(0);
  const [showHint, setShowHint] = useState(false);

  const handleCheck = () => {
    const normalized   = input.trim().toLowerCase();
    const correctAnswer = exercise.answer.trim().toLowerCase();

    if (normalized === correctAnswer) {
      setStatus('correct');
      onCorrect?.();
    } else {
      setStatus('wrong');
      setAttempts((a) => a + 1);
      setTimeout(() => setStatus('idle'), 1500);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && status === 'idle') handleCheck();
  };

  if (status === 'correct') {
    return (
      <div className="bg-green-900/30 border border-green-500/40 rounded-xl p-4 text-center animate-fadeIn">
        <p className="text-green-400 font-semibold text-lg">¡Correcto! ✓</p>
        <p className="text-green-300/80 text-sm mt-1">
          La respuesta era: <strong>{exercise.answer}</strong>
        </p>
      </div>
    );
  }

  return (
    <div className="bg-brand-hover rounded-xl p-4 space-y-3">
      {/* Pregunta */}
      <p className="text-white text-sm leading-relaxed">
        {exercise.question}
      </p>

      {/* Input */}
      <div className="flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Tu respuesta..."
          className={`flex-1 bg-brand-dark rounded-lg px-3 py-2 text-white text-sm placeholder-brand-text focus:outline-none transition-colors ${
            status === 'wrong'
              ? 'border border-red-500/60'
              : 'border border-white/10 focus:border-brand-green'
          }`}
        />
        <button
          onClick={handleCheck}
          disabled={!input.trim()}
          className="bg-brand-green text-black font-semibold px-4 py-2 rounded-lg text-sm hover:bg-green-400 transition-colors disabled:opacity-40"
        >
          ✓
        </button>
      </div>

      {/* Feedback incorrecto */}
      {status === 'wrong' && (
        <p className="text-red-400 text-xs animate-fadeIn">Inténtalo de nuevo</p>
      )}

      {/* Pista después de 2 intentos */}
      {attempts >= 2 && exercise.hint && (
        <button
          onClick={() => setShowHint(!showHint)}
          className="text-brand-text text-xs hover:text-white transition-colors"
        >
          {showHint ? '▲ Ocultar pista' : '💡 Ver pista'}
        </button>
      )}
      {showHint && exercise.hint && (
        <p className="text-yellow-400/80 text-xs bg-yellow-900/20 rounded-lg px-3 py-2 animate-fadeIn">
          Pista: {exercise.hint}
        </p>
      )}
    </div>
  );
}
