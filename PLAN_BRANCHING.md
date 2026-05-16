# 📊 Plan de Estrategia de Branching - Notara

**Documento:** Plan de Branching  
**Proyecto:** Notara - Plataforma de aprendizaje de inglés  
**Fecha:** Mayo 2026  
**Equipo:** 1tokkio, eliecer1, panxo

---

## 1. Modelo de Branching

### Estrategia: Git Flow Simplificado

```
main (producción)
  ↓
  ├─── feature/1tokkio (frontend + integración)
  ├─── feature/eliecer1 (ms-usuarios + autenticación)
  ├─── feature/panxo (UX/diseño + frontend)
  └─── feature/login-registro (autenticación mejorada)
```

### Reglas:

1. **main**: Solo código listo para producción
   - Solo merges de PRs revisados
   - Cada merge = versión estable
   
2. **feature/***:  Ramas de desarrollo por persona/característica
   - Crear desde: `git checkout -b feature/nombre main`
   - Commits descriptivos: `feat: ...`, `fix: ...`, `docs: ...`
   - No pushear directamente a main

3. **Ciclo de vida**:
   - Crear rama feature desde main
   - Trabajo local (commits frecuentes)
   - Push a remote
   - Pull Request → review
   - Merge a main
   - Eliminar rama

---

## 2. Ramas Actuales del Proyecto

| Rama | Propósito | Responsable | Estado |
|------|-----------|-------------|--------|
| `main` | Código estable | Equipo | Activa |
| `1tokkio` | Frontend + integración | 1tokkio | Activa |
| `eliecer1` | ms-usuarios + auth | eliecer1 | Activa |
| `panxo` | UX/diseño frontend | panxo | Activa |
| `feature/login-registro` | Auth mejorada | Equipo | Activa |

---

## 3. Proceso de Desarrollo

### 3.1 Crear una rama nueva

```bash
# Actualizar main local
git checkout main
git pull origin main

# Crear rama feature
git checkout -b feature/mi-caracteristica

# Empezar a trabajar
echo "código nuevo" >> archivo.js
git add archivo.js
git commit -m "feat: descripción clara de qué hace"
```

### 3.2 Commits descriptivos

**Formato:** `<tipo>: <descripción>`

```bash
# Bueno ✓
git commit -m "feat: agregar estrategia de lyrics display"
git commit -m "fix: corregir manejo de errores en CircuitBreaker"
git commit -m "docs: actualizar README de ms-canciones"
git commit -m "test: agregar pruebas para Factory pattern"

# Malo ✗
git commit -m "cambios" 
git commit -m "actualizaciones"
git commit -m "fix bug"
```

### 3.3 Push a remote

```bash
# Primera vez (crea rama remota)
git push -u origin feature/mi-caracteristica

# Siguientes pushes
git push
```

### 3.4 Pull Request y Merge

```bash
# 1. Push todos los cambios
git push

# 2. Crear PR en GitHub
#    - Desde feature/mi-caracteristica → main
#    - Descripción: qué cambios, por qué
#    - Adjuntar screenshot/gif si es UI

# 3. Review por al menos 1 persona

# 4. Merge (después de review)
#    - Opción recomendada: "Squash and merge"
#    - Mantiene main limpio

# 5. Borrar rama después de merge
git branch -d feature/mi-caracteristica
git push origin --delete feature/mi-caracteristica
```

---

## 4. Gestión de Conflictos

### 4.1 Cuando surge un conflicto

**Escenario real:** 
- Persona A edita `ms-canciones/src/routes/songs.js` línea 45
- Persona B edita el mismo archivo, misma línea
- Ambas hacen push → conflicto en merge

### 4.2 Resolución paso a paso

```bash
# Paso 1: Estás en tu rama feature/X
git fetch origin

# Paso 2: Intentas mergear main (para sincronizar)
git merge origin/main
# ERROR: Conflicto en ms-canciones/src/routes/songs.js

# Paso 3: Editar el archivo
# Git marca los conflictos así:
# <<<<<<< HEAD
# mi código aquí (de feature/X)
# =======
# código del otro (de main)
# >>>>>>> origin/main

# Paso 4: Decidir qué mantener
# Opción 1: Mantener solo tu código
# Opción 2: Mantener solo el código de main
# Opción 3: Combinar ambos (RECOMENDADO - coordinarse)

# Paso 5: Editar el archivo, remover marcas
nano ms-canciones/src/routes/songs.js
# ... resolver conflictos manualmente ...

# Paso 6: Marcar como resuelto
git add ms-canciones/src/routes/songs.js

# Paso 7: Completar el merge
git commit -m "fix: resolver conflicto en songs.js integrando cambios de ambas ramas"

# Paso 8: Verificar y testear
npm test

# Paso 9: Push
git push
```

### 4.3 Ejemplo real (Notara)

**Conflicto ocurrido:** Merge de `feature/login-registro` a `main`

**Archivo conflictivo:** `api-gateway/src/auth/authenticator.js`

**Cambios en conflicto:**
- `main`: Valida email con regex simple
- `feature/login-registro`: Valida email con librería external

**Resolución:**
```javascript
// ANTES (conflicto)
<<<<<<< HEAD
const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
=======
const validator = require('email-validator');
const isValidEmail = (email) => validator.validate(email);
>>>>>>> feature/login-registro

// DESPUÉS (resuelto - combinamos)
const validator = require('email-validator');
const isValidEmail = (email) => {
  // Usar librería externa más robusta
  return validator.validate(email);
};
```

**Commits relacionados:**
```
61dcb5f fix: resolver conflicto en authenticator.js usando email-validator
59e5d2a Merge pull request #XX: feature/login-registro → main
```

---

## 5. Merges Realizados

| # | De rama | A rama | Conflictos | Commits | Fecha |
|----|---------|--------|-----------|---------|-------|
| 1 | eliecer1 | main | 0 | 3 | Mayo 7 |
| 2 | 1tokkio | main | 0 | 5 | Mayo 8 |
| 3 | panxo | main | 0 | 4 | Mayo 14 |
| 4 | feature/login-registro | main | **1** | 2 | Mayo 15 |

**Total:** 14 commits, 1 conflicto resuelto exitosamente

---

## 6. Beneficios para la Colaboración

### ✅ Evita conflictos frecuentes
- Cada persona en rama independiente
- Trabajo paralelo sin bloqueos

### ✅ Fácil revertir cambios
```bash
git revert <commit-hash>  # Si algo sale mal
```

### ✅ Historial limpio
- Commits descriptivos permiten entender qué cambió
- Fácil hacer `git log --oneline` y entender la historia

### ✅ Control de versiones efectivo
- main siempre estable
- Ramas feature pueden experimentar sin riesgo

### ✅ Facilita code review
- PR muestra exactamente qué cambió
- Revisor puede comentar línea por línea

---

## 7. Flujo Recomendado para Nuevo Feature

```bash
# 1. Actualizar main local
git checkout main
git pull origin main

# 2. Crear rama
git checkout -b feature/nueva-funcionalidad

# 3. Trabajar (múltiples commits)
echo "código" >> file1.js
git add file1.js
git commit -m "feat: implementar parte 1 de nueva-funcionalidad"

echo "más código" >> file2.js
git add file2.js
git commit -m "feat: implementar parte 2"

# 4. Sincronizar con main (por si otros mergearon)
git fetch origin main
git merge origin/main  # Resolver conflictos si existen

# 5. Testear en local
npm test

# 6. Push
git push -u origin feature/nueva-funcionalidad

# 7. En GitHub: Crear PR
# - Descripción clara: qué hace, por qué
# - Link a issues relacionados si existen
# - Screenshot/video si es UI

# 8. Esperar review + aprobación

# 9. Merge en GitHub (botón "Squash and merge")

# 10. Borrar rama remota (auto-delete en GitHub)

# 11. En local, limpiar
git checkout main
git pull origin main
git branch -d feature/nueva-funcionalidad
```

---

## 8. Comandos Útiles

```bash
# Ver rama actual
git branch

# Ver todas las ramas (local + remoto)
git branch -a

# Ver commits en tu rama que no están en main
git log main..HEAD --oneline

# Ver cambios sin commitar
git status

# Ver cambios en un archivo
git diff archivo.js

# Ver commit history
git log --oneline --graph --all

# Revertir cambios locales (cuidado!)
git restore archivo.js

# Crear rama desde commit específico
git checkout -b feature/nueva <commit-hash>
```

---

## 9. Recomendaciones Finales

1. **Commit frecuentemente** (cada feature pequeña)
2. **Sincroniza con main regularmente** (evita conflictos grandes)
3. **Revisa PRs antes de mergear** (calidad de código)
4. **Escribe mensajes de commit claros** (facilita búsqueda)
5. **Testea antes de mergear** (no rompes main)
6. **Borrar ramas después de merge** (limpieza)

---

**Conclusión:** Esta estrategia permite trabajo paralelo eficiente, fácil colaboración, y mantiene `main` siempre estable. Con disciplina en commits y reviews, el proyecto es escalable a más personas sin caos.
