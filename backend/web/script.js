// RentEase Web Portal JavaScript

// API Configuration
const API_BASE_URL = '';

// WebView detection and header management
function detectWebView() {
    const userAgent = navigator.userAgent || navigator.vendor || window.opera;
    
    // Check for Android WebView
    const isAndroidWebView = /wv/.test(userAgent) || 
                            /Android.*Version\/\d+\.\d+/.test(userAgent) ||
                            window.AndroidInterface !== undefined;
    
    // Check for iOS WebView
    const isIOSWebView = /(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(userAgent);
    
    return isAndroidWebView || isIOSWebView;
}

function setupConditionalHeader() {
    const appBar = document.querySelector('.app-bar');
    const content = document.querySelector('.content');
    
    if (detectWebView()) {
        // Hide the web app header when in WebView
        appBar.classList.add('webview-hidden');
        content.classList.add('webview-fullheight');
    }
}

// Load properties when page loads
document.addEventListener('DOMContentLoaded', function() {
    setupConditionalHeader();
    loadProperties();
});

async function loadProperties() {
    showLoading();
    hideError();

    try {
        const response = await fetch(`${API_BASE_URL}/properties`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success && data.data && data.data.properties) {
            displayProperties(data.data.properties);
            updateStats(data.data.properties.length);
        } else {
            throw new Error('Invalid response format');
        }
        
    } catch (error) {
        console.error('Error loading properties:', error);
        showError('Failed to load properties. Please check your connection and try again.');
    } finally {
        hideLoading();
    }
}

function displayProperties(properties) {
    const container = document.getElementById('propertiesContainer');
    const noPropertiesDiv = document.getElementById('noProperties');

    if (!properties || properties.length === 0) {
        container.innerHTML = '';
        noPropertiesDiv.style.display = 'block';
        return;
    }

    noPropertiesDiv.style.display = 'none';

    container.innerHTML = properties.map(property => {
        let imageSection = '';
        if (property.image_url) {
            imageSection = '<img src="' + property.image_url + '" alt="Property Image" onerror="this.parentElement.innerHTML=\'<div class=&quot;property-image-placeholder&quot;>No Image Available</div>\'">';
        } else {
            imageSection = '<div class="property-image-placeholder">No Image Available</div>';
        }

        let chipSection = '';
        if (property.furniture_type) {
            chipSection = '<div class="property-type-chip">' + escapeHtml(toTitleCase(property.furniture_type)) + '</div>';
        }

        return `
        <div class="property-card">
            <div class="property-image">
                ${imageSection}
                ${chipSection}
            </div>
            <div class="property-content">
                <div class="property-title">${escapeHtml(property.title || 'Untitled Property')}</div>
                <div class="property-price">$${property.price ? parseFloat(property.price).toFixed(0) : '0'}/month</div>
                <div class="property-location">
                    ${escapeHtml(property.address || 'Address not provided')} • ${property.bedroom_count || 0} bed • ${property.bathroom_count || 0} bath
                </div>
                ${property.description ?
                    `<div class="property-description">${escapeHtml(property.description)}</div>` : ''
                }
                ${property.landlord_name ?
                    `<div class="landlord-info">Listed by: ${escapeHtml(property.landlord_name)}</div>` : ''
                }
            </div>
        </div>
        `;
    }).join('');
}

function updateStats(count) {
    document.getElementById('propertyCount').textContent = count;
    document.getElementById('stats').style.display = 'block';
}

function showLoading() {
    document.getElementById('loading').style.display = 'block';
    document.getElementById('propertiesContainer').style.display = 'none';
    document.getElementById('noProperties').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('propertiesContainer').style.display = 'block';
}

function showError(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('error').style.display = 'block';
    document.getElementById('propertiesContainer').style.display = 'none';
    document.getElementById('noProperties').style.display = 'none';
}

function hideError() {
    document.getElementById('error').style.display = 'none';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function toTitleCase(str) {
    if (!str) return '';
    return str.toLowerCase().split(' ').map(word =>
        word.charAt(0).toUpperCase() + word.slice(1)
    ).join(' ');
}
