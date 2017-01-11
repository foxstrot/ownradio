using System;
using System.Windows;

namespace OwnRadio.Client.Desktop
{
	public partial class SettingsWindow : Window
	{
		public SettingsWindow(Guid guid)
		{
			InitializeComponent();
			NewGuid.Text = guid.ToString();
		}

		private void ChangeDeviceId(object sender, RoutedEventArgs e)
		{
			try
			{
				var guid = Guid.Parse(NewGuid.Text);

				Properties.Settings.Default.DeviceId = guid;
				Properties.Settings.Default.Save();

				MessageBox.Show("DeviceId успешно изменен!");

				Close();
			}
			catch (Exception ex)
			{
				MessageBox.Show(ex.Message);
				NewGuid.Focus();
			}
		}

		private void CloseWindow(object sender, RoutedEventArgs e)
		{
			Close();
		}
	}
}
