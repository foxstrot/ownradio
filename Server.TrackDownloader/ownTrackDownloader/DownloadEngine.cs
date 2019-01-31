using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace ownTrackDownloader
{
    public class DownloadEngine
    {
        public async void Start()
        {
            GlobalSettings.ReadFromJson();

            PageGetter pageGetter = new PageGetter();
            TracksGetter tracksGetter = new TracksGetter();
            TracksChecker tracksChecker = new TracksChecker();
            TrackUploader trackUploader = new TrackUploader();

            try
            {
                do
                {

                    //получаем контент страницы с нужного раздела
                    var pageContent = await pageGetter.NextPage(ZaycevRubric.top);

                    //вычленяем из ответа трэки
                    List<Track> tracks = tracksGetter.GetTracks(pageContent.ToString());

                    //удаляем из списка те трэки которые у нас уже есть
                    tracks = tracksChecker.CheckForNew(tracks);

                    if (tracks.Count > 0)
                    {
                        foreach (Track track in tracks)
                        {

                            // скачиваем сам файл
                            var audio = await tracksGetter.GetTrack(track.Url).ConfigureAwait(false);

                            //генерим UUID для базы ownRadio
                            track.Recid = trackUploader.Generate_UUID();

                            //закачиваем на сервак
                            trackUploader.SaveOnDisk(audio, track);

                            //вносим запись в бд таблицу tracks
                            trackUploader.BDTrackInsert(track);


                        }
                    }
                } while (true);
            }
            catch (Exception ex)
            {
                
            }
        }

        
    }
}
