/**
 * Функции для работы с формами авторизации и подсказками
 */

// Инициализация при загрузке документа
document.addEventListener('DOMContentLoaded', function() {
    // Инициализация подсказок для полей ввода, если они есть на странице
    initInputHelpers();
});

/**
 * Инициализирует выпадающие подсказки для полей ввода
 */
function initInputHelpers() {
    // Находим все поля с классом helper-input
    const helperInputs = document.querySelectorAll('.helper-input');
    
    helperInputs.forEach(input => {
        const helperId = input.getAttribute('data-helper');
        const helperButton = input.parentElement.querySelector('.helper-button');
        const helperContent = document.getElementById(helperId);
        
        if (helperButton && helperContent) {
            // Открытие подсказки при клике на кнопку
            helperButton.addEventListener('click', function(e) {
                e.preventDefault();
                toggleHelper(helperContent);
            });
            
            // Закрытие подсказки при клике вне поля и подсказки
            document.addEventListener('click', function(e) {
                if (!helperButton.contains(e.target) && 
                    !helperContent.contains(e.target)) {
                    helperContent.classList.remove('show');
                }
            });
            
            // Применение значения из подсказки
            const helperOptions = helperContent.querySelectorAll('.helper-option');
            helperOptions.forEach(option => {
                option.addEventListener('click', function() {
                    const value = this.getAttribute('data-value');
                    const description = this.textContent;
                    
                    input.value = value;
                    helperContent.classList.remove('show');
                    
                    // Фокусируемся на следующем поле, если это указано
                    const nextInputId = this.getAttribute('data-next');
                    if (nextInputId) {
                        const nextInput = document.getElementById(nextInputId);
                        if (nextInput) {
                            nextInput.focus();
                        }
                    }
                });
            });
        }
    });
}

/**
 * Переключает видимость выпадающей подсказки
 */
function toggleHelper(helperContent) {
    // Закрываем все открытые подсказки
    document.querySelectorAll('.helper-content').forEach(content => {
        if (content !== helperContent) {
            content.classList.remove('show');
        }
    });
    
    // Переключаем текущую подсказку
    helperContent.classList.toggle('show');
}

/**
 * Форматирует телефонный номер во время ввода
 */
function formatPhoneNumber(input) {
    // Удаляем все, кроме цифр
    let value = input.value.replace(/\D/g, '');
    
    // Форматируем номер телефона
    if (value.length > 0) {
        if (value.length <= 1) {
            value = '+7' + value;
        } else if (value.length <= 3) {
            value = '+7 (' + value.substring(1);
        } else if (value.length <= 6) {
            value = '+7 (' + value.substring(1, 4) + ') ' + value.substring(4);
        } else if (value.length <= 8) {
            value = '+7 (' + value.substring(1, 4) + ') ' + value.substring(4, 7) + '-' + value.substring(7);
        } else {
            value = '+7 (' + value.substring(1, 4) + ') ' + value.substring(4, 7) + '-' + 
                    value.substring(7, 9) + '-' + value.substring(9, 11);
        }
    }
    
    input.value = value;
}

/**
 * Проверяет валидность формы перед отправкой
 */
function validateAuthForm(form) {
    const usernameInput = form.querySelector('#username');
    const passwordInput = form.querySelector('#password');
    const confirmPasswordInput = form.querySelector('#confirmPassword');
    
    let isValid = true;
    
    // Проверка имени пользователя
    if (usernameInput && usernameInput.value.trim() === '') {
        showError(usernameInput, 'Введите имя пользователя');
        isValid = false;
    } else {
        clearError(usernameInput);
    }
    
    // Проверка пароля
    if (passwordInput && passwordInput.value === '') {
        showError(passwordInput, 'Введите пароль');
        isValid = false;
    } else {
        clearError(passwordInput);
    }
    
    // Проверка подтверждения пароля (если есть)
    if (confirmPasswordInput && passwordInput && 
        confirmPasswordInput.value !== passwordInput.value) {
        showError(confirmPasswordInput, 'Пароли не совпадают');
        isValid = false;
    } else if (confirmPasswordInput) {
        clearError(confirmPasswordInput);
    }
    
    return isValid;
}

/**
 * Показывает сообщение об ошибке для поля ввода
 */
function showError(input, message) {
    const formGroup = input.closest('.form-group');
    const errorDiv = formGroup.querySelector('.error-message') || 
                    document.createElement('div');
    
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    
    if (!formGroup.querySelector('.error-message')) {
        formGroup.appendChild(errorDiv);
    }
    
    input.classList.add('error');
}

/**
 * Очищает сообщение об ошибке для поля ввода
 */
function clearError(input) {
    const formGroup = input.closest('.form-group');
    const errorDiv = formGroup.querySelector('.error-message');
    
    if (errorDiv) {
        formGroup.removeChild(errorDiv);
    }
    
    input.classList.remove('error');
} 