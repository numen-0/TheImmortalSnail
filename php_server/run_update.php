<?php
include 'config.php';

$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);
if (!$con) {
    die(json_encode(["error" => "Connection failed: " . mysqli_connect_error()]));
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    die(json_encode(["error" => "Invalid request method. Use POST."]));
}

$input = json_decode(file_get_contents("php://input"), true);

if (!isset($input['user_id']) || !isset($input['snail_id']) || !isset($input['distance'] || !isset($input['distane_from_user']))) {
    die(json_encode(["error" => "Missing required fields"]));
}

$user_id = intval($input['user_id']);
$snail_id = intval($input['snail_id']);
$distance = (float)$input['distance'];
$distance_from_user = (float)$input['distance_from_user'];

// Fetch current max/min to compare
$query = "SELECT max_distance, min_distance FROM snail WHERE id = $snail_id AND user_id = $user_id";
$result = mysqli_query($con, $query);

if (!$result || mysqli_num_rows($result) == 0) {
    die(json_encode(["error" => "Snail not found."]));
}

$row = mysqli_fetch_assoc($result);
$max = max($row['max_distance'], $distance_from_user);
$min = min($row['min_distance'], $distance_from_user);

$update = "UPDATE snail SET distance = $distance, max_distance = $max, min_distance = $min WHERE id = $snail_id AND user_id = $user_id";

if (mysqli_query($con, $update)) {
    echo json_encode(["message" => "Run updated successfully."]);
    // achievements:
    $achievementsToCheck = [];

    // Step 1.a: Re-fetch max_distance to verify what was saved
    $checkDistQuery = "SELECT max_distance FROM snail WHERE id = $snail_id AND user_id = $user_id";
    $checkResult = mysqli_query($con, $checkDistQuery);
    $checkRow = mysqli_fetch_assoc($checkResult);
    $maxDistance = floatval($checkRow['max_distance']);
    
    // Step 2.a: Distance Achievement thresholds
    if ($maxDistance >= 1000) $achievementsToCheck[] = 1;   // 1km
    if ($maxDistance >= 10000) $achievementsToCheck[] = 2;  // 10km
    if ($maxDistance >= 100000) $achievementsToCheck[] = 3; // 100km

    // Step 1.b: fetch
    $timeQuery = "SELECT TIMESTAMPDIFF(SECOND, start_time, NOW()) AS alive_seconds FROM snail WHERE id = $snail_id AND user_id = $user_id AND end_time IS NULL";
    $timeResult = mysqli_query($con, $timeQuery);
    // Step 2.b: Time Achievements thresholds
    if ($timeResult && mysqli_num_rows($timeResult) > 0) {
        $timeRow = mysqli_fetch_assoc($timeResult);
        $aliveSeconds = intval($timeRow['alive_seconds']);
        if ($aliveSeconds >= 3600) $achievementsToCheck[] = 7;   // 1h
        if ($aliveSeconds >= 36000) $achievementsToCheck[] = 8;  // 10h
        if ($aliveSeconds >= 360000) $achievementsToCheck[] = 9; // 100h
    }
    
    // Step 3: update
    foreach ($achievementsToCheck as $achievementId) {
        $existsQuery = "SELECT 1 FROM user_achievements WHERE user_id = $user_id AND achievement_id = $achievementId";
        $existsResult = mysqli_query($con, $existsQuery);
        if (mysqli_num_rows($existsResult) === 0) {
            $insertQuery = "INSERT INTO user_achievements (user_id, achievement_id) VALUES ($user_id, $achievementId)";
            mysqli_query($con, $insertQuery);
        }
    }
} else {
    echo json_encode(["error" => "Update failed: " . mysqli_error($con)]);
}

mysqli_close($con);
?>
