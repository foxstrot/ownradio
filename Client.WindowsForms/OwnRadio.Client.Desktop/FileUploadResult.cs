namespace OwnRadio.Client.Desktop
{
	// Класс - результат загрузки файла
	class FileUploadResult
	{
		// Путь к файлу на сервере
		public string LocalFilePath { get; set; }
		// Имя файла
		public string FileName { get; set; }
		// Длина файла
		public long FileLength { get; set; }
	}
}
