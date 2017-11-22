using System;
using System.Windows;
using System.Windows.Input;
using OwnRadio.Client.Desktop.ViewModel;

namespace OwnRadio.Client.Desktop
{
	public partial class MainWindow : Window
	{
		public MainWindow()
		{
			InitializeComponent();

			OuterBorder.MouseLeftButtonDown += delegate { DragMove(); };
			BtnClose.Click += delegate { Close(); };
		}

		private void ChangeDeviceId(object sender, MouseButtonEventArgs e)
		{
			var settings = new SettingsWindow(Properties.Settings.Default.DeviceId);
			settings.ShowDialog();

			TbDeviceId.Text = $"DeviceId: {Properties.Settings.Default.DeviceId}";
		}
	}
}
