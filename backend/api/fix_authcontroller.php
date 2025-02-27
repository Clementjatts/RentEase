<?php
// Get the contents of the AuthController file
$file = '/var/www/html/controllers/AuthController.php';
$contents = file_get_contents($file);

// Add the require statement after the class comment
$contents = preg_replace(
    '/\/\*\*\s*\* Auth Controller\s*\*\s*\* Handles authentication-related API requests\s*\*\/\s*/s',
    "/**\n * Auth Controller\n *\n * Handles authentication-related API requests\n */\n\n// Explicitly require the User model\nrequire_once __DIR__ . '/../models/User.php';\n\n",
    $contents
);

// Write the modified contents back to the file
file_put_contents($file, $contents);

echo "AuthController.php updated successfully.\n";
?>
