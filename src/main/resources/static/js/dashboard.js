document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menu-toggle');
    const menuClose = document.getElementById('menu-close');
    const navLinks = document.querySelectorAll('nav a');
    const pages = document.querySelectorAll('.page-content');
    const pageTitle = document.getElementById('page-title');

    // Manejar la navegación de la barra lateral
    navLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            const targetPage = link.getAttribute('data-page');

            // Ocultar todas las páginas
            pages.forEach(page => page.classList.add('hidden'));

            // Mostrar la página objetivo y actualizar el título
            const activePage = document.getElementById(targetPage);
            if (activePage) {
                activePage.classList.remove('hidden');
                pageTitle.textContent = link.querySelector('span').textContent;
            }
            
            // Ocultar la barra lateral en móviles
            if (window.innerWidth < 768) {
                sidebar.classList.add('-translate-x-full');
            }
        });
    });

    // Alternar la barra lateral en móviles
    menuToggle.addEventListener('click', () => {
        sidebar.classList.remove('-translate-x-full');
    });
    menuClose.addEventListener('click', () => {
        sidebar.classList.add('-translate-x-full');
    });

});