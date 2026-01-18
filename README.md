# 🌤️ Weather Viewer - Приложение для просмотра погоды

**Weather Viewer** — это веб-приложение для отслеживания погоды в ваших любимых городах. Пользователи могут регистрироваться, добавлять локации и просматривать текущую погоду.

## 🚀 Живая демо-версия

Приложение развернуто и доступно по адресу: **http://144.31.139.91:8080**

---

## 📋 Содержание

- [Технологический стек](#-технологический-стек)
- [Требования](#-требования)
- [Установка и запуск](#-установка-и-запуск)
  - [1. Клонирование репозитория](#1-клонирование-репозитория)
  - [2. Установка PostgreSQL](#2-установка-postgresql)
  - [3. Создание базы данных](#3-создание-базы-данных)
  - [4. Настройка конфигурации](#4-настройка-конфигурации)
  - [5. Установка Apache Tomcat](#5-установка-apache-tomcat)
  - [6. Сборка проекта](#6-сборка-проекта)
  - [7. Развертывание на Tomcat](#7-развертывание-на-tomcat)


---

## 🛠️ Технологический стек

### Backend
- **Java 11**
- **Spring MVC 5.3.30** — веб-фреймворк
- **Hibernate 5.6.15** — ORM для работы с базой данных
- **QueryDSL 5.1.0** — типобезопасные запросы к БД
- **PostgreSQL 42.7.1** — реляционная база данных
- **HikariCP 5.1.0** — пул соединений
- **Flyway 8.5.13** — миграции базы данных

### Security
- **BCrypt** — хеширование паролей
- **Собственная система сессий** (UUID-based, без Spring Security)

### Frontend
- **Thymeleaf 3.1.2** — шаблонизатор
- **HTML/CSS**
- **Bootstrap 5** (опционально, для улучшения UI)

### Build & Deploy
- **Maven** — система сборки
- **Apache Tomcat 9.x** — сервер приложений

---

## ✅ Требования

Перед установкой убедитесь, что на вашем компьютере установлены:

1. **Java Development Kit (JDK) 11 или выше**
   - Проверьте версию: `java -version`
   - Скачать: [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) или [OpenJDK](https://openjdk.org/)

2. **Apache Maven 3.6+**
   - Проверьте версию: `mvn -version`
   - Скачать: [Maven](https://maven.apache.org/download.cgi)

3. **PostgreSQL 12+**
   - Проверьте версию: `psql --version`
   - Скачать: [PostgreSQL](https://www.postgresql.org/download/)

4. **Apache Tomcat 9.x**
   - Скачать: [Tomcat 9](https://tomcat.apache.org/download-90.cgi)

5. **Git** (для клонирования репозитория)
   - Проверьте версию: `git --version`

---

## 📥 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/vlad-k0/weatherproject.git
cd WeatherProject
```

---

### 2. Установка PostgreSQL

#### macOS (через Homebrew)
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### Windows
1. Скачайте установщик с [официального сайта PostgreSQL](https://www.postgresql.org/download/windows/)
2. Запустите установщик и следуйте инструкциям
3. Запомните пароль для пользователя `postgres`

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

---

### 3. Создание базы данных

1. **Войдите в PostgreSQL:**

```bash
# macOS/Linux
psql -U postgres

# Windows (через PowerShell или Command Prompt)
# Найдите psql в меню "Пуск" или используйте pgAdmin
```

2. **Создайте пользователя и базу данных:**

```sql
-- Создание пользователя
CREATE USER weather_admin WITH PASSWORD 'your_secure_password';

-- Создание базы данных
CREATE DATABASE weather_db OWNER weather_admin;

-- Предоставление привилегий
GRANT ALL PRIVILEGES ON DATABASE weather_db TO weather_admin;

-- Выход
\q
```

3. **Проверьте подключение:**

```bash
psql -U weather_admin -d weather_db -h localhost
# Введите пароль, который вы указали выше
```

Если вход успешен, база данных настроена правильно!

---

### 4. Настройка конфигурации

1. **Скопируйте файл-пример конфигурации:**

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2. **Откройте `src/main/resources/application.yml` и заполните:**

```yaml
# Database Configuration
db:
  driver: org.postgresql.Driver
  url: jdbc:postgresql://localhost:5432/weather_db
  username: weather_admin
  password: your_secure_password  # Пароль, который вы создали в шаге 3

# Hibernate Configuration
hibernate:
  dialect: org.hibernate.dialect.PostgreSQLDialect
  show_sql: false          # Установите true для отладки
  hbm2ddl.auto: validate   # НЕ МЕНЯЙТЕ! Миграции управляются Flyway

# Connection Pool
connectionPool:
  size: 10

# Weather API Configuration
weather-api:
  key: YOUR_WEATHER_API_KEY  # Укажите ваш API ключ
  base-url: http://api.weatherapi.com/v1
```

3. **Получите API ключ для Weather API:**
   - Зарегистрируйтесь на [WeatherAPI.com](https://www.weatherapi.com/)
   - Скопируйте ваш бесплатный API ключ
   - Вставьте его в `weather-api.key` в `application.yml`

> ⚠️ **ВАЖНО:** Файл `application.yml` содержит конфиденциальные данные. **НЕ КОММИТЬТЕ** его в Git! Он уже добавлен в `.gitignore`.

---

### 5. Установка Apache Tomcat

#### Вариант A: Скачать вручную

1. **Скачайте Tomcat 9:**
   - Перейдите на [официальный сайт](https://tomcat.apache.org/download-90.cgi)
   - Скачайте архив `.tar.gz` (Linux/macOS) или `.zip` (Windows)

2. **Распакуйте архив:**

```bash
# macOS/Linux
tar -xzf apache-tomcat-9.*.tar.gz
mv apache-tomcat-9.* ~/apache-tomcat-9

# Windows
# Распакуйте архив в удобную папку, например C:\apache-tomcat-9
```

3. **Настройте права доступа (только для macOS/Linux):**

```bash
chmod +x ~/apache-tomcat-9/bin/*.sh
```

#### Вариант B: Через Homebrew (macOS)

```bash
brew install tomcat@9
```

---

### 6. Сборка проекта

1. **Убедитесь, что вы находитесь в корневой директории проекта:**

```bash
cd /path/to/WeatherProject
```

2. **Соберите WAR файл с помощью Maven:**

```bash
mvn clean package
```

Процесс сборки:
- Очистит предыдущие сборки (`clean`)
- Скомпилирует Java код
- Запустит миграции Flyway (при наличии)
- Создаст WAR файл в `target/WeatherProject.war`

> ✅ Успешная сборка завершится сообщением: **BUILD SUCCESS**

3. **Проверьте наличие WAR файла:**

```bash
ls -lh target/WeatherProject.war
```

---

### 7. Развертывание на Tomcat

1. **Остановите Tomcat** (если запущен):

```bash
# macOS/Linux
~/apache-tomcat-9/bin/shutdown.sh

# Windows
C:\apache-tomcat-9\bin\shutdown.bat
```

2. **Удалите старые файлы:**

```bash
# macOS/Linux
rm -rf ~/apache-tomcat-9/webapps/ROOT
rm -f ~/apache-tomcat-9/webapps/ROOT.war

# Windows
rmdir /S /Q C:\apache-tomcat-9\webapps\ROOT
del C:\apache-tomcat-9\webapps\ROOT.war
```

3. **Скопируйте WAR файл:**

```bash
# macOS/Linux
cp target/WeatherProject.war ~/apache-tomcat-9/webapps/ROOT.war

# Windows
copy target\WeatherProject.war C:\apache-tomcat-9\webapps\ROOT.war
```

4. **Запустите Tomcat:**

```bash
# macOS/Linux
~/apache-tomcat-9/bin/startup.sh

# Windows
C:\apache-tomcat-9\bin\startup.bat
```

---

### 8. Проверка работы приложения

1. **Подождите 10-15 секунд** для полного развертывания приложения

2. **Откройте браузер и перейдите по адресу:**

```
http://localhost:8080
```

Или напрямую на страницу авторизации:

```
http://localhost:8080/auth/login
```

3. **Проверьте логи Tomcat** (если возникли проблемы):

```bash
# macOS/Linux
tail -f ~/apache-tomcat-9/logs/catalina.out

# Windows
type C:\apache-tomcat-9\logs\catalina.out
```

---