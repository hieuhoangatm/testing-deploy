// Theme toggle
(function () {
    const toggle = document.getElementById('theme-toggle');
    if (!toggle) return;

    function getTheme() {
        return localStorage.getItem('theme') ||
            (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    }

    applyTheme(getTheme());

    toggle.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        applyTheme(current === 'dark' ? 'light' : 'dark');
    });

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (!localStorage.getItem('theme')) applyTheme(e.matches ? 'dark' : 'light');
    });
})();

// Mobile menu toggle
(function () {
    const btn = document.getElementById('mobile-menu-toggle');
    const menu = document.getElementById('navMenu');
    if (!btn || !menu) return;

    btn.addEventListener('click', () => {
        menu.classList.toggle('open');
    });

    document.addEventListener('click', (e) => {
        if (!btn.contains(e.target) && !menu.contains(e.target)) {
            menu.classList.remove('open');
        }
    });
})();

// Footer year
(function () {
    const el = document.getElementById('year');
    if (el) el.textContent = new Date().getFullYear();
})();

// Highlight active nav link
(function () {
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href === path || (href !== '/' && path.startsWith(href))) {
            link.style.color = 'var(--text)';
            link.style.background = 'var(--bg-2)';
            link.setAttribute('aria-current', 'page');
        }
    });
})();

// Lazy load images with IntersectionObserver (fallback for browsers without native lazy)
(function () {
    if ('loading' in HTMLImageElement.prototype) return;
    const images = document.querySelectorAll('img[loading="lazy"]');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(e => {
            if (e.isIntersecting) {
                const img = e.target;
                img.src = img.dataset.src || img.src;
                observer.unobserve(img);
            }
        });
    });
    images.forEach(img => observer.observe(img));
})();

// Smooth image reveal on load
document.querySelectorAll('img').forEach(img => {
    img.style.opacity = '0';
    img.style.transition = 'opacity 0.4s ease';
    if (img.complete) {
        img.style.opacity = '1';
    } else {
        img.addEventListener('load', () => { img.style.opacity = '1'; });
        img.addEventListener('error', () => { img.style.opacity = '1'; });
    }
});
