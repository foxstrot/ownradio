# Java сервер для OwnRadio 

Адрес сервера api.ownradio.ru

Этот проект разрабатывается с целью тестирования и обкатки Java технологий  

Run
---
##### Запускаем с помощью maven
* $ mvn -Dupload.dir=c:\ -Dspring.profiles.active=dev -Dserver.port=8080 spring-boot:run
* $ mvn -Dupload.dir=c:\ -Dspring.profiles.active=prod -Dserver.port=8080 spring-boot:run

##### Запускаем упакованный jar 
* $ java -Dupload.dir=c:\ -jar ownradio.jar --spring.profiles.active=dev --server.port=8080
* $ java -Dupload.dir=c:\ -jar ownradio.jar --spring.profiles.active=prod --server.port=8080

Web API v3
---

### Загрузка трека на сервер

##### POST /v3/tracks
* `fileGuid` – UUID трека
* `filePath` - Полный локальный путь к файлу на пользовательском устройстве, включая имя файла (String)
* `deviceId` – UUID device
* `musicFile` – прикрепленный файл

##### HttpStatus
* `400, "Bad Request"` - Если пользователь ввел некорректные данные
* `201, "Created"` – если все ок
* `500, "Internal Server Error"` – если произошел сбой на сервере

### Получение трека с сервера
##### GET /v3/tracks/{trackId}
* `{trackId}` – UUID трека

##### HttpStatus
* `200, "OK"` – в теле ответа будет лежать трек
* `404, "Not Found"` – если трек с таким recid не найден

### Получение следующего трека с сервера
##### GET /v3/tracks/{deviceId}/next
* `{deviceId}` – UUID девайса

##### Response
Content-Type →application/json;charset=UTF-8
{
  "artist": "Artist",
  "length": "duration",
  "name": "Title",
  "methodid": "1",
  "id": "00000000-0000-0000-0000-000000000000"
}

##### HttpStatus
* `200, "OK"` – в теле ответа будет лежать UUID трека
* `404, "Not Found"` – если трек с таким recid не найден

### Сохранение истории треков
##### POST /v3/histories/{deviceId}/{trackId}
* `{trackId}` – UUID прослушанного трека
* `{deviceId}` – UUID устройства где был прослушан трек
* `lastListen` - Время последнего прослушивания или пропуска трека для данного пользователя ("yyyy-MM-ddTHH:mm:ss")
* `isListen` - Признак прослушан ли трек до конца:  1 - прослушан, -1 – нет (int)
* `methodid` - ID метода выбора трека (int), равен значению, полученному при получении данных для следующего трека

##### HttpStatus
* `200, "OK"` – если все ок
* `500, "Internal Server Error"` – если произошел сбой на сервере


Web API v4
---
### Загрузка трека на сервер

##### POST /v4/tracks
* `fileGuid` – UUID трека
* `filePath` - Полный локальный путь к файлу на пользовательском устройстве, включая имя файла (String)
* `deviceId` – UUID device
* `musicFile` – прикрепленный файл

##### HttpStatus
* `400, "Bad Request"` - Если пользователь ввел некорректные данные
* `201, "Created"` – если все ок
* `500, "Internal Server Error"` – если произошел сбой на сервере


### Получение трека с сервера
##### GET /v4/tracks/{trackId}
* `{trackId}` – UUID трека

##### HttpStatus
* `200, "OK"` – в теле ответа будет лежать трек
* `404, "Not Found"` – если трек с таким recid не найден


### Регистрация нового устройства
##### GET /v4/devices/{deviceId}/{deviceName}/registerdevice
* `{deviceId}` - UUID устройства
* `{deviceName}` - название устройства (может отсутствовать)
##### HttpStatus
* `200, "OK"` – если все ок
* `400, "Bad Request"` - Если пользователь ввел некорректные данные


### Получение следующего трека с сервера
##### GET /v4/tracks/{deviceId}/next
* `{deviceId}` – UUID девайса

##### Response
Content-Type →application/json;charset=UTF-8
{
  "artist": "Artist",
  "length": "duration",
  "name": "Title",
  "id": "00000000-0000-0000-0000-000000000000",
  "pathupload":"C:\\music\\track.mp3", 
  "timeexecute": "00:00:00.006238"
}

### Сохранение истории треков
##### POST /v4/histories/{deviceId}/{trackId}
* `{historyId}` - UUID записи истории в БД устройства
* `{trackId}` – UUID прослушанного трека
* `{deviceId}` – UUID устройства где был прослушан трек
* `lastListen` - Время последнего прослушивания или пропуска трека для данного пользователя ("yyyy-MM-ddTHH:mm:ss")
* `isListen` - Признак прослушан ли трек до конца:  1 - прослушан, -1 – нет (int)

##### HttpStatus
* `200, "OK"` – если все ок
* `208, "Already Reported"` - если история с таким UUID уже отдавалась
* `404, "Not found"` - если deviceId или trackid не найден
* `500, "Internal Server Error"` – если произошел сбой на сервере
