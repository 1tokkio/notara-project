/**
 * LyricsDisplayStrategy — Patrón Strategy (GoF)
 *
 * Problema que resuelve:
 *   La visualización de la letra soporta cuatro modos distintos: solo inglés,
 *   solo español, bilingüe y sincronizada. Sin este patrón, el componente
 *   LessonPage contendría bloques if/else profundos por cada modo, acoplando
 *   la lógica de renderizado con la lógica de negocio y dificultando agregar
 *   nuevos modos en el futuro.
 *
 * Solución:
 *   Cada modo de visualización es una estrategia intercambiable que implementa
 *   la misma interfaz: { id, label, render(props) }.
 *   LessonPage actúa como contexto: selecciona la estrategia activa y delega
 *   el renderizado sin conocer los detalles de cada modo.
 *
 * Ventaja de mantenibilidad:
 *   Agregar un nuevo modo (ej: "solo vocabulario resaltado") solo requiere
 *   añadir un nuevo objeto estrategia — sin modificar el componente principal.
 */

// ─── Estilos compartidos ──────────────────────────────────────────────────────

const lineBase    = 'px-3 py-2 rounded-lg cursor-pointer text-sm leading-relaxed transition-all duration-200';
const lineActive  = `${lineBase} bg-brand-green/15 text-brand-green font-semibold`;
const lineInactive = `${lineBase} text-white/75 hover:text-white hover:bg-brand-hover/60`;

// ─── Estrategias ──────────────────────────────────────────────────────────────

/**
 * EnOnlyStrategy — muestra únicamente la letra en inglés.
 * Útil cuando el estudiante quiere practicar comprensión sin ver la traducción.
 */
const EnOnlyStrategy = {
  id: 'en-only',
  label: 'Solo EN',
  render({ lines, activeIdx, onLineClick }) {
    return (
      <div className="space-y-0.5">
        {lines.map((line, i) => (
          <p
            key={i}
            data-line={i}
            onClick={() => onLineClick(line.text, i)}
            className={i === activeIdx ? lineActive : lineInactive}
          >
            {line.text || ' '}
          </p>
        ))}
      </div>
    );
  },
};

/**
 * EsOnlyStrategy — muestra únicamente las traducciones al español.
 * Útil cuando el estudiante ya conoce la letra y quiere revisar el significado completo.
 */
const EsOnlyStrategy = {
  id: 'es-only',
  label: 'Solo ES',
  render({ lines, translations }) {
    return (
      <div className="space-y-1">
        {lines.map((line, i) => (
          <p key={i} data-line={i} className="py-1.5 px-2 text-sm text-brand-text leading-relaxed">
            {translations[i] || <span className="opacity-30">—</span>}
          </p>
        ))}
      </div>
    );
  },
};

/**
 * BilingualStrategy — muestra inglés y español en columnas paralelas.
 * Facilita la comprensión línea a línea y permite comparar expresiones.
 */
const BilingualStrategy = {
  id: 'bilingual',
  label: 'EN / ES',
  render({ lines, translations, activeIdx, onLineClick }) {
    return (
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-white/5">
            <th className="text-left text-brand-text text-[10px] font-semibold uppercase pb-2 pr-4 w-1/2">EN</th>
            <th className="text-left text-brand-text text-[10px] font-semibold uppercase pb-2 w-1/2">ES</th>
          </tr>
        </thead>
        <tbody>
          {lines.map((line, i) => (
            <tr
              key={i}
              data-line={i}
              onClick={() => onLineClick(line.text, i)}
              className={`cursor-pointer transition-colors ${i === activeIdx ? 'bg-brand-green/10' : 'hover:bg-brand-hover/50'}`}
            >
              <td className={`py-2 pr-4 text-sm leading-relaxed rounded-l-lg ${i === activeIdx ? 'text-brand-green font-medium' : 'text-white/80'}`}>
                {line.text || ' '}
              </td>
              <td className="py-2 text-sm leading-relaxed text-brand-text rounded-r-lg">
                {translations[i] || <span className="opacity-30">—</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  },
};

/**
 * SyncedStrategy — igual que EnOnly pero con highlight automático de la línea activa
 * según el tiempo de reproducción del player. Requiere que la letra esté sincronizada (LRC).
 */
const SyncedStrategy = {
  id: 'synced',
  label: 'Letra sincronizada',
  render({ lines, activeIdx, onLineClick }) {
    return EnOnlyStrategy.render({ lines, activeIdx, onLineClick });
  },
};

// ─── Registro de estrategias ──────────────────────────────────────────────────

export const LYRICS_STRATEGIES = [
  EnOnlyStrategy,
  BilingualStrategy,
  EsOnlyStrategy,
  SyncedStrategy,
];

/**
 * Devuelve la estrategia activa por su id.
 * Si no se encuentra, retorna EnOnlyStrategy como estrategia por defecto.
 */
export function getLyricsStrategy(id) {
  return LYRICS_STRATEGIES.find(s => s.id === id) ?? EnOnlyStrategy;
}
