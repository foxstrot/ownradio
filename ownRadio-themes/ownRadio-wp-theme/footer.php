<?php
/**
 * The template for displaying the footer
 */
?>

<footer class="page-footer">
    <div class="page-footer__left-part">
        <a href="https://itunes.apple.com/app/ownradio/id1179868370?mt=8" class="btn appstore"></a>
        <a href="https://play.google.com/store/apps/details?id=ru.netvoxlab.ownradio" class="btn playmarket"></a>
    </div>
    <div class="page-footer__right-part">
        <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/pic.png" width="186" height="254" alt="phones">
    </div>
</footer>





<div id="vk_comments"></div>
<script type="text/javascript">
    VK.Widgets.Comments("vk_comments", {limit: 10, attach: "*",pageUrl: "https://vk.com/ownradio"});
</script>



</div><!-- container end-->
<?php wp_footer(); ?>

</body>
</html>
