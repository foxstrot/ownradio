using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace OwnRadio.Client.Desktop
{
	public class MusicFile : INotifyPropertyChanged
	{
		private bool _uploaded;
		public string FileName { get; set; }
		public string FilePath { get; set; }

		public string FullFilePath => $"{$"{FilePath}\\{FileName}"} - {(Uploaded ? "Загружено" : "В очереди")}";

		public Guid FileGuid { get; set; }

		public bool Uploaded
		{
			get { return _uploaded; }
			set
			{
				_uploaded = value;
				OnPropertyChanged();
				OnPropertyChanged("FullFilePath");
			}
		}

		public event PropertyChangedEventHandler PropertyChanged;

		protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
		{
			PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
		}
	}
}
