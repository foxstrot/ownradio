using System;
using System.Windows.Forms;

namespace OwnRadio.Client.Desktop
{
	public partial class SettingsForm : Form
	{
		internal Settings settings { get; set; }
		public SettingsForm()
		{
			InitializeComponent();
		}

		private void buttonOK_Click(object sender, EventArgs e)
		{
			settings.userId = textBoxUserId.Text;
			settings.serverAddress = textBoxServerUri.Text;
			settings.updateSettings();
			Close();
		}

		private void buttonCancel_Click(object sender, EventArgs e)
		{
			Close();
		}

		private void SettingsForm_Shown(object sender, EventArgs e)
		{
			textBoxUserId.Text = settings.userId;
			textBoxServerUri.Text = settings.serverAddress;
		}
	}
}
