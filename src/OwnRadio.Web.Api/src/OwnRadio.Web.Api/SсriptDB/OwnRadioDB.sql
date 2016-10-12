-- Database: "ownRadio"

-- DROP DATABASE "ownRadio";

CREATE DATABASE "ownRadio"
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Регистрируем расширение uuid-ossp - генератор гуидов
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------

-- Table: ownuser

-- DROP TABLE ownuser;

CREATE TABLE ownuser
(
  id uuid NOT NULL,
  username character varying(100) NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ownuser
  OWNER TO postgres;
  
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Table: device

-- DROP TABLE device;

CREATE TABLE device
(
  id uuid NOT NULL,
  userid uuid,
  devicename character varying(100),
  CONSTRAINT pk_device PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (userid)
      REFERENCES ownuser (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.device
  OWNER TO postgres;

-- Index: fki_user

-- DROP INDEX fki_user;

CREATE INDEX fki_user
  ON device
  USING btree
  (userid);

----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Table: track

-- DROP TABLE track;

CREATE TABLE track
(
  id uuid NOT NULL,
  uploaduserid uuid NOT NULL,
  localdevicepathupload character varying(2048),
  path character varying(2048),
  uploaddatetime timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT pk_track PRIMARY KEY (id),
  CONSTRAINT fk_track_user FOREIGN KEY (uploaduserid)
      REFERENCES ownuser (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE track
  OWNER TO postgres;

-- Index: fki_track_user

-- DROP INDEX fki_track_user;

CREATE INDEX fki_track_user
  ON track
  USING btree
  (uploaduserid);
  
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Table: history

-- DROP TABLE history;

CREATE TABLE history
(
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  userid uuid NOT NULL,
  trackid uuid NOT NULL,
  listendatetime timestamp with time zone NOT NULL DEFAULT now(),
  islisten integer,
  method character varying(50),
  CONSTRAINT pk_history PRIMARY KEY (id),
  CONSTRAINT fk_history_track FOREIGN KEY (trackid)
      REFERENCES track (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_history_user FOREIGN KEY (userid)
      REFERENCES ownuser (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE history
  OWNER TO postgres;

-- Index: fki_history_track

-- DROP INDEX fki_history_track;

CREATE INDEX fki_history_track
  ON history
  USING btree
  (trackid);

-- Index: fki_history_user

-- DROP INDEX fki_history_user;

CREATE INDEX fki_history_user
  ON history
  USING btree
  (userid);
  
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Table: rating

-- DROP TABLE rating;

CREATE TABLE rating
(
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  userid uuid NOT NULL,
  trackid uuid NOT NULL,
  lastlistendatetime timestamp with time zone NOT NULL DEFAULT now(),
  ratingsum integer NOT NULL DEFAULT 0,
  CONSTRAINT pk_rating PRIMARY KEY (id),
  CONSTRAINT fk_rating_track FOREIGN KEY (trackid)
      REFERENCES track (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_rating_user FOREIGN KEY (userid)
      REFERENCES ownuser (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE rating
  OWNER TO postgres;
COMMENT ON TABLE rating
  IS 'Рейтинг';

-- Index: fki_rating_track

-- DROP INDEX fki_rating_track;

CREATE INDEX fki_rating_track
  ON rating
  USING btree
  (trackid);

-- Index: fki_rating_user

-- DROP INDEX fki_rating_user;

CREATE INDEX fki_rating_user
  ON public.rating
  USING btree
  (userid);

----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
-- Function: registertrack(uuid, character varying, character varying, uuid)

-- DROP FUNCTION registertrack(uuid, character varying, character varying, uuid);

CREATE OR REPLACE FUNCTION registertrack(
    i_trackid uuid,
    i_localdevicepathupload character varying,
    i_path character varying,
    i_userid uuid)
  RETURNS void AS
$BODY$
-- 
-- Функция добавляет запись о треке в таблицу треков и делает сопутствующие записи в
-- таблицу статистики прослушивания и рейтингов. Если пользователя, загружающего трек 
-- нет в базе, то он добавляется в таблицу пользователей.
--

-- Добавляем пользователя, если его еще не существует
INSERT INTO ownuser (id, username)
	SELECT i_userid, 'Anonymous new user'
		WHERE NOT EXISTS(SELECT * FROM ownuser WHERE id = i_userid);

-- Добавляем трек в базу данных
INSERT INTO track(id, localdevicepathupload, path, uploaduserid) 
	VALUES(i_trackid, i_localdevicepathupload, i_path, i_userid);

-- Добавляем запись о прослушивании трека в таблицу истории прослушивания
INSERT INTO history(userid, trackid, islisten)
	VALUES(i_userid, i_trackid, 1);

-- Добавляем запись в таблицу рейтингов
INSERT INTO rating(userid, trackid, ratingsum)
	VALUES(i_userid, i_trackid, 1);

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION registertrack(uuid, character varying, character varying, uuid)
  OWNER TO postgres;

----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
-- Function: getnexttrackid(uuid)

-- DROP FUNCTION getnexttrackid(uuid);

CREATE OR REPLACE FUNCTION getnexttrackid(i_deviceid uuid)
  RETURNS uuid AS
$BODY$

-- Получает id следующего трэка для пользователя по DeviceID


-- Добавляем устройство, если его еще не существует
INSERT INTO device (id, userid, devicename)
	SELECT i_deviceid, '12345678-1234-1234-1234-123456789012', 'Test user device'
		WHERE NOT EXISTS(SELECT id FROM device WHERE id = i_deviceid);


-- На данный момент возвращает случайный id трэка
SELECT id 
	FROM track 
	ORDER BY RANDOM()
	LIMIT 1

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION getnexttrackid(uuid)
  OWNER TO postgres;
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
-- Function: setstatustrack(uuid, uuid, integer, timestamp with time zone)

-- DROP FUNCTION setstatustrack(uuid, uuid, integer, timestamp with time zone);

CREATE OR REPLACE FUNCTION setstatustrack(
    i_deviceid uuid,
    i_trackid uuid,
    i_islisten integer,
    i_datetimelisten timestamp with time zone)
  RETURNS void AS
$BODY$

BEGIN
-- Устанавливает статус прослушивания трека: добавляет запись в таблицу статистики и обновляет рейтинг

INSERT INTO history(userid, trackid, listendatetime, islisten)
VALUES((SELECT userid FROM device WHERE id=i_deviceid LIMIT 1), i_trackid, i_datetimelisten, i_islisten);

-- Обновляем рейтинг трека и дату его последнего прослушивания, если указанный пользователь уже слушал этот трек
UPDATE rating 
SET ratingsum = ratingsum + i_islisten, lastlistendatetime = i_datetimelisten 
WHERE id = (SELECT id FROM rating WHERE trackid=i_trackid AND userid IN (SELECT userid FROM device WHERE id=i_deviceid LIMIT 1));

-- Если таблица была обновлена - выход из функции, иначе - добавим запись
IF found THEN
RETURN;
END IF;

-- Добавляем новую запись о прослушивании трека пользователем, если трек слушается пользователем впервые
INSERT INTO rating (userid, trackid, lastlistendatetime, ratingsum) 
SELECT (SELECT userid FROM device WHERE id=i_deviceid LIMIT 1), i_trackid, i_datetimelisten, i_islisten
WHERE NOT EXISTS (SELECT id FROM rating WHERE trackid=i_trackid AND userid IN (SELECT userid FROM device WHERE id=i_deviceid LIMIT 1));
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION setstatustrack(uuid, uuid, integer, timestamp with time zone)
  OWNER TO postgres;

  
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
-- Function: gettrackpathbyid(uuid)

-- DROP FUNCTION gettrackpathbyid(uuid);

CREATE OR REPLACE FUNCTION gettrackpathbyid(i_trackid uuid)
  RETURNS character varying AS
$BODY$

-- Возвращает путь к треку с заданным идентификатором 

SELECT path 
	FROM track
	WHERE id = i_trackid

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION gettrackpathbyid(uuid)
  OWNER TO postgres;
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	

INSERT INTO ownuser (id, username) VALUES ('12345678-1234-1234-1234-123456789012', 'Test User');
INSERT INTO device (id, userid, devicename) VALUES ('00000000-0000-0000-0000-000000000000', '12345678-1234-1234-1234-123456789012', 'TEST-USER-PC');

----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
-- Function: public.mergeuserid(uuid, uuid)

-- DROP FUNCTION public.mergeuserid(uuid, uuid);

CREATE OR REPLACE FUNCTION public.mergeuserid(
    i_useridold uuid,
    i_useridnew uuid)
  RETURNS void AS
$BODY$

BEGIN
-- Функция выполняет слияние по двум User ID одного пользователя

-- Объединяем устройства одного пользователя
UPDATE device SET userid = i_useridnew WHERE userid = i_useridold;

-- Заменяем старый ID пользователя на новый в таблице track
UPDATE track SET uploaduserid = i_useridnew WHERE uploaduserid = i_useridold;

-- Заменяем старый ID пользователя на новый в таблице history
UPDATE history SET userid = i_useridnew WHERE userid = i_useridold;

-- Удаляем записи, связанные с i_useridold или userid = i_useridnew из таблицы рейтинг
DELETE FROM rating WHERE userid = i_useridold OR userid = i_useridnew;

-- Добавляем в таблицу рейтинг статистику прослушиваний посчитанную по таблице история
INSERT INTO rating (userid, trackid, lastlistendatetime, ratingsum)
	SELECT i_useridnew, trackid, MAX(listendatetime), SUM(islisten) FROM history
		WHERE userid = i_useridold OR userid = i_useridnew
		GROUP BY trackid;
END;


$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.mergeuserid(uuid, uuid)
  OWNER TO postgres;

----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------	
-- Function: public.registerdevice(uuid, character varying, character varying)

-- DROP FUNCTION public.registerdevice(uuid, character varying, character varying);

CREATE OR REPLACE FUNCTION public.registerdevice(
    i_deviceid uuid,
    i_username character varying,
    i_devicename character varying)
  RETURNS void AS
$BODY$

BEGIN

-- Если ID устройства еще нет в БД
IF NOT EXISTS(SELECT id FROM device WHERE id = i_deviceid) THEN

-- Добавляем нового пользователя
INSERT INTO ownuser (id, username)
	VALUES(uuid_generate_v1mc(), i_username);

-- Добавляем новое устройство
INSERT INTO device (id, userid, devicename)
	SELECT i_deviceid, (SELECT id FROM ownuser WHERE username = i_username), i_devicename;
END IF;

END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.registerdevice(uuid, character varying, character varying)
  OWNER TO postgres;

----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
-- Function: public.renameuser(uuid, character varying)

-- DROP FUNCTION public.renameuser(uuid, character varying);

CREATE OR REPLACE FUNCTION public.renameuser(
    i_userid uuid,
    i_newusername character varying)
  RETURNS void AS
$BODY$

-- Переименовывает пользователя
UPDATE ownuser SET username = i_newusername
	WHERE id = i_userid;

	$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION public.renameuser(uuid, character varying)
  OWNER TO postgres;
  
----------------------------------------------------------------------------------------	
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
-- Function: public.getuserid(uuid)

-- DROP FUNCTION public.getuserid(uuid);

CREATE OR REPLACE FUNCTION public.getuserid(i_deviceid uuid)
  RETURNS uuid AS
$BODY$

-- Получает ID пользователя по DeviceID
SELECT userid FROM device WHERE id = i_deviceid;

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION public.getuserid(uuid)
  OWNER TO postgres;
  