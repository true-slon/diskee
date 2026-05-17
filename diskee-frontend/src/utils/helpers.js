export function validateName(name) {
  if (!name || !name.trim()) {
    return 'Имя не может быть пустым';
  }
  if (name.length > 255) {
    return 'Максимальная длина 255 символов';
  }
  const allowedChars = /^[a-zA-Zа-яА-Я0-9 _-]+$/;
  if (!allowedChars.test(name)) {
    return 'Допустимы только буквы, цифры, пробелы, дефисы, подчёркивания';
  }
  return null;
}

export function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 Б';
  const k = 1024;
  const sizes = ['Б', 'КБ', 'МБ', 'ГБ'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

export function formatDate(dateString) {
  return new Date(dateString).toLocaleString('ru-RU');
}