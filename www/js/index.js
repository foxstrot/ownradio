/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onSuccess() {

}
function onError() {

}

var currentTrack;

function setPlayIcon() {
    var button = $("#play");
    button.removeClass("ui-icon-play");
    button.addClass("ui-icon-pause");
}
function setPauseIcon() {
    var button = $("#play");
    button.removeClass("ui-icon-pause");
    button.addClass("ui-icon-play");
}
function updateTrack(track)
{
    $("#trackName").text(track.name);
    setPlayIcon();
    currentTrack = track.id;
}

var app = {
    // Application Constructor
    initialize: function () {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
      //  this.createMusicControl();
        this.bindEvents();
        this.initializeService();
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

       // console.log('Received Event: ' + id);
    },
    initializeService: function(){
        dataService.initialize();
        this.playNextTrack();
    },
    playNextTrack: function(){
        dataService.getTrack(function(track){
            updateTrack(track);
        });
    },
    bindEvents: function(){
        var self = this;
        $("#play").on('click', function(){
            var button = $("#play");
            if(button.hasClass("ui-icon-play"))
                setPlayIcon();          
            else
                setPauseIcon();
        });

        $("#next").on('click', function(){
            self.playNextTrack();
        });

    },
    
    createMusicControl: function(){
        
        MusicControls.create({
            track       : 'Time is Running Out',        // optional, default : ''
            artist      : 'Muse',                     // optional, default : ''
        //    cover       : 'albums/absolution.jpg',      // optional, default : nothing
            // cover can be a local path (use fullpath 'file:///storage/emulated/...', or only 'my_image.jpg' if my_image.jpg is in the www folder of your app)
            //           or a remote url ('http://...', 'https://...', 'ftp://...')
            isPlaying   : true,                           // optional, default : true
            dismissable : true,                         // optional, default : false

            // hide previous/next/close buttons:
            hasPrev   : false,      // show previous button, optional, default: true
            hasNext   : false,      // show next button, optional, default: true
            hasClose  : true,       // show close button, optional, default: false

          // iOS only, optional
            album : 'Absolution',     // optional, default: ''
            duration : 60, // optional, default: 0
            elapsed : 10, // optional, default: 0

            // Android only, optional
            // text displayed in the status bar when the notification (and the ticker) are updated
            ticker    : 'Now playing "Time is Running Out"'
        }, onSuccess, onError);

        function events(action) {
            switch(action) {
                case 'music-controls-next':
                    // Do something
                    break;
                case 'music-controls-previous':
                    // Do something
                    break;
                case 'music-controls-pause':
                    // Do something
                    break;
                case 'music-controls-play':
                    // Do something
                    break;
                case 'music-controls-destroy':
                    // Do something
                    break;

            // External controls (iOS only)
            case 'music-controls-toggle-play-pause' :
                    // Do something
                    break;

                // Headset events (Android only)
                case 'music-controls-media-button' :
                    // Do something
                    break;
                case 'music-controls-headset-unplugged':
                    // Do something
                    break;
                case 'music-controls-headset-plugged':
                    // Do something
                    break;
                default:
                    break;
            }
        }

        // Register callback
        MusicControls.subscribe(events);
        MusicControls.updateIsPlaying(true);

        // Start listening for events
        // The plugin will run the events function each time an event is fired
        MusicControls.listen();
    }
};

app.initialize();

        
