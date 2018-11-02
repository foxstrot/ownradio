using System.IO;

namespace AudioWebApi
{
    public interface IACRCloud
    {
        InfoResponse GetInfo(Stream fileStream);
    }
}
