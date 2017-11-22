using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace OwnRadio.Client.Desktop.ViewModel.Commands
{
	public class ClearDbCommand : ICommand
	{
		public ViewModelUploader Uploader { get; set; }

		public ClearDbCommand(ViewModelUploader uploader)
		{
			Uploader = uploader;
		}

		public bool CanExecute(object parameter)
		{
			return !Uploader.IsUploading;
		}

		public void Execute(object parameter)
		{
			Uploader.ClearDatabase();
		}

		public event EventHandler CanExecuteChanged;
	}
}
