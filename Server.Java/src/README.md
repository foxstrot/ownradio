# Java сервер для OwnRadio 

Этот проект разрабатывается с целью тестирования и обкатки Java технологий  

Run
---
##### Запускаем с помощью maven
* $ mvn -Dupload.dir=c:\ -Dspring.profiles.active=dev -Dserver.port=8080 spring-boot:run
* $ mvn -Dupload.dir=c:\ -Dspring.profiles.active=prod -Dserver.port=8080 spring-boot:run

##### Запускаем упакованный jar 
* $ java -Dupload.dir=c:\ -jar ownradio.jar --spring.profiles.active=dev --server.port=8080
* $ java -Dupload.dir=c:\ -jar ownradio.jar --spring.profiles.active=prod --server.port=8080


Web API
---

### Загрузка трека на сервер

##### POST /api/v2/tracks
* `fileGuid` – UUID трека
* `fileName` – имя файла
* `filePath` - Локальный путь к файлу на пользовательском устройстве
* `deviceId` – UUID device
* `musicFile` – прикрепленный файл

##### HttpStatus
* `400, "Bad Request"` - Если пользователь ввел некорректные данные
* `201, "Created"` – если все ок
* `500, "Internal Server Error"` – если произошел сбой на сервере

### Получение трека с сервера
##### GET /api/v2/tracks/{trackId}
* `{trackId}` – UUID трека 

##### HttpStatus
* `200, "OK"` – в теле ответа будет лежать трек
* `404, "Not Found"` – если трек с таким recid не найден

### Получение следующего трека с сервера
##### GET /api/v2/tracks/{deviceId}/next
* `{deviceId}` – UUID девайса

##### HttpStatus
* `200, "OK"` – в теле ответа будет лежать UUID трека
* `404, "Not Found"` – если трек с таким recid не найден

### Сохранение истории треков
##### POST /api/v2/histories/{deviceId}/{trackId}
* `{trackId}` – UUID прослушанного трека
* `{deviceId}` – UUID устройства где был прослушан трек
* `lastListen` - Время последнего прослушивания или пропуска трека для данного пользователя
* `isListen` - Признак прослушан ли трек до конца:  1 - прослушан, -1 – нет
* `method` - Метод выбора трека 

##### HttpStatus
* `200, "OK"` – если все ок
* `500, "Internal Server Error"` – если произошел сбой на сервере
