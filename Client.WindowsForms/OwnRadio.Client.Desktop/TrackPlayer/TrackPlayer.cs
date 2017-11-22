using NLog;
using System;
using WMPLib;

namespace OwnRadio.Client.Desktop
{
	class TrackPlayer : IDisposable
	{              
		private TrackSource trackSource;
		private TrackInfo currentTrackInfo;
		private Settings settings;
		public bool IsPause { private set; get; }
		private WindowsMediaPlayer player;
		private Logger log;

		public TrackPlayer(Logger logger)
		{
			log = logger;
			
			try
			{
				settings = new Settings(logger);
				trackSource = new TrackSource(settings, logger);
				IsPause = false;
				player = new WindowsMediaPlayer();
				player.PlayStateChange += windowsMediaPlayer_PlayStateChange;
				startPlay();
			}
			catch (Exception ex)
			{
				log.Error(ex.Message);
			}
		}

		private void windowsMediaPlayer_PlayStateChange(int NewState)
		{			
			if (NewState == (int)WMPPlayState.wmppsMediaEnded)
			{
				playNextTrack(true);                
			}
			if (NewState == (int)WMPPlayState.wmppsStopped)
			{
				player.controls.play();                
			}
		}

		private void startPlay()
		{
			try
			{
				currentTrackInfo = trackSource.GetFirstTrackInfo();        
				player.URL = trackSource.GetTrackPlayUrl(currentTrackInfo.TrackId);
			}   
			catch(Exception ex)
			{
				log.Error(ex.Message);
			}         
		}

		public void Resume()
		{
			if (IsPause)
			{
				player.controls.play();
				IsPause = false;               
			}          
		}

		public void Pause()
		{
			if (!IsPause)
			{
				player.controls.pause();
				IsPause = true;
			}
		}

		public void PlayNextTrack()
		{
			playNextTrack(false);
		}

		private void playNextTrack(bool listedTillTheEnd)
		{
			try
			{
				currentTrackInfo = trackSource.GetNextTrackInfo(currentTrackInfo.TrackId, currentTrackInfo.Method, listedTillTheEnd);
				player.URL = trackSource.GetTrackPlayUrl(currentTrackInfo.TrackId);
			}
			catch (Exception ex)
			{
				log.Error(ex.Message);
			}
		}

		public void Dispose()
		{
			player.close();
		}
	}
}
