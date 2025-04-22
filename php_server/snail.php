<?php
include 'config.php';

$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);

if (!$con) {
    die(json_encode(["error" => "Connection failed: " . mysqli_connect_error()]));
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === "POST") {
    // Create a new snail
    $input = json_decode(file_get_contents("php://input"), true);

    $required = ['name', 'user_id'];
    foreach ($required as $field) {
        if (!isset($input[$field])) {
            die(json_encode(["error" => "Missing field: $field"]));
        }
    }

    $name = mysqli_real_escape_string($con, $input['name']);
    $user_id = (int)$input['user_id'];

    $query = "INSERT INTO snail (name, start_time, distance, max_distance, min_distance, user_id)
              VALUES ('$name', NOW(), 0, 0, 999999999, $user_id)";

    if (mysqli_query($con, $query)) {
        echo json_encode([
            "message" => "Snail added successfully",
            "snail_id" => mysqli_insert_id($con)
        ]);
    } else {
        echo json_encode(["error" => "Insert failed: " . mysqli_error($con)]);
    }
}
elseif ($method === "GET") {
    // Get all snails for a user
    if (!isset($_GET['user_id'])) {
        die(json_encode(["error" => "Missing user_id"]));
    }

    $user_id = (int)$_GET['user_id'];
    $query = "SELECT * FROM snail WHERE user_id = $user_id";
    $result = mysqli_query($con, $query);

    $snails = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $snails[] = $row;
    }

    echo json_encode($snails);
}

mysqli_close($con);
?>