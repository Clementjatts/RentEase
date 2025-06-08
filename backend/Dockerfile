FROM php:8.1-apache

# Install essential dependencies for RentEase API
RUN apt-get update && apt-get install -y \
    sqlite3 \
    libsqlite3-dev \
    && docker-php-ext-install pdo pdo_sqlite \
    && rm -rf /var/lib/apt/lists/*

# Enable required Apache modules
RUN a2enmod rewrite

# Set working directory
WORKDIR /var/www/html

# Create database and uploads directories with proper permissions
RUN mkdir -p /var/www/database /var/www/uploads/properties && \
    chown -R www-data:www-data /var/www/database /var/www/uploads && \
    chmod -R 755 /var/www/database /var/www/uploads

# Configure Apache for RentEase API
RUN echo '<VirtualHost *:80>\n\
    DocumentRoot /var/www/html\n\
    <Directory /var/www/html>\n\
        AllowOverride All\n\
        Require all granted\n\
    </Directory>\n\
</VirtualHost>' > /etc/apache2/sites-available/000-default.conf

# Set permissions
RUN chown -R www-data:www-data /var/www/html

# Start Apache
CMD ["apache2-foreground"]
