# CloudFileStorage

CloudFileStorage - это облачная система хранения файлов, написанная на Java. Этот проект позволяет пользователям загружать, хранить и управлять своими файлами в облаке. Проект включает веб-интерфейс, созданный с использованием HTML, JavaScript и CSS.

## Особенности

- Загрузка и хранение файлов в облаке
- Веб-интерфейс
- Безопасное управление файлами
- Адаптивный дизайн

## Используемые технологии

- Java (56%)
- HTML (35%)
- JavaScript (7.7%)
- CSS (1.3%)

## Установка

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/mak7im-05/CloudFileStorage.git
   ```
2. Перейдите в директорию проекта:
   ```bash
   cd CloudFileStorage
   ```
3. создать .env в корневой директории, пример заполнения:
    ```angular2html
    MINIO_URL=url
    MINIO_ACCESS_KEY=accessKey
    MINIO_SECRET_KEY=secretKey
    MINIO_BUCKET_NAME=bucketName
    
    DB_URL=db_url
    DB_USERNAME=username
    DB_PASSWORD=password
    
    REDIS_HOST=host
    REDIS_PORT=port        
    ```
4.Соберите проект, используя предпочтительную IDE или инструмент сборки.

## Использование

1. Откройте веб-браузер и перейдите по адресу `http://localhost:8080`, чтобы получить доступ к приложению.