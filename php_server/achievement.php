<?php
include 'config.php';

$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);
if (!$con) {
    die(json_encode(["error" => "Connection failed: " . mysqli_connect_error()]));
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === "GET") {
    if (!isset($_GET['user_id'])) {
        die(json_encode(["error" => "Missing user_id"]));
    }

    $user_id = intval($_GET['user_id']);

    $query = "
        SELECT 
            a.id,
            a.en_desc,
            a.eu_desc,
            a.es_desc,
            CASE WHEN ua.user_id IS NOT NULL THEN 1 ELSE 0 END AS done
        FROM achievement a
        LEFT JOIN user_achievements ua ON a.id = ua.achievement_id AND ua.user_id = $user_id
    ";

    $result = mysqli_query($con, $query);
    $achievements = [];

    while ($row = mysqli_fetch_assoc($result)) {
        $achievements[] = [
            "id" => $row["id"],
            "en_desc" => $row["en_desc"],
            "eu_desc" => $row["eu_desc"],
            "es_desc" => $row["es_desc"],
            "done" => boolval($row["done"])
        ];
    }

    echo json_encode($achievements);

} elseif ($method === "POST") {
    $input = json_decode(file_get_contents("php://input"), true);

    if (!isset($input['user_id']) || !isset($input['achievement_id'])) {
        die(json_encode(["error" => "Missing user_id or achievement_id"]));
    }

    $user_id = intval($input['user_id']);
    $achievement_id = intval($input['achievement_id']);

    $query = "
        INSERT IGNORE INTO user_achievements (user_id, achievement_id)
        VALUES ($user_id, $achievement_id)
    ";

    if (mysqli_query($con, $query)) {
        echo json_encode(["message" => "Achievement recorded"]);
    } else {
        echo json_encode(["error" => "Failed to insert: " . mysqli_error($con)]);
    }
}

mysqli_close($con);
?>