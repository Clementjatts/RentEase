FROM php:8.1-apache

# Install dependencies and PHP extensions
RUN apt-get update && apt-get install -y \
    libzip-dev \
    zip \
    unzip \
    git \
    && docker-php-ext-install pdo pdo_mysql zip mysqli

# Configure PHP
RUN echo "error_reporting = E_ALL" >> /usr/local/etc/php/conf.d/docker-php-error-reporting.ini \
    && echo "display_errors = On" >> /usr/local/etc/php/conf.d/docker-php-error-reporting.ini \
    && echo "log_errors = On" >> /usr/local/etc/php/conf.d/docker-php-error-reporting.ini

# Enable Apache modules
RUN a2enmod rewrite
RUN a2enmod headers

# Set working directory
WORKDIR /var/www/html

# Create database directory with proper permissions
RUN mkdir -p /var/www/database && \
    chown -R www-data:www-data /var/www/database && \
    chmod -R 777 /var/www/database

# Install Composer
COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

# Set up Apache virtual host
RUN echo '<VirtualHost *:80>\n\
    DocumentRoot /var/www/html\n\
    <Directory /var/www/html>\n\
        Options Indexes FollowSymLinks\n\
        AllowOverride All\n\
        Require all granted\n\
    </Directory>\n\
    ErrorLog ${APACHE_LOG_DIR}/error.log\n\
    CustomLog ${APACHE_LOG_DIR}/access.log combined\n\
</VirtualHost>' > /etc/apache2/sites-available/000-default.conf

# Set permissions
RUN chown -R www-data:www-data /var/www/html

# Expose port 80
EXPOSE 80

# Start Apache in foreground
CMD ["apache2-foreground"]
