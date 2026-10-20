# Frontend Library — Patrón de desarrollo JS

## Stack y contexto

- HTML + CSS + JS vanilla (sin frameworks)
- Backend Spring Boot en `http://localhost:8080/api/v1/`
- Auth via cookies httpOnly (`credentials: 'include'` en todos los fetch)
- SweetAlert2 disponible globalmente como `Swal` (CDN en cada HTML)
- CSS en `css/styles.css` — usar siempre las variables CSS del proyecto

---

## Backend — Arquitectura y contratos

### Arquitectura hexagonal

```
Controller (infrastructure/adapter/input/rest)
  → UseCase interface (domain/port/input)
    → Service (application/service)          ← lógica de negocio
      → Repository (JPA)
```

Los DTOs son Java `record`. Los campos usan **camelCase**.

### PaginatedResponse — shape EXACTO de toda respuesta paginada

```java
public record PaginatedResponse<T>(
    List<T> data,      // ← siempre "data", nunca "content" ni "items"
    int page,
    int perPage,
    long total,
    int lastPage
)
```

En JS leer **siempre** así:
```js
const items = data.data;
const total = data.total;
```

### SuccessResponse — shape de respuestas POST/PUT/PATCH/DELETE

```java
public record SuccessResponse(int statusCode, String message, LocalDateTime timestamp)
```

### ErrorResponse — shape de errores del GlobalExceptionHandler

- `404 Not Found` → `{ "error": "mensaje" }`
- `409 Conflict` (nombre duplicado, etc.) → `{ "error": "mensaje" }`
- `400 Bad Request` (validación `@Valid`) → `{ "campo": "mensaje", ... }` (mapa de campos)

En JS leer el error así:
```js
const err = await res.json().catch(() => ({}));
const msg = err.error || err.message || Object.values(err)[0] || 'Error desconocido';
```

---

## Endpoints por entidad

### Categorías — `/api/v1/categories`

| Método | URL | Body / Params | Roles |
|--------|-----|---------------|-------|
| GET | `/api/v1/categories` | `?q, active, page, perPage` | ADMIN, LIBRARIAN |
| GET | `/api/v1/categories/:id` | — | ADMIN, LIBRARIAN |
| POST | `/api/v1/categories` | `{ name, description }` | ADMIN, LIBRARIAN |
| PUT | `/api/v1/categories/:id` | `{ name, description }` | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/categories/:id/activate` | — | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/categories/:id/deactivate` | — | ADMIN, LIBRARIAN |
| DELETE | `/api/v1/categories/:id` | — | solo ADMIN |

**CategoryRequest:** `{ name: string (required, max 100), description: string (max 255) }`

**CategoryResponse:** `{ id, name, description, active, bookCount, createdAt, updatedAt }`

---

### Autores — `/api/v1/authors`

| Método | URL | Body / Params | Roles |
|--------|-----|---------------|-------|
| GET | `/api/v1/authors` | `?q, nationality, active, page, perPage` | ADMIN, LIBRARIAN |
| GET | `/api/v1/authors/:id` | — | ADMIN, LIBRARIAN |
| GET | `/api/v1/authors/select` | — | ADMIN, LIBRARIAN |
| POST | `/api/v1/authors` | ver AuthorRequest | ADMIN, LIBRARIAN |
| PUT | `/api/v1/authors/:id` | ver AuthorRequest | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/authors/:id/activate` | — | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/authors/:id/deactivate` | — | ADMIN, LIBRARIAN |
| DELETE | `/api/v1/authors/:id` | — | solo ADMIN |

**AuthorRequest:**
```json
{ "firstName": "string (required)", "lastName": "string (required)",
  "pseudonym": "string", "birthDate": "YYYY-MM-DD",
  "nationality": "string", "biography": "string", "email": "string" }
```

**AuthorResponse:** `{ id, firstName, lastName, pseudonym, birthDate, nationality, biography, email, active, bookCount, createdAt, updatedAt }`

**AuthorSelectResponse** (para `<select>`): `{ id, firstName, lastName }`

---

### Libros — `/api/v1/books`

| Método | URL | Body / Params | Roles |
|--------|-----|---------------|-------|
| GET | `/api/v1/books` | `?q, authorId, categoryId, language, active, page, perPage` | ADMIN, LIBRARIAN |
| GET | `/api/v1/books/:id` | — | ADMIN, LIBRARIAN |
| POST | `/api/v1/books` | ver BookRequest | ADMIN, LIBRARIAN |
| PUT | `/api/v1/books/:id` | ver BookRequest | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/books/:id/activate` | — | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/books/:id/deactivate` | — | ADMIN, LIBRARIAN |
| DELETE | `/api/v1/books/:id` | — | solo ADMIN |

**BookRequest:**
```json
{ "title": "string (required)", "isbn": "string (ISBN-10 o ISBN-13)",
  "description": "string", "publicationYear": 2024, "pages": 300,
  "language": "string", "publisher": "string", "coverUrl": "string",
  "authorId": 1, "categoryId": 1 }
```

**BookResponse:** `{ id, title, isbn, description, publicationYear, pages, language, publisher, coverUrl, authorId, authorName, categoryId, categoryName, active, createdAt, updatedAt }`

---

### Ejemplares — `/api/v1/book-copies`

| Método | URL | Body / Params | Roles |
|--------|-----|---------------|-------|
| GET | `/api/v1/book-copies` | `?q, bookId, status, condition, page, perPage` | ADMIN, LIBRARIAN |
| GET | `/api/v1/book-copies/:id` | — | ADMIN, LIBRARIAN |
| POST | `/api/v1/book-copies` | ver BookCopyRequest | ADMIN, LIBRARIAN |
| PUT | `/api/v1/book-copies/:id` | ver BookCopyRequest | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/book-copies/:id/lost` | — | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/book-copies/:id/damaged` | `?condition` | ADMIN, LIBRARIAN |
| PATCH | `/api/v1/book-copies/:id/restore` | — | ADMIN, LIBRARIAN |
| DELETE | `/api/v1/book-copies/:id` | — | solo ADMIN |

**BookCopyRequest:**
```json
{ "bookId": 1, "code": "EJ-0001 (required)",
  "status": "AVAILABLE|LOANED|LOST|DAMAGED",
  "condition": "NEW|GOOD|FAIR|DAMAGED (required)",
  "acquisitionDate": "YYYY-MM-DD", "price": 29.99, "location": "Estante A-1" }
```

**BookCopyResponse:** `{ id, code, status, condition, acquisitionDate, price, location, bookId, bookTitle, createdAt, updatedAt }`

**Enums CopyStatus:** `AVAILABLE`, `LOANED`, `LOST`, `DAMAGED`

**Enums CopyCondition:** `NEW`, `GOOD`, `FAIR`, `DAMAGED`

---

## Estructura de respuesta paginada del backend

Todos los endpoints GET paginados devuelven **exactamente** este shape:

```json
{
  "data": [...],
  "page": 1,
  "perPage": 10,
  "total": 42,
  "lastPage": 5
}
```

Leer siempre así:
```js
const items = data.data;
const total = data.total;
```

## Patrón de módulo JS (seguir SIEMPRE este patrón)

Cada entidad tiene su propio archivo `js/<entidad>.js`.
El prefijo de todas las variables y funciones es el código corto de la entidad (ej: `cat` para categorías, `aut` para autores, `lib` para libros).

### Variables de estado (al tope del archivo)
```js
const XXX_API = 'http://localhost:8080/api/v1/<recurso>';

let xxxPage    = 1;
let xxxTotal   = 0;
let xxxPerPage = 10;
let xxxEditId  = null;
```

### Fetch helper (credentials siempre)
```js
function xxxFetch(url, options = {}) {
  return fetch(url, { credentials: 'include', ...options });
}
```

### Notificaciones — usar SIEMPRE SweetAlert2, nunca alert/confirm nativos

**Toast (éxito o error):**
```js
function xxxToast(msg, icon = 'success') {
  Swal.fire({
    toast: true,
    position: 'bottom-end',
    icon,
    title: msg,
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
  });
}
```

**Confirmación de eliminación:**
```js
const result = await Swal.fire({
  title: '¿Eliminar <entidad>?',
  text: `"${name}" será eliminada permanentemente.`,
  icon: 'warning',
  showCancelButton: true,
  confirmButtonText: 'Sí, eliminar',
  cancelButtonText: 'Cancelar',
  confirmButtonColor: '#92352c',
  cancelButtonColor: '#6b6250',
});
if (!result.isConfirmed) return;
```

### Render de tabla
```js
function xxxRenderTable(items) {
  const tbody = document.getElementById('xxx-tbody');
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="N" style="text-align:center;padding:2em;color:var(--ink-soft)">Sin resultados</td></tr>`;
    return;
  }
  tbody.innerHTML = items.map(item => `<tr>...</tr>`).join('');
}
```

### Paginación
```js
function xxxRenderPagination() {
  const totalPages = Math.ceil(xxxTotal / xxxPerPage) || 1;
  document.getElementById('xxx-pag-info').textContent =
    `Página ${xxxPage} de ${totalPages} · ${xxxTotal} registros`;
  document.getElementById('xxx-pag-prev').disabled = xxxPage <= 1;
  document.getElementById('xxx-pag-next').disabled = xxxPage >= totalPages;
}
```

### Load (GET paginado)
```js
async function xxxLoad() {
  const q = document.getElementById('xxx-search')?.value.trim() || '';
  let url = `${XXX_API}?page=${xxxPage}&perPage=${xxxPerPage}`;
  if (q) url += `&q=${encodeURIComponent(q)}`;

  try {
    const res = await xxxFetch(url);
    if (!res.ok) throw new Error();
    const data = await res.json();
    xxxTotal = data.total;
    xxxRenderTable(data.data);
    xxxRenderPagination();
  } catch {
    xxxToast('Error al cargar', 'error');
  }
}
```

### Abrir modal crear
```js
function xxxOpenCreate() {
  xxxEditId = null;
  document.getElementById('xxx-modal-titulo').textContent = 'Nuevo <entidad>';
  document.getElementById('form-xxx').reset();
  document.getElementById('xxx-form-error').style.display = 'none';
  openModal('modal-xxx');
}
```

### Abrir modal editar (GET por id)
```js
async function xxxOpenEdit(id) {
  xxxEditId = id;
  document.getElementById('xxx-modal-titulo').textContent = 'Editar <entidad>';
  document.getElementById('xxx-form-error').style.display = 'none';
  try {
    const res = await xxxFetch(`${XXX_API}/${id}`);
    if (!res.ok) throw new Error();
    const item = await res.json();
    // rellenar campos del form
    openModal('modal-xxx');
  } catch {
    xxxToast('No se pudo cargar el registro', 'error');
  }
}
```

### Guardar (POST o PUT)
```js
async function xxxSave(e) {
  e.preventDefault();
  const errorEl = document.getElementById('xxx-form-error');
  errorEl.style.display = 'none';

  const body = { /* campos del form */ };
  const url    = xxxEditId ? `${XXX_API}/${xxxEditId}` : XXX_API;
  const method = xxxEditId ? 'PUT' : 'POST';

  try {
    const res = await xxxFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      errorEl.textContent = err.message || 'Error al guardar';
      errorEl.style.display = 'block';
      return;
    }
    closeModal('modal-xxx');
    xxxToast(xxxEditId ? '<Entidad> actualizada' : '<Entidad> creada');
    xxxLoad();
  } catch {
    errorEl.textContent = 'No se pudo conectar con el servidor';
    errorEl.style.display = 'block';
  }
}
```

### Activar / Desactivar (PATCH)
```js
async function xxxToggleActive(id, activate) {
  const action = activate ? 'activate' : 'deactivate';
  try {
    const res = await xxxFetch(`${XXX_API}/${id}/${action}`, { method: 'PATCH' });
    if (!res.ok) throw new Error();
    xxxToast(activate ? '<Entidad> activada' : '<Entidad> desactivada');
    xxxLoad();
  } catch {
    xxxToast('Error al cambiar el estado', 'error');
  }
}
```

### Eliminar (DELETE con confirmación Swal)
```js
async function xxxDelete(id, name) {
  const result = await Swal.fire({
    title: '¿Eliminar <entidad>?',
    text: `"${name}" será eliminada permanentemente.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#92352c',
    cancelButtonColor: '#6b6250',
  });
  if (!result.isConfirmed) return;
  try {
    const res = await xxxFetch(`${XXX_API}/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error();
    xxxToast('<Entidad> eliminada');
    if (xxxPage > 1 && xxxTotal - 1 <= (xxxPage - 1) * xxxPerPage) xxxPage--;
    xxxLoad();
  } catch {
    xxxToast('No se pudo eliminar', 'error');
  }
}
```

### Init (DOMContentLoaded)
```js
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('btn-nuevo-xxx')?.addEventListener('click', xxxOpenCreate);

  const searchInput = document.getElementById('xxx-search');
  if (searchInput) {
    let debounce;
    searchInput.addEventListener('input', () => {
      clearTimeout(debounce);
      debounce = setTimeout(() => { xxxPage = 1; xxxLoad(); }, 350);
    });
  }

  document.getElementById('xxx-filter-active')
    ?.addEventListener('change', () => { xxxPage = 1; xxxLoad(); });

  document.getElementById('xxx-pag-prev')
    ?.addEventListener('click', () => { xxxPage--; xxxLoad(); });
  document.getElementById('xxx-pag-next')
    ?.addEventListener('click', () => { xxxPage++; xxxLoad(); });

  document.getElementById('form-xxx')
    ?.addEventListener('submit', xxxSave);

  // Si está dentro de tabs, cargar al activar la pestaña
  document.querySelectorAll('.tab-link').forEach(link => {
    link.addEventListener('click', () => {
      if (link.dataset.tab === '<tab-name>') xxxLoad();
    });
  });
});
```

## Convenciones HTML para los paneles

Cada panel de entidad en el HTML debe tener:

```html
<!-- Panel -->
<div id="panel-xxx" style="display:none">
  <section class="table-card">
    <div class="table-card-head">
      <div class="search-row">
        <input type="search" id="xxx-search" placeholder="Buscar…" />
        <select id="xxx-filter-active">
          <option value="">Todos los estados</option>
          <option value="true">Activos</option>
          <option value="false">Inactivos</option>
        </select>
      </div>
      <button class="btn btn-primary" id="btn-nuevo-xxx">+ Nuevo</button>
    </div>
    <div class="table-scroll">
      <table>
        <thead>...</thead>
        <tbody id="xxx-tbody">
          <tr><td colspan="N" style="text-align:center;padding:2em;color:var(--ink-soft)">Cargando…</td></tr>
        </tbody>
      </table>
    </div>
    <div class="pagination">
      <span id="xxx-pag-info"></span>
      <div style="display:flex;gap:0.5em">
        <button class="btn btn-secondary" id="xxx-pag-prev" disabled>Anterior</button>
        <button class="btn btn-secondary" id="xxx-pag-next">Siguiente</button>
      </div>
    </div>
  </section>
</div>

<!-- Modal -->
<div class="modal-backdrop" id="modal-xxx" role="dialog" aria-modal="true" aria-labelledby="xxx-modal-titulo">
  <div class="modal">
    <div class="modal-header">
      <h2 id="xxx-modal-titulo">Nuevo <entidad></h2>
      <button class="modal-close" onclick="closeModal('modal-xxx')" aria-label="Cerrar">✕</button>
    </div>
    <div class="modal-body">
      <form id="form-xxx" novalidate>
        <div id="xxx-form-error" class="form-error"></div>
        <!-- campos -->
      </form>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeModal('modal-xxx')">Cancelar</button>
      <button class="btn btn-primary" form="form-xxx" type="submit">Guardar</button>
    </div>
  </div>
</div>
```

## Stamps (badges de estado)

```html
<span class="stamp stamp-available">Activo</span>
<span class="stamp stamp-lost">Inactivo</span>
<span class="stamp stamp-loaned">Prestado</span>
<span class="stamp stamp-overdue">Vencido</span>
<span class="stamp stamp-pending">Pendiente</span>
```

## Scripts en cada HTML

```html
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<!-- al final del body, antes del script inline de tabs/modales -->
<script src="../../js/auth-guard.js"></script>
<script src="../../js/session.js"></script>
<script src="../../js/<entidad>.js"></script>
```

## Referencia canónica

El archivo `js/categoria.js` es la implementación de referencia completa.
Ante cualquier duda de patrón, seguir ese archivo exactamente.
