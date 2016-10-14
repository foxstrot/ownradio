using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace OwnRadio.Client.Desktop
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        /// <summary>
        /// WebClient object shared among whole app
        /// </summary>
        public static OwnRadioWebApiClient WebClient { get; private set; } = new OwnRadioWebApiClient();

        /// <summary>
        /// Event handler fired then application started
        /// Generate new DeviceID if needed and save it to user's configuartion
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Application_Startup(object sender, StartupEventArgs e)
        {
            //
            // Check if we already generate DeviceId
            if(Desktop.Properties.Settings.Default.DeviceId == Guid.Empty)
            {
                Desktop.Properties.Settings.Default.DeviceId = Guid.NewGuid();
                Desktop.Properties.Settings.Default.Save();
            }
        }
    }
}
