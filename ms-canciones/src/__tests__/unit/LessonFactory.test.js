const { LessonFactory, VocabularyLesson, GrammarLesson, PronunciationLesson } = require('../../patterns/LessonFactory');

const SONG = { id: 'abc123', title: 'Test Song', artist: 'Test Artist' };

describe('LessonFactory — Factory Method Pattern', () => {

  describe('create() según género', () => {
    test('crea VocabularyLesson para género pop', () => {
      const lesson = LessonFactory.create('pop', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
      expect(lesson.type).toBe('vocabulary');
    });

    test('crea VocabularyLesson cuando el género es desconocido', () => {
      const lesson = LessonFactory.create('', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(VocabularyLesson);
    });

    test('crea GrammarLesson para género rock', () => {
      const lesson = LessonFactory.create('rock', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
      expect(lesson.type).toBe('grammar');
    });

    test('crea GrammarLesson para género country', () => {
      const lesson = LessonFactory.create('country', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(GrammarLesson);
    });

    test('crea PronunciationLesson para género hip-hop', () => {
      const lesson = LessonFactory.create('hip-hop', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
      expect(lesson.type).toBe('pronunciation');
    });

    test('crea PronunciationLesson para género reggaeton', () => {
      const lesson = LessonFactory.create('reggaeton', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
    });

    test('crea PronunciationLesson para género trap', () => {
      const lesson = LessonFactory.create('trap', SONG.id, SONG.title, SONG.artist);
      expect(lesson).toBeInstanceOf(PronunciationLesson);
    });
  });

  describe('estructura de cada lección', () => {
    test('VocabularyLesson tiene exercises y focus', () => {
      const lesson = LessonFactory.create('pop', SONG.id, SONG.title, SONG.artist);
      expect(Array.isArray(lesson.exercises)).toBe(true);
      expect(lesson.exercises.length).toBeGreaterThan(0);
      expect(lesson.focus).toBeTruthy();
    });

    test('cada lección incluye songId, title y artist', () => {
      const lesson = LessonFactory.create('pop', SONG.id, SONG.title, SONG.artist);
      expect(lesson.songId).toBe(SONG.id);
      expect(lesson.title).toBe(SONG.title);
      expect(lesson.artist).toBe(SONG.artist);
    });

    test('describe() retorna string con el título de la canción', () => {
      const lesson = LessonFactory.create('pop', SONG.id, SONG.title, SONG.artist);
      expect(typeof lesson.describe()).toBe('string');
      expect(lesson.describe()).toContain(SONG.title);
    });
  });

  describe('createByType()', () => {
    test('crea VocabularyLesson por tipo explícito', () => {
      expect(LessonFactory.createByType('vocabulary', SONG.id, SONG.title, SONG.artist)).toBeInstanceOf(VocabularyLesson);
    });

    test('crea GrammarLesson por tipo explícito', () => {
      expect(LessonFactory.createByType('grammar', SONG.id, SONG.title, SONG.artist)).toBeInstanceOf(GrammarLesson);
    });

    test('crea PronunciationLesson por tipo explícito', () => {
      expect(LessonFactory.createByType('pronunciation', SONG.id, SONG.title, SONG.artist)).toBeInstanceOf(PronunciationLesson);
    });

    test('tipo desconocido retorna VocabularyLesson por defecto', () => {
      expect(LessonFactory.createByType('desconocido', SONG.id, SONG.title, SONG.artist)).toBeInstanceOf(VocabularyLesson);
    });
  });
});
