// Mobile menu toggle
function toggleMenu() {
    const menu = document.getElementById('mobile-menu');
    if (menu) menu.classList.toggle('hidden');
}

// Animated stat counters on the landing page
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-count]').forEach(el => {
        const target = parseInt(el.getAttribute('data-count'), 10);
        const duration = 1600;
        const start = performance.now();
        function tick(now) {
            const p = Math.min((now - start) / duration, 1);
            const eased = 1 - Math.pow(1 - p, 3);
            el.textContent = Math.floor(eased * target).toLocaleString();
            if (p < 1) requestAnimationFrame(tick);
        }
        requestAnimationFrame(tick);
    });

    // Auto-dismiss flash alerts
    setTimeout(() => {
        document.querySelectorAll('.auto-dismiss').forEach(el => {
            el.style.transition = 'opacity .6s'; el.style.opacity = '0';
            setTimeout(() => el.remove(), 650);
        });
    }, 5000);
});
