# Frontend — Notara

Aplicación web para aprender idiomas a través de canciones. Desarrollada con Next.js 14 y React, consume los microservicios de Notara a través del API Gateway.

## Stack

- Next.js 14 (App Router)
- React 18
- Tailwind CSS + shadcn/ui
- Jest + Testing Library (cobertura mínima 85%)

## Variables de entorno

Crea un archivo `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:3000
```

## Instalación y ejecución

```bash
npm install
npm run dev      # http://localhost:3001
npm run build
npm start
```

## Pruebas

```bash
npm test                  # ejecutar tests
npm run test:coverage     # generar reporte de cobertura
```

Los reportes HTML se generan en `coverage/lcov-report/index.html`.

La configuración exige cobertura mínima del **85%** en branches, functions, lines y statements.

## Estructura del proyecto

```
src/
├── app/          # Rutas Next.js (App Router)
├── components/   # Componentes reutilizables (ui/, lesson/)
├── context/      # Contextos React (auth, player)
├── lib/          # Utilidades y clientes API
├── patterns/     # Patrones de diseño implementados
└── tests/        # Pruebas unitarias
```

## Docker

```bash
docker build -t notara-frontend .
docker run -p 3001:3000 --env-file .env.local notara-frontend
```
