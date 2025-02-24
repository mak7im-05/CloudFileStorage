let activeDropdown = null;

function toggleDropdown(event) {
    event.preventDefault();
    event.stopPropagation();
    const dropdown = event.currentTarget.nextElementSibling;

    if (activeDropdown && activeDropdown !== dropdown) {
        activeDropdown.classList.add('hidden');
    }

    dropdown.classList.toggle('hidden');
    activeDropdown = dropdown.classList.contains('hidden') ? null : dropdown;
}

window.addEventListener('click', function (event) {
    if (!event.target.closest('.dropdown')) {
        document.querySelectorAll('.dropdown-menu').forEach(dropdown => {
            dropdown.classList.add('hidden');
        });
        activeDropdown = null;
    }
});