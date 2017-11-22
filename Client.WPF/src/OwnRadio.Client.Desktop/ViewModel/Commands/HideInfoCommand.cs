using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace OwnRadio.Client.Desktop.ViewModel.Commands
{
	public class HideInfoCommand : ICommand
	{
		public event EventHandler CanExecuteChanged;
		public ViewModelUploader Uploader { get; set; }

		public HideInfoCommand(ViewModelUploader uploader)
		{
			Uploader = uploader;
		}

		public bool CanExecute(object parameter)
		{
			return true;
		}

		public void Execute(object parameter)
		{
			Uploader.Info = "Collapsed";
		}
	}
}
