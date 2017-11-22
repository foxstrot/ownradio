namespace OwnRadio.Web.Api.Infrastructure
{
	public class FileUploadResult
	{
		public string LocalFilePath { get; set; }
		public string FileName { get; set; }
		public long FileLength { get; set; }
	}
}