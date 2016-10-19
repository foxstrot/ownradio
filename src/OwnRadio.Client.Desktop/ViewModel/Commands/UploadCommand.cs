using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Windows.Forms;

namespace OwnRadio.Client.Desktop.ViewModel.Commands
{
    public class UploadCommand : ICommand
    {
        public event EventHandler CanExecuteChanged;
        public ViewModelUploader Uploader { get; set; }

        public UploadCommand(ViewModelUploader uploader)
        {
            Uploader = uploader;
        }

        public bool CanExecute(object parameter)
        {
	        return !Uploader.IsUploading;
        }

        public void Execute(object parameter)
        {
            Uploader.Upload();
        }
    }
}
