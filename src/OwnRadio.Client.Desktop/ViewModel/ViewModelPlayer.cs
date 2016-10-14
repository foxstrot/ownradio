using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using OwnRadio.Client.Desktop.Model;
using OwnRadio.Client.Desktop.ViewModel.Commands;

namespace OwnRadio.Client.Desktop.ViewModel
{
    public class ViewModelPlayer : DependencyObject
    {
        public readonly MediaElement Player = new MediaElement();

        public PlayCommand PlayCommand { get; set; }
        public NextCommand NextCommand { get; set; }
        public PauseCommand PauseCommand { get; set; }
        
        public Track CurrentTrack
        {
            get { return (Track)GetValue(CurrentTrackProperty); }
            set { SetValue(CurrentTrackProperty, value); }
        }

        public static readonly DependencyProperty CurrentTrackProperty =
            DependencyProperty.Register("CurrentTrack", typeof(Track), typeof(ViewModelPlayer), new PropertyMetadata(null));
        
        public bool IsRunning
        {
            get { return (bool)GetValue(IsRunningProperty); }
            set { SetValue(IsRunningProperty, value); }
        }

        public static readonly DependencyProperty IsRunningProperty =
            DependencyProperty.Register("IsRunning", typeof(bool), typeof(ViewModelPlayer), new PropertyMetadata(false));
        
        public ViewModelPlayer()
        {
            this.PlayCommand = new PlayCommand(this);
            this.NextCommand = new NextCommand(this);
            this.PauseCommand = new PauseCommand(this);

            Player.LoadedBehavior = MediaState.Manual;
            Player.UnloadedBehavior = MediaState.Manual;
            Player.MediaEnded += delegate
            {
                try
                {
                    Stop();
                    CurrentTrack.ListenEnd = DateTime.Now;
                    CurrentTrack.Status = Track.Statuses.Listened;
                    App.WebClient.SendStatus(Properties.Settings.Default.DeviceId, CurrentTrack);

                    GetNextTrack();
                    Play();
                }
                catch (Exception exception)
                {
                    MessageBox.Show("Error: " + exception.Message);
                }
            };

            try
            {
                GetNextTrack();
                Play();
            }
            catch (Exception exception)
            {
                MessageBox.Show("Error: " + exception.Message);
            }
        }

        public void Play()
        {
            Player.Play();
            IsRunning = true;
        }

        public void Pause()
        {
            Player.Pause();
            IsRunning = false;
        }

        public void Stop()
        {
            Player.Stop();
            IsRunning = false;
        }

        public void Next()
        {
            try
            {
                Stop();
                CurrentTrack.ListenEnd = DateTime.Now;
                CurrentTrack.Status = Track.Statuses.Skipped;
                App.WebClient.SendStatus(Properties.Settings.Default.DeviceId, CurrentTrack);

                GetNextTrack();
                Play();
            }
            catch (Exception exception)
            {
                MessageBox.Show("Error: " + exception.Message);
            }
        }

        private void GetNextTrack()
        {
            CurrentTrack = App.WebClient.GetNextTrack(Properties.Settings.Default.DeviceId).Result;
            Player.Source = CurrentTrack.Uri;
        }
    }
}
