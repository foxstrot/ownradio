using NLog;
using System;
using System.Configuration;

namespace OwnRadio.Client.Desktop
{
	// Настройки программы
	class Settings
	{
		// Путь к файлу на сервере
		public string serverAddress { get; set; }
		// Имя файла
		public string userId { get; set; }
		// Строка подключения
		public string connectionString { get; set; }
		private Logger log;

		// Конструктор - чтение настроек из конфигурации
		public Settings(Logger logger)
		{
			log = logger;
			try
			{
				userId = ConfigurationManager.AppSettings["userId"];
				serverAddress = ConfigurationManager.AppSettings["serverAddress"];
				connectionString = ConfigurationManager.ConnectionStrings["OwnradioDesktopClient"].ConnectionString;
			}
			catch(Exception ex)
			{
				log.Error(ex);
			}
		}

		// Сохранение настроек
		public void updateSettings()
		{
			try
			{
				var currentConfig = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
				currentConfig.AppSettings.Settings["userId"].Value = userId;
				currentConfig.AppSettings.Settings["serverAddress"].Value = serverAddress;
				currentConfig.Save(ConfigurationSaveMode.Modified);
				ConfigurationManager.RefreshSection("appSettings");
			}
			catch (Exception ex)
			{
				log.Error(ex);
			}
		}
	}
}
