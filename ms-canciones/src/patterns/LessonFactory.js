/**
 * LessonFactory — Factory Method Pattern
 *
 * Crea instancias de diferentes tipos de lección según las características
 * de la canción (vocabulario, gramática, pronunciación).
 * Centraliza la lógica de creación y permite extender sin modificar el código cliente.
 */

// ─── Clases base y concretas ──────────────────────────────────────────────────

class Lesson {
  constructor(songId, title, artist) {
    this.songId = songId;
    this.title = title;
    this.artist = artist;
    this.createdAt = new Date().toISOString();
  }

  describe() {
    throw new Error('describe() debe ser implementado por la subclase');
  }
}

/**
 * VocabularyLesson
 * Enfocada en palabras nuevas, definiciones y uso en contexto.
 * Se activa para canciones con vocabulario variado (géneros: pop, indie, soul).
 */
class VocabularyLesson extends Lesson {
  constructor(songId, title, artist) {
    super(songId, title, artist);
    this.type = 'vocabulary';
    this.focus = 'Identificar palabras nuevas y aprender su significado en contexto';
    this.exercises = [
      'Subrayar palabras desconocidas en la letra',
      'Crear oraciones propias con el vocabulario identificado',
      'Completar el espacio en blanco con la palabra correcta',
    ];
  }

  describe() {
    return `Lección de Vocabulario: Aprende nuevas palabras a través de la letra de "${this.title}" por ${this.artist}.`;
  }
}

/**
 * GrammarLesson
 * Enfocada en estructuras gramaticales, tiempos verbales y sintaxis.
 * Se activa para canciones con estructuras narrativas complejas (géneros: rock, country, folk).
 */
class GrammarLesson extends Lesson {
  constructor(songId, title, artist) {
    super(songId, title, artist);
    this.type = 'grammar';
    this.focus = 'Identificar y practicar estructuras gramaticales en contexto real';
    this.exercises = [
      'Identificar el tiempo verbal predominante en la letra',
      'Transformar oraciones de presente a pasado',
      'Identificar oraciones condicionales (if/would)',
    ];
  }

  describe() {
    return `Lección de Gramática: Estudia estructuras del inglés con la letra de "${this.title}" por ${this.artist}.`;
  }
}

/**
 * PronunciationLesson
 * Enfocada en fonética, ritmo y entonación usando el ritmo de la música.
 * Se activa para canciones con ritmo marcado o rap (géneros: hip-hop, reggaeton, EDM).
 */
class PronunciationLesson extends Lesson {
  constructor(songId, title, artist) {
    super(songId, title, artist);
    this.type = 'pronunciation';
    this.focus = 'Mejorar pronunciación y ritmo usando la música como guía';
    this.exercises = [
      'Escuchar y repetir líneas del coro',
      'Identificar palabras con sonidos difíciles (th, r, w)',
      'Practicar el ritmo cantando junto a la canción',
    ];
  }

  describe() {
    return `Lección de Pronunciación: Mejora tu acento con el ritmo de "${this.title}" por ${this.artist}.`;
  }
}

// ─── Factory ──────────────────────────────────────────────────────────────────

class LessonFactory {
  /**
   * Determina el tipo de lección apropiado según el género y artista.
   * @param {string} genre - Género musical de Spotify (puede venir vacío)
   * @param {string} songId
   * @param {string} title
   * @param {string} artist
   * @returns {VocabularyLesson|GrammarLesson|PronunciationLesson}
   */
  static create(genre = '', songId, title, artist) {
    const g = genre.toLowerCase();

    const pronunciationGenres = ['hip-hop', 'rap', 'reggaeton', 'edm', 'dance', 'trap'];
    const grammarGenres = ['rock', 'country', 'folk', 'alternative', 'metal'];

    if (pronunciationGenres.some((pg) => g.includes(pg))) {
      return new PronunciationLesson(songId, title, artist);
    }

    if (grammarGenres.some((gg) => g.includes(gg))) {
      return new GrammarLesson(songId, title, artist);
    }

    // Por defecto: VocabularyLesson (pop, soul, indie, R&B, desconocido)
    return new VocabularyLesson(songId, title, artist);
  }

  /**
   * Crea una lección directamente por tipo (para uso explícito).
   * @param {'vocabulary'|'grammar'|'pronunciation'} type
   * @param {string} songId
   * @param {string} title
   * @param {string} artist
   */
  static createByType(type, songId, title, artist) {
    switch (type) {
      case 'vocabulary':
        return new VocabularyLesson(songId, title, artist);
      case 'grammar':
        return new GrammarLesson(songId, title, artist);
      case 'pronunciation':
        return new PronunciationLesson(songId, title, artist);
      default:
        return new VocabularyLesson(songId, title, artist);
    }
  }
}

module.exports = { LessonFactory, VocabularyLesson, GrammarLesson, PronunciationLesson };
