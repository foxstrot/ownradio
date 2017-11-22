<?php
/**
 * The template for displaying the header
 */
?><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width">
    <title><?php bloginfo(); ?></title>
    <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500" rel="stylesheet">
    <link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/style.css?v2017.05.26">
    <script src="https://vk.com/js/api/openapi.js?146" type="text/javascript"></script>
    <script type="text/javascript">
        VK.init({
            apiId: 5978191,
            onlyWidgets: true
        });
    </script>
    <?php wp_head(); ?>
</head>

<body <?php body_class(); ?>>
    <div class="container">
        <header class="page-header">
            <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/logo.svg" width="170" height="120" alt="logo">
        </header>