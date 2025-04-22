<?php
define('DB_SERVER', '127.0.0.1');
define('DB_USER', 'Xiduenas003');
define('DB_PASS', '4DJBXyg6u');
define('DB_DATABASE', 'Xiduenas003_');

define('DEBUG_MODE', true);
// define('DEBUG_MODE', false);

if (defined('DEBUG_MODE') && DEBUG_MODE) {
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
} else {
    ini_set('display_errors', 0);
    // Optionally, log errors to a file instead of showing them
    // ini_set('log_errors', 1);
    // ini_set('error_log', '/path/to/your/error.log');
}
?>