CREATE OR REPLACE FUNCTION public.getuserrecommended(i_userid uuid)
 RETURNS TABLE(track character varying, methodid integer, useridrecommended character varying, txtrecommendedinfo character varying)
 LANGUAGE plpgsql
AS $function$ 	
--declare methodid integer;
--methodid = 10;
--declare return_val UUID;
declare --temp_track record;
		 end_trackid UUID;
		useridrecommended UUID;
begin
	
		DROP TABLE IF EXISTS temp_track; 
	CREATE TEMP TABLE temp_track(trackid character varying, meth integer, useridrecommended character varying, txtrecommeninfo character varying);
	
	end_trackid = cast('10000000-0000-0000-0000-000000000001' as UUID);
useridrecommended = cast('11111111-0000-0888-0000-000000000000' as UUID);
	
	

	
--выбираем произвольный трэк из невыданных данному пользователю
--select cast(tracks.recid as character varying) as trackid , 10 as methodid , '11111111-0000-0888-0000-000000000000' as useridrecommended, 'выдан по тестовой рекомендации'  as txtrecommendedinfo
-- into temp_track 
--	from tracks
--	left join downloadtracks
--		on tracks.recid = downloadtracks.trackid
--		where downloadtracks.trackid is null and tracks.deviceid = cast ('11111111-0000-0888-0000-000000000000' as UUID) and tracks.recid<>end_trackid -- =i_userid
--		order by random()
--		fetch first 1 rows only;
	
	insert into temp_track
	select cast(tracks.recid as character varying) as trackid , 10 , '11111111-0000-0888-0000-000000000000' as useridrecommended, 'выдан по тестовой рекомендации'  as txtrecommendedinfo
 	 from tracks 
	where deviceid = cast ('11111111-0000-0888-0000-000000000000' as UUID) 
						and recid<> end_trackid 
						and recid not in 
								(
									select trackid from downloadtracks where deviceid = cast ('11111111-0000-0888-0000-111111111111' as UUID)
								)
		order by random()
		fetch first 1 rows only;
	
	
--	select count(*) from tracks where deviceid = cast ('11111111-0000-0888-0000-000000000000' as UUID) and recid<> cast('10000000-0000-0000-0000-000000000001' as UUID) and recid not in 
--(
--	select trackid from downloadtracks where deviceid = cast ('11111111-0000-0888-0000-111111111111' as UUID)
--)
	
	
	--если нашли трэк на выдачу то записываем в downloadtracks что выдали
if (select count(*) from temp_track)>0 --is not null 
then 
		INSERT INTO downloadtracks 
				(
					recid,
					reccreated,
					deviceid,
					trackid,
					methodid,
					txtrecommendinfo
				)
		
				select
					uuid_generate_v4(),
					now(),
					cast ('11111111-0000-0888-0000-111111111111' as UUID),
					cast(trackid as UUID),
					temp_track.meth,
					txtrecommeninfo
					from temp_track;
				
				return query select * from temp_track;
			
				
else
		return query
		select cast('10000000-0000-0000-0000-000000000001' as character varying) as trackid , 
		10 as methodid ,
		cast('11111111-0000-0888-0000-000000000000' as character varying) as useridrecommended, 
		cast ('конец' as character varying ) as txtrecommendedinfo;
end if;
	

	

	

	return ;
end;
 $function$
;



CREATE OR REPLACE FUNCTION public.getnexttrack_v2(i_deviceid uuid)
 RETURNS TABLE(track character varying, method integer, useridrecommended character varying, txtrecommendedinfo character varying, timeexecute character varying)
 LANGUAGE plpgsql
AS $function$
DECLARE
		declare t timestamptz := clock_timestamp(); -- запоминаем начальное время выполнения процедуры
		i_userid UUID = i_deviceid; -- в дальнейшем заменить получением userid по deviceid
		declare user_name character varying;
BEGIN
	-- Добавляем устройство, если его еще не существует
	PERFORM registerdevice(i_deviceid, 'New device');


--если пользователь alexv_test то пускаем выполнение в getuserrecommended, иначе идем по старому

--select userid into i_userid from devices where recid = i_deviceid; --получаем id пользователя по id_device
select recname into user_name from users where recid = i_userid; --получаем имя пользователя по его id


if user_name = 'alexv2test' then
--return QUERY select getuserrecommended(i_userid); --return getuserrecommended(i_userid)



	RETURN QUERY SELECT
					 nexttrack.track,
					 nexttrack.methodid,
					 nexttrack.useridrecommended,
					 nexttrack.txtrecommendedinfo,
					 CAST((clock_timestamp() - t ) AS CHARACTER VARYING) -- возвращаем время выполнения процедуры
				 FROM getuserrecommended(i_userid) AS nexttrack;

return;
end if;

	-- Возвращаем trackid, конвертируя его в character varying, и methodid
	RETURN QUERY SELECT
					 CAST((nexttrack.track) AS CHARACTER VARYING),
					 nexttrack.methodid,
					 CAST((nexttrack.useridrecommended) AS CHARACTER VARYING),
					 nexttrack.txtrecommendedinfo,
					 CAST((clock_timestamp() - t ) AS CHARACTER VARYING) -- возвращаем время выполнения процедуры
				 FROM getnexttrackid_v17(i_deviceid) AS nexttrack;
END;
$function$
;

