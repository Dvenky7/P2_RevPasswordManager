/**
 * Password Generator Interactive Logic
 */

/**
 * Copies a generated password to the clipboard
 * @param {string} id - The ID of the element containing the password
 */
function copyPass(id) {
    const passElement = document.getElementById(id);
    if (!passElement) return;

    const pass = passElement.innerText;
    navigator.clipboard.writeText(pass).then(() => {
        // Find the button that was clicked
        const btn = event.currentTarget;
        const originalHtml = btn.innerHTML;

        // Show success feedback
        btn.innerHTML = '<i class="fas fa-check text-success"></i>';

        // Reset button after 1.5 seconds
        setTimeout(() => {
            btn.innerHTML = originalHtml;
        }, 1500);
    }).catch(err => {
        console.error('Failed to copy password: ', err);
    });
}

/**
 * Redirects to the "Add Credential" page with the password pre-filled
 * @param {string} id - The ID of the element containing the password
 */
function saveToVault(id) {
    const passElement = document.getElementById(id);
    if (!passElement) return;

    const pass = passElement.innerText;
    window.location.href = '/vault/add?prefilledPassword=' + encodeURIComponent(pass);
}
