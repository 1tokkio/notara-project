'use client';
import { useState, useEffect } from 'react';
import { ia } from '../../lib/api';

const styles = {
  wrap:        'flex-1 flex flex-col overflow-hidden animate-fadeIn',
  header:      'flex-shrink-0 flex items-center justify-between px-4 py-3 border-b border-white/5',
  title:       'text-white font-semibold text-sm',
  closeBtn:    'text-brand-text hover:text-white text-sm transition-colors px-2 py-1 rounded-lg hover:bg-brand-hover',
  body:        'flex-1 overflow-y-auto px-6 py-6',
  loading:     'flex items-center justify-center h-full',
  spinner:     'w-6 h-6 rounded-full border-2 border-brand-green border-t-transparent animate-spin',
  question:    'text-white text-base font-medium mb-6 leading-relaxed',
  optionBtn:   'w-full text-left px-4 py-3 rounded-xl border text-sm transition-all duration-150 mb-2',
  optionIdle:  'border-white/10 bg-brand-card text-white hover:border-brand-green/40 hover:bg-brand-green/5',
  optionRight: 'border-brand-green bg-brand-green/15 text-brand-green font-semibold',
  optionWrong: 'border-red-500/50 bg-red-500/10 text-red-400',
  input:       'w-full bg-brand-hover border border-white/10 rounded-xl px-4 py-3 text-white text-sm placeholder-brand-text focus:outline-none focus:border-brand-green transition-colors mb-3',
  submitBtn:   'px-5 py-2 bg-brand-green text-black text-sm font-semibold rounded-lg hover:bg-green-400 transition-colors disabled:opacity-40',
  feedback:    'mt-4 p-3 rounded-xl text-sm',
  feedbackOk:  'bg-brand-green/10 text-brand-green border border-brand-green/20',
  feedbackErr: 'bg-red-500/10 text-red-400 border border-red-500/20',
  nav:         'flex-shrink-0 flex items-center justify-between px-4 py-3 border-t border-white/5',
  navDots:     'flex gap-1.5',
  dot:         'w-2 h-2 rounded-full transition-colors',
  nextBtn:     'px-4 py-1.5 text-xs font-semibold rounded-lg transition-colors',
  nextActive:  'bg-brand-green text-black hover:bg-green-400',
  nextDisabled:'bg-brand-hover text-brand-text cursor-not-allowed',
};

function MultipleChoice({ question, options, correct, explanation, onResult }) {
  const [selected, setSelected] = useState(null);

  const pick = (i) => {
    if (selected !== null) return;
    setSelected(i);
    onResult(i === correct);
  };

  return (
    <div>
      <p className={styles.question}>{question}</p>
      {options.map((opt, i) => {
        let cls = `${styles.optionBtn} `;
        if (selected === null) cls += styles.optionIdle;
        else if (i === correct) cls += styles.optionRight;
        else if (i === selected) cls += styles.optionWrong;
        else cls += styles.optionIdle;
        return (
          <button key={i} className={cls} onClick={() => pick(i)}>{opt}</button>
        );
      })}
      {selected !== null && explanation && (
        <div className={`${styles.feedback} ${selected === correct ? styles.feedbackOk : styles.feedbackErr}`}>
          {explanation}
        </div>
      )}
    </div>
  );
}

function FillBlank({ question, answer, hint, onResult }) {
  const [value, setValue]     = useState('');
  const [checked, setChecked] = useState(false);
  const [correct, setCorrect] = useState(false);

  const check = () => {
    if (!value.trim()) return;
    const ok = value.trim().toLowerCase() === answer.toLowerCase();
    setCorrect(ok);
    setChecked(true);
    onResult(ok);
  };

  return (
    <div>
      <p className={styles.question}>{question}</p>
      {hint && <p className="text-brand-text text-xs mb-4">Pista: {hint}</p>}
      <input
        className={styles.input}
        placeholder="Escribe tu respuesta..."
        value={value}
        onChange={e => setValue(e.target.value)}
        onKeyDown={e => e.key === 'Enter' && !checked && check()}
        disabled={checked}
      />
      {!checked && (
        <button className={styles.submitBtn} onClick={check} disabled={!value.trim()}>
          Verificar
        </button>
      )}
      {checked && (
        <div className={`${styles.feedback} ${correct ? styles.feedbackOk : styles.feedbackErr}`}>
          {correct ? 'Correcto!' : `La respuesta era: "${answer}"`}
        </div>
      )}
    </div>
  );
}

function Translation({ question, answer, onResult }) {
  const [value, setValue]     = useState('');
  const [checked, setChecked] = useState(false);
  const [correct, setCorrect] = useState(false);

  const check = () => {
    if (!value.trim()) return;
    const ok = value.trim().toLowerCase().includes(answer.toLowerCase().slice(0, Math.floor(answer.length * 0.7)));
    setCorrect(ok);
    setChecked(true);
    onResult(ok);
  };

  return (
    <div>
      <p className={styles.question}>{question}</p>
      <input
        className={styles.input}
        placeholder="Escribe la traducción..."
        value={value}
        onChange={e => setValue(e.target.value)}
        onKeyDown={e => e.key === 'Enter' && !checked && check()}
        disabled={checked}
      />
      {!checked && (
        <button className={styles.submitBtn} onClick={check} disabled={!value.trim()}>
          Verificar
        </button>
      )}
      {checked && (
        <div className={`${styles.feedback} ${correct ? styles.feedbackOk : styles.feedbackErr}`}>
          {correct ? 'Muy bien!' : `Respuesta esperada: "${answer}"`}
        </div>
      )}
    </div>
  );
}

export default function ExercisePanel({ songId, phrase, exerciseType, onClose, onXP }) {
  const [exercises, setExercises] = useState([]);
  const [current, setCurrent]     = useState(0);
  const [loading, setLoading]     = useState(true);
  const [results, setResults]     = useState([]);
  const [canNext, setCanNext]     = useState(false);
  const [done, setDone]           = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const p = phrase || 'the song';
        const data = await ia.exercises(songId, p);
        setExercises(data.exercises || []);
      } catch {
        setExercises([
          {
            type: 'multiple_choice',
            question: `¿Qué significa "${phrase}"?`,
            options: ['Una expresión de calma', 'Ir despacio', 'Tomar algo lento', 'Relajarse'],
            correct: 1,
            explanation: '"Take it slow" significa ir despacio o con calma.',
          },
        ]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [songId, phrase]);

  const handleResult = (ok) => {
    setResults(prev => [...prev, ok]);
    setCanNext(true);
    if (ok) onXP?.(5);
  };

  const next = () => {
    if (current + 1 >= exercises.length) {
      setDone(true);
    } else {
      setCurrent(c => c + 1);
      setCanNext(false);
    }
  };

  const score = results.filter(Boolean).length;

  if (loading) {
    return (
      <div className={styles.wrap}>
        <div className={styles.header}>
          <span className={styles.title}>{exerciseType}</span>
          <button className={styles.closeBtn} onClick={onClose}>Volver a la letra</button>
        </div>
        <div className={styles.loading}>
          <div className={styles.spinner} />
        </div>
      </div>
    );
  }

  if (done) {
    return (
      <div className={styles.wrap}>
        <div className={styles.header}>
          <span className={styles.title}>Ejercicio completado</span>
          <button className={styles.closeBtn} onClick={onClose}>Volver a la letra</button>
        </div>
        <div className="flex-1 flex flex-col items-center justify-center text-center px-6">
          <div className="w-16 h-16 rounded-full bg-brand-green/20 flex items-center justify-center mb-4">
            <span className="text-brand-green font-bold text-2xl">{score}/{exercises.length}</span>
          </div>
          <p className="text-white font-semibold text-lg mb-2">
            {score === exercises.length ? '¡Perfecto!' : score >= exercises.length / 2 ? '¡Buen trabajo!' : 'Sigue practicando'}
          </p>
          <p className="text-brand-text text-sm mb-6">
            Respondiste {score} de {exercises.length} correctamente · +{score * 5} XP
          </p>
          <button
            className="px-6 py-2.5 bg-brand-green text-black font-semibold rounded-lg hover:bg-green-400 transition-colors"
            onClick={onClose}
          >
            Volver a la letra
          </button>
        </div>
      </div>
    );
  }

  const ex = exercises[current];

  return (
    <div className={styles.wrap}>
      <div className={styles.header}>
        <span className={styles.title}>{exerciseType}</span>
        <button className={styles.closeBtn} onClick={onClose}>← Volver a la letra</button>
      </div>

      <div className={styles.body}>
        <p className="text-brand-text text-xs mb-4">
          Pregunta {current + 1} de {exercises.length}
          {phrase && <span className="ml-2 text-brand-purple">· "{phrase}"</span>}
        </p>

        {ex?.type === 'multiple_choice' && (
          <MultipleChoice key={current} {...ex} onResult={handleResult} />
        )}
        {ex?.type === 'fill_blank' && (
          <FillBlank key={current} {...ex} onResult={handleResult} />
        )}
        {ex?.type === 'translation' && (
          <Translation key={current} {...ex} onResult={handleResult} />
        )}
      </div>

      <div className={styles.nav}>
        <div className={styles.navDots}>
          {exercises.map((_, i) => (
            <span
              key={i}
              className={`${styles.dot} ${
                i < results.length
                  ? results[i] ? 'bg-brand-green' : 'bg-red-500'
                  : i === current ? 'bg-white' : 'bg-brand-hover'
              }`}
            />
          ))}
        </div>
        <button
          className={`${styles.nextBtn} ${canNext ? styles.nextActive : styles.nextDisabled}`}
          onClick={next}
          disabled={!canNext}
        >
          {current + 1 >= exercises.length ? 'Finalizar' : 'Siguiente →'}
        </button>
      </div>
    </div>
  );
}
