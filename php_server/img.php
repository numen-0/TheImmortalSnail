<?php
$imgDir = __DIR__ . "/img";
$allowedExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'];

if (!file_exists($imgDir)) {
    mkdir($imgDir, 0755, true);
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') { // Upload logic
    if (!isset($_POST['snail_id']) || !isset($_FILES['image'])) {
        http_response_code(400);
        echo json_encode(["error" => "Missing snail_id or image file."]);
        exit;
    }

    $snailId = intval($_POST['snail_id']);
    $image = $_FILES['image'];

    if ($image['error'] !== UPLOAD_ERR_OK) {
        http_response_code(400);
        echo json_encode(["error" => "Upload error."]);
        exit;
    }

    $mime = mime_content_type($image['tmp_name']);
    $ext = null;

    switch ($mime) {
        case 'image/jpeg': $ext = 'jpg'; break;
        case 'image/png':  $ext = 'png'; break;
        case 'image/gif':  $ext = 'gif'; break;
        case 'image/webp': $ext = 'webp'; break;
        case 'image/bmp':  $ext = 'bmp'; break;
        default:
            http_response_code(415);
            echo json_encode(["error" => "Unsupported image type."]);
            exit;
    }

    $targetPath = "$imgDir/$snailId.$ext";

    // Delete old images with any extension for this snail
    foreach ($allowedExtensions as $e) {
        $old = "$imgDir/$snailId.$e";
        if (file_exists($old)) {
            unlink($old);
        }
    }

    if (move_uploaded_file($image['tmp_name'], $targetPath)) {
        echo json_encode(["message" => "Image uploaded successfully."]);
    } else {
        http_response_code(500);
        echo json_encode(["error" => "Failed to move uploaded file."]);
    }

} elseif ($method === 'GET') { // Fetch logic
    if (!isset($_GET['snail_id'])) {
        http_response_code(400);
        echo json_encode(["error" => "Missing snail_id."]);
        exit;
    }

    $snailId = intval($_GET['snail_id']);

    // Try finding an image with any allowed extension
    foreach ($allowedExtensions as $ext) {
        $path = "$imgDir/$snailId.$ext";
        if (file_exists($path)) {
            header("Content-Type: image/$ext");
            readfile($path);
            exit;
        }
    }

    // Fallback: default image
    $defaultPath = "$imgDir/default.png";
    if (file_exists($defaultPath)) {
        header("Content-Type: image/png");
        readfile($defaultPath);
    } else {
        http_response_code(404);
        echo json_encode(["error" => "Image not found."]);
    }
}
?>