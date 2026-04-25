/**
 * api.js — Cliente centralizado para el API Gateway
 *
 * Todas las llamadas al backend pasan por aquí.
 * Maneja automáticamente el token JWT en los headers.
 */

<<<<<<< HEAD
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';
=======
const API_URL = (() => {
  // En CodeSandbox, detectar automáticamente la URL del gateway
  if (
    typeof window !== "undefined" &&
    window.location.hostname.includes(".csb.app")
  ) {
    return window.location.origin.replace("-3001.csb.app", "-3000.csb.app");
  }
  return process.env.NEXT_PUBLIC_API_URL || "http://localhost:3000";
})();
>>>>>>> origin/panxo

/**
 * Función base de fetch con manejo de errores y token automático.
 */
async function request(path, options = {}) {
<<<<<<< HEAD
  const token = typeof window !== 'undefined'
    ? localStorage.getItem('access_token')
    : null;

  const headers = {
    'Content-Type': 'application/json',
=======
  const token =
    typeof window !== "undefined" ? localStorage.getItem("access_token") : null;

  const headers = {
    "Content-Type": "application/json",
>>>>>>> origin/panxo
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${API_URL}${path}`, { ...options, headers });

  // Si el token expiró, intentar renovarlo
  if (res.status === 401) {
    const renewed = await refreshToken();
    if (renewed) {
<<<<<<< HEAD
      const newToken = localStorage.getItem('access_token');
=======
      const newToken = localStorage.getItem("access_token");
>>>>>>> origin/panxo
      const retryRes = await fetch(`${API_URL}${path}`, {
        ...options,
        headers: { ...headers, Authorization: `Bearer ${newToken}` },
      });
      return retryRes.json();
    }
    // Si no se pudo renovar, redirigir al login
    localStorage.clear();
<<<<<<< HEAD
    window.location.href = '/login';
=======
    window.location.href = "/login";
>>>>>>> origin/panxo
    return;
  }

  const data = await res.json();
  if (!res.ok) throw new Error(data.error || `Error ${res.status}`);
  return data;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export const auth = {
  register: (name, email, password) =>
<<<<<<< HEAD
    request('/auth/register', {
      method: 'POST',
=======
    request("/auth/register", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ name, email, password }),
    }),

  login: async (email, password) => {
<<<<<<< HEAD
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (data?.accessToken) {
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
=======
    const data = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    if (data?.accessToken) {
      localStorage.setItem("access_token", data.accessToken);
      localStorage.setItem("refresh_token", data.refreshToken);
      localStorage.setItem("user", JSON.stringify(data.user));
>>>>>>> origin/panxo
    }
    return data;
  },

  logout: () => {
<<<<<<< HEAD
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
  },

  me: () => request('/users/me'),
=======
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");
  },

  me: () => request("/users/me"),
>>>>>>> origin/panxo
};

async function refreshToken() {
  try {
<<<<<<< HEAD
    const rt = localStorage.getItem('refresh_token');
    if (!rt) return false;
    const res = await fetch(`${API_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
=======
    const rt = localStorage.getItem("refresh_token");
    if (!rt) return false;
    const res = await fetch(`${API_URL}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
>>>>>>> origin/panxo
      body: JSON.stringify({ refreshToken: rt }),
    });
    if (!res.ok) return false;
    const data = await res.json();
<<<<<<< HEAD
    localStorage.setItem('access_token', data.accessToken);
=======
    localStorage.setItem("access_token", data.accessToken);
>>>>>>> origin/panxo
    return true;
  } catch {
    return false;
  }
}

// ─── Canciones ────────────────────────────────────────────────────────────────

export const songs = {
  search: (query, limit = 10) =>
    request(`/songs/search?q=${encodeURIComponent(query)}&limit=${limit}`),

<<<<<<< HEAD
  getById: (id) =>
    request(`/songs/${id}`),

  getLyrics: (id) =>
    request(`/songs/${id}/lyrics`),

  getLessonType: (id) =>
    request(`/songs/${id}/lesson-type`),
=======
  getById: (id) => request(`/songs/${id}`),

  getLyrics: (id) => request(`/songs/${id}/lyrics`),

  getLessonType: (id) => request(`/songs/${id}/lesson-type`),
>>>>>>> origin/panxo
};

// ─── IA Tutor ─────────────────────────────────────────────────────────────────

export const ia = {
<<<<<<< HEAD
  explain: (songId, phrase, userLevel = 'intermediate') =>
    request('/ia/explain', {
      method: 'POST',
=======
  explain: (songId, phrase, userLevel = "intermediate") =>
    request("/ia/explain", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ songId, phrase, userLevel }),
    }),

  getExercises: (songId, phrase) =>
<<<<<<< HEAD
    request('/ia/exercises', {
      method: 'POST',
=======
    request("/ia/exercises", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ songId, phrase }),
    }),

  chat: (songId, message, history = []) =>
<<<<<<< HEAD
    request('/ia/chat', {
      method: 'POST',
=======
    request("/ia/chat", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ songId, message, history }),
    }),
};

// ─── Progreso ─────────────────────────────────────────────────────────────────

export const progress = {
<<<<<<< HEAD
  getStats: () =>
    request('/progress/stats'),

  saveWord: (word, songId, context) =>
    request('/progress/word', {
      method: 'POST',
=======
  getStats: () => request("/progress/stats"),

  saveWord: (word, songId, context) =>
    request("/progress/word", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ word, songId, context }),
    }),

  completeLesson: (songId, lessonType, wordsLearned) =>
<<<<<<< HEAD
    request('/progress/lesson-complete', {
      method: 'POST',
=======
    request("/progress/lesson-complete", {
      method: "POST",
>>>>>>> origin/panxo
      body: JSON.stringify({ songId, lessonType, wordsLearned }),
    }),
};
