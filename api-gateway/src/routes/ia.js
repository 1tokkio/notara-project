const Anthropic = require('@anthropic-ai/sdk');

const LEVELS = {
  beginner:     'principiante',
  intermediate: 'intermedio',
  advanced:     'avanzado',
};

async function fetchLyrics(msCanciones, songId) {
  try {
    const res = await fetch(`${msCanciones}/${songId}/lyrics`);
    if (!res.ok) return null;
    const data = await res.json();
    return data.lyrics || null;
  } catch {
    return null;
  }
}

function extractJSON(text) {
  const match = text.match(/\{[\s\S]*\}/);
  return JSON.parse(match ? match[0] : text);
}

module.exports = function registerIaRoutes(app, msCanciones) {
  const claude = process.env.ANTHROPIC_API_KEY
    ? new Anthropic({ apiKey: process.env.ANTHROPIC_API_KEY })
    : null;

  async function ask(system, userMsg, maxTokens = 800) {
    if (!claude) throw new Error('ANTHROPIC_API_KEY no configurada');
    const response = await claude.messages.create({
      model: 'claude-haiku-4-5-20251001',
      max_tokens: maxTokens,
      system,
      messages: [{ role: 'user', content: userMsg }],
    });
    return response.content[0].text;
  }

  app.post('/ia/explain', async (req, res) => {
    const { songId, phrase, userLevel = 'intermediate' } = req.body || {};
    if (!phrase) return res.status(400).json({ error: 'phrase es requerido' });

    try {
      const lyrics  = songId ? await fetchLyrics(msCanciones, songId) : null;
      const level   = LEVELS[userLevel] || 'intermedio';
      const system  = `Eres un tutor de inglés experto para estudiantes hispanohablantes de nivel ${level}. Responde únicamente con el JSON solicitado, sin texto adicional.`;
      const userMsg = `${lyrics ? `Letra de la canción:\n${lyrics.slice(0, 1500)}\n\n` : ''}El estudiante seleccionó: "${phrase}"\n\nResponde con este JSON exacto:\n{"significado":"traducción clara en español","vocabulario":[{"palabra":"word","traduccion":"traducción","tipo":"verbo|sustantivo|adjetivo|expresión"}],"gramatica":"nota gramatical breve o null","ejemplo":"ejemplo de uso en otra oración"}`;

      const raw  = await ask(system, userMsg);
      const json = extractJSON(raw);
      res.json(json);
    } catch (err) {
      console.error('[IA] explain:', err.message);
      res.status(500).json({ error: 'Error generando explicación', detail: err.message });
    }
  });

  app.post('/ia/exercises', async (req, res) => {
    const { songId, phrase } = req.body || {};
    if (!phrase) return res.status(400).json({ error: 'phrase es requerido' });

    try {
      const lyrics  = songId ? await fetchLyrics(msCanciones, songId) : null;
      const system  = 'Eres un tutor de inglés que crea ejercicios educativos. Responde únicamente con el JSON solicitado, sin texto adicional.';
      const userMsg = `${lyrics ? `Contexto de la letra:\n${lyrics.slice(0, 800)}\n\n` : ''}Frase seleccionada: "${phrase}"\n\nCrea 3 ejercicios en este JSON exacto:\n{"exercises":[{"type":"multiple_choice","question":"¿Qué significa...?","options":["opción A","opción B","opción C","opción D"],"correct":0,"explanation":"por qué es correcta"},{"type":"fill_blank","question":"Completa la oración: ___ ...","answer":"respuesta","hint":"pista"},{"type":"translation","question":"Traduce al inglés: '...'","answer":"traducción"}]}`;

      const raw  = await ask(system, userMsg);
      const json = extractJSON(raw);
      res.json(json);
    } catch (err) {
      console.error('[IA] exercises:', err.message);
      res.status(500).json({ error: 'Error generando ejercicios', detail: err.message });
    }
  });

  app.post('/ia/chat', async (req, res) => {
    const { songId, message, history = [] } = req.body || {};
    if (!message) return res.status(400).json({ error: 'message es requerido' });

    try {
      const lyrics = songId ? await fetchLyrics(msCanciones, songId) : null;
      const system = `Eres un tutor de inglés amigable y conciso que ayuda a estudiantes hispanohablantes a entender canciones en inglés.${lyrics ? `\n\nLetra de la canción:\n${lyrics.slice(0, 1200)}` : ''}\n\nResponde siempre en español, en 2-4 oraciones máximo.`;

      if (!claude) throw new Error('ANTHROPIC_API_KEY no configurada');

      const response = await claude.messages.create({
        model: 'claude-haiku-4-5-20251001',
        max_tokens: 400,
        system,
        messages: [
          ...history.slice(-6).map(h => ({ role: h.role, content: h.content })),
          { role: 'user', content: message },
        ],
      });

      res.json({ response: response.content[0].text });
    } catch (err) {
      console.error('[IA] chat:', err.message);
      res.status(500).json({ error: 'Error en chat', detail: err.message });
    }
  });
};
