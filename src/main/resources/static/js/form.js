/* 
   Shared JavaScript for Add/Edit Credential Forms
*/

/**
 * Generates a strong password using the backend API
 */
function generateStrongPassword() {
    const field = document.getElementById('passwordField');
    const originalType = field.type;

    // Briefly show the password while generating for user visibility
    field.type = 'text';

    fetch('/api/generate-password?length=18')
        .then(response => {
            if (!response.ok) throw new Error('Generation failed');
            return response.text();
        })
        .then(password => {
            field.value = password;

            // Visual feedback
            field.classList.add('is-valid');
            setTimeout(() => {
                field.classList.remove('is-valid');
                field.type = originalType;
            }, 2000);
        })
        .catch(error => {
            console.error('Password generation error:', error);
            alert('Could not generate password. Please try again.');
        });
}

/**
 * Toggles the visibility of the password field
 */
function toggleVisibility() {
    const field = document.getElementById('passwordField');
    const icon = document.getElementById('toggleIcon');

    if (field.type === "password") {
        field.type = "text";
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        field.type = "password";
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// Initializing tooltips or other UI enhancements if needed
document.addEventListener('DOMContentLoaded', function () {
    // Add any specific form initialization here
});
