/**
 * Dashboard JavaScript Logic
 * Handles interactive elements of the vault dashboard.
 */

let currentRevealId = null;
let currentDeleteId = null;
let currentDeleteRow = null;
let timerInterval;

/**
 * Shows the password reveal modal and resets its state
 */
function revealPassword(id, accountName) {
    currentRevealId = id;
    document.getElementById('modalAccountName').innerText = accountName;

    // Reset modal state to challenge view
    document.getElementById('passwordChallengeSection').style.display = 'block';
    document.getElementById('revealedSection').style.display = 'none';
    document.getElementById('masterPasswordInput').value = '';
    document.getElementById('verificationError').style.display = 'none';

    const modalElement = document.getElementById('passwordModal');
    let modal = bootstrap.Modal.getOrCreateInstance(modalElement);
    modal.show();
}

/**
 * Verifies master password and reveals the credential password
 */
function confirmAndReveal() {
    const masterPassword = document.getElementById('masterPasswordInput').value;
    const errorDiv = document.getElementById('verificationError');

    if (!masterPassword) return;

    const formData = new FormData();
    formData.append('masterPassword', masterPassword);

    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch('/vault/reveal/' + currentRevealId, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        body: formData
    })
        .then(response => {
            if (response.status === 401) throw new Error('UNAUTHORIZED');
            if (!response.ok) throw new Error('Failed to decrypt');
            return response.text();
        })
        .then(password => {
            document.getElementById('passwordChallengeSection').style.display = 'none';
            document.getElementById('revealedSection').style.display = 'block';
            document.getElementById('revealedPassword').innerText = password;
            startTimer();
        })
        .catch(error => {
            if (error.message === 'UNAUTHORIZED') {
                errorDiv.style.display = 'block';
            } else {
                console.error('Error revealing password:', error);
                alert('An error occurred while revealing the password.');
            }
        });
}

/**
 * Starts a countdown timer to auto-hide the revealed password
 */
function startTimer() {
    clearInterval(timerInterval);
    const timer = document.getElementById('timer');
    let timeLeft = 15;
    timer.innerText = timeLeft;

    timerInterval = setInterval(() => {
        timeLeft--;
        timer.innerText = timeLeft;
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            const modalElement = document.getElementById('passwordModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) modal.hide();
        }
    }, 1000);
}

/**
 * Copies revealed password to clipboard
 */
function copyToClipboard() {
    const password = document.getElementById('revealedPassword').innerText;
    navigator.clipboard.writeText(password).then(() => {
        const copyBtn = document.querySelector('.copy-btn i');
        if (copyBtn) {
            copyBtn.classList.replace('fa-copy', 'fa-check');
            setTimeout(() => copyBtn.classList.replace('fa-check', 'fa-copy'), 2000);
        }
    });
}

/**
 * Utility to show toast notifications
 */
function showToast(message) {
    const toastEl = document.getElementById('successToast');
    const toastMsgEl = document.getElementById('toastMessage');
    if (toastEl && toastMsgEl) {
        toastMsgEl.innerText = message;
        const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
        toast.show();
    }
}

/**
 * Toggles the favorite status of a credential (local UI only for now)
 */
function toggleFavorite(el, event) {
    event.stopPropagation();
    const isActive = el.classList.contains('active');

    el.classList.toggle('active');
    el.classList.toggle('text-muted');

    const row = el.closest('tr');
    if (row) {
        row.setAttribute('data-favorite', !isActive);
    }

    showToast(!isActive ? "Added to favorites" : "Removed from favorites");
}

/**
 * Filters the table to show only favorite items
 */
function filterFavorites() {
    const isChecked = document.getElementById('favoritesToggle').checked;
    const rows = document.querySelectorAll('.credential-row');

    rows.forEach(row => {
        if (isChecked) {
            row.style.display = (row.getAttribute('data-favorite') === 'true') ? '' : 'none';
        } else {
            row.style.display = '';
        }
    });
}

/**
 * Applies search, category, and sorting filters via URL redirect
 */
function applyFilters() {
    const query = document.querySelector('input[name="query"]').value;
    const category = document.getElementById('categoryFilter').value;
    const sortBy = document.getElementById('sortOption').value;

    let url = '/dashboard?';
    if (query) url += 'query=' + encodeURIComponent(query) + '&';
    if (category) url += 'category=' + encodeURIComponent(category) + '&';
    if (sortBy) url += 'sortBy=' + encodeURIComponent(sortBy);

    window.location.href = url;
}

/**
 * Opens deletion confirmation modal
 */
function openDeleteModal(btn) {
    currentDeleteId = btn.getAttribute('data-id');
    currentDeleteRow = btn.closest('tr');
    document.getElementById('deleteAccountName').innerText = btn.getAttribute('data-name');
    document.getElementById('deleteMasterPassword').value = '';
    document.getElementById('deleteError').style.display = 'none';

    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

/**
 * Submits the deletion request after local validation
 */
function confirmDeletion() {
    const password = document.getElementById('deleteMasterPassword').value;
    const errorDiv = document.getElementById('deleteError');

    if (!password) {
        errorDiv.innerText = "Master password is required";
        errorDiv.style.display = 'block';
        return;
    }

    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
    modal.hide();

    showToast("Verifying and deleting...");

    // Create a hidden form to submit the request
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/vault/delete';

    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
    }

    const idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.name = 'id';
    idInput.value = currentDeleteId;
    form.appendChild(idInput);

    const passwordInput = document.createElement('input');
    passwordInput.type = 'hidden';
    passwordInput.name = 'masterPassword';
    passwordInput.value = password;
    form.appendChild(passwordInput);

    document.body.appendChild(form);
    form.submit();
}

// Clean up timer on modal close
document.addEventListener('DOMContentLoaded', () => {
    const passwordModal = document.getElementById('passwordModal');
    if (passwordModal) {
        passwordModal.addEventListener('hidden.bs.modal', () => {
            clearInterval(timerInterval);
        });
    }
});
