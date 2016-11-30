using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Configuration;
using System.Data.SQLite;
using System.Windows.Forms;
using System.IO;

namespace OwnRadio.Client.Desktop
{
	internal class DataAccessLayer
	{
		private readonly SQLiteConnection _connection;

		public DataAccessLayer()
		{
			try
			{
				var connectionString = ConfigurationManager.ConnectionStrings["OwnradioDesktopClient"].ConnectionString;

				var databaseFileName = connectionString.Split('=')[1];

				if (File.Exists(databaseFileName))
				{
					_connection = new SQLiteConnection(connectionString);
				}
				else
				{
					SQLiteConnection.CreateFile(databaseFileName);
					_connection = new SQLiteConnection(connectionString);

					var command = new SQLiteCommand("CREATE TABLE \"Files\" ( `ID` TEXT NOT NULL, `FileName` TEXT NOT NULL, `SubPath` TEXT, `Uploaded` INTEGER DEFAULT 0, PRIMARY KEY(`ID`) );", _connection);
					_connection.Open();
					command.ExecuteNonQuery();
					_connection.Close();
				}
			}
			catch (Exception ex)
			{
				MessageBox.Show(ex.Message, "Ошибка инициализации DAL");
			}
		}

		public int AddToQueue(MusicFile musicFile)
		{
			var rowsAffected = 0;
			try
			{
				_connection.Open();

				var commandSql = string.Format("INSERT INTO Files (ID, FileName, SubPath) VALUES ($fileGuid, $fileName, $filePath)");
				var cmd = new SQLiteCommand(commandSql, _connection);
				cmd.Parameters.AddWithValue("$fileGuid", musicFile.FileGuid.ToString());
				cmd.Parameters.AddWithValue("$fileName", musicFile.FileName);
				cmd.Parameters.AddWithValue("$filePath", musicFile.FilePath);

				rowsAffected += cmd.ExecuteNonQuery();

				_connection.Close();
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}

			return rowsAffected;
		}

		private bool Exist(string fileName)
		{
			var count = 0;
			try
			{
				var commandSql = string.Format("SELECT count(*) FROM Files WHERE FileName LIKE $fileName");
				var cmd = new SQLiteCommand(commandSql, _connection);
				cmd.Parameters.AddWithValue("$fileName", fileName);
				var result = cmd.ExecuteScalar();
				count = Convert.ToInt16(result);
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}

			return count > 0;
		}

		internal ObservableCollection<MusicFile> GetNotUploaded()
		{
			var files = new ObservableCollection<MusicFile>();

			try
			{
				_connection.Open();
				var commandSql = "SELECT * FROM Files";
				var cmd = new SQLiteCommand(commandSql, _connection);
				var reader = cmd.ExecuteReader();

				while (reader.Read())
				{
					var file = new MusicFile()
					{
						FileGuid = Guid.Parse((string)reader["ID"]),
						FileName = (string)reader["FileName"],
						FilePath = (string)reader["SubPath"],
						Uploaded = (long)reader["Uploaded"] > 0
					};

					files.Add(file);
				}

				_connection.Close();
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}
			return files;
		}

		internal bool MarkAsUploaded(MusicFile musicFile)
		{
			int count = 0;
			try
			{
				_connection.Open();
				var commandSql = string.Format("UPDATE Files SET Uploaded=1 WHERE ID LIKE $fileGuid");
				var cmd = new SQLiteCommand(commandSql, _connection);
				cmd.Parameters.AddWithValue("$fileGuid", musicFile.FileGuid.ToString());
				var result = cmd.ExecuteScalar();
				_connection.Close();

				musicFile.Uploaded = true;
				count = Convert.ToInt16(result);
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}

			return count > 0;
		}
	}
}
