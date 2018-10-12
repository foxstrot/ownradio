using System.ComponentModel.DataAnnotations;

namespace Downloader
{
	internal class Track
	{
		[Key]
		public string Guid { get; set; }
		public string Url { get; set; }
        public string Title { get; set; }
        public string Artist { get; set; }
        public string Duration { get; set; }

    }
}
