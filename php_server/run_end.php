<?php
include 'config.php';

// Connect to the database
$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);

if (!$con) {
    die(json_encode(["error" => "Connection failed: " . mysqli_connect_error()]));
}

// Ensure POST method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    die(json_encode(["error" => "Invalid request method. Use POST."]));
}

// Parse JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Validate input
if (!isset($input['user_id']) || !isset($input['snail_id'])) {
    die(json_encode(["error" => "Missing user_id or snail_id"]));
}

$user_id = intval($input['user_id']);
$snail_id = intval($input['snail_id']);

// Update the snail's end_time to the current timestamp
$query = "UPDATE snail SET end_time = NOW() WHERE id = $snail_id AND user_id = $user_id";

if (mysqli_query($con, $query)) {
    if (mysqli_affected_rows($con) > 0) {
        echo json_encode(["message" => "Run ended successfully."]);
        // death achievements
        // Step 1: Count number of ended runs
        $deathCountQuery = "SELECT COUNT(*) as deaths FROM snail WHERE user_id = $user_id AND end_time IS NOT NULL";
        $deathResult = mysqli_query($con, $deathCountQuery);
        $deathRow = mysqli_fetch_assoc($deathResult);
        $deathCount = intval($deathRow['deaths']);

        // Step 2: Determine which achievements to check
        $achievementsToCheck = []; // hardcoded but it is what it is...
        if ($deathCount >= 1) $achievementsToCheck[] = 4;  // Die once
        if ($deathCount >= 5) $achievementsToCheck[] = 5;  // Die 5 times
        if ($deathCount >= 10) $achievementsToCheck[] = 6; // Die 10 times

        foreach ($achievementsToCheck as $achievementId) {
            // Step 3: Only insert if not already unlocked
            $checkQuery = "SELECT 1 FROM user_achievements WHERE user_id = $user_id AND achievement_id = $achievementId";
            $checkResult = mysqli_query($con, $checkQuery);

            if (mysqli_num_rows($checkResult) === 0) {
                $insertQuery = "INSERT INTO user_achievements (user_id, achievement_id) VALUES ($user_id, $achievementId)";
                mysqli_query($con, $insertQuery);
            }
        }
    } else {
        echo json_encode(["error" => "No matching record found or run already ended."]);
    }
} else {
    echo json_encode(["error" => "Failed to update: " . mysqli_error($con)]);
}

mysqli_close($con);
?>