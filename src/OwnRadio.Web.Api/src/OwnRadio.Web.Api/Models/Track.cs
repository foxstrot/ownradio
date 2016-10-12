using Npgsql;
using System;
using System.Data;

namespace OwnRadio.Web.Api.Models
{
	public class Track
    {
		// Строка подключения к БД
		private string connectionString;

		// Конструктор - инициализация данных
		public Track(string connectionString)
		{
			this.connectionString = connectionString;
		}

		// Получает путь на сервере к треку по его идентификатору
		internal string GetTrackPath(Guid trackID)
		{
			var trackPath = string.Empty;
			using (var npgSqlConnection = new NpgsqlConnection(connectionString))
			{
				// Создаем комманду - с регистром имени функции проблема: не видит
				var npgSqlCommand = new NpgsqlCommand();
				// Указываем имя хранимой процедуры (функции)
				npgSqlCommand.CommandText = "gettrackpathbyid";
				// Указываем подключение
				npgSqlCommand.Connection = npgSqlConnection;
				// Уточняем тип комманды - хранимая процедура
				npgSqlCommand.CommandType = CommandType.StoredProcedure;
				// Добавляем параметры
				npgSqlCommand.Parameters.AddWithValue("i_trackid", trackID);
				// Открываем соединение
				npgSqlConnection.Open();
				// Выполняем хранимую процедуру (функцию)
				trackPath = (string)npgSqlCommand.ExecuteScalar();
			}
			return trackPath;
		}

		// Получает идентификатор следующего трека для заданного идентификатором устройства
		internal Guid GetNextTrackID(Guid deviceID)
		{
			var nextTrackID = Guid.Empty;
			using (var npgSqlConnection = new NpgsqlConnection(connectionString))
			{
				// Создаем комманду - с регистром имени функции проблема: не видит
				var npgSqlCommand = new NpgsqlCommand();
				// Указываем имя хранимой процедуры (функции)
				npgSqlCommand.CommandText = "getnexttrackid";
				// Указываем подключение
				npgSqlCommand.Connection = npgSqlConnection;
				// Уточняем тип комманды - хранимая процедура
				npgSqlCommand.CommandType = CommandType.StoredProcedure;
				// Добавляем параметры
				npgSqlCommand.Parameters.AddWithValue("i_deviceid", deviceID);
				// Открываем соединение
				npgSqlConnection.Open();
				// Выполняем хранимую процедуру (функцию)
				nextTrackID = (Guid)npgSqlCommand.ExecuteScalar();
			}
			return nextTrackID;
		}

		// Устанавливает статус прослушивания трека
		internal int SetStatusTrack(Guid DeviceID, Guid TrackID, int IsListen, DateTime DateTimeListen)
		{
			var rowCount = 0;
			using (var npgSqlConnection = new NpgsqlConnection(connectionString))
			{
				// Создаем комманду - с регистром имени функции проблема: не видит
				var npgSqlCommand = new NpgsqlCommand();
				// Указываем имя хранимой процедуры (функции)
				npgSqlCommand.CommandText = "setstatustrack";
				// Указываем подключение
				npgSqlCommand.Connection = npgSqlConnection;
				// Уточняем тип комманды - хранимая процедура
				npgSqlCommand.CommandType = CommandType.StoredProcedure;
				// Добавляем параметры
				npgSqlCommand.Parameters.AddWithValue("i_deviceid", DeviceID);
				npgSqlCommand.Parameters.AddWithValue("i_trackid", TrackID);
				npgSqlCommand.Parameters.AddWithValue("i_islisten", IsListen);
				npgSqlCommand.Parameters.AddWithValue("i_datetimelisten", DateTimeListen);
				// Открываем соединение
				npgSqlConnection.Open();
				// Выполняем хранимую процедуру (функцию)
				rowCount = npgSqlCommand.ExecuteNonQuery();
			}
			return rowCount;
		}
    }
}
