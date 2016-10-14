using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace OwnRadio.Client.Desktop.ViewModel.Commands
{
    public class PauseCommand : ICommand
    {
        public event EventHandler CanExecuteChanged;
        private ViewModelPlayer Player { get; set; }

        public PauseCommand(ViewModelPlayer player)
        {
            Player = player;
        }

        public bool CanExecute(object parameter)
        {
            return true;
        }

        public void Execute(object parameter)
        {
            Player.Pause();
        }
    }
}
