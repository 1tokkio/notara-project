/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          green:  '#22c55e',
          dark:   '#0f0f0f',
          card:   '#1a1a1a',
          hover:  '#252525',
          text:   '#9ca3af',
          purple: '#8b5cf6',
          orange: '#f97316',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        geist: ['Geist', 'system-ui', 'sans-serif'],
        'geist-mono': ['Geist Mono', 'monospace'],
      },
      keyframes: {
        fadeIn:  { from: { opacity: 0, transform: 'translateY(4px)' },  to: { opacity: 1, transform: 'translateY(0)' } },
        slideUp: { '0%': { opacity: '0', transform: 'translateY(28px)' }, '100%': { opacity: '1', transform: 'translateY(0)' } },
      },
      animation: {
        fadeIn:     'fadeIn 0.2s ease-out',
        'slide-up': 'slideUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) both',
      },
    },
  },
  plugins: [],
};
