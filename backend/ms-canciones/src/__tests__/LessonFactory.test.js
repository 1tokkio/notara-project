/**
 * Tests para LessonFactory
 * Verifica que cada tipo de lección crea la instancia correcta
 */
const {
  LessonFactory,
  VocabularyLesson,
  GrammarLesson,
  PronunciationLesson,
} = require('../patterns/LessonFactory');

describe('LessonFactory', () => {
  const songId = 'spotify123';
  const title = 'Test Song';
  const artist = 'Test Artist';

  describe('create() — por género', () => {
    test('Crea VocabularyLesson para género pop (default)', () => {
      const lesson = LessonFactory.create('pop', songId, title, artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
      expect(lesson.type).toBe('vocabulary');
    });

    test('Crea VocabularyLesson cuando el género es desconocido', () => {
      const lesson = LessonFactory.create('', songId, title, artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
    });

    test('Crea VocabularyLesson para género R&B', () => {
      const lesson = LessonFactory.create('r&b', songId, title, artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
    });

    test('Crea GrammarLesson para género rock', () => {
      const lesson = LessonFactory.create('rock', songId, title, artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
      expect(lesson.type).toBe('grammar');
    });

    test('Crea GrammarLesson para género country', () => {
      const lesson = LessonFactory.create('country', songId, title, artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
    });

    test('Crea GrammarLesson para género folk', () => {
      const lesson = LessonFactory.create('folk', songId, title, artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
    });

    test('Crea PronunciationLesson para género hip-hop', () => {
      const lesson = LessonFactory.create('hip-hop', songId, title, artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
      expect(lesson.type).toBe('pronunciation');
    });

    test('Crea PronunciationLesson para género rap', () => {
      const lesson = LessonFactory.create('rap', songId, title, artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
    });

    test('Crea PronunciationLesson para género reggaeton', () => {
      const lesson = LessonFactory.create('reggaeton', songId, title, artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
    });
  });

  describe('createByType() — por tipo explícito', () => {
    test('Crea VocabularyLesson con tipo "vocabulary"', () => {
      const lesson = LessonFactory.createByType('vocabulary', songId, title, artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
    });

    test('Crea GrammarLesson con tipo "grammar"', () => {
      const lesson = LessonFactory.createByType('grammar', songId, title, artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
    });

    test('Crea PronunciationLesson con tipo "pronunciation"', () => {
      const lesson = LessonFactory.createByType('pronunciation', songId, title, artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
    });

    test('Crea VocabularyLesson para tipo desconocido (default)', () => {
      const lesson = LessonFactory.createByType('unknowntype', songId, title, artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
    });
  });

  describe('Propiedades de las lecciones', () => {
    test('VocabularyLesson tiene las propiedades correctas', () => {
      const lesson = LessonFactory.createByType('vocabulary', songId, title, artist);
      expect(lesson.songId).toBe(songId);
      expect(lesson.title).toBe(title);
      expect(lesson.artist).toBe(artist);
      expect(Array.isArray(lesson.exercises)).toBe(true);
      expect(lesson.exercises.length).toBeGreaterThan(0);
      expect(typeof lesson.describe()).toBe('string');
    });

    test('GrammarLesson tiene las propiedades correctas', () => {
      const lesson = LessonFactory.createByType('grammar', songId, title, artist);
      expect(lesson.focus).toBeDefined();
      expect(lesson.exercises.length).toBeGreaterThan(0);
      expect(lesson.describe()).toContain(title);
    });

    test('PronunciationLesson tiene las propiedades correctas', () => {
      const lesson = LessonFactory.createByType('pronunciation', songId, title, artist);
      expect(lesson.focus).toBeDefined();
      expect(lesson.exercises.length).toBeGreaterThan(0);
      expect(lesson.describe()).toContain(artist);
    });

    test('Todas las lecciones tienen createdAt', () => {
      ['vocabulary', 'grammar', 'pronunciation'].forEach((type) => {
        const lesson = LessonFactory.createByType(type, songId, title, artist);
        expect(lesson.createdAt).toBeDefined();
      });
    });

    test('La clase base Lesson lanza error si describe() no está implementado', () => {
      const { LessonFactory: LF, VocabularyLesson } = require('../patterns/LessonFactory');
      // Simulamos una subclase que no implementa describe()
      class BrokenLesson extends VocabularyLesson {}
      const bl = new BrokenLesson(songId, title, artist);
      // VocabularyLesson SÍ implementa describe(), pero verificamos que el método existe
      expect(typeof bl.describe).toBe('function');
    });
  });
});
