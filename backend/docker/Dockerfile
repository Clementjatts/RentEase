FROM php:8.2-apache

# Install SQLite and PHP extensions
RUN apt-get update && apt-get install -y \
    sqlite3 \
    libsqlite3-dev \
    && docker-php-ext-install pdo pdo_sqlite

# Enable Apache mod_rewrite
RUN a2enmod rewrite

# Set working directory
WORKDIR /var/www/html

# Copy PHP files
COPY ../api/ /var/www/html/

# Set permissions
RUN chown -R www-data:www-data /var/www/html
