using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SQLite;
using System.Windows.Forms;
using System.IO;

namespace OwnRadio.Client.Desktop
{
	class DataAccessLayer
	{
		SQLiteConnection connection;
        
		public DataAccessLayer()
		{
			try
			{
			    var connectionString = ConfigurationManager.ConnectionStrings["OwnradioDesktopClient"].ConnectionString;
                
                var databaseFileName = connectionString.Split('=')[1];

				if (File.Exists(databaseFileName))
				{
					connection = new SQLiteConnection(connectionString);
				}
				else
				{
					SQLiteConnection.CreateFile(databaseFileName);
					connection = new SQLiteConnection(connectionString);

					var command = new SQLiteCommand("CREATE TABLE \"Files\" ( `ID` TEXT NOT NULL, `FileName` TEXT NOT NULL, `SubPath` TEXT, `Uploaded` INTEGER DEFAULT 0, PRIMARY KEY(`ID`) );", connection);
					connection.Open();
					command.ExecuteNonQuery();
					connection.Close();
				}
			}
			catch(Exception ex)
			{
				MessageBox.Show(ex.Message, "Ошибка инициализации DAL");
			}
		}
        
		public int AddToQueue(MusicFile musicFile)
		{
			int rowsAffected = 0;
			try
			{
				connection.Open();
				if (!Exist(musicFile.fileName))
				{
					var commandSQL = string.Format("INSERT INTO Files (ID, FileName, SubPath) VALUES ($fileGuid, $fileName, $filePath)");

					SQLiteCommand cmd = new SQLiteCommand(commandSQL, connection);
					cmd.Parameters.AddWithValue("$fileGuid", musicFile.fileGuid.ToString());
					cmd.Parameters.AddWithValue("$fileName", musicFile.fileName);
					cmd.Parameters.AddWithValue("$filePath", musicFile.filePath);

					rowsAffected += cmd.ExecuteNonQuery();
				}

				connection.Close();
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
				var commandSQL = string.Format("SELECT count(*) FROM Files WHERE FileName LIKE $fileName");
				SQLiteCommand cmd = new SQLiteCommand(commandSQL, connection);
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
        
		internal List<MusicFile> GetNotUploaded()
		{
			var files = new List<MusicFile>();

			try
			{
				connection.Open();
				var commandSQL = "SELECT * FROM Files";
				SQLiteCommand cmd = new SQLiteCommand(commandSQL, connection);
				var reader = cmd.ExecuteReader();

				while (reader.Read())
				{
					var file = new MusicFile()
					{
						fileGuid = Guid.Parse((string)reader["ID"]),
						fileName = (string)reader["FileName"],
						filePath = (string)reader["SubPath"],
						uploaded = (long)reader["Uploaded"] > 0
					};

					files.Add(file);
				}

				connection.Close();
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
				connection.Open();
				string commandSQL = string.Format("UPDATE Files SET Uploaded=1 WHERE ID LIKE $fileGuid");
				SQLiteCommand cmd = new SQLiteCommand(commandSQL, connection);
				cmd.Parameters.AddWithValue("$fileGuid", musicFile.fileGuid.ToString());
				var result = cmd.ExecuteScalar();
				connection.Close();

				musicFile.uploaded = true;
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
