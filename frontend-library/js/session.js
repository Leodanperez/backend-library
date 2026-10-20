document.addEventListener('DOMContentLoaded', () => {
    const nombre = sessionStorage.getItem('nombre') || '';
    const initials = nombre.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();

    // Topbar con sidebar (admin / biblioteca)
    const elNombre = document.getElementById('topbar-nombre');
    const elAvatar = document.getElementById('topbar-avatar');
    if (elNombre) elNombre.textContent = nombre;
    if (elAvatar) elAvatar.textContent = initials;

    // Topnav simple (student) — user-chip inline
    document.querySelectorAll('.topnav-nombre').forEach(el => { el.textContent = nombre; });
    document.querySelectorAll('.topnav-avatar').forEach(el => { el.textContent = initials; });

    // Banner de bienvenida (student dashboard)
    const elDashNombre = document.getElementById('dash-nombre');
    if (elDashNombre) elDashNombre.textContent = nombre;

    // Banner de bienvenida (admin dashboard)
    const elWelcomeNombre = document.getElementById('welcome-nombre');
    if (elWelcomeNombre) elWelcomeNombre.textContent = nombre;
});
