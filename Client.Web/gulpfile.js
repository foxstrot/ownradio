'use strict';

var gulp = require('gulp');
var $ = require('gulp-load-plugins')({rename:{'gulp-combine-mq':'combMQ'}});

//var data = require('./data.json');
var app = './app/';
var build = './build/assets/';

gulp.task('sass',function(){
    gulp.src(app+'scss/*.scss')
        .pipe($.sass({style: 'compact'}))
        .on('error',error)
        .pipe($.inlineBase64({baseDir:app+'scss/',maxSize:14*1024,debug:false}))
        .pipe($.autoprefixer({browsers:['last 2 versions'],cascade:false}))
        .pipe($.combMQ())
        .pipe(gulp.dest(build+'css'))
        .pipe($.minifyCss())
        .pipe($.rename({suffix:'.min'}))
        .pipe(gulp.dest(build+'css'));
})


gulp.task('watch',function(){
    gulp.watch(app+'scss/*.scss', ['sass']);
})


function error(error){
    console.log(error.toString());
    this.emit('end');
}

