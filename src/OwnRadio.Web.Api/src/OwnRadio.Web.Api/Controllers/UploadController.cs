using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using OwnRadio.Web.Api.Infrastructure;
using System.IO;
using Microsoft.AspNetCore.Http;
using OwnRadio.Web.Api.Models;
using OwnRadio.Web.Api.Infrastructure;
using Microsoft.Extensions.Options;

namespace OwnRadio.Web.Api.Controllers
{
	[Route("api/[controller]")]
	public class UploadController : Controller
	{
		public Settings settings { get; }

		public UploadController(IOptions<Settings> settings)
		{
			this.settings = settings.Value;
		}

		// POST api/upload
		[MimeMultipart]
		// Получение и сохранение файла
		public async Task<FileUploadResult> Post(IFormFile musicFile, string fileGuid, string fileName, string filePath, string userId)
		{
			FileUploadResult result = null;
			try
			{
				// Формируем путь для загрузки файлов пользователя
				var uploadPath = Path.Combine(settings.uploadFolder, userId);
				// Если директория не существует, то создает ее
				if (!Directory.Exists(uploadPath))
					Directory.CreateDirectory(uploadPath);
				// Если длина файла не нулевая, то начинаем получение файла
				if (musicFile.Length > 0)
				{
					// Получаем полный путь файла для загрузки
					var fullFileName = Path.Combine(uploadPath, musicFile.FileName);
					using (var fileStream = new FileStream(fullFileName, FileMode.Create))
					{
						// Сохраняем файл
						await musicFile.CopyToAsync(fileStream);
					}
					// Создаем ответ клиенту
					result = new FileUploadResult
					{
						LocalFilePath = musicFile.FileName,
						FileName = Path.GetFileName(fullFileName),
						FileLength = new FileInfo(fullFileName).Length
					};
					// Формируем объект класса файл из полученных данных о файле
					var newMusicFile = new MusicFile(settings.connectionString)
					{
						id = Guid.Parse(fileGuid),
						localDevicePathUpload = Path.Combine(filePath,fileName),
						path = fullFileName,
						userId = Guid.Parse(userId)
					};
					// Добавляем в БД информацию о принятом файле
					newMusicFile.registerTrack();
				}
			}
			catch (Exception ex)
			{
				var message = ex.Message;
			}
			return result;
		}
	}
}
