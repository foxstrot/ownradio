-- Database: "ownRadioJava"

-- DROP DATABASE "ownRadioJava";

CREATE DATABASE "ownRadioJava"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'ru_RU.UTF-8'
       LC_CTYPE = 'ru_RU.UTF-8'
       CONNECTION LIMIT = -1;
	   

-- Table: public.users

DROP TABLE IF EXISTS public.users CASCADE;

CREATE TABLE public.users
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  CONSTRAINT users_pkey PRIMARY KEY (recid)
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.users
  OWNER TO postgres;
     
-- Table: public.devices

DROP TABLE IF EXISTS public.devices CASCADE;

CREATE TABLE public.devices
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  userid uuid,
  CONSTRAINT devices_pkey PRIMARY KEY (recid),
  CONSTRAINT fk9xjj6x9ueb7id644i4ycukpug FOREIGN KEY (userid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkrfbri1ymrwywdydc4dgywe1bt FOREIGN KEY (userid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.devices
  OWNER TO postgres;
  
 
-- Table: public.tracks

DROP TABLE IF EXISTS public.tracks CASCADE;

CREATE TABLE public.tracks 
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  localdevicepathupload character varying(255) NOT NULL,
  path character varying(255),
  deviceid uuid,
  uploaduserid uuid,
  artist character varying(255),
  iscensorial integer,
  iscorrect integer,
  isfilledinfo integer,
  length integer,
  size integer,
  CONSTRAINT tracks_pkey PRIMARY KEY (recid),
  CONSTRAINT fk4n44h9fs1to11otqj5ek7xtus FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk7901v2785f03qrr9ruiwy7nd FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkfp7ki0smfcrvbvfjdnddxi1fb FOREIGN KEY (uploaduserid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.tracks
  OWNER TO postgres;

 
-- Table: public.downloadtracks

DROP TABLE IF EXISTS public.downloadtracks;

CREATE TABLE public.downloadtracks
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  deviceid uuid,
  trackid uuid,
  CONSTRAINT download_tracks_pkey PRIMARY KEY (recid),
  CONSTRAINT fkcsqwol33buwhcijea4w2ty5k2 FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkerttthmbxldbrlonworltybaq FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkf38s60by3ys41nkrvo0wpghqu FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fklkvcfwa2nxrdfs6q20x3slfsk FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.downloadtracks
  OWNER TO postgres;
  
  
  
-- Table: public.histories

   DROP TABLE IF EXISTS public.histories;

CREATE TABLE public.histories
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  islisten integer NOT NULL,
  lastlisten timestamp without time zone NOT NULL,
  method character varying(255),
  deviceid uuid,
  trackid uuid,
  userid uuid,
  methodid integer,
  CONSTRAINT histories_pkey PRIMARY KEY (recid),
  CONSTRAINT fk66xoney4xhu7rp7yxwye0tuw4 FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk6kk9amb55jghcg30cxstw4yw FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk8w9eva74w7t7xtf2opb33f8bq FOREIGN KEY (userid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkbc0htpqvevq196g2vpa9ipkci FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkbjn2i4ry8qwwp12wwbq4n96aa FOREIGN KEY (deviceid)
      REFERENCES public.devices (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.histories
  OWNER TO postgres;

  
-- Table: public.ratings

   DROP TABLE IF EXISTS public.ratings;

CREATE TABLE public.ratings
(
  recid uuid NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  lastlisten timestamp without time zone NOT NULL,
  ratingsum integer NOT NULL,
  trackid uuid,
  userid uuid,
  CONSTRAINT ratings_pkey PRIMARY KEY (recid),
  CONSTRAINT fk1wogw2je0eguqyvbegwgqwmku FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk9obht0874ty4owpd9a3hqa7gr FOREIGN KEY (userid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb3354ee2xxvdrbyq9f42jdayd FOREIGN KEY (userid)
      REFERENCES public.users (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkkewx1qhpt2egcdq7x92cv63p7 FOREIGN KEY (trackid)
      REFERENCES public.tracks (recid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE,
  autovacuum_enabled=true
);
ALTER TABLE public.ratings
  OWNER TO postgres;

-- Index: public.idx_lastlisten

-- DROP INDEX public.idx_lastlisten;

CREATE INDEX idx_lastlisten
  ON public.ratings
  USING btree
  (lastlisten);

-- Index: public.idx_trackid

-- DROP INDEX public.idx_trackid;

CREATE INDEX idx_trackid
  ON public.ratings
  USING btree
  (trackid);

-- Index: public.idx_userid

-- DROP INDEX public.idx_userid;

CREATE INDEX idx_userid
  ON public.ratings
  USING btree
  (userid);
  
   
-- Table: public.ratios

   DROP TABLE IF EXISTS public.ratios;

CREATE TABLE public.ratios
(
  recid uuid NOT NULL DEFAULT uuid_generate_v4(),
  userid1 uuid NOT NULL,
  userid2 uuid NOT NULL,
  ratio integer,
  CONSTRAINT ratios_pkey PRIMARY KEY (recid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.ratios
  OWNER TO postgres;


-- Table: public.logs

   DROP TABLE IF EXISTS public.logs;

CREATE TABLE public.logs
(
  recid bytea NOT NULL,
  reccreated timestamp without time zone,
  recname character varying(255),
  recupdated timestamp without time zone,
  deviceid bytea,
  logtext text,
  response text,
  CONSTRAINT logs_pkey PRIMARY KEY (recid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.logs
  OWNER TO postgres;

-- Table: public.execution_start_time

   DROP TABLE IF EXISTS public.execution_start_time;

CREATE TABLE public.execution_start_time
(
  timeofday timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.execution_start_time
  OWNER TO postgres;

-- Table: public.rnd

   DROP TABLE IF EXISTS public.rnd;

CREATE TABLE public.rnd
(
  "?column?" double precision
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.rnd
  OWNER TO postgres;


-- Function: public.getnexttrack(uuid)

-- DROP FUNCTION public.getnexttrack(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrack(IN i_deviceid uuid)
  RETURNS TABLE(track character varying, methodid integer, useridrecommended character varying, txtrecommendedinfo character varying) AS
$BODY$
DECLARE
  i_userid UUID = i_deviceid; -- в дальнейшем заменить получением userid по deviceid
BEGIN
  -- Добавляем устройство, если его еще не существует
  -- Если ID устройства еще нет в БД
  IF NOT EXISTS(SELECT recid
      FROM devices
      WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;

  -- Возвращаем trackid, конвертируя его в character varying и methodid
  RETURN QUERY SELECT
       CAST((nexttrack.track) AS CHARACTER VARYING),
       nexttrack.methodid,
       CAST((nexttrack.useridrecommended) AS CHARACTER VARYING),
       nexttrack.txtrecommendedinfo
     FROM getnexttrackid_v10(i_deviceid) AS nexttrack;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrack(uuid)
  OWNER TO postgres;


  
-- Function: public.getnexttrackid(uuid)

-- DROP FUNCTION public.getnexttrackid(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid(i_deviceid uuid)
  RETURNS SETOF uuid AS
$BODY$
DECLARE
  i_userid UUID = i_deviceid;
BEGIN
  -- Добавляем устройство, если его еще не существует
  -- Если ID устройства еще нет в БД
  IF NOT EXISTS(SELECT recid
          FROM devices
          WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
                       i_userid,
                       'New user recname',
                       now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
                       i_deviceid,
                       i_userid,
                       'New device recname',
                       now();
  ELSE
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;

  RETURN QUERY
  SELECT tracks.recid
  FROM tracks
    LEFT JOIN
    ratings
      ON tracks.recid = ratings.trackid AND ratings.userid = i_userid
  WHERE ratings.ratingsum >= 0 OR ratings.ratingsum IS NULL
  ORDER BY RANDOM()
  LIMIT 1;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid(uuid)
  OWNER TO postgres;


  
-- Function: public.getnexttrackid_string(uuid)

-- DROP FUNCTION public.getnexttrackid_string(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_string(i_deviceid uuid)
  RETURNS SETOF character varying AS
$BODY$
BEGIN
  RETURN QUERY SELECT CAST(getnexttrackid(i_deviceid) AS CHARACTER VARYING);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_string(uuid)
  OWNER TO postgres;


  
-- Function: public.getnexttrackid_v2(uuid)

-- DROP FUNCTION public.getnexttrackid_v2(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v2(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer) AS
$BODY$
DECLARE
	i_userid uuid = i_deviceid;
	rnd integer = (select trunc(random() * 10)); -- получаем случайное число от 0 до 9
    o_methodid integer; -- id метода выбора трека
BEGIN

  -- Выбираем следующий трек

  -- В 9/10 случаях выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
  -- с положительным рейтингом, за исключением прослушанных за последние сутки
	IF (rnd > 1)
	THEN
		o_methodid = 2;
		RETURN QUERY
		SELECT trackid, o_methodid
          FROM ratings
          WHERE userid = i_userid
            AND lastlisten < localtimestamp - interval '1 day'
            AND ratingsum >= 0
          ORDER BY RANDOM()
          LIMIT 1;

		-- Если такой трек найден - выход из функции, возврат найденного значения
		IF FOUND
	      THEN RETURN;
		END IF;
	END IF;

	-- В 1/10 случае выбираем случайный трек из ни разу не прослушанных пользователем треков
	o_methodid = 3;
	RETURN QUERY
	SELECT recid, o_methodid
      FROM tracks
      WHERE recid NOT IN
		(SELECT trackid
		FROM ratings
		WHERE userid = i_userid)
      ORDER BY RANDOM()
      LIMIT 1;

  -- Если такой трек найден - выход из функции, возврат найденного значения
	IF FOUND
	THEN RETURN;
	END IF;

	-- Если предыдущие запросы вернули null, выбираем случайный трек
	o_methodid = 1;
	RETURN QUERY
	SELECT recid, o_methodid
	  FROM tracks
      ORDER BY RANDOM()
      LIMIT 1;
	RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v2(uuid)
  OWNER TO postgres;

  
-- Function: public.getnexttrackid_v3(uuid)

-- DROP FUNCTION public.getnexttrackid_v3(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v3(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer) AS
$BODY$
DECLARE
	i_userid   uuid = i_deviceid;
	rnd        integer = (select trunc(random() * 1001));
	o_methodid integer; -- id метода выбора трека
    owntracks integer; -- количество "своих" треков пользователя (обрезаем на 900 шт)
BEGIN
	-- Выбираем следующий трек

	-- Определяем количество "своих" треков пользователя, ограничивая его 900
	owntracks = (SELECT COUNT(*) FROM (
		SELECT * FROM ratings
			WHERE userid = i_userid
					AND ratingsum >=0
			LIMIT 900) AS count) ;

	-- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
	-- с положительным рейтингом, за исключением прослушанных за последние сутки

	IF (rnd < owntracks)
	THEN
		o_methodid = 2; -- метод выбора из своих треков
		RETURN QUERY
		SELECT trackid, o_methodid
          FROM ratings
          WHERE userid = i_userid
                AND lastlisten < localtimestamp - interval '1 day'
                AND ratingsum >= 0
		ORDER BY RANDOM()
		LIMIT 1;

		-- Если такой трек найден - выход из функции, возврат найденного значения
		IF FOUND
		THEN RETURN;
		END IF;
	END IF;

	-- В 1/10 случае выбираем случайный трек из ни разу не прослушанных пользователем треков
	o_methodid = 3; -- метод выбора из непрослушанных треков
	RETURN QUERY
	SELECT recid, o_methodid
      FROM tracks
      WHERE recid NOT IN
            (SELECT trackid
             FROM ratings
             WHERE userid = i_userid)
    ORDER BY RANDOM()
	LIMIT 1;

	-- Если такой трек найден - выход из функции, возврат найденного значения
	IF FOUND
	  THEN RETURN;
	END IF;

	-- Если предыдущие запросы вернули null, выбираем случайный трек
	o_methodid = 1; -- метод выбора случайного трека
	RETURN QUERY
	SELECT recid, o_methodid
      FROM tracks
      ORDER BY RANDOM()
    LIMIT 1;
    RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v3(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v5(uuid)

-- DROP FUNCTION public.getnexttrackid_v5(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v5(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer) AS
$BODY$
DECLARE
	i_userid   UUID = i_deviceid;
	rnd        INTEGER = (SELECT trunc(random() * 1001));
	o_methodid INTEGER; -- id метода выбора трека
	owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
BEGIN
	-- Выбираем следующий трек

	-- Определяем количество "своих" треков пользователя, ограничивая его 900
	owntracks = (SELECT COUNT(*)
				 FROM (
						  SELECT *
						  FROM ratings
						  WHERE userid = i_userid
								AND ratingsum >= 0
						  LIMIT 900) AS count);

	-- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
	-- с положительным рейтингом, за исключением прослушанных за последние сутки

	IF (rnd < owntracks)
	THEN
		o_methodid = 2; -- метод выбора из своих треков
		RETURN QUERY
		SELECT
			trackid,
			o_methodid
		FROM ratings
		WHERE userid = i_userid
			  AND lastlisten < localtimestamp - INTERVAL '1 day'
			  AND ratingsum >= 0
			  AND (SELECT isexist
				   FROM tracks
				   WHERE recid = trackid) = 1
			  AND ((SELECT length
					FROM tracks
					WHERE recid = trackid) >= 120
				   OR (SELECT length
					   FROM tracks
					   WHERE recid = trackid) IS NULL)
			  AND ((SELECT iscensorial
					FROM tracks
					WHERE recid = trackid) IS NULL
				   OR (SELECT iscensorial
					   FROM tracks
					   WHERE recid = trackid) != 0)
			  AND trackid NOT IN (SELECT trackid
								  FROM downloadtracks
								  WHERE reccreated > localtimestamp - INTERVAL '1 day')
		ORDER BY RANDOM()
		LIMIT 1;

		-- Если такой трек найден - выход из функции, возврат найденного значения
		IF FOUND
		THEN RETURN;
		END IF;
	END IF;

	-- Если rnd больше количества "своих" треков - выбираем случайный трек из ни разу не прослушанных пользователем треков
	o_methodid = 3; -- метод выбора из непрослушанных треков
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE recid NOT IN
		  (SELECT trackid
		   FROM ratings
		   WHERE userid = i_userid)
		  AND isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
		  AND recid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
	ORDER BY RANDOM()
	LIMIT 1;

	-- Если такой трек найден - выход из функции, возврат найденного значения
	IF FOUND
	THEN RETURN;
	END IF;

	-- Если предыдущие запросы вернули null, выбираем случайный трек
	o_methodid = 1; -- метод выбора случайного трека
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
	ORDER BY RANDOM()
	LIMIT 1;
	RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v5(uuid)
  OWNER TO postgres;
  
  
  
-- Function: public.getnexttrackid_v6(uuid)

-- DROP FUNCTION public.getnexttrackid_v6(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v6(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer) AS
$BODY$
DECLARE
	i_userid   UUID = i_deviceid;
	rnd        INTEGER = (SELECT trunc(random() * 1001));
	o_methodid INTEGER; -- id метода выбора трека
	owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
	arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
BEGIN
	-- Выбираем следующий трек

	-- Определяем количество "своих" треков пользователя, ограничивая его 900
	owntracks = (SELECT COUNT(*)
				 FROM (
						  SELECT *
						  FROM ratings
						  WHERE userid = i_userid
								AND ratingsum >= 0
						  LIMIT 900) AS count);

	-- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
	-- с положительным рейтингом, за исключением прослушанных за последние сутки

	IF (rnd < owntracks)
	THEN
		o_methodid = 2; -- метод выбора из своих треков
		RETURN QUERY
		SELECT
			trackid,
			o_methodid
		FROM ratings
		WHERE userid = i_userid
			  AND lastlisten < localtimestamp - INTERVAL '1 day'
			  AND ratingsum >= 0
			  AND (SELECT isexist
				   FROM tracks
				   WHERE recid = trackid) = 1
			  AND ((SELECT length
					FROM tracks
					WHERE recid = trackid) >= 120
				   OR (SELECT length
					   FROM tracks
					   WHERE recid = trackid) IS NULL)
			  AND ((SELECT iscensorial
					FROM tracks
					WHERE recid = trackid) IS NULL
				   OR (SELECT iscensorial
					   FROM tracks
					   WHERE recid = trackid) != 0)
			  AND trackid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
		ORDER BY RANDOM()
		LIMIT 1;

		-- Если такой трек найден - выход из функции, возврат найденного значения
		IF FOUND
		THEN RETURN;
		END IF;
	END IF;

	-- Если rnd больше количества "своих" треков - рекомендуем трек из треков пользователя с наибольшим 
	-- коэффициентом схожести интересов и наибольшим рейтингом прослушивания

	-- Выберем всех пользователей с неотрицательным коэффициентом схожести интересов для i_userid
	-- отсортировав по убыванию коэффициентов
	arrusers = (SELECT ARRAY (SELECT CASE WHEN userid1 = i_userid THEN userid2
							WHEN userid2 = i_userid THEN userid1
							ELSE NULL
						END
						FROM ratios
						WHERE userid1 = i_userid OR userid2 = i_userid
							AND ratio >= 0
						ORDER BY ratio DESC
						));
	-- Выбираем пользователя i, с которым у него максимальный коэффициент. Среди его треков ищем трек 
	-- с максимальным рейтингом прослушивания, за исключением уже прослушанных пользователем i_userid. 
	-- Если рекомендовать нечего - берем следующего пользователя с наибольшим коэффициентом из оставшихся.
	FOR i IN 1.. (SELECT COUNT (*) FROM unnest(arrusers)) LOOP
		o_methodid = 4; -- метод выбора из рекомендованных треков
		RETURN QUERY
		SELECT
			trackid,
			o_methodid
			FROM ratings
			WHERE userid = arrusers[i]
				AND ratingsum > 0
				AND trackid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
				AND trackid NOT IN (SELECT trackid 
							FROM downloadtracks
							WHERE deviceid = i_deviceid
								AND reccreated > localtimestamp - INTERVAL '1 day')
				AND (SELECT isexist
					   FROM tracks
					   WHERE recid = trackid) = 1
				AND ((SELECT length
						FROM tracks
						WHERE recid = trackid) >= 120
					   OR (SELECT length
						   FROM tracks
						   WHERE recid = trackid) IS NULL)
				AND ((SELECT iscensorial
						FROM tracks
						WHERE recid = trackid) IS NULL
					   OR (SELECT iscensorial
						   FROM tracks
						   WHERE recid = trackid) != 0)
			ORDER BY ratingsum DESC
			LIMIT 1;
	-- Если нашли что рекомендовать - выходим из функции
		IF found THEN
		RETURN;
		END IF;
	END LOOP;
	
	-- При отсутствии рекомендаций, выдавать случайный трек из непрослушанных треков с неотрицательным 
	-- рейтингом среди пользователей со схожим вкусом.
	FOR i IN 1.. (SELECT COUNT (*) FROM unnest(arrusers)) LOOP
		o_methodid = 5; -- метод выбора из непрослушанных треков с неотрицательным рейтингом среди пользователей со схожим вкусом
		RETURN QUERY
		SELECT
			recid,
			o_methodid
			FROM tracks
			WHERE recid NOT IN (SELECT trackid FROM ratings WHERE userid = arrusers[i] AND ratingsum < 0)
				AND recid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
				AND isexist = 1
				AND (iscensorial IS NULL OR iscensorial != 0)
				AND (length > 120 OR length IS NULL)
				AND recid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
			ORDER BY RANDOM()
			LIMIT 1;
	-- Если нашли что рекомендовать - выходим из функции			
		IF found THEN
		RETURN;
		END IF;
	END LOOP;

	-- Если таких треков нет - выбираем случайный трек из ни разу не прослушанных пользователем треков
	o_methodid = 3; -- метод выбора из непрослушанных треков
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE recid NOT IN
		  (SELECT trackid
		   FROM ratings
		   WHERE userid = i_userid)
		  AND isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
		  AND recid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
	ORDER BY RANDOM()
	LIMIT 1;

	-- Если такой трек найден - выход из функции, возврат найденного значения
	IF FOUND
	THEN RETURN;
	END IF;

	-- Если предыдущие запросы вернули null, выбираем случайный трек
	o_methodid = 1; -- метод выбора случайного трека
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
	ORDER BY RANDOM()
	LIMIT 1;
	RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v6(uuid)
  OWNER TO postgres;

  
-- Function: public.registertrack(uuid, character varying, character varying, uuid)

-- DROP FUNCTION public.registertrack(uuid, character varying, character varying, uuid);

CREATE OR REPLACE FUNCTION public.registertrack(
    i_trackid uuid,
    i_localdevicepathupload character varying,
    i_path character varying,
    i_deviceid uuid)
  RETURNS boolean AS
$BODY$
DECLARE
  i_userid    UUID = i_deviceid;
  i_historyid UUID;
  i_ratingid  UUID;
BEGIN
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  SELECT uuid_generate_v4()
  INTO i_historyid;
  SELECT uuid_generate_v4()
  INTO i_ratingid;

  --
  -- Функция добавляет запись о треке в таблицу треков и делает сопутствующие записи в
  -- таблицу статистики прослушивания и рейтингов. Если пользователя, загружающего трек
  -- нет в базе, то он добавляется в таблицу пользователей.
  --

  -- Добавляем устройство, если его еще не существует
  -- Если ID устройства еще нет в БД
  IF NOT EXISTS(SELECT recid
          FROM devices
          WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;

  -- Добавляем трек в базу данных
  INSERT INTO tracks (recid, localdevicepathupload, path, deviceid, reccreated, iscensorial, isexist)
  VALUES (i_trackid, i_localdevicepathupload, i_path, i_deviceid, now(), 2, 1);

  -- Добавляем запись о прослушивании трека в таблицу истории прослушивания
  INSERT INTO histories (recid, deviceid, trackid, isListen, lastListen, reccreated)
  VALUES (i_historyid, i_deviceid, i_trackid, 1, now(), now());

  -- Добавляем запись в таблицу рейтингов
  INSERT INTO ratings (recid, userid, trackid, lastListen, ratingsum, reccreated)
  VALUES (i_ratingid, i_userid, i_trackid, now(), 1, now());

  RETURN TRUE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.registertrack(uuid, character varying, character varying, uuid)
  OWNER TO postgres;

  
-- Function: public.selectdownloadhistory(uuid)

-- DROP FUNCTION public.selectdownloadhistory(uuid);

CREATE OR REPLACE FUNCTION public.selectdownloadhistory(IN i_deviceid uuid)
  RETURNS TABLE(recid uuid, reccreated timestamp without time zone, recname character varying, recupdated timestamp without time zone, deviceid uuid, trackid uuid, isstatisticback integer) AS
$BODY$
  BEGIN
    -- Выводит список треков по которым не была отдана история прослушивания для данного устройства
    RETURN QUERY  SELECT * FROM downloadtracks
    WHERE 
      downloadtracks.trackid NOT IN
        (SELECT histories.trackid FROM histories WHERE histories.deviceid = i_deviceid)
        AND downloadtracks.deviceid = i_deviceid;

  END;
  $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.selectdownloadhistory(uuid)
  OWNER TO postgres;
  
-- Function: public.calculateratios()

-- DROP FUNCTION public.calculateratios();

CREATE OR REPLACE FUNCTION public.calculateratios()
  RETURNS boolean AS
$BODY$
DECLARE
  -- объявляем курсор и запрос для него
    curs1 CURSOR FOR SELECT * FROM(
        -- рассчитываем матрицу коэффициентов схожести интересов для каждой пары пользователей
        SELECT r.userid as userid01, r2.userid as userid02, 
              SUM(r.ratingsum * r2.ratingsum) as s
      --  SUM(CASE WHEN r.ratingsum > 0 AND r2.ratingsum > 0 THEN r.ratingsum * r2.ratingsum
--          WHEN r.ratingsum < 0 AND r2.ratingsum < 0 THEN 0
--          ELSE r.ratingsum * r2.ratingsum
--          END) as S
        FROM ratings r
            INNER JOIN ratings r2 ON r.trackid = r2.trackid
               AND r.userid != r2.userid
        WHERE r.ratingsum > 0 AND r2.ratingsum > 0 -- было решено не учитывать пропущенные треки
        GROUP BY r.userid, r2.userid
        ) AS cursor1;
  cuser1 uuid;
  cuser2 uuid;
  cratio integer;
BEGIN
  DROP TABLE IF EXISTS temp_ratio;
  CREATE TEMP TABLE temp_ratio(userid1 uuid, userid2 uuid, ratio integer);

  OPEN curs1; -- открываем курсор
  LOOP -- в цикле проходим по строкам результата запроса курсора
    FETCH curs1 INTO cuser1, cuser2, cratio;

    IF NOT FOUND THEN EXIT; -- если данных нет - выходим
    END IF;
    -- если для данной пары пользователей уже записан коэффициент - пропускаем, иначе - записываем во временную таблицу
    --IF NOT EXISTS (SELECT * FROM temp_ratio WHERE userid1 = cuser2 AND userid2 = cuser1 OR userid1 = cuser1 AND userid2 = cuser2) THEN
      INSERT INTO temp_ratio(userid1, userid2, ratio)
      VALUES (cuser1, cuser2, cratio);
    --END IF;
  END LOOP;
  CLOSE curs1; -- закрываем курсор

  -- обновляем имеющиеся коэффициенты в таблице ratios
  UPDATE ratios SET ratio = temp_ratio.ratio, recupdated = now() FROM temp_ratio
  WHERE (ratios.userid1 = temp_ratio.userid1 AND ratios.userid2 = temp_ratio.userid2);
--      OR (ratios.userid1 = temp_ratio.userid2 AND ratios.userid2 = temp_ratio.userid1);

  -- если в ratios меньше пар пользователей, чем во временной таблице - вставляем недостающие записи
  --IF (SELECT COUNT(*) FROM ratios) < (SELECT COUNT(*) FROM temp_ratio) THEN
--    INSERT INTO ratios (userid1, userid2, ratio, reccreated)
--      (SELECT tr.userid1, tr.userid2, tr.ratio, now()  FROM temp_ratio AS tr
--        LEFT OUTER JOIN ratios AS rr ON tr.userid1 = rr.userid1 AND tr.userid2 = rr.userid2 OR tr.userid1 = rr.userid2 AND tr.userid2 = rr.userid1
--      WHERE rr.userid1 IS NULL OR rr.userid2 IS NULL
--      );
  --END IF;
  INSERT INTO ratios (userid1, userid2, ratio, reccreated)
  (SELECT temp_ratio.userid1,temp_ratio.userid2, temp_ratio.ratio, now() 
    FROM temp_ratio
    LEFT OUTER JOIN ratios ON 
      temp_ratio.userid1 = ratios.userid1 AND temp_ratio.userid2 = ratios.userid2
    WHERE ratios.userid1 IS NULL OR ratios.userid2 IS NULL
  );
  RETURN TRUE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.calculateratios()
  OWNER TO postgres;


  
-- Function: public.updateratios(uuid)

-- DROP FUNCTION public.updateratios(uuid);

CREATE OR REPLACE FUNCTION public.updateratios(i_userid uuid)
  RETURNS boolean AS
$BODY$
-- функция обновляет таблицу коэффециентов схожести интересов для всех пользователей, прослушавших те же треки, что и i_userid
DECLARE
  cuser1 uuid;
  cuser2 uuid;
  cratio integer;
BEGIN

--  RETURN true;
  
  DROP TABLE IF EXISTS temp_ratio;
  CREATE TEMP TABLE temp_ratio(userid1 uuid, userid2 uuid, ratio integer);

  -- рассчитываем матрицу коэффициентов схожести интересов для каждой пары пользователей
  INSERT INTO temp_ratio(userid1, userid2, ratio)
      (SELECT r.userid as userid01, r2.userid as userid02, --SUM(r.ratingsum * r2.ratingsum) as s
        -- считаем сумму произведений с учетом весов коэффициентиов: ratingsum<0 => weight=1, ratingsum>0 => weight=3
        -- SUM(CASE WHEN r.ratingsum > 0 AND r2.ratingsum > 0 THEN r.ratingsum * r2.ratingsum * 3

        SUM(r.ratingsum * r2.ratingsum) as S
        -- было решено не учитывать пропущенные треки вообще, поэтому условие case было заменено на проверку в блоке where
        -- SUM(CASE WHEN r.ratingsum > 0 AND r2.ratingsum > 0 THEN r.ratingsum * r2.ratingsum
--          WHEN r.ratingsum < 0 AND r2.ratingsum < 0 THEN 0
--          ELSE r.ratingsum * r2.ratingsum
--          END) as S
        FROM ratings r
          INNER JOIN ratings r2 ON r.trackid = r2.trackid
               AND r.userid != r2.userid
               AND ((r.userid = i_userid AND r2.userid IN (SELECT recid FROM users WHERE experience >= 10)) 
                OR (r2.userid = i_userid AND r.userid IN (SELECT recid FROM users WHERE experience >= 10)))
        WHERE r.ratingsum > 0 AND r2.ratingsum > 0
        GROUP BY r.userid, r2.userid);

  -- обновляем ratio, если пара пользователей уже была в таблице
  UPDATE ratios SET ratio = temp_ratio.ratio, recupdated = now() FROM temp_ratio
    WHERE ratios.userid1 = temp_ratio.userid1 AND ratios.userid2 = temp_ratio.userid2;

  -- Добавляем записи для новой пары с пользователейм
  INSERT INTO ratios (userid1, userid2, ratio, reccreated)
    (SELECT temp_ratio.userid1,temp_ratio.userid2, temp_ratio.ratio, now() 
      FROM temp_ratio
      LEFT OUTER JOIN ratios ON 
        temp_ratio.userid1 = ratios.userid1 AND temp_ratio.userid2 = ratios.userid2
      WHERE ratios.userid1 IS NULL OR ratios.userid2 IS NULL
    );

RETURN TRUE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.updateratios(uuid)
  OWNER TO postgres;
  
  -- Function: getnexttrackid_v7(uuid)

-- DROP FUNCTION getnexttrackid_v7(uuid);

CREATE OR REPLACE FUNCTION getnexttrackid_v7(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer) AS
$BODY$

-- Функция выдачи следующего трека пользователю
-- С учетом рекомендаций от других пользователей

DECLARE
	i_userid   UUID = i_deviceid;
	rnd        INTEGER = (SELECT trunc(random() * 1001));
	o_methodid INTEGER; -- id метода выбора трека
	owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
	arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
	exceptusers uuid ARRAY; -- массив пользователей для i_userid с котороми не было пересечений по трекам
BEGIN
	-- Выбираем следующий трек

	-- Определяем количество "своих" треков пользователя, ограничивая его 900
	owntracks = (SELECT COUNT(*)
				 FROM (
						  SELECT *
						  FROM ratings
						  WHERE userid = i_userid
								AND ratingsum >= 0
						  LIMIT 900) AS count);

	-- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
	-- с положительным рейтингом, за исключением прослушанных за последние сутки

	IF (rnd < owntracks)
	THEN
		o_methodid = 2; -- метод выбора из своих треков
		RETURN QUERY
		SELECT
			trackid,
			o_methodid
		FROM ratings
		WHERE userid = i_userid
			  AND lastlisten < localtimestamp - INTERVAL '1 day'
			  AND ratingsum >= 0
			  AND (SELECT isexist
				   FROM tracks
				   WHERE recid = trackid) = 1
			  AND ((SELECT length
					FROM tracks
					WHERE recid = trackid) >= 120
				   OR (SELECT length
					   FROM tracks
					   WHERE recid = trackid) IS NULL)
			  AND ((SELECT iscensorial
					FROM tracks
					WHERE recid = trackid) IS NULL
				   OR (SELECT iscensorial
					   FROM tracks
					   WHERE recid = trackid) != 0)
			  AND trackid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
		ORDER BY RANDOM()
		LIMIT 1;

		-- Если такой трек найден - выход из функции, возврат найденного значения
		IF FOUND
		THEN RETURN;
		END IF;
	END IF;

	-- Если rnd больше количества "своих" треков - рекомендуем трек из треков пользователя с наибольшим 
	-- коэффициентом схожести интересов и наибольшим рейтингом прослушивания

	-- Выберем всех пользователей с неотрицательным коэффициентом схожести интересов для i_userid
	-- отсортировав по убыванию коэффициентов
	arrusers = (SELECT ARRAY (SELECT CASE WHEN userid1 = i_userid THEN userid2
							WHEN userid2 = i_userid THEN userid1
							ELSE NULL
							END
						FROM ratios
						WHERE userid1 = i_userid OR userid2 = i_userid
							AND ratio >= 0
						ORDER BY ratio DESC
						));
	-- Выбираем пользователя i, с которым у него максимальный коэффициент. Среди его треков ищем трек 
	-- с максимальным рейтингом прослушивания, за исключением уже прослушанных пользователем i_userid. 
	-- Если рекомендовать нечего - берем следующего пользователя с наибольшим коэффициентом из оставшихся.
	FOR i IN 1.. (SELECT COUNT (*) FROM unnest(arrusers)) LOOP
		o_methodid = 4; -- метод выбора из рекомендованных треков
		RETURN QUERY
		SELECT
			trackid,
			o_methodid
			FROM ratings
			WHERE userid = arrusers[i]
				AND ratingsum > 0
				AND trackid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
				AND trackid NOT IN (SELECT trackid 
							FROM downloadtracks
							WHERE deviceid = i_deviceid 
								AND reccreated > localtimestamp - INTERVAL '1 day')
				AND (SELECT isexist
					   FROM tracks
					   WHERE recid = trackid) = 1
				AND ((SELECT length
						FROM tracks
						WHERE recid = trackid) >= 120
					   OR (SELECT length
						   FROM tracks
						   WHERE recid = trackid) IS NULL)
				AND ((SELECT iscensorial
						FROM tracks
						WHERE recid = trackid) IS NULL
					   OR (SELECT iscensorial
						   FROM tracks
						   WHERE recid = trackid) != 0)
			ORDER BY ratingsum DESC
			LIMIT 1;
	-- Если нашли что рекомендовать - выходим из функции
		IF found THEN
		RETURN;
		END IF;
	END LOOP;
	
	-- При отсутствии рекомендаций, выдавать случайный трек из непрослушанных треков с неотрицательным
	-- рейтингом среди пользователей с которыми не было пересечений по трекам.
	exceptusers = (SELECT ARRAY (
				SELECT * FROM (
					SELECT recid FROM users WHERE recid != i_userid
						EXCEPT
						(SELECT CASE WHEN userid1 = i_userid THEN userid2
							 WHEN userid2 = i_userid THEN userid1
							 ELSE NULL
							 END
							FROM ratios WHERE userid1 = i_userid OR userid2 = i_userid)
				) AS us
			ORDER BY RANDOM()
			)
		);
	FOR i IN 1.. (SELECT COUNT (*) FROM unnest(exceptusers)) LOOP
		o_methodid = 6; -- метод выбора из непрослушанных треков с неотрицательным рейтингом среди пользователей с которыми не было пересечений
		RETURN QUERY 
		SELECT
			recid,
			o_methodid
		FROM tracks
		WHERE recid IN (SELECT trackid FROM ratings WHERE userid = exceptusers[i] AND ratingsum >= 0)
			  AND recid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
			  AND isexist = 1
			  AND (iscensorial IS NULL OR iscensorial != 0)
			  AND (length > 120 OR length IS NULL)
			  AND recid NOT IN (SELECT trackid
					FROM downloadtracks
					WHERE reccreated > localtimestamp - INTERVAL '1 day')
		ORDER BY RANDOM()
		LIMIT 1;
		-- Если нашли что рекомендовать - выходим из функции
		IF found THEN
			RETURN;
		ELSE 
		
		END IF;
	END LOOP;

	-- Если таких треков нет - выбираем случайный трек из ни разу не прослушанных пользователем треков
	o_methodid = 3; -- метод выбора из непрослушанных треков
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE recid NOT IN
		  (SELECT trackid
		   FROM ratings
		   WHERE userid = i_userid)
		  AND isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
		  AND recid NOT IN (SELECT trackid
							FROM downloadtracks
							WHERE reccreated > localtimestamp - INTERVAL '1 day')
	ORDER BY RANDOM()
	LIMIT 1;

	-- Если такой трек найден - выход из функции, возврат найденного значения
	IF FOUND
	THEN RETURN;
	END IF;

	-- Если предыдущие запросы вернули null, выбираем случайный трек
	o_methodid = 1; -- метод выбора случайного трека
	RETURN QUERY
	SELECT
		recid,
		o_methodid
	FROM tracks
	WHERE isexist = 1
		  AND (iscensorial IS NULL OR iscensorial != 0)
		  AND (length > 120 OR length IS NULL)
	ORDER BY RANDOM()
	LIMIT 1;
	RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION getnexttrackid_v7(uuid)
  OWNER TO "postgres";


-- Function: public.getrecommendedtrackid_v1(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v1(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v1(in_userid uuid)
  RETURNS uuid AS
$BODY$

DECLARE
preferenced_track uuid;

BEGIN
  -- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
  -- у конкретного пользователя для возможности вывода дополнительной информации о треке
  -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid INTO preferenced_track
    --tracks.recid, table2.sum_rate, tracks.localdevicepathupload, tracks.path
        FROM tracks
        INNER JOIN (
          --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
          --каждого из них
          SELECT trackid, SUM(track_rating) AS sum_rate
          FROM(
            --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
            --с исходным, умноженным на их коэффициент
            SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
            FROM ratings


              --------------------------------------------------
              ---------------НОВЫЙ INNER JOIN-------------------
              --------------------------------------------------

              INNER JOIN
              (
                --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                --с таблицой с UUID'ми всех экспертов.
                --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                --качестве коэффициента
                SELECT COALESCE(associated_experts.ratio, 1) AS ratio, all_experts.userid AS expert_id
                FROM
                (
                  --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                  --и UUID'ы этих экспертов
                  SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                  FROM ratios
                  WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience = 10)
                ) AS associated_experts
                RIGHT JOIN 
                (
                  --Выберем UUID'ы всех экспертов
                  SELECT recid AS userid
                  FROM users
                  WHERE experience = 10
                ) AS all_experts
                ON associated_experts.userid = all_experts.userid
              ) AS experts_ratios
              ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
              AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
              




              
              --------------------------------------------------
              --------------СТАРЫЙ INNER JOIN-------------------
              --------------------------------------------------
              
              -- INNER JOIN ratios
--              --Выбираем рейтинги треков у тех пользователей, у которых есть пересечение
--              --с исходным в таблице ratios (кэффициенты совпадения вкусов), проверяя сначала
--              --с левой стороны
--              ON ((ratings.userid = ratios.userid2 AND ratios.userid1 = in_userid)
--                -- потом с правой
--                OR (ratings.userid = ratios.userid1 AND ratios.userid2 = in_userid))

 --             AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
 --             AND ratios.ratio > 0 --Считать рейтинги треков, только у пользователей с положительным коэффициентом совпадения вкусов с исходным




              
          ) AS TracksRatings
          GROUP BY trackid
          ORDER BY sum_rate DESC
        ) AS table2
        ON tracks.recid = table2.trackid
        AND tracks.isexist = 1 --Трек должен существовать на сервере
        AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
        AND tracks.length >= 120
        --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев
        AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                     WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
        AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
        ORDER BY table2.sum_rate DESC
           --Сортировка по второму столбцу нужна для случаев, когда получаем много треков с одинковым table2.sum_rate,
           --в таких случаях план выполнения запроса меняется и производительность сильно падает
           --,tracks.recid
           ,random()
        LIMIT 1;
  RETURN preferenced_track;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.getrecommendedtrackid_v1(uuid)
  OWNER TO postgres;


-- Function: public.get_user_tracks_preference(uuid)

-- DROP FUNCTION public.get_user_tracks_preference(uuid);

CREATE OR REPLACE FUNCTION public.get_user_tracks_preference(IN in_userid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_track_sum_rate bigint, rn_localdevicepathupload character varying, rn_path character varying) AS
$BODY$

DECLARE 
rnd DOUBLE PRECISION;

BEGIN
RETURN QUERY (
  -- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
    -- у конкретного пользователя для возможности вывода дополнительной информации о треке
    -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid AS track_id, tracks_sum_rates.sum_rate AS track_sum_rate, tracks.localdevicepathupload, tracks.path-- INTO preferenced_track
    --tracks.recid, tracks_sum_rates.sum_rate, tracks.localdevicepathupload, tracks.path
          FROM tracks
          INNER JOIN (
            --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
            --каждого из них
            SELECT trackid, SUM(track_rating) AS sum_rate
            FROM(
              --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
              --с исходным, умноженным на их коэффициент
              SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
              FROM ratings
                INNER JOIN
                (
                  --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                  --с таблицой с UUID'ми всех экспертов.
                  --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                  --качестве коэффициента
                  SELECT COALESCE(associated_experts.ratio, 1) AS ratio, all_experts.userid AS expert_id
                  FROM
                  (
                    --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                    --и UUID'ы этих экспертов
                    SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                    FROM ratios
                    WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience >= 10)
                  ) AS associated_experts
                  RIGHT JOIN 
                  (
                    --Выберем UUID'ы всех экспертов
                    SELECT recid AS userid
                    FROM users
                    WHERE experience >= 10
                  ) AS all_experts
                  ON associated_experts.userid = all_experts.userid
                ) AS experts_ratios
                ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
                AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
                AND experts_ratios.ratio > 0 --Считать рейтинги треков, только у пользователей с положительным коэффициентом совпадения вкусов с исходным
            ) AS tracks_ratings
            GROUP BY trackid
            ORDER BY sum_rate DESC
          ) AS tracks_sum_rates
          ON tracks.recid = tracks_sum_rates.trackid
          AND tracks.isexist = 1 --Трек должен существовать на сервере
          AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
          AND tracks.length >= 120
          --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев
          AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                 WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
          AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
          ORDER BY tracks_sum_rates.sum_rate DESC
          );
        
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.get_user_tracks_preference(uuid)
  OWNER TO postgres;



-- Function: public.getlastdevices()

-- DROP FUNCTION public.getlastdevices();

CREATE OR REPLACE FUNCTION public.getlastdevices()
  RETURNS TABLE(recid character varying) AS
$BODY$
BEGIN

  RETURN QUERY SELECT CAST((dev.recid) AS CHARACTER VARYING)
         FROM devices dev
           INNER JOIN downloadtracks down
             ON dev.recid = down.deviceid
         GROUP BY dev.recid
         ORDER BY MAX(down.reccreated) DESC
         LIMIT 100;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getlastdevices()
  OWNER TO postgres;



-- Function: public.getlasttracks(uuid, integer)

-- DROP FUNCTION public.getlasttracks(uuid, integer);

CREATE OR REPLACE FUNCTION public.getlasttracks(
    IN i_deviceid uuid,
    IN i_count integer)
  RETURNS TABLE(recid uuid, reccreated timestamp without time zone, recname character varying, recupdated timestamp without time zone, deviceid uuid, trackid uuid, methodid integer, txtrecommendinfo character varying, userrecommend uuid) AS
$BODY$
BEGIN
  IF i_count < 0 THEN
    i_count = null;
  END IF;
RETURN QUERY SELECT *
  FROM downloadtracks
  WHERE downloadtracks.deviceid = i_deviceid
  ORDER BY downloadtracks.reccreated DESC
  LIMIT i_count;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getlasttracks(uuid, integer)
  OWNER TO postgres;



-- Function: public.getlastusers(integer)

-- DROP FUNCTION public.getlastusers(integer);

CREATE OR REPLACE FUNCTION public.getlastusers(IN i_count integer)
  RETURNS TABLE(usid character varying, regdate character varying, lastactive character varying, devicename character varying, recupdated character varying, owntracks bigint, downloadtracks bigint) AS
$BODY$
BEGIN
  IF i_count < 0 THEN
    i_count = null;
  END IF;
  RETURN QUERY SELECT
    CAST((res1.recid) AS CHARACTER VARYING),
    CAST((res1.reccreated) AS CHARACTER VARYING), 
    CAST((MAX(res2.reccreated)) AS CHARACTER VARYING), 
    res1.recname, 
    CAST((res1.recupdated) AS CHARACTER VARYING), 
    res1.owntracks, 
    COUNT(res2.userid) AS lasttracks
    FROM
    (SELECT u.recid, u.reccreated, u.recname, u.recupdated, COUNT(r.recid) AS owntracks
      FROM users u
      LEFT OUTER JOIN ratings r ON u.recid = r.userid
        AND r.ratingsum >= 0
      GROUP BY u.recid) res1
    LEFT OUTER JOIN (SELECT d.reccreated, dev.userid FROM downloadtracks d
          INNER JOIN devices dev
          ON dev.recid= d.deviceid
            --AND d.reccreated > localtimestamp - INTERVAL '1 day'
          ) res2
      ON res2.userid = res1.recid
    GROUP BY res1.recid, res1.reccreated, res1.recname, res1.recupdated, res1.owntracks
    ORDER BY MAX(res2.reccreated) DESC NULLS LAST 
    LIMIT i_count;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getlastusers(integer)
  OWNER TO postgres;


-- Function: public.getnexttrack_v2(uuid)

-- DROP FUNCTION public.getnexttrack_v2(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrack_v2(IN i_deviceid uuid)
  RETURNS TABLE(track character varying, method integer, useridrecommended character varying, txtrecommendedinfo character varying, timeexecute character varying) AS
$BODY$
DECLARE
    declare t timestamptz := clock_timestamp(); -- запоминаем начальное время выполнения процедуры
    i_userid UUID = i_deviceid; -- в дальнейшем заменить получением userid по deviceid
BEGIN
  -- Добавляем устройство, если его еще не существует
  PERFORM registerdevice(i_deviceid, 'New device');

  -- Возвращаем trackid, конвертируя его в character varying, и methodid
  RETURN QUERY SELECT
           CAST((nexttrack.track) AS CHARACTER VARYING),
           nexttrack.methodid,
           CAST((nexttrack.useridrecommended) AS CHARACTER VARYING),
           nexttrack.txtrecommendedinfo,
           CAST((clock_timestamp() - t ) AS CHARACTER VARYING) -- возвращаем время выполнения процедуры
         FROM getnexttrackid_v17(i_deviceid) AS nexttrack;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrack_v2(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v10(uuid)

-- DROP FUNCTION public.getnexttrackid_v10(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v10(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying) AS
$BODY$
DECLARE
  i_userid   UUID = i_deviceid; --пока не реалезовано объединение пользователей - гуиды одинаковые
  rnd        INTEGER = (SELECT trunc(random() * 1001)); -- генерируем случайное целое число в диапазоне от 1 до 1000
  o_methodid INTEGER; -- id метода выбора трека
  owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
  arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
  exceptusers uuid ARRAY; -- массив пользователей для i_userid с котороми не было пересечений по трекам
BEGIN
  DROP TABLE IF EXISTS temp_track;
  CREATE TEMP TABLE temp_track(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying);

  -- Выбираем следующий трек

  -- Определяем количество "своих" треков пользователя, ограничивая его 900
  owntracks = (SELECT COUNT(*)
         FROM (
              SELECT *
              FROM ratings
              WHERE userid = i_userid
                AND ratingsum >= 0
              LIMIT 900) AS count);

  -- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
  -- с положительным рейтингом, за исключением прослушанных за последние сутки

  IF (rnd < owntracks)
  THEN
    o_methodid = 2; -- метод выбора из своих треков
    INSERT INTO temp_track (
    SELECT
      trackid,
      o_methodid,
      (SELECT CAST((null) AS UUID)),
      (SELECT CAST((null) AS CHARACTER VARYING))
    FROM ratings
    WHERE userid = i_userid
        AND lastlisten < localtimestamp - INTERVAL '1 day'
        AND ratingsum >= 0
        AND (SELECT isexist
           FROM tracks
           WHERE recid = trackid) = 1
        AND ((SELECT length
          FROM tracks
          WHERE recid = trackid) >= 120
           OR (SELECT length
             FROM tracks
             WHERE recid = trackid) IS NULL)
        AND ((SELECT iscensorial
          FROM tracks
          WHERE recid = trackid) IS NULL
           OR (SELECT iscensorial
             FROM tracks
             WHERE recid = trackid) != 0)
        AND trackid NOT IN (SELECT trackid
                  FROM downloadtracks
                  WHERE reccreated > localtimestamp - INTERVAL '1 day')
    ORDER BY RANDOM()
    LIMIT 1);

    -- Если такой трек найден - выход из функции, возврат найденного значения
    IF FOUND THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
      RETURN QUERY SELECT * FROM temp_track;
      RETURN;
    END IF;
  END IF;

  -- Если rnd больше количества "своих" треков - рекомендуем трек из треков пользователя с наибольшим
  -- коэффициентом схожести интересов и наибольшим рейтингом прослушивания

  -- Выберем всех пользователей с неотрицательным коэффициентом схожести интересов для i_userid
  -- отсортировав по убыванию коэффициентов
  arrusers = (SELECT ARRAY (SELECT CASE WHEN userid1 = i_userid THEN userid2
                   WHEN userid2 = i_userid THEN userid1
                   ELSE NULL
                   END
                FROM ratios
                WHERE (userid1 = i_userid OR userid2 = i_userid) AND ratio >= 0
                ORDER BY ratio DESC
  ));
  -- Выбираем пользователя i, с которым у него максимальный коэффициент. Среди его треков ищем трек
  -- с максимальным рейтингом прослушивания, за исключением уже прослушанных пользователем i_userid.
  -- Если рекомендовать нечего - берем следующего пользователя с наибольшим коэффициентом из оставшихся.
  FOR i IN 1.. (SELECT COUNT (*) FROM unnest(arrusers)) LOOP
    o_methodid = 4; -- метод выбора из рекомендованных треков
    INSERT INTO temp_track (
      SELECT
      trackid,
      o_methodid,
      arrusers[i],
      (SELECT CAST ((concat('Коэффициент схожести ', ratio)) AS CHARACTER VARYING)
       FROM ratios
       WHERE userid1 = i_userid AND userid2 = arrusers[i] OR userid2 = i_userid AND userid1 = arrusers[i]  LIMIT 1)
    FROM ratings
    WHERE userid = arrusers[i]
        AND ratingsum > 0
        AND trackid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
        AND trackid NOT IN (SELECT trackid
                  FROM downloadtracks
                  WHERE deviceid = i_deviceid
                    AND reccreated > localtimestamp - INTERVAL '1 day')
        AND (SELECT isexist
           FROM tracks
           WHERE recid = trackid) = 1
        AND ((SELECT length
          FROM tracks
          WHERE recid = trackid) >= 120
           OR (SELECT length
             FROM tracks
             WHERE recid = trackid) IS NULL)
        AND ((SELECT iscensorial
          FROM tracks
          WHERE recid = trackid) IS NULL
           OR (SELECT iscensorial
             FROM tracks
             WHERE recid = trackid) != 0)
    ORDER BY ratingsum DESC, RANDOM()
    LIMIT 1);
    -- Если нашли что рекомендовать - выходим из функции
    IF found THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
      RETURN QUERY SELECT * FROM temp_track;
      RETURN;
    END IF;
  END LOOP;
  -- При отсутствии рекомендаций, выдавать случайный трек из непрослушанных треков с неотрицательным
  -- рейтингом среди пользователей с которыми не было пересечений по трекам.
  exceptusers = (SELECT ARRAY (
    SELECT * FROM (
              SELECT recid FROM users WHERE recid != i_userid
              EXCEPT
              (SELECT CASE WHEN userid1 = i_userid THEN userid2
                  WHEN userid2 = i_userid THEN userid1
                  ELSE NULL
                  END
               FROM ratios WHERE userid1 = i_userid OR userid2 = i_userid)
            ) AS us
    ORDER BY RANDOM()
  )
  );
  FOR i IN 1.. (SELECT COUNT (*) FROM unnest(exceptusers)) LOOP
    o_methodid = 6; -- метод выбора из непрослушанных треков с неотрицательным рейтингом среди пользователей с которыми не было пересечений
    INSERT INTO temp_track (
    SELECT
      recid,
      o_methodid,
      exceptusers[i],
      (SELECT CAST(('рекомендовано от пользователя с которым не было пересечений') AS CHARACTER VARYING))
    FROM tracks
    WHERE recid IN (SELECT trackid FROM ratings WHERE userid = exceptusers[i] AND ratingsum >= 0)
        AND recid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
        AND isexist = 1
        AND (iscensorial IS NULL OR iscensorial != 0)
        AND (length > 120 OR length IS NULL)
        AND recid NOT IN (SELECT trackid
                FROM downloadtracks
                WHERE reccreated > localtimestamp - INTERVAL '1 day')
    ORDER BY RANDOM()
    LIMIT 1);
    -- Если нашли что рекомендовать - выходим из функции
    IF found THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
      RETURN QUERY SELECT * FROM temp_track;
      RETURN;
    ELSE

    END IF;
  END LOOP;

  -- Если таких треков нет - выбираем случайный трек из ни разу не прослушанных пользователем треков
  o_methodid = 3; -- метод выбора из непрослушанных треков
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST((null) AS CHARACTER VARYING))
  FROM tracks
  WHERE recid NOT IN
      (SELECT trackid
       FROM ratings
       WHERE userid = i_userid)
      AND isexist = 1
      AND (iscensorial IS NULL OR iscensorial != 0)
      AND (length > 120 OR length IS NULL)
      AND recid NOT IN (SELECT trackid
              FROM downloadtracks
              WHERE reccreated > localtimestamp - INTERVAL '1 day')
  ORDER BY RANDOM()
  LIMIT 1);

  -- Если такой трек найден - выход из функции, возврат найденного значения
  IF FOUND THEN
    INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
    RETURN QUERY SELECT * FROM temp_track;
    RETURN;
  END IF;

  -- Если предыдущие запросы вернули null, выбираем случайный трек
  o_methodid = 1; -- метод выбора случайного трека
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST((null) AS CHARACTER VARYING))
  FROM tracks
  WHERE isexist = 1
      AND (iscensorial IS NULL OR iscensorial != 0)
      AND (length > 120 OR length IS NULL)
  ORDER BY RANDOM()
  LIMIT 1);
  INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
  RETURN QUERY SELECT * FROM temp_track;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v10(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v15(uuid)

-- DROP FUNCTION public.getnexttrackid_v15(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v15(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying) AS
$BODY$

-- Функция выдачи треков пользователю
DECLARE
  i_userid   UUID = i_deviceid; -- На случай, если устройство еще не зарегистрировано и пользователь не существует
  rnd        INTEGER = (SELECT trunc(random() * 1001)); -- генерируем случайное целое число в диапазоне от 1 до 1000
  o_methodid INTEGER; -- id метода выбора трека
  owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
  arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
  exceptusers uuid ARRAY; -- массив пользователей для i_userid с котороми не было пересечений по трекам
  temp_trackid uuid; 
  tmp_txtrecommendinfo text;
BEGIN
  -- temp_track - временная таблица для промежуточного результата (понадобилась чтобы найденные данные сначала сохранять в таблицу downloadtracks, а потом возвращать
  DROP TABLE IF EXISTS temp_track; 
  CREATE TEMP TABLE temp_track(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying);

  --Если устройство не было зарегистрировано ранее - регистрируем его
  IF NOT EXISTS(SELECT recid
      FROM devices
      WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
  -- Если устройство зарегистрировано - ищем соответствующего ему пользователя
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;


  -- Выбираем следующий трек

  -- Определяем количество "своих" треков пользователя, ограничивая его 900
  -- owntracks = (SELECT COUNT(*)
--         FROM (
--              SELECT *
--              FROM ratings
--              WHERE userid = i_userid
--                AND ratingsum >= 0
--              LIMIT 900) AS count);

  -- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
  -- с положительным рейтингом, за исключением прослушанных за последние сутки

--  IF (rnd < owntracks)
--  THEN
--    o_methodid = 2; -- метод выбора из своих треков
--    INSERT INTO temp_track (
--    SELECT
--      trackid, -- выбираем id трека
--      o_methodid,
--      (SELECT CAST((null) AS UUID)),
--      (SELECT CAST(('случайный трек из своих') AS CHARACTER VARYING))
--    FROM ratings -- из треков, имеющих рейтинг для данного пользователя
--    WHERE userid = i_userid
--        AND lastlisten < localtimestamp - INTERVAL '1 day' -- для которого последнее прослушивание было ранее, чем за сутки до выдачи
--        AND ratingsum >= 0 -- рейтинг трека неотрицательный
--        AND (SELECT isexist
--           FROM tracks
--           WHERE recid = trackid) = 1 -- трек существует на сервере
--        AND ((SELECT length
--          FROM tracks
--          WHERE recid = trackid) >= 120 -- продолжительность трека больше двух минут
--           OR (SELECT length
--             FROM tracks
--             WHERE recid = trackid) IS NULL) -- или длина трека не известна
--        AND ((SELECT iscensorial
--          FROM tracks
--          WHERE recid = trackid) IS NULL -- трек должен быть цензурный или непроверенный
--           OR (SELECT iscensorial
--             FROM tracks
--             WHERE recid = trackid) != 0)
--        AND trackid NOT IN (SELECT trackid
--                  FROM downloadtracks
--                  WHERE reccreated > localtimestamp - INTERVAL '1 week' AND deviceid = i_deviceid) -- трек недолжен быть выдан в последнюю неделю
--    ORDER BY RANDOM()
--    LIMIT 1);

--    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
--    IF FOUND THEN
--      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
--      RETURN QUERY SELECT * FROM temp_track;
--      RETURN;
--    END IF;
--  END IF;

  -- Если rnd больше количества "своих" треков - используем алгоритм рекоммендаций

  -- Если положительный коэффициент схожести интересов больше чем с пятью пользователями,
--  IF (SELECT COUNT (*) FROM ratios WHERE (userid1 = i_userid OR userid2 = i_userid) AND ratio >=0) > 5 THEN
  -- рекомендуем трек с максимальным рейтингом среди пользователей, с которыми были пересечения
    o_methodid = 7; -- метод выбора из рекомендованных треков
    SELECT rn_trackid, rn_txtrecommendinfo INTO temp_trackid, tmp_txtrecommendinfo FROM getrecommendedtrackid_v5(i_userid);
    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
    IF temp_trackid IS NOT null THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),
              now(),
              null,
              null, 
              i_deviceid,
              temp_trackid,
              o_methodid,
              (SELECT CAST((
                tmp_txtrecommendinfo
                ) AS CHARACTER VARYING)),
              (SELECT CAST((null) AS UUID)) );
    RETURN QUERY 
      SELECT temp_trackid,
      o_methodid,
      (SELECT CAST((null) AS UUID)),
      (SELECT CAST((
        tmp_txtrecommendinfo
        ) AS CHARACTER VARYING));
    RETURN;
    END IF;
--  END IF;

  -- Если таких треков нет - выбираем случайный трек из ни разу не прослушанных пользователем треков
  o_methodid = 3; -- метод выбора из непрослушанных треков
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('случайный трек из непрослушанных пользователем') AS CHARACTER VARYING))
  FROM tracks
  WHERE recid NOT IN
      (SELECT trackid
       FROM ratings
       WHERE userid = i_userid)
      AND isexist = 1
      AND (iscensorial IS NULL OR iscensorial != 0)
      AND (length > 120 OR length IS NULL)
      AND recid NOT IN (SELECT trackid
              FROM downloadtracks
              WHERE reccreated > localtimestamp - INTERVAL '1 week'  AND deviceid = i_deviceid)
  ORDER BY RANDOM()
  LIMIT 1);

  -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
  IF FOUND THEN
    INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
    RETURN QUERY SELECT * FROM temp_track;
    RETURN;
  END IF;

  -- Если предыдущие запросы вернули null, выбираем случайный трек
  o_methodid = 1; -- метод выбора случайного трека
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('случайный трек из всех') AS CHARACTER VARYING))
  FROM tracks
  WHERE isexist = 1 -- существующий на сервере 
      AND (iscensorial IS NULL OR iscensorial != 0) -- цензурный
      AND (length > 120 OR length IS NULL) -- продолжительностью более 2х минут
  ORDER BY RANDOM()
  LIMIT 1);
  INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
  RETURN QUERY SELECT * FROM temp_track;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v15(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v16(uuid)

-- DROP FUNCTION public.getnexttrackid_v16(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v16(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying) AS
$BODY$

-- Функция выдачи треков пользователю
DECLARE
  i_userid   UUID = i_deviceid; -- На случай, если устройство еще не зарегистрировано и пользователь не существует
  rnd        INTEGER = (SELECT trunc(random() * 1001)); -- генерируем случайное целое число в диапазоне от 1 до 1000
  o_methodid INTEGER; -- id метода выбора трека
  owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
  arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
  exceptusers uuid ARRAY; -- массив пользователей для i_userid с котороми не было пересечений по трекам
  temp_trackid uuid; 
  tmp_txtrecommendinfo text;
BEGIN
  -- temp_track - временная таблица для промежуточного результата (понадобилась чтобы найденные данные сначала сохранять в таблицу downloadtracks, а потом возвращать
  DROP TABLE IF EXISTS temp_track; 
  CREATE TEMP TABLE temp_track(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying);

  --Если устройство не было зарегистрировано ранее - регистрируем его
  IF NOT EXISTS(SELECT recid
      FROM devices
      WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
  -- Если устройство зарегистрировано - ищем соответствующего ему пользователя
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;


  -- Выбираем следующий трек

  -- Определяем количество "своих" треков пользователя, ограничивая его 900
  -- owntracks = (SELECT COUNT(*)
--         FROM (
--              SELECT *
--              FROM ratings
--              WHERE userid = i_userid
--                AND ratingsum >= 0
--              LIMIT 900) AS count);

  -- Если rnd меньше количества "своих" треков, выбираем трек из треков пользователя (добавленных им или прослушанных до конца)
  -- с положительным рейтингом, за исключением прослушанных за последние сутки

--  IF (rnd < owntracks)
--  THEN
--    o_methodid = 2; -- метод выбора из своих треков
--    INSERT INTO temp_track (
--    SELECT
--      trackid, -- выбираем id трека
--      o_methodid,
--      (SELECT CAST((null) AS UUID)),
--      (SELECT CAST(('случайный трек из своих') AS CHARACTER VARYING))
--    FROM ratings -- из треков, имеющих рейтинг для данного пользователя
--    WHERE userid = i_userid
--        AND lastlisten < localtimestamp - INTERVAL '1 day' -- для которого последнее прослушивание было ранее, чем за сутки до выдачи
--        AND ratingsum >= 0 -- рейтинг трека неотрицательный
--        AND (SELECT isexist
--           FROM tracks
--           WHERE recid = trackid) = 1 -- трек существует на сервере
--        AND ((SELECT length
--          FROM tracks
--          WHERE recid = trackid) >= 120 -- продолжительность трека больше двух минут
--           OR (SELECT length
--             FROM tracks
--             WHERE recid = trackid) IS NULL) -- или длина трека не известна
--        AND ((SELECT iscensorial
--          FROM tracks
--          WHERE recid = trackid) IS NULL -- трек должен быть цензурный или непроверенный
--           OR (SELECT iscensorial
--             FROM tracks
--             WHERE recid = trackid) != 0)
--        AND trackid NOT IN (SELECT trackid
--                  FROM downloadtracks
--                  WHERE reccreated > localtimestamp - INTERVAL '1 week' AND deviceid = i_deviceid) -- трек недолжен быть выдан в последнюю неделю
--    ORDER BY RANDOM()
--    LIMIT 1);

--    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
--    IF FOUND THEN
--      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
--      RETURN QUERY SELECT * FROM temp_track;
--      RETURN;
--    END IF;
--  END IF;

  -- Если rnd больше количества "своих" треков - используем алгоритм рекоммендаций

  -- Если положительный коэффициент схожести интересов больше чем с пятью пользователями,
--  IF (SELECT COUNT (*) FROM ratios WHERE (userid1 = i_userid OR userid2 = i_userid) AND ratio >=0) > 5 THEN
  -- рекомендуем трек с максимальным рейтингом среди пользователей, с которыми были пересечения
    o_methodid = 7; -- метод выбора из рекомендованных треков
    SELECT rn_trackid, rn_txtrecommendinfo INTO temp_trackid, tmp_txtrecommendinfo FROM getrecommendedtrackid_v6(i_userid);
    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
    IF temp_trackid IS NOT null THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),
              now(),
              null,
              null, 
              i_deviceid,
              temp_trackid,
              o_methodid,
              (SELECT CAST((
                tmp_txtrecommendinfo
                ) AS CHARACTER VARYING)),
              (SELECT CAST((null) AS UUID)) );
    RETURN QUERY 
      SELECT temp_trackid,
      o_methodid,
      (SELECT CAST((null) AS UUID)),
      (SELECT CAST((
        tmp_txtrecommendinfo
        ) AS CHARACTER VARYING));
    RETURN;
    END IF;
--  END IF;

  -- Если таких треков нет - выбираем популярный трек из ни разу не прослушанных пользователем треков
  o_methodid = 3; -- метод выбора популярных из непрослушанных треков
  INSERT INTO temp_track (
  SELECT
    trackid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('популярный трек из непрослушанных пользователем') AS CHARACTER VARYING))
    FROM ratings
      WHERE userid IN (SELECT recid FROM users WHERE experience >= 10)
        AND userid != i_userid
        AND (SELECT recid FROM tracks 
            WHERE recid = trackid
              AND isexist = 1 -- трек существует на сервере
              AND (length >= 120 OR length IS NULL) -- продолжительность трека больше двух минут или длина трека не известна
              AND (iscensorial != 0 OR iscensorial IS NULL)) IS NOT NULL --трек должен быть цензурный или непроверенный
        AND trackid NOT IN (SELECT trackid
              FROM downloadtracks
              WHERE deviceid = i_deviceid)


    GROUP BY trackid
    ORDER BY sum(ratingsum) DESC, RANDOM()
    LIMIT 1);

  -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
  IF FOUND THEN
    INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
    RETURN QUERY SELECT * FROM temp_track;
    RETURN;
  END IF;

  -- Если предыдущие запросы вернули null, выбираем случайный трек
  o_methodid = 1; -- метод выбора случайного трека
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('случайный трек из всех') AS CHARACTER VARYING))
  FROM tracks
  WHERE isexist = 1 -- существующий на сервере 
      AND (iscensorial IS NULL OR iscensorial != 0) -- цензурный
      AND (length > 120 OR length IS NULL) -- продолжительностью более 2х минут
  ORDER BY RANDOM()
  LIMIT 1);
  INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
  RETURN QUERY SELECT * FROM temp_track;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v16(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v17(uuid)

-- DROP FUNCTION public.getnexttrackid_v17(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v17(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying) AS
$BODY$

-- Функция выдачи треков пользователю
DECLARE
  i_userid   UUID = i_deviceid; -- На случай, если устройство еще не зарегистрировано и пользователь не существует
  rnd        INTEGER = (SELECT trunc(random() * 1001)); -- генерируем случайное целое число в диапазоне от 1 до 1000
  o_methodid INTEGER; -- id метода выбора трека
  owntracks  INTEGER; -- количество "своих" треков пользователя (обрезаем на 900 шт)
  arrusers uuid ARRAY; -- массив пользователей для i_userid с неотрицательнымм коэффициентами схожести интересов
  exceptusers uuid ARRAY; -- массив пользователей для i_userid с котороми не было пересечений по трекам
  temp_trackid uuid; 
  tmp_txtrecommendinfo text;
BEGIN

  -- temp_track - временная таблица для промежуточного результата (понадобилась чтобы найденные данные сначала сохранять в таблицу downloadtracks, а потом возвращать
  DROP TABLE IF EXISTS temp_track; 
  CREATE TEMP TABLE temp_track(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying);-- 

-- IF i_deviceid = '6d164484-295e-43fa-a6ca-b42a5896ba68' THEN 
--  INSERT INTO downloadtracks (SELECT uuid_generate_v4(),
--              now(),
--              null,
--              null, 
--              i_deviceid,
--              (SELECT CAST(('11111111-1111-1111-1111-000000000000') AS UUID)) ,
--              o_methodid,
--              (SELECT CAST((
--                tmp_txtrecommendinfo
--                ) AS CHARACTER VARYING)),
--              (SELECT CAST((null) AS UUID)) );
--    RETURN QUERY 
--      SELECT (SELECT CAST(('11111111-1111-1111-1111-000000000000') AS UUID)),
--      1,
--      (SELECT CAST((null) AS UUID)),
--      (SELECT CAST((
--        tmp_txtrecommendinfo
--        ) AS CHARACTER VARYING));
--    RETURN;
-- END IF;

  --Если устройство не было зарегистрировано ранее - регистрируем его
  IF NOT EXISTS(SELECT recid
      FROM devices
      WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
  -- Если устройство зарегистрировано - ищем соответствующего ему пользователя
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;


  -- Выбираем следующий трек

  -- Определяем количество "своих" треков пользователя
  owntracks = (SELECT COUNT(*)
        FROM ratings
          WHERE userid = i_userid
            AND ratingsum >= 0);

  -- Если количество "своих" треков = 0 - выполняем процедуру предрекомендации
  IF (owntracks = 0) THEN
    o_methodid = 8; -- метод выбора из рекомендованных треков
    SELECT o_trackid, o_textinfo INTO temp_trackid, tmp_txtrecommendinfo FROM populartracksrecommend_v1(i_userid);
    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
    IF temp_trackid IS NOT null THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),
              now(),
              null,
              null, 
              i_deviceid,
              temp_trackid,
              o_methodid,
              (SELECT CAST((
                tmp_txtrecommendinfo
                ) AS CHARACTER VARYING)),
              (SELECT CAST((null) AS UUID)) );
    RETURN QUERY 
      SELECT temp_trackid,
      o_methodid,
      (SELECT CAST((null) AS UUID)),
      (SELECT CAST((
        tmp_txtrecommendinfo
        ) AS CHARACTER VARYING));
    RETURN;
    END IF;
  END IF;
--  IF (rnd < owntracks)
--  THEN
--    o_methodid = 2; -- метод выбора из своих треков
--    INSERT INTO temp_track (
--    SELECT
--      trackid, -- выбираем id трека
--      o_methodid,
--      (SELECT CAST((null) AS UUID)),
--      (SELECT CAST(('случайный трек из своих') AS CHARACTER VARYING))
--    FROM ratings -- из треков, имеющих рейтинг для данного пользователя
--    WHERE userid = i_userid
--        AND lastlisten < localtimestamp - INTERVAL '1 day' -- для которого последнее прослушивание было ранее, чем за сутки до выдачи
--        AND ratingsum >= 0 -- рейтинг трека неотрицательный
--        AND (SELECT isexist
--           FROM tracks
--           WHERE recid = trackid) = 1 -- трек существует на сервере
--        AND ((SELECT length
--          FROM tracks
--          WHERE recid = trackid) >= 120 -- продолжительность трека больше двух минут
--           OR (SELECT length
--             FROM tracks
--             WHERE recid = trackid) IS NULL) -- или длина трека не известна
--        AND ((SELECT iscensorial
--          FROM tracks
--          WHERE recid = trackid) IS NULL -- трек должен быть цензурный или непроверенный
--           OR (SELECT iscensorial
--             FROM tracks
--             WHERE recid = trackid) != 0)
--        AND trackid NOT IN (SELECT trackid
--                  FROM downloadtracks
--                  WHERE reccreated > localtimestamp - INTERVAL '1 week' AND deviceid = i_deviceid) -- трек недолжен быть выдан в последнюю неделю
--    ORDER BY RANDOM()
--    LIMIT 1);

--    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
--    IF FOUND THEN
--      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
--      RETURN QUERY SELECT * FROM temp_track;
--      RETURN;
--    END IF;
--  END IF;

  -- Если rnd больше количества "своих" треков - используем алгоритм рекоммендаций

  -- Если положительный коэффициент схожести интересов больше чем с пятью пользователями,
--  IF (SELECT COUNT (*) FROM ratios WHERE (userid1 = i_userid OR userid2 = i_userid) AND ratio >=0) > 5 THEN
  -- рекомендуем трек с максимальным рейтингом среди пользователей, с которыми были пересечения
    o_methodid = 7; -- метод выбора из рекомендованных треков
    SELECT rn_trackid, rn_txtrecommendinfo INTO temp_trackid, tmp_txtrecommendinfo FROM getrecommendedtrackid_v5(i_userid);
    -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
    IF temp_trackid IS NOT null THEN
      INSERT INTO downloadtracks (SELECT uuid_generate_v4(),
              now(),
              null,
              null, 
              i_deviceid,
              temp_trackid,
              o_methodid,
              (SELECT CAST((
                tmp_txtrecommendinfo
                ) AS CHARACTER VARYING)),
              (SELECT CAST((null) AS UUID)) );
    RETURN QUERY 
      SELECT temp_trackid,
      o_methodid,
      (SELECT CAST((null) AS UUID)),
      (SELECT CAST((
        tmp_txtrecommendinfo
        ) AS CHARACTER VARYING));
    RETURN;
    END IF;
--  END IF;

  -- Если таких треков нет - выбираем популярный трек из ни разу не прослушанных пользователем треков
  o_methodid = 3; -- метод выбора популярных из непрослушанных треков
  INSERT INTO temp_track (
  SELECT
    trackid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('популярный трек из непрослушанных пользователем') AS CHARACTER VARYING))
    FROM ratings
      WHERE userid IN (SELECT recid FROM users WHERE experience >= 10)
        AND userid != i_userid
        AND (SELECT recid FROM tracks 
            WHERE recid = trackid
              AND isexist = 1 -- трек существует на сервере
              AND (iscorrect IS NULL OR iscorrect != 0)
              AND (length >= 120 OR length IS NULL) -- продолжительность трека больше двух минут или длина трека не известна
              AND (iscensorial != 0 OR iscensorial IS NULL)) IS NOT NULL --трек должен быть цензурный или непроверенный
        AND trackid NOT IN (SELECT trackid
              FROM downloadtracks
              WHERE deviceid = i_deviceid)


    GROUP BY trackid
    ORDER BY sum(ratingsum) DESC, RANDOM()
    LIMIT 1);

  -- Если такой трек найден - запись информацию о нем в downloadtracks, выход из функции, возврат найденного значения
  IF FOUND THEN
    INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
    RETURN QUERY SELECT * FROM temp_track;
    RETURN;
  END IF;

  -- Если предыдущие запросы вернули null, выбираем случайный трек
  o_methodid = 1; -- метод выбора случайного трека
  INSERT INTO temp_track (
  SELECT
    recid,
    o_methodid,
    (SELECT CAST((null) AS UUID)),
    (SELECT CAST(('случайный трек из всех') AS CHARACTER VARYING))
  FROM tracks
  WHERE isexist = 1 -- существующий на сервере 
    AND (iscorrect IS NULL OR iscorrect != 0)
      AND (iscensorial IS NULL OR iscensorial != 0) -- цензурный
      AND (length > 120 OR length IS NULL) -- продолжительностью более 2х минут
  ORDER BY RANDOM()
  LIMIT 1);
  INSERT INTO downloadtracks (SELECT uuid_generate_v4(),now(),null, null, i_userid, temp_track.track AS trackid, temp_track.methodid AS methodid, temp_track.txtrecommendedinfo AS txtrecommendinfo, temp_track.useridrecommended AS userrecommend FROM temp_track);
  RETURN QUERY SELECT * FROM temp_track;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v17(uuid)
  OWNER TO postgres;


-- Function: public.getnexttrackid_v18_test(uuid)

-- DROP FUNCTION public.getnexttrackid_v18_test(uuid);

CREATE OR REPLACE FUNCTION public.getnexttrackid_v18_test(IN i_deviceid uuid)
  RETURNS TABLE(track uuid, methodid integer, useridrecommended uuid, txtrecommendedinfo character varying) AS
$BODY$
DECLARE
  i_userid   UUID = i_deviceid; --пока не реалезовано объединение пользователей - гуиды одинаковые
  ex_userid uuid;
  rate integer;

BEGIN

-- функция рекомендаций (способ 1)
-- для тестирования соотношения скорости хранимых процедур и hibernate
  SELECT r2.userid
     , SUM(r.ratingsum * r2.ratingsum) as s INTO ex_userid, rate
     FROM ratings r
       INNER JOIN ratings r2 ON r.trackid = r2.trackid
     AND r.userid != r2.userid
     AND (
         r.userid = i_userid
         AND r2.userid IN (SELECT recid FROM users WHERE experience >= 10)
         ) 

    GROUP BY  r2.userid
    ORDER BY s DESC
    LIMIT 1;
    
    RETURN QUERY
    SELECT
      trackid,
      1,
      ex_userid,
      CAST ((concat('Коэффициент схожести ', rate)) AS CHARACTER VARYING)
    FROM ratings
    WHERE userid = ex_userid
        AND ratingsum > 0
        AND trackid NOT IN (SELECT trackid FROM ratings WHERE userid = i_userid)
        -- AND trackid NOT IN (SELECT trackid
--                  FROM downloadtracks
--                  WHERE deviceid = i_deviceid
--                    --AND reccreated > localtimestamp - INTERVAL '1 day'
--                    )
--        AND (SELECT isexist
--           FROM tracks
--           WHERE recid = trackid) = 1
--        AND ((SELECT length
--          FROM tracks
--          WHERE recid = trackid) >= 120
--           OR (SELECT length
--             FROM tracks
--             WHERE recid = trackid) IS NULL)
--        AND ((SELECT iscensorial
--          FROM tracks
--          WHERE recid = trackid) IS NULL
--           OR (SELECT iscensorial
--             FROM tracks
--             WHERE recid = trackid) != 0)
    ORDER BY ratingsum DESC--, RANDOM()
    LIMIT 1;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getnexttrackid_v18_test(uuid)
  OWNER TO postgres;


-- Function: public.getrecommendedtrackid_v2(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v2(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v2(IN in_userid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_sum_rate bigint) AS
$BODY$

--DECLARE
--preferenced_track uuid;

BEGIN
RETURN QUERY (
  -- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
  -- у конкретного пользователя для возможности вывода дополнительной информации о треке
  -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid, table2.sum_rate-- INTO preferenced_track
    --tracks.recid, table2.sum_rate, tracks.localdevicepathupload, tracks.path
        FROM tracks
        INNER JOIN (
          --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
          --каждого из них
          SELECT trackid, SUM(track_rating) AS sum_rate
          FROM(
            --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
            --с исходным, умноженным на их коэффициент
            SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
            FROM ratings


              --------------------------------------------------
              ---------------НОВЫЙ INNER JOIN-------------------
              --------------------------------------------------

              INNER JOIN
              (
                --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                --с таблицой с UUID'ми всех экспертов.
                --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                --качестве коэффициента
                SELECT COALESCE(associated_experts.ratio, 1) AS ratio, all_experts.userid AS expert_id
                FROM
                (
                  --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                  --и UUID'ы этих экспертов
                  SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                  FROM ratios
                  WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience = 10)
                ) AS associated_experts
                RIGHT JOIN 
                (
                  --Выберем UUID'ы всех экспертов
                  SELECT recid AS userid
                  FROM users
                  WHERE experience = 10
                ) AS all_experts
                ON associated_experts.userid = all_experts.userid
              ) AS experts_ratios
              ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
              AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
              




              
              --------------------------------------------------
              --------------СТАРЫЙ INNER JOIN-------------------
              --------------------------------------------------
              
              -- INNER JOIN ratios
--              --Выбираем рейтинги треков у тех пользователей, у которых есть пересечение
--              --с исходным в таблице ratios (кэффициенты совпадения вкусов), проверяя сначала
--              --с левой стороны
--              ON ((ratings.userid = ratios.userid2 AND ratios.userid1 = in_userid)
--                -- потом с правой
--                OR (ratings.userid = ratios.userid1 AND ratios.userid2 = in_userid))

 --             AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
 --             AND ratios.ratio > 0 --Считать рейтинги треков, только у пользователей с положительным коэффициентом совпадения вкусов с исходным




              
          ) AS TracksRatings
          GROUP BY trackid
          ORDER BY sum_rate DESC
        ) AS table2
        ON tracks.recid = table2.trackid
        AND tracks.isexist = 1 --Трек должен существовать на сервере
        AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
        AND tracks.length >= 120
        --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев
        AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                     WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
        AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
        ORDER BY table2.sum_rate DESC
           --Сортировка по второму столбцу нужна для случаев, когда получаем много треков с одинковым table2.sum_rate,
           --в таких случаях план выполнения запроса меняется и производительность сильно падает
           --,tracks.recid
           ,random()
        LIMIT 1);
  --RETURN preferenced_track;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v2(uuid)
  OWNER TO postgres;


-- Function: public.getrecommendedtrackid_v3(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v3(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v3(IN in_userid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_sum_rate bigint, rn_rnd_in_range double precision) AS
$BODY$
BEGIN

DROP TABLE IF EXISTS tracks_with_sum_rates;
CREATE TEMP TABLE tracks_with_sum_rates
AS
-- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
  -- у конкретного пользователя для возможности вывода дополнительной информации о треке
  -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid AS track_id, tracks_sum_rates.sum_rate AS track_sum_rate-- INTO preferenced_track
    --tracks.recid, tracks_sum_rates.sum_rate, tracks.localdevicepathupload, tracks.path
        FROM tracks
        INNER JOIN (
          --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
          --каждого из них
          SELECT trackid, SUM(track_rating) AS sum_rate
          FROM(
            --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
            --с исходным, умноженным на их коэффициент
            SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
            FROM ratings
              INNER JOIN
              (
                --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                --с таблицой с UUID'ми всех экспертов.
                --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                --качестве коэффициента
                SELECT COALESCE(associated_experts.ratio, 1) AS ratio, all_experts.userid AS expert_id
                FROM
                (
                  --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                  --и UUID'ы этих экспертов
                  SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                  FROM ratios
                  WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience = 10)
                ) AS associated_experts
                RIGHT JOIN 
                (
                  --Выберем UUID'ы всех экспертов
                  SELECT recid AS userid
                  FROM users
                  WHERE experience = 10
                ) AS all_experts
                ON associated_experts.userid = all_experts.userid
              ) AS experts_ratios
              ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
              AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
          ) AS tracks_ratings
          GROUP BY trackid
          ORDER BY sum_rate DESC
        ) AS tracks_sum_rates
        ON tracks.recid = tracks_sum_rates.trackid
        AND tracks.isexist = 1 --Трек должен существовать на сервере
        AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
        AND tracks.length >= 120
        --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев
        AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                     WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
        AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
        ORDER BY tracks_sum_rates.sum_rate DESC;
           --Сортировка по второму столбцу нужна для случаев, когда получаем много треков с одинковым tracks_sum_rates.sum_rate,
           --в таких случаях план выполнения запроса меняется и производительность сильно падает
           --,tracks.recid
           --,random()
        --LIMIT 1


RETURN QUERY
  WITH rnd_in_range_table AS (
  SELECT random() * (SELECT MAX(tracks_with_sum_rates.track_sum_rate) FROM tracks_with_sum_rates AS tracks_with_sum_rates) AS rnd_in_range
  )
  SELECT *
  FROM (
      SELECT tracks_with_sum_rates.track_id, tracks_with_sum_rates.track_sum_rate, rnd_in_range
      FROM tracks_with_sum_rates CROSS JOIN rnd_in_range_table
  ) T
  WHERE track_sum_rate >= rnd_in_range
  ORDER BY track_sum_rate
    ,random()
  LIMIT 1;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v3(uuid)
  OWNER TO postgres;

-- Function: public.getrecommendedtrackid_v4(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v4(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v4(IN in_userid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_txtrecommendinfo text) AS
$BODY$

BEGIN
RETURN QUERY (
  SELECT result_table.track_id, 'getrecommendedtrackid_v4; sum_rate:' || result_table.sum_track_rate::text || '; rnd:' || result_table.rnd::text || '; rnd_sum_rate:' || (result_table.sum_track_rate * result_table.rnd)::text
  FROM
  (
  -- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
  -- у конкретного пользователя для возможности вывода дополнительной информации о треке
  -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid AS track_id, random() AS rnd, table2.sum_rate AS sum_track_rate-- INTO preferenced_track
    --tracks.recid, table2.sum_rate, tracks.localdevicepathupload, tracks.path
        FROM tracks
        INNER JOIN (
          --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
          --каждого из них
          SELECT trackid, SUM(track_rating) AS sum_rate
          FROM(
            --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
            --с исходным, умноженным на их коэффициент
            SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
            FROM ratings
            INNER JOIN
              (
                --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                --с таблицой с UUID'ми всех экспертов.
                --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                --качестве коэффициента
                SELECT COALESCE(associated_experts.ratio, 1) AS ratio, all_experts.userid AS expert_id
                FROM
                (
                  --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                  --и UUID'ы этих экспертов
                  SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                  FROM ratios
                  WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience = 10)
                ) AS associated_experts
                RIGHT JOIN 
                (
                  --Выберем UUID'ы всех экспертов
                  SELECT recid AS userid
                  FROM users
                  WHERE experience = 10
                ) AS all_experts
                ON associated_experts.userid = all_experts.userid
              ) AS experts_ratios
              ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
              AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
          ) AS TracksRatings
          GROUP BY trackid
          ORDER BY sum_rate DESC
        ) AS table2
        ON tracks.recid = table2.trackid
        AND tracks.isexist = 1 --Трек должен существовать на сервере
        AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
        AND tracks.length >= 120
        --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев
        AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                     WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
        AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
        ORDER BY table2.sum_rate DESC
           --Сортировка по второму столбцу нужна для случаев, когда получаем много треков с одинковым table2.sum_rate,
           --в таких случаях план выполнения запроса меняется и производительность сильно падает
           --,tracks.recid
           --,random()
        --LIMIT 100
        ) AS result_table
        --ORDER BY rnd_sum_rate DESC
        ORDER BY result_table.sum_track_rate * result_table.rnd DESC
        LIMIT 1
        );
  --RETURN preferenced_track;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v4(uuid)
  OWNER TO postgres;

-- Function: public.getrecommendedtrackid_v5(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v5(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v5(IN in_userid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_txtrecommendinfo text) AS
$BODY$

DECLARE 
rnd DOUBLE PRECISION;
execution_start_time TIMESTAMP;
tracks_with_sum_rates_created_time TIMESTAMP;
rnd_generated_time TIMESTAMP;

tracks_with_sum_rates_creation_time_txt TEXT;
rnd_generation_time_txt TEXT;

BEGIN
  --Время начала выполнения тела функции
  SELECT timeofday()::timestamp INTO execution_start_time;

  DROP TABLE IF EXISTS tracks_with_sum_rates;
  CREATE TEMP TABLE tracks_with_sum_rates
  AS
  -- Соединяем таблицу tracks с таблицой сумм произведений рейтинга трека на коэффициент
    -- у конкретного пользователя для возможности вывода дополнительной информации о треке
    -- в отладочных целях и для фильтра по столбцам tracks
    SELECT tracks.recid AS track_id, tracks_sum_rates.sum_rate AS track_sum_rate-- INTO preferenced_track
    --tracks.recid, tracks_sum_rates.sum_rate, tracks.localdevicepathupload, tracks.path
          FROM tracks
          INNER JOIN (
            --Группируем по треку и считаем сумму произведений рейтингов на коэффициент для
            --каждого из них
            SELECT trackid, SUM(track_rating) AS sum_rate
            FROM(
              --Запрашиваем таблицу с рейтингом всех треков, оцененных пользователями, которые имеют коэффициент
              --с исходным, умноженным на их коэффициент
              SELECT ratings.trackid, ratings.ratingsum * experts_ratios.ratio AS track_rating, ratings.userid--, ratios.ratio
              FROM ratings
                INNER JOIN
                (
                  --Соединим таблицу коэффициентов совпадения вкусов исходного пользователя с экспертами
                  --с таблицой с UUID'ми всех экспертов.
                  --Если у исходного пользователя нет пересечения с каким-либо экспертом, то вернем 1 в
                  --качестве коэффициента
                  SELECT COALESCE(associated_experts.ratio, 0.7) AS ratio, all_experts.userid AS expert_id
                  FROM
                  (
                    --Выберем коэффициенты исходно пользователя с кем-либо из экспертов
                    --и UUID'ы этих экспертов
                    SELECT ratios.ratio AS ratio, ratios.userid2 AS userid
                    FROM ratios
                    WHERE ratios.userid1 = in_userid AND ratios.userid2 IN (SELECT recid FROM users WHERE experience >= 10)
                  ) AS associated_experts
                  RIGHT JOIN 
                  (
                    --Выберем UUID'ы всех экспертов
                    SELECT recid AS userid
                    FROM users
                    WHERE experience >= 10
                  ) AS all_experts
                  ON associated_experts.userid = all_experts.userid
                ) AS experts_ratios
                ON ratings.userid = experts_ratios.expert_id-- AND ratios.userid1 = in_userid
                AND ratings.userid <> in_userid --Выбирем все оценки треков, кроме оценок, данных исходным пользователем
                AND experts_ratios.ratio > 0 --Считать рейтинги треков, только у пользователей с положительным коэффициентом совпадения вкусов с исходным
            ) AS tracks_ratings
            GROUP BY trackid
            ORDER BY sum_rate DESC
          ) AS tracks_sum_rates
          ON tracks.recid = tracks_sum_rates.trackid
          AND tracks.isexist = 1 --Трек должен существовать на сервере
          AND (iscorrect IS NULL OR iscorrect <> 0) -- Трек не должен быть битым
          AND tracks.iscensorial <> 0 --Трек не должен быть помечен как нецензурный
          AND tracks.length >= 120
          
          --Трек не должен был выдаваться исходному пользователю в течении последних двух месяцев (пока заменено на условие ниже)
          --AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                 --WHERE reccreated > localtimestamp - INTERVAL '2 months' AND deviceid = in_userid)
                 
          --Трек не должен был выдаваться исходному пользователю вообще никогда
          AND tracks.recid NOT IN (SELECT trackid FROM downloadtracks
                 WHERE deviceid = in_userid)
                 
          AND sum_rate >= 0 --В итоге рекомендоваться будут только треки с положительной суммой произведений рейтингов на коэффициенты
          ORDER BY tracks_sum_rates.sum_rate DESC;


  --Время после создания таблицы tracks_with_sum_rates
  SELECT timeofday()::timestamp INTO tracks_with_sum_rates_created_time;
  
  --От текущего времени отнимаем execution_start_time и приводим к миллисекундам (numeric(18,3)), затем записываем в строковую переменную tracks_with_sum_rates_creation_time
  --Таким образом вычисленно время создания временной таблицы tracks_with_sum_rates
  SELECT (cast(extract(epoch from (tracks_with_sum_rates_created_time - execution_start_time)) as numeric(18,3)))::text INTO tracks_with_sum_rates_creation_time_txt;
        
  --Сгруппируем треки по рейтингу и умножим рандомное число от 0 до 1 на сумму этих рейтингов
  --полученное число запишем в переменную rnd
  --Сумма рейтингов групп треков обозначает общую область вероятности рекомендации трека из какой-либо группы,
  --где сумма рейтингов группы n с рейтингом группы n + 1 (упорядоченных по возрастанию рейтинга) обозначает
  --область вероятности рекомендации трека из группы n + 1
  --Группа, из которой в итоге порекомендуется трек, будет определяется числом в переменной rnd
  SELECT (random() * SUM(groups_by_rate.group_rate)) INTO rnd FROM
  ( --NULLIF возвращает NULL, если rate == 0, а COALESCE возвращает 0.3,
    --если NULLIF вернет NULL, соответственно оператор просто выставляет
    --рейтинг 0.3 трекам с рейтингом 0
    SELECT COALESCE(NULLIF(track_sum_rate, 0), 0.3) AS group_rate FROM tracks_with_sum_rates
    GROUP BY track_sum_rate
    ORDER BY track_sum_rate
  ) AS groups_by_rate;

  --Время после генерации rnd
  SELECT timeofday()::timestamp INTO rnd_generated_time;

  --Время, затраченное на генерацию rnd
  SELECT (cast(extract(epoch from (rnd_generated_time - tracks_with_sum_rates_created_time)) as numeric(18,3)))::text INTO rnd_generation_time_txt;

  RETURN QUERY
  (
    --Выберем рандомный трек из группы, отобранной во вложенной запросе
    SELECT track_id, 'getrecommendedtrackid_v5; sum_rate:' || track_sum_rate::text || '; rnd_in_range:' || rnd::text || '; temp_table_creation:' || tracks_with_sum_rates_creation_time_txt || '; rnd_creation:' || rnd_generation_time_txt
    FROM tracks_with_sum_rates
    WHERE track_sum_rate = 
    (
      --Если рейтинг полученной группы окажется равен 0.3, то заменяем на 0
      --т.к. треков с рейтингом 0.3 не существует. Им присваивалось значение 0.3 вместо 0
      --чтобы они так же имели небольшой шанс рекомендоваться для прослушивания
      SELECT COALESCE(NULLIF(groups_by_rate_with_range_max.track_sum_rate, 0.3), 0)
      FROM
      (
          --Выберем первую группу треков из групп, упорядоченных по возрастанию рейтинга, рейтинг которой окажется больше числа rnd
          SELECT SUM(groups_by_rate.group_rate) OVER (ORDER BY groups_by_rate.group_rate) AS group_range_max, groups_by_rate.group_rate AS track_sum_rate, rnd as rnd_in_range
          FROM
          (
        SELECT COALESCE(NULLIF(track_sum_rate, 0), 0.3) AS group_rate FROM tracks_with_sum_rates
        GROUP BY track_sum_rate
        ORDER BY track_sum_rate
          ) AS groups_by_rate
      ) AS groups_by_rate_with_range_max
      WHERE group_range_max >= rnd
      ORDER BY group_range_max
      LIMIT 1
    )
    ORDER BY random()
    LIMIT 1
  );  
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v5(uuid)
  OWNER TO postgres;

-- Function: public.getrecommendedtrackid_v5_debug(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v5_debug(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v5_debug(IN in_uuid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_txtrecommendinfo text) AS
$BODY$

BEGIN

  DROP TABLE IF EXISTS test_results;
  CREATE TEMP TABLE test_results
  (
    r_track_id UUID,
    r_info TEXT
  );
  
  FOR i IN 1..100
  LOOP
  INSERT INTO test_results
    SELECT * FROM getrecommendedtrackid_v5(in_uuid);
  END LOOP;

  RETURN QUERY
    SELECT * FROM test_results;
  
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v5_debug(uuid)
  OWNER TO postgres;

-- Function: public.getrecommendedtrackid_v6(uuid)

-- DROP FUNCTION public.getrecommendedtrackid_v6(uuid);

CREATE OR REPLACE FUNCTION public.getrecommendedtrackid_v6(IN in_uuid uuid)
  RETURNS TABLE(rn_trackid uuid, rn_txtrecommendinfo text) AS
$BODY$

BEGIN

-- Алгоритм рекомендацией для пользователя Z
-- Шаг 1. Выбрать не более 5 пользователей экспертов с наибольшим кол-вом совпадающих прослушиваний треков с пользователем Z
-- Шаг 2. Выбрать не более 1 пользователя эксперта с наибольшим кол-вом совпадающих пропусков треков с пользователем Z
-- Шаг 3. Выбрать среди данных 6 экспертов 10 треков прослушанных наибольшим кол-вом пользователей среди данных 6-ти экспертов
-- Шаг 4. Выбрать случайный трек среди получившихся 10


  RETURN QUERY 
    (SELECT trackid, 'getrecommendedtrackid_v6; count_listen:' || tracks.count::text -- Шаг 4
      FROM (SELECT trackid, count(*) as count -- Шаг 3
        FROM histories
          WHERE (deviceid IN 
            (SELECT deviceid FROM ( -- Шаг 1
              SELECT deviceid, count(*) as countListen 
                 FROM histories 
                 WHERE  islisten = 1 AND deviceid != in_uuid AND deviceid in (SELECT recid FROM users WHERE experience >= 10) 
                  AND trackid in (SELECT trackid FROM histories WHERE deviceid = in_uuid AND islisten = 1)  
                 GROUP BY deviceid
                 ORDER BY countListen DESC
                 LIMIT 5) as res1)
            
            OR deviceid IN ( 
              SELECT deviceid FROM ( -- Шаг 2
                SELECT deviceid, count(*) as countListen 
                   FROM histories 
                   WHERE  islisten = -1 AND deviceid != in_uuid AND deviceid in (SELECT recid FROM users WHERE experience >= 10) 
                    AND trackid in (SELECT trackid FROM histories WHERE deviceid = in_uuid AND islisten = -1)  
                   GROUP BY deviceid
                   ORDER BY countListen DESC
                   LIMIT 1) as res2
                   )
            )
            AND trackid NOT IN (SELECT trackid FROM histories WHERE deviceid = in_uuid)
            AND trackid NOT IN (SELECT trackid FROM downloadtracks WHERE deviceid = in_uuid)
            
        GROUP BY trackid
        ORDER BY count DESC
        LIMIT 10) AS tracks
      ORDER BY random()
      LIMIT 1);
  
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getrecommendedtrackid_v6(uuid)
  OWNER TO postgres;

-- Function: public.gettrackshistorybydevice(uuid, integer)

-- DROP FUNCTION public.gettrackshistorybydevice(uuid, integer);

CREATE OR REPLACE FUNCTION public.gettrackshistorybydevice(
    IN i_deviceid uuid,
    IN i_count integer)
  RETURNS TABLE(downloadtrackrecid character varying, historyrecid character varying) AS
$BODY$
BEGIN
  IF i_count < 0 THEN
    i_count = null;
  END IF;
  RETURN QUERY SELECT CAST((d.recid) AS CHARACTER VARYING), CAST((h.recid) AS CHARACTER VARYING)
         FROM downloadtracks d
           LEFT OUTER JOIN histories h
             ON h.deviceid = d.deviceid AND h.trackid = d.trackid
         WHERE d.deviceid = i_deviceid
         ORDER BY d.reccreated DESC, h.reccreated DESC, h.lastlisten DESC
         LIMIT i_count;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.gettrackshistorybydevice(uuid, integer)
  OWNER TO postgres;


-- Function: public.gettracksratingbydevice(uuid, integer)

-- DROP FUNCTION public.gettracksratingbydevice(uuid, integer);

CREATE OR REPLACE FUNCTION public.gettracksratingbydevice(
    IN i_deviceid uuid,
    IN i_count integer)
  RETURNS TABLE(downloadtrackrecid character varying, ratingsrecid character varying) AS
$BODY$

-- Возвращает id записей о загруженных треках и отданой по ним статистике
BEGIN
  IF i_count < 0 THEN
    i_count = null;
  END IF;
  RETURN QUERY SELECT CAST((d.recid) AS CHARACTER VARYING), CAST((r.recid) AS CHARACTER VARYING)
         FROM downloadtracks d
           LEFT OUTER JOIN ratings r
             ON r.userid = (SELECT userid FROM devices WHERE recid = d.deviceid) AND r.trackid = d.trackid
         WHERE d.deviceid = i_deviceid
         ORDER BY d.reccreated DESC, r.reccreated DESC, r.lastlisten DESC
         LIMIT i_count;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.gettracksratingbydevice(uuid, integer)
  OWNER TO postgres;

-- Function: public.getuploadersrating()

-- DROP FUNCTION public.getuploadersrating();

CREATE OR REPLACE FUNCTION public.getuploadersrating()
  RETURNS TABLE(userid character varying, uploadtracks bigint, lastactive character varying) AS
$BODY$

BEGIN
  --функция возвращает рейтинг uploader'ов
  RETURN QUERY SELECT CAST((u.recid) AS CHARACTER VARYING), COUNT(t.recid), CAST((MAX(t.reccreated)) AS CHARACTER VARYING)
  FROM users u
    INNER JOIN tracks t
      ON u.recid = t.deviceid
  GROUP BY u.recid
  ORDER BY MAX(t.reccreated) DESC;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getuploadersrating()
  OWNER TO postgres;

-- Function: public.getuserdevices(uuid)

-- DROP FUNCTION public.getuserdevices(uuid);

CREATE OR REPLACE FUNCTION public.getuserdevices(IN i_userid uuid)
  RETURNS TABLE(recid uuid, reccreated timestamp without time zone, recname character varying, recupdated timestamp without time zone, userid uuid) AS
$BODY$
BEGIN
  RETURN QUERY
  SELECT * FROM devices WHERE devices.userid = i_userid;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getuserdevices(uuid)
  OWNER TO postgres;

-- Function: public.getusersrating(integer)

-- DROP FUNCTION public.getusersrating(integer);

CREATE OR REPLACE FUNCTION public.getusersrating(IN i_count integer)
  RETURNS TABLE(tuserid character varying, treccreated character varying, trecname character varying, trecupdated character varying, towntracks bigint, tlasttracks bigint) AS
$BODY$

BEGIN
  IF i_count < 0 THEN
    i_count = null;
  END IF;
RETURN QUERY SELECT CAST((res1.recid) AS CHARACTER VARYING), CAST((res1.reccreated) AS CHARACTER VARYING), res1.recname, CAST((res1.recupdated) AS CHARACTER VARYING), res1.owntracks, COUNT(res2.userid) AS lasttracks
FROM
  (SELECT u.recid, u.reccreated, u.recname, u.recupdated, COUNT(r.recid) AS owntracks
    FROM users u
    LEFT OUTER JOIN ratings r ON u.recid = r.userid
    GROUP BY u.recid) res1
  LEFT OUTER JOIN (SELECT d.reccreated, dev.userid FROM downloadtracks d
        INNER JOIN devices dev
        ON dev.recid= d.deviceid AND d.reccreated > localtimestamp - INTERVAL '1 day') res2
    ON res2.userid = res1.recid
  GROUP BY res1.recid, res1.reccreated, res1.recname, res1.recupdated, res1.owntracks
  ORDER BY lasttracks DESC, owntracks DESC
  LIMIT i_count;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getusersrating(integer)
  OWNER TO postgres;

-- Function: public.getuserstracks(uuid)

-- DROP FUNCTION public.getuserstracks(uuid);

CREATE OR REPLACE FUNCTION public.getuserstracks(IN i_userid uuid)
  RETURNS TABLE(tuserid character varying, listentracks bigint, downloadtracks bigint) AS
$BODY$

BEGIN
  RETURN QUERY SELECT CAST((res1.userid) AS CHARACTER VARYING), res1.owntracks, COUNT(res2.userid)
    FROM (SELECT userid, COUNT(recid) AS owntracks -- считаем все прослушанные пользователем треки
      FROM ratings
      WHERE userid = i_userid
      GROUP BY userid) res1
    LEFT OUTER JOIN (SELECT dev.userid FROM downloadtracks d -- выбираем все выданные пользователю треки
          INNER JOIN devices dev
          ON dev.recid = d.deviceid 
        ) res2
      ON res2.userid = res1.userid
    GROUP BY res1.userid, res1.owntracks;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION public.getuserstracks(uuid)
  OWNER TO postgres;


-- Function: public.populartracksrecommend_v1(uuid)

-- DROP FUNCTION public.populartracksrecommend_v1(uuid);

CREATE OR REPLACE FUNCTION public.populartracksrecommend_v1(
    IN i_userid uuid,
    OUT o_trackid uuid,
    OUT o_textinfo character varying)
  RETURNS record AS
$BODY$

-- Функция выдачи треков пользователю, не имеющему пользователей со схожим вкусом
BEGIN

  WITH exclude_users AS (
    SELECT r.userid 
      FROM downloadtracks d
        INNER JOIN ratings r
          ON d.trackid = r.trackid
      WHERE d.deviceid = i_userid
        AND r.userid IN (SELECT recid FROM users WHERE experience >= 10)
      GROUP BY r.userid)
  SELECT recid, 'предрекомендация, суммарный рейтинг трека ' || rate INTO o_trackid, o_textinfo 
    FROM (
    SELECT t.recid, SUM(r.ratingsum) AS rate
      FROM tracks t
        INNER JOIN ratings r
          ON t.recid = r.trackid    
            AND r.userid IN (SELECT recid FROM users WHERE experience >= 10)
      WHERE t.recid NOT IN (SELECT trackid FROM ratings WHERE userid IN (SELECT * FROM exclude_users) GROUP BY trackid)
        AND isexist = 1
        AND (iscorrect IS NULL OR iscorrect <> 0)
        AND (iscensorial IS NULL OR iscensorial != 0)
        AND (length > 120 OR length IS NULL)
      GROUP BY t.recid
      ORDER BY rate DESC) AS res
    WHERE rate > 0
    LIMIT 1;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.populartracksrecommend_v1(uuid)
  OWNER TO postgres;


-- Function: public.registerdevice(uuid, character varying)

-- DROP FUNCTION public.registerdevice(uuid, character varying);

CREATE OR REPLACE FUNCTION public.registerdevice(
    i_deviceid uuid,
    i_devicename character varying)
  RETURNS boolean AS
$BODY$
BEGIN
  -- Функция регистрации нового устройства

  -- Добавляем устройство, если его еще не существует
  -- Если ID устройства еще нет в БД
  IF NOT EXISTS(SELECT recid
          FROM devices
          WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_deviceid,
               i_devicename,
               now()
             WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_deviceid)
    ON CONFLICT (recid) DO NOTHING;

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
             i_deviceid,
             i_deviceid,
             i_devicename,
             now()
    ON CONFLICT (recid) DO NOTHING;
  END IF;
  RETURN TRUE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.registerdevice(uuid, character varying)
  OWNER TO postgres;

-- Function: public.registertrack_v2(uuid, character varying, character varying, uuid, character varying, character varying, integer, integer)

-- DROP FUNCTION public.registertrack_v2(uuid, character varying, character varying, uuid, character varying, character varying, integer, integer);

CREATE OR REPLACE FUNCTION public.registertrack_v2(
    i_trackid uuid,
    i_localdevicepathupload character varying,
    i_path character varying,
    i_deviceid uuid,
    i_title character varying,
    i_artist character varying,
    i_length integer,
    i_size integer)
  RETURNS boolean AS
$BODY$
DECLARE
  i_userid    UUID = i_deviceid;
  i_historyid UUID;
  i_ratingid  UUID;
BEGIN
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  SELECT uuid_generate_v4()
  INTO i_historyid;
  SELECT uuid_generate_v4()
  INTO i_ratingid;

  --
  -- Функция добавляет запись о треке в таблицу треков и делает сопутствующие записи в
  -- таблицу статистики прослушивания и рейтингов. Если пользователя, загружающего трек
  -- нет в базе, то он добавляется в таблицу пользователей.
  --

  -- Добавляем устройство, если его еще не существует
  -- Если ID устройства еще нет в БД
  IF NOT EXISTS(SELECT recid
          FROM devices
          WHERE recid = i_deviceid)
  THEN

    -- Добавляем нового пользователя
    INSERT INTO users (recid, recname, reccreated) SELECT
               i_userid,
               'New user recname',
               now()
    WHERE NOT EXISTS(SELECT recid FROM users WHERE recid = i_userid);

    -- Добавляем новое устройство
    INSERT INTO devices (recid, userid, recname, reccreated) SELECT
               i_deviceid,
               i_userid,
               'New device recname',
               now();
  ELSE
    SELECT (SELECT userid
        FROM devices
        WHERE recid = i_deviceid
        LIMIT 1)
    INTO i_userid;
  END IF;

  -- Добавляем трек в базу данных
  INSERT INTO tracks (recid, localdevicepathupload, path, deviceid, reccreated, iscensorial, isexist, recname, artist, length, size)
  VALUES (i_trackid, i_localdevicepathupload, i_path, i_deviceid, now(), 2, 1, i_title, i_artist, i_length, i_size);

  -- Добавляем запись о прослушивании трека в таблицу истории прослушивания
  INSERT INTO histories (recid, deviceid, trackid, isListen, lastListen, reccreated)
  VALUES (i_historyid, i_deviceid, i_trackid, 1, now(), now());

  -- Добавляем запись в таблицу рейтингов
  INSERT INTO ratings (recid, userid, trackid, lastListen, ratingsum, reccreated)
  VALUES (i_ratingid, i_userid, i_trackid, now(), 1, now());

  RETURN TRUE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.registertrack_v2(uuid, character varying, character varying, uuid, character varying, character varying, integer, integer)
  OWNER TO postgres;

