namespace OwnRadio.Web.Api.Infrastructure
{
	public class Settings
    {
		// Строка подключения к БД
		public string connectionString { get; set; }
		// Папка для загрузки mp3 файлов
		public string uploadFolder { get; set; }
	}
}
