--
-- Файл сгенерирован с помощью SQLiteStudio v3.3.3 в Пн май 31 14:43:27 2021
--
-- Использованная кодировка текста: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: users
CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT, password TEXT);
INSERT INTO users (id, login, password) VALUES (1, 'qwe', 'qwe');
INSERT INTO users (id, login, password) VALUES (2, '123', '123');

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
