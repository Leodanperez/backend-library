/* =========================================================
   categoria.js — CRUD completo de categorías
   Endpoints:
     GET    /api/v1/categories?q=&active=&page=&perPage=
     GET    /api/v1/categories/:id
     POST   /api/v1/categories          { name, description }
     PUT    /api/v1/categories/:id      { name, description }
     PATCH  /api/v1/categories/:id/activate
     PATCH  /api/v1/categories/:id/deactivate
     DELETE /api/v1/categories/:id      (solo ADMIN)
   ========================================================= */

const CAT_API = 'http://localhost:8080/api/v1/categories';

let catPage = 1;
let catTotal = 0;
let catPerPage = 10;
let catEditId = null;

/* ── utilidades ── */
function catFetch(url, options = {}) {
  return fetch(url, { credentials: 'include', ...options });
}

function catToast(msg, icon = 'success') {
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

function catFormatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
}

/* ── render tabla ── */
function catRenderTable(items) {
  const tbody = document.getElementById('cat-tbody');
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2em;color:var(--ink-soft)">Sin resultados</td></tr>`;
    return;
  }
  tbody.innerHTML = items.map(c => `
    <tr>
      <td class="cell-title">${c.name}</td>
      <td>${c.description || '<span style="color:var(--ink-faint)">—</span>'}</td>
      <td>${c.bookCount}</td>
      <td>
        <span class="stamp ${c.active ? 'stamp-available' : 'stamp-lost'}">
          ${c.active ? 'Activa' : 'Inactiva'}
        </span>
      </td>
      <td>
        <div class="row-actions">
          <button class="btn btn-quiet" onclick="catOpenEdit(${c.id})">Editar</button>
          ${c.active
            ? `<button class="btn btn-quiet" onclick="catToggleActive(${c.id}, false)">Desactivar</button>`
            : `<button class="btn btn-quiet" onclick="catToggleActive(${c.id}, true)">Activar</button>`
          }
          <button class="btn btn-danger btn-quiet" onclick="catDelete(${c.id}, '${c.name.replace(/'/g, "\\'")}')">Eliminar</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function catRenderPagination() {
  const totalPages = Math.ceil(catTotal / catPerPage) || 1;
  document.getElementById('cat-pag-info').textContent =
    `Página ${catPage} de ${totalPages} · ${catTotal} categorías`;
  document.getElementById('cat-pag-prev').disabled = catPage <= 1;
  document.getElementById('cat-pag-next').disabled = catPage >= totalPages;
}

/* ── cargar datos ── */
async function catLoad() {
  const q = document.getElementById('cat-search')?.value.trim() || '';
  const activeFilter = document.getElementById('cat-filter-active')?.value;

  let url = `${CAT_API}?page=${catPage}&perPage=${catPerPage}`;
  if (q) url += `&q=${encodeURIComponent(q)}`;
  if (activeFilter !== '') url += `&active=${activeFilter}`;

  try {
    const res = await catFetch(url);
    if (!res.ok) throw new Error();
    const data = await res.json();
    catTotal = data.total ?? data.totalElements ?? 0;
    catRenderTable(data.data ?? data.content ?? data.items ?? []);
    catRenderPagination();
  } catch {
    catToast('Error al cargar categorías', 'error');
  }
}

/* ── abrir modal crear ── */
function catOpenCreate() {
  catEditId = null;
  document.getElementById('mc-titulo').textContent = 'Nueva categoría';
  document.getElementById('form-categoria').reset();
  document.getElementById('cat-form-error').style.display = 'none';
  openModal('modal-categoria');
}

/* ── abrir modal editar ── */
async function catOpenEdit(id) {
  catEditId = id;
  document.getElementById('mc-titulo').textContent = 'Editar categoría';
  document.getElementById('cat-form-error').style.display = 'none';

  try {
    const res = await catFetch(`${CAT_API}/${id}`);
    if (!res.ok) throw new Error();
    const c = await res.json();
    document.getElementById('cat-nombre').value = c.name;
    document.getElementById('cat-descripcion').value = c.description || '';
    openModal('modal-categoria');
  } catch {
    catToast('No se pudo cargar la categoría', 'error');
  }
}

/* ── guardar (crear o editar) ── */
async function catSave(e) {
  e.preventDefault();
  const errorEl = document.getElementById('cat-form-error');
  errorEl.style.display = 'none';

  const body = {
    name: document.getElementById('cat-nombre').value.trim(),
    description: document.getElementById('cat-descripcion').value.trim() || null,
  };

  const url    = catEditId ? `${CAT_API}/${catEditId}` : CAT_API;
  const method = catEditId ? 'PUT' : 'POST';

  try {
    const res = await catFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      errorEl.textContent = err.message || 'Error al guardar la categoría';
      errorEl.style.display = 'block';
      return;
    }

    closeModal('modal-categoria');
    catToast(catEditId ? 'Categoría actualizada' : 'Categoría creada');
    catLoad();
  } catch {
    errorEl.textContent = 'No se pudo conectar con el servidor';
    errorEl.style.display = 'block';
  }
}

/* ── activar / desactivar ── */
async function catToggleActive(id, activate) {
  const action = activate ? 'activate' : 'deactivate';
  try {
    const res = await catFetch(`${CAT_API}/${id}/${action}`, { method: 'PATCH' });
    if (!res.ok) throw new Error();
    catToast(activate ? 'Categoría activada' : 'Categoría desactivada');
    catLoad();
  } catch {
    catToast('Error al cambiar el estado', 'error');
  }
}

/* ── eliminar ── */
async function catDelete(id, name) {
  const result = await Swal.fire({
    title: '¿Eliminar categoría?',
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
    const res = await catFetch(`${CAT_API}/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error();
    catToast('Categoría eliminada');
    if (catPage > 1 && catTotal - 1 <= (catPage - 1) * catPerPage) catPage--;
    catLoad();
  } catch {
    catToast('No se pudo eliminar la categoría', 'error');
  }
}

/* ── init ── */
document.addEventListener('DOMContentLoaded', () => {
  /* reemplazar el botón estático del HTML */
  const btnNueva = document.querySelector('#panel-categorias .btn-primary');
  if (btnNueva) btnNueva.onclick = catOpenCreate;

  /* búsqueda con debounce */
  const searchInput = document.getElementById('cat-search');
  if (searchInput) {
    let debounce;
    searchInput.addEventListener('input', () => {
      clearTimeout(debounce);
      debounce = setTimeout(() => { catPage = 1; catLoad(); }, 350);
    });
  }

  /* filtro activo/inactivo */
  const filterSelect = document.getElementById('cat-filter-active');
  if (filterSelect) {
    filterSelect.addEventListener('change', () => { catPage = 1; catLoad(); });
  }

  /* paginación */
  document.getElementById('cat-pag-prev')?.addEventListener('click', () => { catPage--; catLoad(); });
  document.getElementById('cat-pag-next')?.addEventListener('click', () => { catPage++; catLoad(); });

  /* form submit */
  document.getElementById('form-categoria')?.addEventListener('submit', catSave);

  /* cargar al abrir la pestaña de categorías */
  document.querySelectorAll('.tab-link').forEach(link => {
    link.addEventListener('click', () => {
      if (link.dataset.tab === 'categorias') catLoad();
    });
  });
});
