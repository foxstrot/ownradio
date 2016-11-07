using System;
using System.CodeDom;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Forms;
using OwnRadio.Client.Desktop.ViewModel.Commands;

namespace OwnRadio.Client.Desktop.ViewModel
{
    public class ViewModelUploader : DependencyObject
    {
        public UploadCommand UploadCommand { get; set; }
		public ContinueUploadCommand ContinueUploadCommand { get; set; }
		public HideInfoCommand HideInfoCommand { get; set; }
		public ShowInfoCommand ShowInfoCommand { get; set; }

		public bool IsUploading
        {
            get { return (bool)GetValue(IsUploadingProperty); }
	        set { SetValue(IsUploadingProperty, value); }
        }
		
        public static readonly DependencyProperty IsUploadingProperty =
            DependencyProperty.Register("IsUploading", typeof(bool), typeof(ViewModelUploader), new PropertyMetadata(false));
		
	    public bool IsUploaded
		{
			get { return (bool)GetValue(IsUploadedProperty); }
			set { SetValue(IsUploadedProperty, value); }
		}
		public static readonly DependencyProperty IsUploadedProperty =
			DependencyProperty.Register("IsUploaded", typeof(bool), typeof(ViewModelUploader), new PropertyMetadata(true));
		
		public string Status
        {
            get { return (string)GetValue(StatusProperty); }
            set { SetValue(StatusProperty, value); }
        }

        public static readonly DependencyProperty StatusProperty =
            DependencyProperty.Register("Status", typeof(string), typeof(ViewModelUploader), new PropertyMetadata(""));
		
		public string Message
		{
			get { return (string)GetValue(MessageProperty); }
			set { SetValue(MessageProperty, value); }
		}
		public static readonly DependencyProperty MessageProperty =
			DependencyProperty.Register("Message", typeof(string), typeof(ViewModelUploader), new PropertyMetadata(""));
		
		public string Info
		{
			get { return (string)GetValue(InfoProperty); }
			set { SetValue(InfoProperty, value); }
		}
		public static readonly DependencyProperty InfoProperty =
			DependencyProperty.Register("Info", typeof(string), typeof(ViewModelUploader), new PropertyMetadata("Collapsed"));
		
		private readonly DataAccessLayer _dal;
		
		public ObservableCollection<MusicFile> UploadQueue { get; set; }
        
        public ViewModelUploader()
        {
			UploadCommand = new UploadCommand(this);
			ContinueUploadCommand = new ContinueUploadCommand(this);
			HideInfoCommand = new HideInfoCommand(this);
			ShowInfoCommand = new ShowInfoCommand(this);
			
			try
            {
                _dal = new DataAccessLayer();
                UploadQueue = _dal.GetNotUploaded();
            }
            catch (Exception ex)
			{
				ShowMessage(ex.Message);
			}
		}

        public void GetQueue(string path)
        {
            try
            {
                if (string.IsNullOrEmpty(path)) return;

                var filenames = new List<string>();
                GetMusicFiles(path, ref filenames);

                foreach (var file in filenames)
                {
					var info = new FileInfo(file);
	                if (info.Length > Properties.Settings.Default.MaxTrackSize)
	                {
						System.Windows.MessageBox.Show($"{file} exceeds maximum size - {Properties.Settings.Default.MaxTrackSize} bytes");
						continue;
	                }

                    var musicFile = new MusicFile
                    {
                        FileName = Path.GetFileName(file),
                        FilePath = Path.GetDirectoryName(file),
                        FileGuid = Guid.NewGuid()
                    };

                    if (_dal.AddToQueue(musicFile) > 0)
                    {
                        UploadQueue.Add(musicFile);
                    }
                }
            }
            catch (Exception ex)
			{
				ShowMessage(ex.Message);
			}
        }
        
        private void GetMusicFiles(string sourceDirectory, ref List<string> filenames)
        {
            try
            {
                var allFiles = Directory.EnumerateFiles(sourceDirectory);
                var musicFiles = allFiles.Where(s => s.Split('.')[s.Split('.').Count() - 1].ToLower().Equals("mp3"));
                filenames.AddRange(musicFiles);

                var dirs = Directory.EnumerateDirectories(sourceDirectory);

                foreach (var directory in dirs)
                    GetMusicFiles(directory, ref filenames);
            }
            catch (Exception ex)
			{
				ShowMessage(ex.Message);
			}
        }

        public async void UploadFiles()
        {
            int queued = UploadQueue.Count(s => !s.Uploaded);
            int uploaded = 0;
			ShowMessage("Uploading..");
			Status = $"Uploaded: {uploaded}/{queued}";

			SetCurrentValue(IsUploadedProperty, false);
			SetCurrentValue(IsUploadingProperty, true);
			SetCurrentValue(InfoProperty, "Visible");

			try
            {
	            foreach (var musicFile in UploadQueue.Where(s => !s.Uploaded))
                {
	                if (await IsExist(musicFile.FileGuid))
	                {
						_dal.MarkAsUploaded(musicFile);
						Status = $"Uploaded: {queued - UploadQueue.Count(s => !s.Uploaded)}/{queued}";
						++uploaded;
						continue;
	                }
					
					var fullFileName = musicFile.FilePath + "\\" + musicFile.FileName;
                    
                    var fileStream = File.Open(fullFileName, FileMode.Open);
                    var byteArray = new byte[fileStream.Length];
                    fileStream.Read(byteArray, 0, (int)fileStream.Length);
                    fileStream.Close();
					
                    var httpClient = new HttpClient();
	                var form = new MultipartFormDataContent
	                {
		                {new StringContent(musicFile.FileGuid.ToString()), "fileGuid"},
		                {new StringContent(musicFile.FileName), "fileName"},
		                {new StringContent(musicFile.FilePath), "filePath"},
		                {new StringContent(ConfigurationManager.AppSettings["DeviceId"]), "deviceId"},
		                {new ByteArrayContent(byteArray, 0, byteArray.Count()), "musicFile", musicFile.FileGuid + ".mp3"}
	                };
					
	                var response = await httpClient.PostAsync(@"http://java.ownradio.ru/api/v2/tracks", form);
					
					response.EnsureSuccessStatusCode();
                    httpClient.Dispose();

                    _dal.MarkAsUploaded(musicFile);
					Status = $"Uploaded: {queued - UploadQueue.Count(s => !s.Uploaded)}/{queued}";
	                ++uploaded;
                }
				
				ShowMessage(uploaded > 0 ? "Files uploaded successfully" : "Empty queue");
            }
            catch (Exception ex)
            {
				ShowMessage(ex.Message);
            }
			
			SetCurrentValue(IsUploadedProperty, true);
			SetCurrentValue(IsUploadingProperty, false);
		}
        
        public void Upload()
        {
			SetCurrentValue(InfoProperty, "Visible");
			var dialog = new FolderBrowserDialog();
            if (dialog.ShowDialog() != DialogResult.OK)
            {
                IsUploading = false;
	            IsUploaded = true;
                return;
            }

			GetQueue(dialog.SelectedPath);
			UploadFiles();
        }

	    private async Task<bool> IsExist(Guid guid)
	    {
			var httpClient = new HttpClient();

		    var response = await httpClient.GetAsync($"http://java.ownradio.ru/api/v2/tracks/{guid}");

		    return response.IsSuccessStatusCode;
	    }

	    private void ShowMessage(string message)
	    {
		    Message = message;
		}
    }
}
