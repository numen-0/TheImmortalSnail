<?php
// Invoke-WebRequest -Uri "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/iduenas003/WEB/user.php" `
//                    -Method POST `
//                    -Headers @{ "Content-Type" = "application/json" } `
//                    -Body '{"name": "john", "password": "admin"}'
// curl.exe "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/iduenas003/WEB/user.php?name=john&password=admin"
// Include the database configuration file
include 'config.php';

// Connect to MySQL using the constants from config.php
$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);

if (!$con) {
    die(json_encode(["error" => "Connection failed: " . mysqli_connect_error()]));
}

// Handle HTTP methods (POST for registration, GET for login)
$method = $_SERVER['REQUEST_METHOD'];

if ($method === "POST") {
    // Registration: Accept JSON input
    $input = json_decode(file_get_contents("php://input"), true);
    
    if (!isset($input['name']) || !isset($input['password'])) {
        die(json_encode(["error" => "Missing name or password"]));
    }

    $name = mysqli_real_escape_string($con, $input['name']);
    $password = password_hash($input['password'], PASSWORD_BCRYPT); // Secure password hashing

    // Check if the name already exists
    $check_query = "SELECT * FROM users WHERE name='$name'";
    $check_result = mysqli_query($con, $check_query);

    if (mysqli_num_rows($check_result) > 0) {
        echo json_encode(["error" => "Name already exists"]);
    } else {
        // Insert new name into the database
        $query = "INSERT INTO users (name, password) VALUES ('$name', '$password')";
        if (mysqli_query($con, $query)) {
            echo json_encode(["message" => "User registered successfully"]);
        } else {
            echo json_encode(["error" => "Registration failed: " . mysqli_error($con)]);
        }
    }
} elseif ($method === "GET") {
    // Login: Check name and password
    if (!isset($_GET['name']) || !isset($_GET['password'])) {
        die(json_encode(["error" => "Missing name or password"]));
    }

    $name = mysqli_real_escape_string($con, $_GET['name']);
    $password = $_GET['password'];

    $query = "SELECT * FROM users WHERE name='$name'";
    $result = mysqli_query($con, $query);
    $user_data = mysqli_fetch_assoc($result);

    if ($user_data && password_verify($password, $user_data['password'])) {
        echo json_encode([
            "message" => "Login successful",
            "user" => [
                "id" => $user_data['id'],
                "name" => $user_data['name']
            ]
        ]);
    } else {
        echo json_encode(["error" => "Invalid name or password"]);
    }
}

// Close connection
mysqli_close($con);
?>
