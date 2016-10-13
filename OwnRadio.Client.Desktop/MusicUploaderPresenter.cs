using NLog;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Windows.Forms;

namespace OwnRadio.Client.Desktop
{
	class MusicUploaderPresenter
	{
		// Слой доступа к данным
		private DataAccessLayer dal;
		// Настройки программы
		public Settings settings;
		// Очередь загрузки
		public List<MusicFile> uploadQueue;
		// Логгер
		private Logger log;

		public MusicUploaderPresenter(Logger logger)
		{
			log = logger;
			try
			{
				// Получаем настройки
				settings = new Settings(log);
				// Слой доступа к данным
				dal = new DataAccessLayer(settings, log);
				// загружаем сохраненную очередь
				uploadQueue = dal.getNotUploaded();
			}
			catch (Exception ex)
			{
				log.Error(ex);
			}
		}

		// Получает список файлов из папки и добавляет их в очередь загрузки
		public void getQueue(string path)
		{
			try
			{
				if (!string.IsNullOrEmpty(path))
				{
					// получаем список файлов
					List<string> filenames = new List<string>();
					getMusicFiles(path, ref filenames);
					// заполняем ListView файлами
					foreach (var file in filenames)
					{
						// Создаем объект - файл
						var musicFile = new MusicFile();
						// Получаем имя файла
						musicFile.fileName = Path.GetFileName(file);
						// Получаем относительный путь
						musicFile.filePath = Path.GetDirectoryName(file);
						// Присваиваем файлу идентификатор
						musicFile.fileGuid = Guid.NewGuid();
						if (dal.addToQueue(musicFile) > 0)
						{
							// Добавляем файл в очередь
							uploadQueue.Add(musicFile);
						}
					}
				}
			}
			catch (Exception ex)
			{
				log.Error(ex);
			}
		}

		// Получает список файлов из дирректории рекурсивно
		private void getMusicFiles(string sourceDirectory, ref List<string> filenames)
		{
			try
			{
				log.Debug("Сканируем папку " + sourceDirectory);
				var allFiles = Directory.EnumerateFiles(sourceDirectory);
				// Оставляем только mp3
				var musicFiles = allFiles.Where(s => s.Split('.')[s.Split('.').Count() - 1].ToLower().Equals("mp3"));
				// добавляем все mp3 файлы в список
				filenames.AddRange(musicFiles);
				log.Debug("Добавлено файлов: " + musicFiles.Count());
				log.Debug("Получаем список папок в папке: " + sourceDirectory);
				// получаем список папок в текущей папке
				var dirs = Directory.EnumerateDirectories(sourceDirectory);
				log.Debug("В текущей папке найдено вложенных папок: " + dirs.Count());
				// рекурсивно получаем список файлов и проходим вложенные папки
				foreach (var directory in dirs)
					getMusicFiles(directory, ref filenames);
			}
			catch (Exception ex)
			{
				log.Error(ex);
			}
		}
		// Загружает музыкальные файлоы на сервер
		public async void uploadFiles(IProgress<MusicFile> progress)
		{
			// Счетчик загруженных файлов
			try
			{
				foreach (var musicFile in uploadQueue.Where(s => !s.uploaded))
				{
					// Формируем полный путь к файлу
					var fullFileName = musicFile.filePath + "\\" + musicFile.fileName;
					// Открываем файловый поток
					var fileStream = File.Open(fullFileName, FileMode.Open);
					byte[] byteArray = new byte[fileStream.Length];
					fileStream.Read(byteArray, 0, (int)fileStream.Length);
					// Создаем Http клиент
					HttpClient httpClient = new HttpClient();
					// Создаем контент
					MultipartFormDataContent form = new MultipartFormDataContent();
					// Добавляем в заголовок идентификатор пользователя
					form.Headers.Add("userId", settings.userId);
					// Добавляем в тело данные о музыкальном файле
					form.Add(new StringContent(musicFile.fileGuid.ToString()), "fileGuid");
					form.Add(new StringContent(musicFile.fileName), "fileName");
					form.Add(new StringContent(musicFile.filePath), "filePath");
					form.Add(new StringContent(settings.userId), "userId");
					// добавляем музыкальный файл
					form.Add(new ByteArrayContent(byteArray, 0, byteArray.Count()), "musicFile", musicFile.fileGuid + ".mp3");
					// Выполняем асинхронный запрос к серверу
					HttpResponseMessage response = await httpClient.PostAsync(settings.serverAddress + "api/upload", form);
					// Если код не успешный генерируем исключение
					response.EnsureSuccessStatusCode();
					httpClient.Dispose();
					//string sd = response.Content.ReadAsStringAsync().Result;
					// Помечаем файл как отправленный
					dal.markAsUploaded(musicFile);
					// отправляем в окно статистики сообщение об отправленном файле
					progress.Report(musicFile);
					log.Debug("Отправлен файл " + musicFile.fileName);
				}
				MessageBox.Show("Файлы успешно загружены!");
			}
			catch (Exception ex)
			{
				log.Error(ex);
				MessageBox.Show("Произошла ошибка при попытке загрузки файлов!","Ошибка!");
			}
		}
	}
}
