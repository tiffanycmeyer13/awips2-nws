-- This software was developed and / or modified by NOAA/NWS/OCP/ASDT
--
-- This file contains stored procedures to support the ATCF sandbox system.
--
-- Note that the '--' at the ends of lines must be preserved to keep
-- the function definitions from being split up in
-- PluginVersionDao.parseJarEntryForStatements().  Also, colons must
-- not be used outside of comments as this will trigger Hibernate's
-- parameter parsing.
--
-- SOFTWARE HISTORY
--
-- Date         Ticket#    Engineer    Description
-- ------------ ---------- ----------- --------------------------
-- Jun 12, 2018            pwang       Initial creation
-- Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation
-- Mar 29, 2019 #61590     pwang       Add functions for E/F decks
-- May 20, 2019 #63859     pwang       support DTG of A Deck, A, B, F checkin conflict report
-- Jun 25, 2019 #64739     pwang       Add check_merge_conflict function
-- Jul 18, 2019 #66168     dfriedman   Use alternate syntax to avoid colons.
-- Oct 16, 2019 #69593     pwang       Add checkin_fst_from_sandbox function, support fst purge
-- Apr 08, 2020 #77134     pwang       Add genesis_to_TC function for genesis
-- May 28, 2020 #78298     pwang       Add functions to handle deck record merging
-- Jun 11, 2020 #68118     wpaintsil   Cast null values to prevent exceptions.
-- Aug 10, 2020 #79571     wpaintsil   Fix syntax errors in genesis_to_TC.
-- Sep 23, 2020 #82622     pwang       Add functions to support storm management
-- Jun 25, 2021 #92918     dfriedman   Update for data class refactoring.
-- Oct 27, 2022 #109204    jwu         Add checks for region/cyclone number in update_storm.
--
-- Create function to check out adeck data to a sandbox
--
CREATE OR REPLACE FUNCTION
  atcf.checkout_adeck_to_sandbox
  (
     _sbox_id IN INTEGER
   )
RETURNS integer AS
$FUNC$
DECLARE
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
BEGIN

  -- Retrive storm key columns
  SELECT region, cyclonenum, year INTO _region, _cycloneNum, _year FROM atcf.sandbox WHERE id=_sbox_id; --
  --Check if given sandbox exists
  IF(_region is NULL) THEN
      RAISE EXCEPTION 'Given sandbox does not exist'; --
  END IF; --


  -- Copy storm adeck data to sandbox
  INSERT INTO atcf.sandbox_adeck(sandbox_id, id, reftime,
       basin, clat, clon, closedp, cyclonenum,
       eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
       mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
       quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
       radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
       stormdrct, stormname, stormsped, subregion, technique, techniquenum,
       userdata, userdefined, windmax, year, change_cd)
  SELECT _sbox_id, adt.id, adt.reftime,
       adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
       adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
       adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
       adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
       adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
       adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
       adt.userdata, adt.userdefined, adt.windmax, adt.year, 0
   FROM atcf.adeck adt
   WHERE adt.basin=_region
   AND   adt.cyclonenum = _cycloneNum
   AND   adt.year = _year; --

   -- Handle potential exceptions

   --EXCEPTION WHEN others THEN
   --    RAISE INFO 'Failed to check out data from adeck to the sandbox'; --
   --    RETURN -1; --
  RETURN _sbox_id; --
END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.checkout_adeck_to_sandbox(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkout_adeck_to_sandbox(INTEGER) TO awips;

--
-- Create function to check out one DTG of adeck data to a sandbox
--

CREATE OR REPLACE FUNCTION
  atcf.checkout_adeck_dtg_to_sandbox
  (
     _sbox_id IN INTEGER,
     _dtg IN TIMESTAMP
   )
RETURNS INTEGER AS
$FUNC$
DECLARE
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _conflictGroup VARCHAR; --
BEGIN
  -- Avoid to check out same DTG twice into same sandbox
  IF EXISTS (SELECT 1 FROM atcf.sandbox_dtg WHERE sandbox_id=_sbox_id AND dtg = _dtg) THEN
      RETURN _sbox_id; --
  END IF; --
  -- Retrive storm key columns
  SELECT region, cyclonenum, year, region||year||cyclonenum||scopecd as conflictGroup
  INTO _region, _cycloneNum, _year, _conflictGroup
  FROM atcf.sandbox WHERE id=_sbox_id; --

  --Check if given sandbox exists
  IF(_region is NULL) THEN
      RAISE EXCEPTION 'Given Sandbox is not existing'; --
  END IF; --

  -- Check sandbox if valid
  IF NOT EXISTS (SELECT 1 FROM atcf.sandbox WHERE id=_sbox_id AND submitted IS NULL) THEN
      RAISE EXCEPTION 'Given Sandbox has submitted'; --
  END IF; --

  -- Copy DTG records from the a_deck data to sandbox
  INSERT INTO atcf.sandbox_adeck(sandbox_id, id, reftime,
        basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year, change_cd)
   SELECT _sbox_id, adt.id, adt.reftime,
        adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
        adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
        adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, adt.year, 0
   FROM atcf.adeck adt
   WHERE adt.basin=_region
   AND   adt.cyclonenum = _cycloneNum
   AND   adt.year = _year
   AND   adt.reftime = _dtg; --

   -- register the DTG to the sandbox
   IF EXISTS (SELECT 1 FROM atcf.sandbox_adeck WHERE sandbox_id=_sbox_id AND reftime = _dtg) THEN
       INSERT INTO atcf.sandbox_dtg(sandbox_id, dtg, conflictgroup) VALUES(_sbox_id, _dtg, _conflictGroup); --
       RETURN _sbox_id; --
   ELSE
       RETURN -2; --
   END IF; --

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.checkout_adeck_dtg_to_sandbox(INTEGER, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkout_adeck_dtg_to_sandbox(INTEGER, TIMESTAMP) TO awips;

--
-- Create function to check out bdeck data to a sandbox
--
CREATE OR REPLACE FUNCTION
  atcf.checkout_bdeck_to_sandbox
  (
     _sbox_id IN INTEGER
   )
RETURNS integer AS
$FUNC$
DECLARE
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
BEGIN

  -- Retrive storm key columns
  SELECT region, cyclonenum, year INTO _region, _cycloneNum, _year FROM atcf.sandbox WHERE id=_sbox_id; --
  --Check if given sandbox exists
  IF(_region is NULL) THEN
      RAISE EXCEPTION 'Given Sandbox does not exist'; --
  END IF; --


  -- Copy storm bdeck data to the sandbox
  INSERT INTO atcf.sandbox_bdeck(sandbox_id, id, reftime,
       basin, clat, clon, closedp, cyclonenum,
       eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
       mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
       quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
       radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
       stormdrct, stormname, stormsped, subregion, technique, techniquenum,
       userdata, userdefined, windmax, year, change_cd)
  SELECT _sbox_id, bdt.id, bdt.reftime,
       bdt.basin, bdt.clat, bdt.clon, bdt.closedp, bdt.cyclonenum,
       bdt.eyesize, bdt.fcsthour, bdt.forecaster, bdt.gust, bdt.intensity, bdt.maxseas, bdt.maxwindrad,
       bdt.mslp, bdt.quad1waverad, bdt.quad1windrad, bdt.quad2waverad, bdt.quad2windrad,
       bdt.quad3waverad, bdt.quad3windrad, bdt.quad4waverad, bdt.quad4windrad, bdt.radclosedp,
       bdt.radwave, bdt.radwavequad, bdt.radwind, bdt.radwindquad, bdt.reporttype, bdt.stormdepth,
       bdt.stormdrct, bdt.stormname, bdt.stormsped, bdt.subregion, bdt.technique, bdt.techniquenum,
       bdt.userdata, bdt.userdefined, bdt.windmax, bdt.year, 0
   FROM atcf.bdeck bdt
   WHERE bdt.basin=_region
   AND   bdt.cyclonenum = _cycloneNum
   AND   bdt.year = _year; --

   -- Handle potential exceptions

   --EXCEPTION WHEN others THEN
   --    RAISE INFO 'Failed to check out data from adeck to the sandbox'; --
   --    RETURN -1; --
  RETURN _sbox_id; --
END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.checkout_bdeck_to_sandbox(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkout_bdeck_to_sandbox(INTEGER) TO awips;


--
-- Create function to check in data from given sandbox to the a-deck.
-- The submitted of the sandbox will be set, no change record will be cleared
-- Return rows (sandbox_id, DTG)
-- -1 NULL -- indicates the sandbox is not exist
-- -2 NULL -- indicates the sandbox is no longer valid for check in
-- -3 NULL -- indicates the sandbox has nothing need to be checked in
-- 123 2019-05-10 06:00:00 indicates the sandbox 123 may has conflict records, set to validflag = 1
--

--Because return type change, need to
DROP FUNCTION IF EXISTS atcf.checkin_adeck_from_sandbox(integer); --
--if the function exists

CREATE OR REPLACE FUNCTION
  atcf.checkin_adeck_from_sandbox
  (
     _sandbox_id IN INTEGER
  )
RETURNS TABLE (sid INTEGER, dtg TIMESTAMP) AS
$FUNC$
DECLARE
  _rows_impact_a INTEGER; --
  _conflictGroup VARCHAR; --
BEGIN

  -- Check if the sandbox is valid for check in
  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.sandbox
     WHERE id=_sandbox_id
     ) THEN
     RETURN QUERY SELECT -1, CAST(NULL as timestamp); --
  END IF; --

  -- Check if the sandbox is valid for check in
  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.sandbox
     WHERE id=_sandbox_id
     AND validflag=0
     AND submitted IS NULL
     ) THEN
     RETURN QUERY SELECT -2, CAST(NULL as timestamp); --
  END IF; --

  -- Check if there is any new, updated or deleted record
  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.sandbox_adeck
     WHERE sandbox_id=_sandbox_id
     AND change_cd > 0
     ) THEN
     RETURN QUERY SELECT -3, CAST(NULL as timestamp); --
  END IF; --

  SELECT region||year||cyclonenum||scopecd as conflictGroup
  INTO _conflictGroup
  FROM atcf.sandbox WHERE id=_sandbox_id; --

  -- Insert new a_deck rows when change_cd=1
  INSERT INTO atcf.adeck(id, reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT adt.id, adt.reftime, adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
        adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
        adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, adt.year
  FROM atcf.sandbox_adeck adt
  WHERE adt.sandbox_id=_sandbox_id
  AND   adt.change_cd=1; -- 1-- NEW ROW

  UPDATE atcf.adeck t
  SET   (reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
      = (adt.reftime, adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
        adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
        adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, adt.year)
  FROM   atcf.sandbox_adeck adt
  WHERE  adt.sandbox_id=_sandbox_id
  AND    t.id= adt.id
  AND    adt.change_cd=2;  -- 2-- MODIFIED

  DELETE FROM atcf.adeck t
  USING   atcf.sandbox_adeck adt
  WHERE t.id=adt.id
  AND   adt.sandbox_id=_sandbox_id
  AND   adt.change_cd=3;   -- 3-- DELETED

  --After checked in, only keep changed records, clean others
  DELETE FROM atcf.sandbox_adeck WHERE sandbox_id=_sandbox_id and change_cd=0; --

  -- Set submitted with now date time
  UPDATE atcf.sandbox SET submitted=NOW() WHERE id=_sandbox_id; --

  -- UPDATE any conflict sandbox to invalid
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND id IN (SELECT distinct sd.sandbox_id
             FROM atcf.sandbox_dtg sd
             WHERE sd.sandbox_id != _sandbox_id
             AND   sd.conflictgroup = _conflictGroup
             AND   sd.dtg IN (SELECT distinct cad.reftime
                              FROM atcf.sandbox_adeck cad
                              WHERE cad.sandbox_id = _sandbox_id)); --

  -- Return conflict sandboxes
  RETURN QUERY SELECT sd.sandbox_id, sd.dtg
               FROM atcf.sandbox_dtg sd
               INNER JOIN atcf.sandbox sb
                     ON sd.sandbox_id=sb.id
                     AND sb.validflag=1
               WHERE sd.sandbox_id != _sandbox_id
               AND   sd.conflictgroup = _conflictGroup
               AND   sd.dtg IN (SELECT distinct cad.reftime
                                FROM atcf.sandbox_adeck cad
                                WHERE cad.sandbox_id = _sandbox_id); --
END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.checkin_adeck_from_sandbox(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_adeck_from_sandbox(INTEGER) TO awips;


-- End checkin_adeck_from_sandbox


--
-- Create function to check in data from given sandbox to the b-deck.
-- The unchanged record in the sandbox will be cleaned up after successfully check-in.
-- Return rows (sandbox_id, DTG)
-- -1  -- indicates the sandbox is not exist
-- -2  -- indicates the sandbox is no longer valid for check in
-- -3  -- indicates the sandbox has nothing need to be checked in
-- 123 indicates that submitted sandbox may has conflict with 123, set 123 to invalid
--

--Because return type change, need to
DROP FUNCTION IF EXISTS atcf.checkin_bdeck_from_sandbox(integer); --
--if the function exists

CREATE OR REPLACE FUNCTION atcf.checkin_bdeck_from_sandbox(_sandbox_id integer)
  RETURNS TABLE(sid INTEGER) AS
$BODY$
DECLARE
  _rows_impact_b INTEGER; --
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
  ) THEN
    RETURN QUERY SELECT -1; -- No such sandbox
  END IF; --

  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
    AND validflag=0
    AND submitted IS NULL
  ) THEN
    RETURN QUERY SELECT -2;  -- Not valid for check-in
  END IF; --

  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.sandbox_bdeck
     WHERE sandbox_id=_sandbox_id
     AND change_cd > 0
     ) THEN
     RETURN QUERY SELECT -3; --
  END IF; --

  INSERT INTO atcf.bdeck(id, reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT adt.id, adt.reftime, adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
       adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
       adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
       adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
       adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
       adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
       adt.userdata, adt.userdefined, adt.windmax, adt.year
  FROM atcf.sandbox_bdeck adt
  WHERE adt.sandbox_id=_sandbox_id
  AND   adt.change_cd=1; -- 1-- NEW ROW

  UPDATE atcf.bdeck t
  SET     (reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
      = (adt.reftime, adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
        adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
        adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, adt.year)
  FROM   atcf.sandbox_bdeck adt
  WHERE  adt.sandbox_id=_sandbox_id
  AND    t.id= adt.id
  AND    adt.change_cd=2;  -- 2-- MODIFIED

  DELETE FROM atcf.bdeck t
  USING   atcf.sandbox_bdeck adt
  WHERE t.id=adt.id
  AND   adt.sandbox_id=_sandbox_id
  AND   adt.change_cd=3;   -- 3-- DELETED

  --After checked in, only keep changed records, clean others
  DELETE FROM atcf.sandbox_bdeck WHERE sandbox_id=_sandbox_id and change_cd=0; --

  -- Set submitted with now date time
  UPDATE atcf.sandbox SET submitted=NOW() WHERE id=_sandbox_id; --

  -- UPDATE any conflic sandbox to invalid
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND id IN (SELECT s1.id
             FROM atcf.sandbox s1
             INNER JOIN atcf.sandbox s2
                   ON s1.region = s2.region
                   AND s1.year = s2.year
                   AND s1.cyclonenum = s2.cyclonenum
                   AND s2.id = _sandbox_id
             WHERE s1.scopecd = 'BDECK'
             AND   s1.sandboxtype = 'CHECKOUT'
             AND s1.validflag = 0
             AND s1.id != _sandbox_id); --

  -- Return conflict sandboxes
  RETURN QUERY SELECT s1.id
               FROM atcf.sandbox s1
               INNER JOIN atcf.sandbox s2
                     ON s1.region = s2.region
                     AND s1.year = s2.year
                     AND s1.cyclonenum = s2.cyclonenum
                     AND s2.id = _sandbox_id
               WHERE s1.scopecd = 'BDECK'
               AND   s1.sandboxtype = 'CHECKOUT'
               AND s1.validflag = 1
               AND s1.id != _sandbox_id; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.checkin_bdeck_from_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_bdeck_from_sandbox(integer) TO public;
GRANT EXECUTE ON FUNCTION atcf.checkin_bdeck_from_sandbox(integer) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_bdeck_from_sandbox(integer) TO awips;
GRANT EXECUTE ON FUNCTION atcf.checkin_bdeck_from_sandbox(integer) TO pguser;


-- checkout E deck
CREATE OR REPLACE FUNCTION atcf.checkout_edeck_to_sandbox(_sbox_id integer)
  RETURNS integer AS
$BODY$
DECLARE
_region VARCHAR; --
_year INTEGER; --
_cycloneNum INTEGER; --
BEGIN
SELECT region, cyclonenum, year INTO _region, _cycloneNum, _year FROM atcf.sandbox WHERE id=_sbox_id; --
IF(_region is NULL) THEN
RAISE EXCEPTION 'Given sandbox does not exist'; --
END IF; --
INSERT INTO atcf.sandbox_edeck(sandbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
       box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
       developmentlevel, ellipseangle, ellipseralong, ellipsercross,
       eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
       minutes, polygonpointstext, probformat, probability, probabilityitem,
       radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
       tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal,
       change_cd)
SELECT _sbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
       box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
       developmentlevel, ellipseangle, ellipseralong, ellipsercross,
       eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
       minutes, polygonpointstext, probformat, probability, probabilityitem,
       radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
       tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal,
       0
FROM atcf.edeck
WHERE basin=_region
AND   cyclonenum = _cycloneNum
AND   year = _year; --
RETURN _sbox_id; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION atcf.checkout_edeck_to_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkout_edeck_to_sandbox(integer) TO awips;


-- checkout F deck
CREATE OR REPLACE FUNCTION atcf.checkout_fdeck_to_sandbox(_sbox_id integer)
  RETURNS integer AS
$BODY$
DECLARE
_region VARCHAR; --
_year INTEGER; --
_cycloneNum INTEGER; --
BEGIN
SELECT region, cyclonenum, year INTO _region, _cycloneNum, _year FROM atcf.sandbox WHERE id=_sbox_id; --
IF(_region is NULL) THEN
RAISE EXCEPTION 'Given sandbox does not exist'; --
END IF; --
INSERT INTO atcf.sandbox_fdeck(sandbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, accuracymeteorological, accuracynavigational, algorithm,
       analysisinitials, centerorintensity, centertype, ci24hourforecast,
       ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
       dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
       eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
       fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
       flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
       inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
       inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
       maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
       maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
       maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
       microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
       outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
       outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
       pressureconfidence, pressurederivation, process, quad1windrad,
       quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
       radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
       radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
       radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
       radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
       rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
       rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
       slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
       speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
       tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
       tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
       temppassivemicrowave, tropicalindicator, waveheight, windcode,
       windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
       windrad5, windrad6, windrad7, windrad8, change_cd)
SELECT _sbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, accuracymeteorological, accuracynavigational, algorithm,
       analysisinitials, centerorintensity, centertype, ci24hourforecast,
       ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
       dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
       eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
       fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
       flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
       inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
       inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
       maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
       maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
       maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
       microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
       outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
       outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
       pressureconfidence, pressurederivation, process, quad1windrad,
       quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
       radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
       radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
       radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
       radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
       rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
       rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
       slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
       speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
       tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
       tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
       temppassivemicrowave, tropicalindicator, waveheight, windcode,
       windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
       windrad5, windrad6, windrad7, windrad8, 0
FROM atcf.fdeck
WHERE basin=_region
AND   cyclonenum = _cycloneNum
AND   year = _year; --
RETURN _sbox_id; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION atcf.checkout_fdeck_to_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkout_fdeck_to_sandbox(integer) TO awips;

-- checkin E deck
CREATE OR REPLACE FUNCTION atcf.checkin_edeck_from_sandbox(_sandbox_id integer)
  RETURNS integer AS
$BODY$
DECLARE
_rows_impact_e INTEGER; --
BEGIN
IF NOT EXISTS (
SELECT 1
FROM  atcf.sandbox
WHERE id=_sandbox_id
) THEN
RETURN -1; -- No such sandbox
END IF; --
IF NOT EXISTS (
SELECT 1
FROM  atcf.sandbox
WHERE id=_sandbox_id
AND validflag=0
AND submitted IS NULL
) THEN
RETURN -2;  -- Not valid for check-in
END IF; --
INSERT INTO atcf.edeck(id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
       box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
       developmentlevel, ellipseangle, ellipseralong, ellipsercross,
       eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
       minutes, polygonpointstext, probformat, probability, probabilityitem,
       radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
       tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal)
SELECT id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
       box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
       developmentlevel, ellipseangle, ellipseralong, ellipsercross,
       eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
       minutes, polygonpointstext, probformat, probability, probabilityitem,
       radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
       tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal
FROM atcf.sandbox_edeck
WHERE sandbox_id=_sandbox_id
AND   change_cd=1; -- 1-- NEW ROW

UPDATE atcf.edeck t
SET    (t.reftime, t.basin, t.clat, t.clon, t.cyclonenum,
        t.forecaster, t.reporttype, t.year, t.alongtrackbias, t.alongtrackradius,
        t.box1latns, t.box1lonew, t.box2latns, t.box2lonew, t.crosstrackbias,
        t.crosstrackdirection, t.crosstrackradius, t.developmentlevel, t.ellipseangle,
        t.ellipseralong, t.ellipsercross, t.eventdatetimegroup, t.fcsthour,
        t.genordis, t.genesisnum, t.halfrange, t.minutes, t.polygonpointstext,
        t.probformat, t.probability, t.probabilityitem, t.radwindquad, t.ristarttau,
        t.ristoptau, t.shapetype, t.stormid2, t.tcfamanopdtg, t.tcfamsgdtg, t.tcfaradius,
        t.tcfawtnum, t.technique, t.undefined, t.vfinal)
      = (s.reftime, s.basin, s.clat, s.clon, s.cyclonenum,
        s.forecaster, s.reporttype, s.year, s.alongtrackbias, s.alongtrackradius,
        s.box1latns, s.box1lonew, s.box2latns, s.box2lonew, s.crosstrackbias,
        s.crosstrackdirection, s.crosstrackradius, s.developmentlevel, s.ellipseangle,
        s.ellipseralong, s.ellipsercross, s.eventdatetimegroup, s.fcsthour,
        s.genordis, s.genesisnum, s.halfrange, s.minutes, s.polygonpointstext,
        s.probformat, s.probability, s.probabilityitem, s.radwindquad, s.ristarttau,
        s.ristoptau, s.shapetype, s.stormid2, s.tcfamanopdtg, s.tcfamsgdtg, s.tcfaradius,
        s.tcfawtnum, s.technique, s.undefined, s.vfinal)
FROM   atcf.sandbox_edeck s
WHERE  s.sandbox_id=_sandbox_id
AND    t.id= s.id
AND    s.change_cd=2;  -- 2-- MODIFIED

DELETE FROM atcf.edeck t
USING   atcf.sandbox_edeck s
WHERE t.id=s.id
AND   s.sandbox_id=_sandbox_id
AND   s.change_cd=3;   -- 3-- DELETED

SELECT count(*) FROM atcf.sandbox_edeck WHERE change_cd>0 INTO _rows_impact_e; --
DELETE FROM atcf.sandbox_edeck WHERE sandbox_id=_sandbox_id; --
RETURN _rows_impact_e; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION atcf.checkin_edeck_from_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_edeck_from_sandbox(integer) TO awips;


--
-- Create function to check in data from given sandbox to the f-deck.
-- The unchanged record in the sandbox will be cleaned up after successfully check-in.
-- Return rows (sandbox_id, DTG)
-- -1  -- indicates the sandbox is not exist
-- -2  -- indicates the sandbox is no longer valid for check in
-- -3  -- indicates the sandbox has nothing need to be checked in
-- 123 indicates that submitted sandbox may has conflict with 123, set 123 to invalid
--

--May need to
DROP FUNCTION IF EXISTS atcf.checkin_fdeck_from_sandbox(integer); --
--If the function exists

CREATE OR REPLACE FUNCTION atcf.checkin_fdeck_from_sandbox(_sandbox_id integer)
  RETURNS TABLE(sid INTEGER) AS
$BODY$
DECLARE
_rows_impact_f INTEGER; --
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
  ) THEN
    RETURN QUERY SELECT -1; -- No such sandbox
  END IF; --

  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
    AND validflag=0
    AND submitted IS NULL
  ) THEN
    RETURN QUERY SELECT -2;  -- Not valid for check-in
  END IF; --

  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.sandbox_fdeck
     WHERE sandbox_id=_sandbox_id
     AND change_cd > 0
     ) THEN
     RETURN QUERY SELECT -3; --
  END IF; --

  INSERT INTO atcf.fdeck(id, reftime, basin, clat, clon,
        cyclonenum, forecaster, reporttype, year, accuracymeteorological,
        accuracynavigational, algorithm, analysisinitials, centerorintensity,
        centertype, ci24hourforecast, ciconfidence, cinum, comments, dewpointtemp,
        distancetonearestdatanm, dvorakcodelongtermtrend, dvorakcodeshorttermtrend,
        endtime, eyecharacterorwallcloudthickness, eyediameternm, eyeorientation,
        eyeshape, eyeshortaxis, eyesize, fixformat, fixsite, fixtype, flaggedindicator,
        flightlevel100feet, flightlevelmillibars, flightlevelminimumheightmeters,
        heightmidpointlowest150m, inboundmaxwindazimuth, inboundmaxwindelevationfeet,
        inboundmaxwindrangenm, inboundmaxwindspeed, initials, maxcloudheightfeet,
        maxflwindinboundbearing, maxflwindinbounddirection, maxflwindinboundintensity,
        maxflwindinboundrangenm, maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
        maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
        microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
        outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
        outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
        pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
        quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
        radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
        radarobservationcodecharacteristics, radarobservationcodemovement,
        radarsiteposlat, radarsiteposlon, radartype, radiiconfidence, radiusofmaximumwind,
        radiusofwindintensity, radobcode, rainaccumulationlat, rainaccumulationlon,
        rainaccumulationtimeinterval, rainflag, rainrate, satellitetype, scenetype,
        seasurfacetemp, sensortype, slpraw, slpretrieved, sondeenvironment,
        speedmeanwind0to500mkt, speedmeanwindlowest150mkt, spiraloverlaydegrees,
        starttime, subregion, tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
        tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
        temppassivemicrowave, tropicalindicator, waveheight, windcode, windmax,
        windmaxconfidence, windrad1, windrad2, windrad3, windrad4, windrad5,
        windrad6, windrad7, windrad8)
  SELECT id, reftime, basin, clat, clon,
        cyclonenum, forecaster, reporttype, year, accuracymeteorological,
        accuracynavigational, algorithm, analysisinitials, centerorintensity,
        centertype, ci24hourforecast, ciconfidence, cinum, comments, dewpointtemp,
        distancetonearestdatanm, dvorakcodelongtermtrend, dvorakcodeshorttermtrend,
        endtime, eyecharacterorwallcloudthickness, eyediameternm, eyeorientation,
        eyeshape, eyeshortaxis, eyesize, fixformat, fixsite, fixtype, flaggedindicator,
        flightlevel100feet, flightlevelmillibars, flightlevelminimumheightmeters,
        heightmidpointlowest150m, inboundmaxwindazimuth, inboundmaxwindelevationfeet,
        inboundmaxwindrangenm, inboundmaxwindspeed, initials, maxcloudheightfeet,
        maxflwindinboundbearing, maxflwindinbounddirection, maxflwindinboundintensity,
        maxflwindinboundrangenm, maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
        maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
        microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
        outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
        outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
        pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
        quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
        radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
        radarobservationcodecharacteristics, radarobservationcodemovement,
        radarsiteposlat, radarsiteposlon, radartype, radiiconfidence, radiusofmaximumwind,
        radiusofwindintensity, radobcode, rainaccumulationlat, rainaccumulationlon,
        rainaccumulationtimeinterval, rainflag, rainrate, satellitetype, scenetype,
        seasurfacetemp, sensortype, slpraw, slpretrieved, sondeenvironment,
        speedmeanwind0to500mkt, speedmeanwindlowest150mkt, spiraloverlaydegrees,
        starttime, subregion, tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
        tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
        temppassivemicrowave, tropicalindicator, waveheight, windcode, windmax,
        windmaxconfidence, windrad1, windrad2, windrad3, windrad4, windrad5,
        windrad6, windrad7, windrad8
  FROM atcf.sandbox_fdeck
  WHERE sandbox_id=_sandbox_id
  AND   change_cd=1; -- 1-- NEW ROW

UPDATE atcf.fdeck t
  SET   (reftime, basin, clat, clon, cyclonenum,
        forecaster, reporttype, year, accuracymeteorological,
        accuracynavigational, algorithm, analysisinitials, centerorintensity,
        centertype, ci24hourforecast, ciconfidence, cinum, comments,
        dewpointtemp, distancetonearestdatanm, dvorakcodelongtermtrend,
        dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
        eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
        fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
        flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
        inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
        inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
        maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
        maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
        maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
        microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
        outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
        outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
        pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
        quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
        radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
        radarobservationcodecharacteristics, radarobservationcodemovement,
        radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
        radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
        rainaccumulationlon, rainaccumulationtimeinterval, rainflag, rainrate,
        satellitetype, scenetype, seasurfacetemp, sensortype, slpraw, slpretrieved,
        sondeenvironment, speedmeanwind0to500mkt, speedmeanwindlowest150mkt,
        spiraloverlaydegrees, starttime, subregion, tnumaverage, tnumaveragingderivation,
        tnumaveragingtimeperiod, tnumraw, tempcloudsurroundingeye, tempeye,
        tempinsideeye, tempoutsideeye, temppassivemicrowave, tropicalindicator,
        waveheight, windcode, windmax, windmaxconfidence, windrad1, windrad2,
        windrad3, windrad4, windrad5, windrad6, windrad7, windrad8)
      = (s.reftime, s.basin, s.clat, s.clon, s.cyclonenum,
        s.forecaster, s.reporttype, s.year, s.accuracymeteorological,
        s.accuracynavigational, s.algorithm, s.analysisinitials, s.centerorintensity,
        s.centertype, s.ci24hourforecast, s.ciconfidence, s.cinum, s.comments,
        s.dewpointtemp, s.distancetonearestdatanm, s.dvorakcodelongtermtrend,
        s.dvorakcodeshorttermtrend, s.endtime, s.eyecharacterorwallcloudthickness,
        s.eyediameternm, s.eyeorientation, s.eyeshape, s.eyeshortaxis, s.eyesize,
        s.fixformat, s.fixsite, s.fixtype, s.flaggedindicator, s.flightlevel100feet,
        s.flightlevelmillibars, s.flightlevelminimumheightmeters, s.heightmidpointlowest150m,
        s.inboundmaxwindazimuth, s.inboundmaxwindelevationfeet, s.inboundmaxwindrangenm,
        s.inboundmaxwindspeed, s.initials, s.maxcloudheightfeet, s.maxflwindinboundbearing,
        s.maxflwindinbounddirection, s.maxflwindinboundintensity, s.maxflwindinboundrangenm,
        s.maxrainaccumulation, s.maxseas, s.maxsurfacewindinboundlegbearing,
        s.maxsurfacewindinboundlegintensity, s.maxsurfacewindinboundlegrangenm,
        s.microwaveradiiconfidence, s.missionnumber, s.mslp, s.obheight, s.observationsources,
        s.outboundmaxwindazimuth, s.outboundmaxwindelevationfeet, s.outboundmaxwindrangenm,
        s.outboundmaxwindspeed, s.pcncode, s.percentofeyewallobserved, s.positionconfidence,
        s.pressureconfidence, s.pressurederivation, s.process, s.quad1windrad, s.quad2windrad,
        s.quad3windrad, s.quad4windrad, s.radmod1, s.radmod2, s.radmod3, s.radmod4, s.radmod5,
        s.radmod6, s.radmod7, s.radmod8, s.radwind, s.radwindquad, s.radarformat,
        s.radarobservationcodecharacteristics, s.radarobservationcodemovement,
        s.radarsiteposlat, s.radarsiteposlon, s.radartype, s.radiiconfidence,
        s.radiusofmaximumwind, s.radiusofwindintensity, s.radobcode, s.rainaccumulationlat,
        s.rainaccumulationlon, s.rainaccumulationtimeinterval, s.rainflag, s.rainrate,
        s.satellitetype, s.scenetype, s.seasurfacetemp, s.sensortype, s.slpraw, s.slpretrieved,
        s.sondeenvironment, s.speedmeanwind0to500mkt, s.speedmeanwindlowest150mkt,
        s.spiraloverlaydegrees, s.starttime, s.subregion, s.tnumaverage, s.tnumaveragingderivation,
        s.tnumaveragingtimeperiod, s.tnumraw, s.tempcloudsurroundingeye, s.tempeye,
        s.tempinsideeye, s.tempoutsideeye, s.temppassivemicrowave, s.tropicalindicator,
        s.waveheight, s.windcode, s.windmax, s.windmaxconfidence, s.windrad1, s.windrad2,
        s.windrad3, s.windrad4, s.windrad5, s.windrad6, s.windrad7, s.windrad8)
  FROM   atcf.sandbox_fdeck s
  WHERE  s.sandbox_id=_sandbox_id
  AND    t.id= s.id
  AND    s.change_cd=2;  -- 2-- MODIFIED

  DELETE FROM atcf.fdeck t
  USING   atcf.sandbox_fdeck s
  WHERE t.id=s.id
  AND   s.sandbox_id=_sandbox_id
  AND   s.change_cd=3;   -- 3-- DELETED

--After checked in, only keep changed records, clean others
  DELETE FROM atcf.sandbox_fdeck WHERE sandbox_id=_sandbox_id and change_cd=0; --

  -- Set submitted with now date time
  UPDATE atcf.sandbox SET submitted=NOW() WHERE id=_sandbox_id; --

  -- UPDATE any conflict sandbox to invalid
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND id IN (SELECT s1.id
             FROM atcf.sandbox s1
             INNER JOIN atcf.sandbox s2
                   ON s1.region = s2.region
                   AND s1.year = s2.year
                   AND s1.cyclonenum = s2.cyclonenum
                   AND s2.id = _sandbox_id
             WHERE s1.scopecd = 'FDECK'
             AND   s1.sandboxtype = 'CHECKOUT'
             AND s1.validflag = 0
             AND s1.id != _sandbox_id); --

  -- Return conflict sandboxes
  RETURN QUERY SELECT s1.id
               FROM atcf.sandbox s1
               INNER JOIN atcf.sandbox s2
                     ON s1.region = s2.region
                     AND s1.year = s2.year
                     AND s1.cyclonenum = s2.cyclonenum
                     AND s2.id = _sandbox_id
               WHERE s1.scopecd = 'FDECK'
               AND   s1.sandboxtype = 'CHECKOUT'
               AND s1.validflag = 1
               AND s1.id != _sandbox_id; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.checkin_fdeck_from_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_fdeck_from_sandbox(integer) TO public;
GRANT EXECUTE ON FUNCTION atcf.checkin_fdeck_from_sandbox(integer) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_fdeck_from_sandbox(integer) TO awips;
GRANT EXECUTE ON FUNCTION atcf.checkin_fdeck_from_sandbox(integer) TO pguser;


--Only delete a DECK level sandbox CASCADE
--The sandboxes for A Deck have two children tables
--Others have one child table

CREATE OR REPLACE FUNCTION atcf.delete_deck_sandbox(
    _sandbox_id integer)
  RETURNS integer AS
$BODY$
DECLARE
  _sbox_scope_cd VARCHAR; --
BEGIN

  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
  ) THEN
    RETURN -1; -- No such sandbox
  END IF; --

  SELECT scopecd INTO _sbox_scope_cd FROM atcf.sandbox WHERE id=_sandbox_id; --

  --Clean sandbox children tables
  IF (_sbox_scope_cd='ADECK') THEN
      -- clean DTG table
      DELETE FROM atcf.sandbox_dtg WHERE sandbox_id=_sandbox_id; --
      DELETE FROM atcf.sandbox_adeck WHERE sandbox_id=_sandbox_id; --
  ELSEIF (_sbox_scope_cd='BDECK') THEN
      DELETE FROM atcf.sandbox_bdeck WHERE sandbox_id=_sandbox_id; --
  ELSEIF (_sbox_scope_cd='FDECK') THEN
      DELETE FROM atcf.sandbox_fdeck WHERE sandbox_id=_sandbox_id; --
  ELSEIF (_sbox_scope_cd='EDECK') THEN
      DELETE FROM atcf.sandbox_edeck WHERE sandbox_id=_sandbox_id; --
  ELSEIF (_sbox_scope_cd='FST') THEN
      DELETE FROM atcf.sandbox_fst WHERE sandbox_id=_sandbox_id; --
  END IF; --

  -- Delete the sandbox entry
  DELETE FROM atcf.sandbox WHERE id=_sandbox_id; --

  RETURN _sandbox_id; --
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.delete_deck_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.delete_deck_sandbox(integer) TO public;
GRANT EXECUTE ON FUNCTION atcf.delete_deck_sandbox(integer) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.delete_deck_sandbox(integer) TO awips;
GRANT EXECUTE ON FUNCTION atcf.delete_deck_sandbox(integer) TO pguser;

--Check if the target sandbox can merge with the latest baseline

CREATE OR REPLACE FUNCTION
  atcf.check_sandbox_mergeable
  (
     _sbox_id IN INTEGER
   )
RETURNS TABLE(scope VARCHAR,
              base_sbox_id INTEGER,
              base_chg_cd INTEGER,
              change_counts INTEGER) AS
$FUNC$
DECLARE
  _num_committed INTEGER DEFAULT 0; --
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _scope_cd VARCHAR; --
  _createddt TIMESTAMP without time zone; --
  _base_sbox_id INTEGER; --
BEGIN
  -- check if too late to merge
  SELECT region, cyclonenum, year, scopecd, createddt
  INTO _region, _cycloneNum, _year, _scope_cd, _createddt
  FROM atcf.sandbox
  WHERE id=_sbox_id;--

  SELECT COUNT(*) INTO _num_committed
  FROM atcf.sandbox
  WHERE id != _sbox_id
  AND region = _region
  AND cyclonenum = _cyclonenum
  AND year = _year
  AND scopecd = _scope_cd
  AND submitted is NOT NULL
  AND submitted > _createddt;--

  --If 0 or >1 sandbox submitted since createddt
  --Return "NO" to indiacte the sandbox not allow to check in
  IF _num_committed != 1 THEN
      RETURN QUERY SELECT cast('NO' as VARCHAR), -1, -1, cast(_num_committed as INTEGER); --
      RETURN;--
  END IF;--

  SELECT id
  INTO _base_sbox_id
  FROM atcf.sandbox
  WHERE id != _sbox_id
  AND region = _region
  AND cyclonenum = _cyclonenum
  AND year = _year
  AND scopecd = _scope_cd
  AND submitted is NOT NULL
  AND submitted > _createddt;--

  -- Identify conflicted pairs
  IF _scope_cd = 'ADECK' THEN
      RETURN QUERY SELECT cast(_scope_cd as VARCHAR), _base_sbox_id, change_cd, cast(count(*) as INTEGER)
      FROM atcf.sandbox_adeck
      WHERE  sandbox_id= _base_sbox_id
      AND change_cd IN (1, 2, 3)
      GROUP BY change_cd;--
  ELSIF _scope_cd = 'BDECK' THEN
      RETURN QUERY SELECT cast(_scope_cd as VARCHAR), _base_sbox_id, change_cd, cast(count(*) as INTEGER)
      FROM atcf.sandbox_bdeck
      WHERE  sandbox_id= _base_sbox_id
      AND change_cd IN (1, 2, 3)
      GROUP BY change_cd;--
  ELSIF _scope_cd = 'FDECK' THEN
      RETURN QUERY SELECT cast(_scope_cd as VARCHAR), _base_sbox_id, change_cd, cast(count(*) as INTEGER)
      FROM atcf.sandbox_fdeck
      WHERE  sandbox_id= _base_sbox_id
      AND change_cd IN (1, 2, 3)
      GROUP BY change_cd;--
  ELSE
      RETURN QUERY SELECT cast('NO' as VARCHAR), -1, -1, 0; --
  END IF;--
  RETURN;--

END;--
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.check_sandbox_mergeable(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.check_sandbox_mergeable(INTEGER) TO awips;

-- Check record conflict

CREATE OR REPLACE FUNCTION
  atcf.check_conflict_record
  (
     _base_sbox_id IN INTEGER,
     _sbox_id IN INTEGER,
     _scope_cd IN VARCHAR
   )
RETURNS TABLE(conf_rec_id INTEGER,
              base_sbox_id INTEGER,
              tg_sbox_id INTEGER,
              base_chg_cd INTEGER,
              tg_chg_cd INTEGER,
              scope VARCHAR) AS
$FUNC$
BEGIN
  -- Identify conflicted pairs
  IF _scope_cd = 'ADECK' THEN
      RETURN QUERY SELECT DISTINCT b.id, b.sandbox_id, t.sandbox_id, b.change_cd, t.change_cd, _scope_cd
      FROM atcf.sandbox_adeck b
      INNER JOIN atcf.sandbox_adeck t ON b.id = t.id
      WHERE  b.sandbox_id=_base_sbox_id
      AND t.sandbox_id=_sbox_id
      AND b.change_cd IN (1, 2, 3)
      AND t.change_cd IN (1, 2, 3);--
  ELSIF _scope_cd = 'BDECK' THEN
      RETURN QUERY SELECT DISTINCT b.id, b.sandbox_id, t.sandbox_id, b.change_cd, t.change_cd, _scope_cd
      FROM atcf.sandbox_bdeck b
      INNER JOIN atcf.sandbox_bdeck t ON b.id = t.id
      WHERE  b.sandbox_id=_base_sbox_id
      AND t.sandbox_id=_sbox_id
      AND b.change_cd IN (1, 2, 3)
      AND t.change_cd IN (1, 2, 3);--
  ELSIF _scope_cd = 'FDECK' THEN
      RETURN QUERY SELECT DISTINCT b.id, b.sandbox_id, t.sandbox_id, b.change_cd, t.change_cd, _scope_cd
      FROM atcf.sandbox_fdeck b
      INNER JOIN atcf.sandbox_fdeck t ON b.id = t.id
      WHERE  b.sandbox_id=_base_sbox_id
      AND t.sandbox_id=_sbox_id
      AND b.change_cd IN (1, 2, 3)
      AND t.change_cd IN (1, 2, 3);--
  END IF;--

END;--
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.check_conflict_record(INTEGER, INTEGER, VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.check_conflict_record(INTEGER, INTEGER, VARCHAR) TO awips;

-- Undo added new, updated and marked as Deleted record in a sandbox

CREATE OR REPLACE FUNCTION atcf.undo_modified_record(
    _sbox_id integer,
    _record_id integer)
  RETURNS integer AS
$BODY$
DECLARE
  _region VARCHAR;--
  _year INTEGER;--
  _cycloneNum INTEGER;--
  _scope_cd VARCHAR;--
  _change_cd INTEGER;--
  _sbox_table_name VARCHAR;--
BEGIN
  -- check if too late to merge
  SELECT region, cyclonenum, year, scopecd
  INTO _region, _cycloneNum, _year, _scope_cd
  FROM atcf.sandbox
  WHERE id=_sbox_id;--

  IF _region IS NULL THEN
      RETURN -1;--
  END IF;--

  IF _scope_cd = 'ADECK' THEN
      SELECT 'atcf.sandbox_adeck' INTO _sbox_table_name;--
  ELSIF _scope_cd = 'BDECK' THEN
      SELECT 'atcf.sandbox_bdeck' INTO _sbox_table_name;--
  ELSIF _scope_cd = 'FDECK' THEN
      SELECT 'atcf.sandbox_fdeck' INTO _sbox_table_name;--
  END IF;--
  EXECUTE format('SELECT change_cd FROM %s WHERE  sandbox_id=$1  AND id=$2', _sbox_table_name) INTO _change_cd USING _sbox_id, _record_id;--
  -- change_dc: 1--New, 2--Modified, 3--Marked Deleted
  IF _change_cd = 1 THEN
      EXECUTE format('DELETE FROM %s WHERE sandbox_id=$1  AND id=$2', _sbox_table_name) USING _sbox_id, _record_id;--
  ELSIF _change_cd = 2 THEN
      SELECT atcf.refresh_sandbox_record(_sbox_id, _record_id, _region, _year, _cycloneNum, _scope_cd) INTO _record_id;--
  ELSIF _change_cd = 3 THEN
      EXECUTE format('UPDATE %s SET change_cd=0 WHERE sandbox_id=$1  AND id=$2', _sbox_table_name) USING _sbox_id, _record_id;--
  END IF;--
  RETURN _record_id;--
END;--
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.undo_modified_record(integer, integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.undo_modified_record(integer, integer) TO public;
GRANT EXECUTE ON FUNCTION atcf.undo_modified_record(integer, integer) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.undo_modified_record(integer, integer) TO awips;
GRANT EXECUTE ON FUNCTION atcf.undo_modified_record(integer, integer) TO pguser;

-- Recheck out a record to the sandbox to replace any change on the record
CREATE OR REPLACE FUNCTION
  atcf.refresh_sandbox_record
  (
     _sbox_id IN INTEGER,
     _record_id IN INTEGER,
     _region VARCHAR,
     _year INTEGER,
     _cycloneNum INTEGER,
     _scope_cd VARCHAR
   )
RETURNS INTEGER AS
$FUNC$
DECLARE

BEGIN
  IF _scope_cd = 'ADECK' THEN
      UPDATE atcf.sandbox_adeck t
      SET (reftime, clat, clon, closedp, eyesize,
          fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
          mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
          quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
          radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
          stormdrct, stormname, stormsped, subregion, technique, techniquenum,
          userdata, userdefined, windmax, changecd)
        = (adt.reftime, adt.clat, adt.clon, adt.closedp, adt.eyesize,
          adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
          adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
          adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
          adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
          adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
          adt.userdata, adt.userdefined, adt.windmax, 0)
      FROM   atcf.adeck adt
      WHERE  adt.basin=_region AND adt.year=_year AND adt.cyclonenum=_cycloneNum
      AND adt.id=_record_id AND t.sandbox_id=_sbox_id AND t.id= adt.id;--
  ELSIF _scope_cd = 'BDECK' THEN
      UPDATE atcf.sandbox_bdeck t
      SET (reftime, clat, clon, closedp, eyesize,
          fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
          mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
          quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
          radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
          stormdrct, stormname, stormsped, subregion, technique, techniquenum,
          userdata, userdefined, windmax, changecd)
        = (adt.reftime, adt.clat, adt.clon, adt.closedp, adt.eyesize,
          adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
          adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
          adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
          adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
          adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
          adt.userdata, adt.userdefined, adt.windmax, 0)
      FROM   atcf.bdeck adt
      WHERE  adt.basin=_region AND adt.year=_year AND adt.cyclonenum=_cycloneNum
      AND adt.id=_record_id AND t.sandbox_id=_sbox_id AND t.id= adt.id;--
  ELSIF _scope_cd = 'FDECK' THEN
      UPDATE atcf.sandbox_fdeck t
      SET (reftime, clat, clon,
          forecaster, reporttype, accuracymeteorological,
          accuracynavigational, algorithm, analysisinitials, centerorintensity,
          centertype, ci24hourforecast, ciconfidence, cinum, comments,
          dewpointtemp, distancetonearestdatanm, dvorakcodelongtermtrend,
          dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
          eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
          fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
          flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
          inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
          inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
          maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
          maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
          maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
          microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
          outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
          outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
          pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
          quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
          radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
          radarobservationcodecharacteristics, radarobservationcodemovement,
          radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
          radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
          rainaccumulationlon, rainaccumulationtimeinterval, rainflag, rainrate,
          satellitetype, scenetype, seasurfacetemp, sensortype, slpraw, slpretrieved,
          sondeenvironment, speedmeanwind0to500mkt, speedmeanwindlowest150mkt,
          spiraloverlaydegrees, starttime, subregion, tnumaverage, tnumaveragingderivation,
          tnumaveragingtimeperiod, tnumraw, tempcloudsurroundingeye, tempeye,
          tempinsideeye, tempoutsideeye, temppassivemicrowave, tropicalindicator,
          waveheight, windcode, windmax, windmaxconfidence, windrad1, windrad2,
          windrad3, windrad4, windrad5, windrad6, windrad7, windrad8)
        = (s.reftime, s.clat, s.clon,
          s.forecaster, s.reporttype, s.accuracymeteorological,
          s.accuracynavigational, s.algorithm, s.analysisinitials, s.centerorintensity,
          s.centertype, s.ci24hourforecast, s.ciconfidence, s.cinum, s.comments,
          s.dewpointtemp, s.distancetonearestdatanm, s.dvorakcodelongtermtrend,
          s.dvorakcodeshorttermtrend, s.endtime, s.eyecharacterorwallcloudthickness,
          s.eyediameternm, s.eyeorientation, s.eyeshape, s.eyeshortaxis, s.eyesize,
          s.fixformat, s.fixsite, s.fixtype, s.flaggedindicator, s.flightlevel100feet,
          s.flightlevelmillibars, s.flightlevelminimumheightmeters, s.heightmidpointlowest150m,
          s.inboundmaxwindazimuth, s.inboundmaxwindelevationfeet, s.inboundmaxwindrangenm,
          s.inboundmaxwindspeed, s.initials, s.maxcloudheightfeet, s.maxflwindinboundbearing,
          s.maxflwindinbounddirection, s.maxflwindinboundintensity, s.maxflwindinboundrangenm,
          s.maxrainaccumulation, s.maxseas, s.maxsurfacewindinboundlegbearing,
          s.maxsurfacewindinboundlegintensity, s.maxsurfacewindinboundlegrangenm,
          s.microwaveradiiconfidence, s.missionnumber, s.mslp, s.obheight, s.observationsources,
          s.outboundmaxwindazimuth, s.outboundmaxwindelevationfeet, s.outboundmaxwindrangenm,
          s.outboundmaxwindspeed, s.pcncode, s.percentofeyewallobserved, s.positionconfidence,
          s.pressureconfidence, s.pressurederivation, s.process, s.quad1windrad, s.quad2windrad,
          s.quad3windrad, s.quad4windrad, s.radmod1, s.radmod2, s.radmod3, s.radmod4, s.radmod5,
          s.radmod6, s.radmod7, s.radmod8, s.radwind, s.radwindquad, s.radarformat,
          s.radarobservationcodecharacteristics, s.radarobservationcodemovement,
          s.radarsiteposlat, s.radarsiteposlon, s.radartype, s.radiiconfidence,
          s.radiusofmaximumwind, s.radiusofwindintensity, s.radobcode, s.rainaccumulationlat,
          s.rainaccumulationlon, s.rainaccumulationtimeinterval, s.rainflag, s.rainrate,
          s.satellitetype, s.scenetype, s.seasurfacetemp, s.sensortype, s.slpraw, s.slpretrieved,
          s.sondeenvironment, s.speedmeanwind0to500mkt, s.speedmeanwindlowest150mkt,
          s.spiraloverlaydegrees, s.starttime, s.subregion, s.tnumaverage, s.tnumaveragingderivation,
          s.tnumaveragingtimeperiod, s.tnumraw, s.tempcloudsurroundingeye, s.tempeye,
          s.tempinsideeye, s.tempoutsideeye, s.temppassivemicrowave, s.tropicalindicator,
          s.waveheight, s.windcode, s.windmax, s.windmaxconfidence, s.windrad1, s.windrad2,
          s.windrad3, s.windrad4, s.windrad5, s.windrad6, s.windrad7, s.windrad8)
          FROM   atcf.fdeck s
          WHERE  s.basin=_region AND s.year=_year AND s.cyclonenum=_cycloneNum
          AND s.id=_record_id AND t.sandbox_id=_sbox_id AND t.id= s.id;--
  END IF;--

END;--
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.refresh_sandbox_record(INTEGER, INTEGER, VARCHAR, INTEGER, INTEGER, VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.refresh_sandbox_record(INTEGER, INTEGER, VARCHAR, INTEGER, INTEGER, VARCHAR) TO awips;

--Because return type change, need to
DROP FUNCTION IF EXISTS atcf.checkin_fst_from_sandbox(integer); --
--if the function exists

CREATE OR REPLACE FUNCTION atcf.checkin_fst_from_sandbox(_sandbox_id integer)
  RETURNS INTEGER AS
$BODY$
DECLARE
  _rows_impact INTEGER; --
  _region VARCHAR;--
  _year INTEGER;--
  _cycloneNum INTEGER;--
  _scope_cd VARCHAR;--
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
  ) THEN
    RETURN -1; -- No such sandbox
  END IF; --

  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.sandbox
    WHERE id=_sandbox_id
    AND validflag=0
    AND submitted IS NULL
  ) THEN
    RETURN  -2;  -- Not valid for check-in
  END IF; --

  -- get storm keys
  SELECT region, cyclonenum, year, scopecd
  INTO _region, _cycloneNum, _year, _scope_cd
  FROM atcf.sandbox
  WHERE id=_sandbox_id;--

  IF _region IS NULL THEN
      -- not a valid storm
      RETURN -3;--
  END IF;--

  IF _scope_cd != 'FST' THEN
      -- not a FST sandbox
      RETURN -4;--
  END IF;--

  -- Replace records in the fst
  DELETE FROM atcf.fst WHERE basin=_region AND cyclonenum=_cycloneNum AND year=_year;--

  SELECT count(*) INTO _rows_impact FROM atcf.sandbox_fst WHERE sandbox_id=_sandbox_id; --

  INSERT INTO atcf.fst(id, reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT adt.id, adt.reftime, adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
       adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
       adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
       adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
       adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
       adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
       adt.userdata, adt.userdefined, adt.windmax, adt.year
  FROM atcf.sandbox_fst adt
  WHERE adt.sandbox_id=_sandbox_id; --

  --After checked in, clean the data in sandbox
  DELETE FROM atcf.sandbox_fst WHERE sandbox_id=_sandbox_id; --

  -- Set submitted with now date time
  UPDATE atcf.sandbox SET submitted=NOW() WHERE id=_sandbox_id; --

  RETURN _rows_impact; --

END; --
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.checkin_fst_from_sandbox(integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_fst_from_sandbox(integer) TO public;
GRANT EXECUTE ON FUNCTION atcf.checkin_fst_from_sandbox(integer) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.checkin_fst_from_sandbox(integer) TO awips;
GRANT EXECUTE ON FUNCTION atcf.checkin_fst_from_sandbox(integer) TO pguser;


-- promote a genesis to TC
CREATE OR REPLACE FUNCTION atcf.genesis_to_TC(_region varchar, _year integer, _genesisnum integer, _cyclonenum integer, _stormname varchar)
  RETURNS VARCHAR AS
$BODY$
DECLARE
  _stormId VARCHAR; --
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.genesis
    WHERE region=_region
    AND year=_year
    AND genesisnum=_genesisnum
  ) THEN
    RETURN 'NO SUCH GENISIS'; -- No such genesis
  END IF; --

  IF NOT EXISTS (
    SELECT 1
    FROM  atcf.genesis
    WHERE region=_region
    AND year=_year
    AND genesisnum=_genesisnum
    AND genesisstate='GENESIS'
  ) THEN
    RETURN 'NOT VALID GENESIS';  -- Not valid for promote
  END IF; --

  SELECT CONCAT(_region, CAST(_cyclonenum as text), CAST(_year as text)) INTO _stormId; --

  INSERT INTO atcf.bdeck(id, reftime, basin, clat, clon, closedp,
        cyclonenum, eyesize, fcsthour, forecaster, gust, intensity, maxseas,
        maxwindrad, mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT adt.id, adt.reftime, _region, adt.clat, adt.clon, adt.closedp,
       _cyclonenum, adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas,
       adt.maxwindrad, adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
       adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
       adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
       adt.stormdrct, _stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
       adt.userdata, adt.userdefined, adt.windmax, _year
  FROM atcf.genesisbdeck adt
  WHERE basin=_region
  AND year=_year
  AND genesisnum=_genesisnum;--

  INSERT INTO atcf.edeck(id, reftime,
         basin, clat, clon, cyclonenum, forecaster, reporttype,
         year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
         box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
         developmentlevel, ellipseangle, ellipseralong, ellipsercross,
         eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
         minutes, polygonpointstext, probformat, probability, probabilityitem,
         radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
         tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal)
  SELECT id, reftime,
         _region, clat, clon, _cyclonenum, forecaster, reporttype,
         _year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
         box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
         developmentlevel, ellipseangle, ellipseralong, ellipsercross,
         eventdatetimegroup, fcsthour, genordis, _genesisnum, halfrange,
         minutes, polygonpointstext, probformat, probability, probabilityitem,
         radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
         tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal
  FROM atcf.genesisedeck
  WHERE basin=_region
  AND   genesisnum=_genesisnum
  AND   year = _year; --

  INSERT INTO atcf.storm(stormid,cyclonenum,enddtg,genesisnum,mover,
         par1,par2,priority,region,region2,region3,region4,
         region5,size,startdtg,stormname,stormstate,
         subregion,tchlevel,wtnum,year)
  SELECT _stormid,_cyclonenum,enddtg,_genesisnum,mover,
         par1,par2,priority,_region,region2,region3,region4,
         region5,size,startdtg,_stormname,genesisstate,
         subregion,tchlevel,wtnum,_year
  FROM atcf.genesis
  WHERE region=_region
  AND year=_year
  AND genesisnum=_genesisnum; --

  UPDATE atcf.genesis
  SET cyclonenum=_cyclonenum, genesisstate='TC'
  WHERE region=_region
  AND year=_year
  AND genesisnum=_genesisnum; --

  RETURN _stormId; --

END; --
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.genesis_to_TC(varchar, integer, integer, integer, varchar)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.genesis_to_TC(varchar, integer, integer, integer, varchar) TO public;
GRANT EXECUTE ON FUNCTION atcf.genesis_to_TC(varchar, integer, integer, integer, varchar) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.genesis_to_TC(varchar, integer, integer, integer, varchar) TO awips;
GRANT EXECUTE ON FUNCTION atcf.genesis_to_TC(varchar, integer, integer, integer, varchar) TO pguser;

--Function to identify overlapped DTG range

CREATE OR REPLACE FUNCTION
  atcf.get_overlap_dtgs
  (
     _basin VARCHAR,
     _year INTEGER,
     _cyclonenum INTEGER,
     _deck_type VARCHAR,
     _m_min TIMESTAMP,
     _m_max TIMESTAMP
   )
RETURNS TABLE(max_id Integer,
              begin_dtg TIMESTAMP without time zone,
              end_dtg TIMESTAMP without time zone) AS
$BODY$
DECLARE
    _table_name VARCHAR;--
    _max_id Integer;--
    _overlap_min TIMESTAMP without time zone;--
    _overlap_max TIMESTAMP without time zone;--
BEGIN
    IF _deck_type = 'A' THEN
        SELECT 'atcf.adeck' INTO _table_name;--
    ELSIF _deck_type = 'B' THEN
        SELECT 'atcf.bdeck' INTO _table_name;--
    ELSIF _deck_type = 'E' THEN
        SELECT 'atcf.edeck' INTO _table_name;--
    ELSIF _deck_type = 'F' THEN
        SELECT 'atcf.fdeck' INTO _table_name;--
    END IF;--

    SELECT -1 INTO _max_id;--
    SELECT NULL INTO _overlap_min;--
    SELECT NULL INTO _overlap_max;--

    -- get all intersected DTGs into the temp table
    EXECUTE format('SELECT max(id), min(reftime), max(reftime) FROM %s WHERE basin=$1 AND year=$2 AND cyclonenum=$3 AND reftime BETWEEN $4 AND $5', _table_name)
    INTO _max_id, _overlap_min, _overlap_max USING _basin, _year, _cyclonenum, _m_min, _m_max;--

    -- return results
    RETURN QUERY SELECT _max_id, _overlap_min, _overlap_max;--

END;--
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.get_overlap_dtgs(varchar, integer, integer, varchar, TIMESTAMP, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.get_overlap_dtgs(varchar, integer, integer, varchar, TIMESTAMP, TIMESTAMP) TO public;
GRANT EXECUTE ON FUNCTION atcf.get_overlap_dtgs(varchar, integer, integer, varchar, TIMESTAMP, TIMESTAMP) TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.get_overlap_dtgs(varchar, integer, integer, varchar, TIMESTAMP, TIMESTAMP) TO awips;
GRANT EXECUTE ON FUNCTION atcf.get_overlap_dtgs(varchar, integer, integer, varchar, TIMESTAMP, TIMESTAMP) TO pguser;

--Backup A deck

CREATE OR REPLACE FUNCTION
  atcf.backup_adeck_to_sandbox
  (
     _sbox_id INTEGER,
     _min_dtg TIMESTAMP,
     _max_dtg TIMESTAMP
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _conflictGroup VARCHAR; --
BEGIN
  -- Retrive storm key columns
  SELECT region, cyclonenum, year
  INTO _region, _cycloneNum, _year
  FROM atcf.sandbox WHERE id=_sbox_id; --

  --Check if given sandbox exists
  IF(_region is NULL) THEN
      RAISE EXCEPTION 'Given Sandbox is not existing'; --
  END IF; --

  -- conflictgroup
  SELECT _region||_year||_cycloneNum||'ADECK' as conflictGroup
  INTO _conflictGroup;--

  -- Copy DTG records from the a_deck data to sandbox
  INSERT INTO atcf.sandbox_adeck(sandbox_id, id, reftime,
        basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
        radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year, change_cd)
   SELECT _sbox_id, adt.id, adt.reftime,
        adt.basin, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
        adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
        adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, adt.year, 0
   FROM atcf.adeck adt
   WHERE adt.basin=_region
   AND   adt.cyclonenum = _cycloneNum
   AND   adt.year = _year
   AND   adt.reftime BETWEEN _min_dtg AND _max_dtg; --

   -- return list of potential conflicted sandbox ids
   RETURN QUERY SELECT s1.id
                FROM atcf.sandbox s1
                WHERE s1.submitted IS NULL
                AND s1.scopecd = 'ADECK'
                AND s1.sandboxtype = 'CHECKOUT'
                AND s1.id IN (SELECT distinct sd.sandbox_id
                           FROM   atcf.sandbox_dtg sd
                           WHERE  sd.conflictgroup = _conflictGroup
                           AND    sd.dtg BETWEEN _min_dtg AND _max_dtg);--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.backup_adeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.backup_adeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP) TO awips;


--Backup B deck

CREATE OR REPLACE FUNCTION
  atcf.backup_bdeck_to_sandbox
  (
    _sbox_id INTEGER,
    _min_dtg TIMESTAMP,
    _max_dtg TIMESTAMP
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _region VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
BEGIN

  -- Retrive storm key columns
  SELECT region, cyclonenum, year INTO _region, _cycloneNum, _year FROM atcf.sandbox WHERE id=_sbox_id; --
  --Check if given sandbox exists
  IF(_region is NULL) THEN
      RAISE EXCEPTION 'Given Sandbox does not exist'; --
  END IF; --

  -- Copy storm bdeck data to the sandbox
  INSERT INTO atcf.sandbox_bdeck(sandbox_id, id, reftime,
       basin, clat, clon, closedp, cyclonenum,
       eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
       mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
       quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
       radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
       stormdrct, stormname, stormsped, subregion, technique, techniquenum,
       userdata, userdefined, windmax, year, change_cd)
  SELECT _sbox_id, bdt.id, bdt.reftime,
       bdt.basin, bdt.clat, bdt.clon, bdt.closedp, bdt.cyclonenum,
       bdt.eyesize, bdt.fcsthour, bdt.forecaster, bdt.gust, bdt.intensity, bdt.maxseas, bdt.maxwindrad,
       bdt.mslp, bdt.quad1waverad, bdt.quad1windrad, bdt.quad2waverad, bdt.quad2windrad,
       bdt.quad3waverad, bdt.quad3windrad, bdt.quad4waverad, bdt.quad4windrad, bdt.radclosedp,
       bdt.radwave, bdt.radwavequad, bdt.radwind, bdt.radwindquad, bdt.reporttype, bdt.stormdepth,
       bdt.stormdrct, bdt.stormname, bdt.stormsped, bdt.subregion, bdt.technique, bdt.techniquenum,
       bdt.userdata, bdt.userdefined, bdt.windmax, bdt.year, 0
   FROM atcf.bdeck bdt
   WHERE bdt.basin=_region
   AND   bdt.cyclonenum = _cycloneNum
   AND   bdt.year = _year
   AND   bdt.reftime BETWEEN _min_dtg AND _max_dtg; --

   -- return list of potential conflicted sandbox ids
   RETURN QUERY SELECT s1.id
                FROM atcf.sandbox s1
                WHERE s1.region=_region
                AND   s1.cyclonenum = _cycloneNum
                AND   s1.year = _year
                AND   s1.submitted IS NULL
                AND   s1.scopecd = 'BDECK'
                AND   s1.sandboxtype = 'CHECKOUT';--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.backup_bdeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.backup_bdeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP) TO awips;


-- Backup E Deck

CREATE OR REPLACE FUNCTION
 atcf.backup_edeck_to_sandbox
  (
     _sbox_id INTEGER,
     _min_dtg TIMESTAMP,
     _max_dtg TIMESTAMP
  )
  RETURNS TABLE(sid INTEGER) AS
$BODY$
DECLARE
    _region VARCHAR; --
    _year INTEGER; --
    _cycloneNum INTEGER; --
BEGIN
    SELECT region, cyclonenum, year
    INTO _region, _cycloneNum, _year
    FROM atcf.sandbox WHERE id=_sbox_id; --

    IF(_region is NULL) THEN
        RAISE EXCEPTION 'Given sandbox does not exist'; --
    END IF; --

    INSERT INTO atcf.sandbox_edeck(sandbox_id, id,reftime,
        basin,
        clat,clon,cyclonenum,forecaster,reporttype,year,
        alongtrackbias,alongtrackradius,box1latns,box1lonew,
        box2latns,box2lonew,crosstrackbias,crosstrackdirection,
        crosstrackradius,developmentlevel,ellipseangle,
        ellipseralong,ellipsercross,eventdatetimegroup,
        fcsthour,genordis,genesisnum,halfrange,minutes,
        polygonpointstext,probformat,probability,probabilityitem,
        radwindquad,ristarttau,ristoptau,shapetype,stormid2,
        tcfamanopdtg,tcfamsgdtg,tcfaradius,tcfawtnum,technique,
        undefined,vfinal,change_cd)
    SELECT _sbox_id, id,reftime,
        basin,
        clat,clon,cyclonenum,forecaster,reporttype,year,
        alongtrackbias,alongtrackradius,box1latns,box1lonew,
        box2latns,box2lonew,crosstrackbias,crosstrackdirection,
        crosstrackradius,developmentlevel,ellipseangle,
        ellipseralong,ellipsercross,eventdatetimegroup,
        fcsthour,genordis,genesisnum,halfrange,minutes,
        polygonpointstext,probformat,probability,probabilityitem,
        radwindquad,ristarttau,ristoptau,shapetype,stormid2,
        tcfamanopdtg,tcfamsgdtg,tcfaradius,tcfawtnum,technique,
        undefined,vfinal,0
    FROM atcf.edeck
    WHERE basin=_region
    AND   cyclonenum = _cycloneNum
    AND   year = _year
    AND   reftime BETWEEN _min_dtg AND _max_dtg; --

    -- return list of potential conflicted sandbox ids
    RETURN QUERY SELECT s1.id
                 FROM atcf.sandbox s1
                 WHERE s1.region=_region
                 AND   s1.cyclonenum = _cycloneNum
                 AND   s1.year = _year
                 AND   s1.submitted IS NULL
                 AND   s1.scopecd = 'EDECK'
                 AND   s1.sandboxtype = 'CHECKOUT';--
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION atcf.backup_edeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.backup_edeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP) TO awips;


-- Backup F Deck

CREATE OR REPLACE FUNCTION
  atcf.backup_fdeck_to_sandbox
  (
     _sbox_id INTEGER,
     _min_dtg TIMESTAMP,
     _max_dtg TIMESTAMP
   )
  RETURNS TABLE(sid INTEGER) AS
$BODY$
DECLARE
    _region VARCHAR; --
    _year INTEGER; --
    _cycloneNum INTEGER; --
BEGIN
    SELECT region, cyclonenum, year
    INTO _region, _cycloneNum, _year
    FROM atcf.sandbox WHERE id=_sbox_id; --
    IF(_region is NULL) THEN
        RAISE EXCEPTION 'Given sandbox does not exist'; --
    END IF; --

    INSERT INTO atcf.sandbox_fdeck(sandbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, accuracymeteorological, accuracynavigational, algorithm,
       analysisinitials, centerorintensity, centertype, ci24hourforecast,
       ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
       dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
       eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
       fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
       flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
       inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
       inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
       maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
       maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
       maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
       microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
       outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
       outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
       pressureconfidence, pressurederivation, process, quad1windrad,
       quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
       radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
       radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
       radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
       radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
       rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
       rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
       slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
       speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
       tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
       tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
       temppassivemicrowave, tropicalindicator, waveheight, windcode,
       windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
       windrad5, windrad6, windrad7, windrad8, change_cd)
    SELECT _sbox_id, id, reftime,
       basin, clat, clon, cyclonenum, forecaster, reporttype,
       year, accuracymeteorological, accuracynavigational, algorithm,
       analysisinitials, centerorintensity, centertype, ci24hourforecast,
       ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
       dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
       eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
       fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
       flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
       inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
       inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
       maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
       maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
       maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
       microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
       outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
       outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
       pressureconfidence, pressurederivation, process, quad1windrad,
       quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
       radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
       radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
       radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
       radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
       rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
       rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
       slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
       speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
       tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
       tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
       temppassivemicrowave, tropicalindicator, waveheight, windcode,
       windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
       windrad5, windrad6, windrad7, windrad8, 0
    FROM atcf.fdeck
    WHERE basin=_region
    AND   cyclonenum = _cycloneNum
    AND   year = _year
    AND   reftime BETWEEN _min_dtg AND _max_dtg; --

    -- return list of potential conflicted sandbox ids
    RETURN QUERY SELECT s1.id
                 FROM atcf.sandbox s1
                 WHERE s1.region=_region
                 AND   s1.cyclonenum = _cycloneNum
                 AND   s1.year = _year
                 AND   s1.submitted IS NULL
                 AND   s1.scopecd = 'FDECK'
                 AND   s1.sandboxtype = 'CHECKOUT';--
END; --
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION atcf.backup_fdeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.backup_fdeck_to_sandbox(INTEGER, TIMESTAMP, TIMESTAMP) TO awips;

-- Rollback merged A deck

CREATE OR REPLACE FUNCTION
  atcf.rollback_merged_adeck
   (
     _dmlog_id IN INTEGER
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _basin VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _begin_record_id INTEGER;--
  _end_record_id INTEGER;--
  _begin_dtg  TIMESTAMP without time zone;--
  _end_dtg TIMESTAMP without time zone;--
  _new_end_dtg TIMESTAMP without time zone;--
  _sboxid INTEGER;--
  _conflictGroup VARCHAR; --
  _merge_time TIMESTAMP without time zone;--
BEGIN
  -- Retrive storm key columns
  SELECT basin, cyclonenum, year
  INTO _basin, _cycloneNum, _year
  FROM atcf.deckmergelog WHERE id=_dmlog_id; --

  --Check if given sandbox exists
  IF(_basin is NULL) THEN
      RAISE EXCEPTION 'Given DeckMergeLog is not existing'; --
  END IF; --

  IF EXISTS ( SELECT 1 FROM atcf.deckmergelog
              WHERE id>_dmlog_id
              AND basin=_basin
              AND cyclonenum=_cycloneNum
              AND year=_year
              AND decktype='A') THEN
      RAISE EXCEPTION 'Only the latest deck merge could be rollback'; --
  END IF;--

  SELECT endrecordid, newendrecordid, begindtg, enddtg, sandboxid, mergetime
  INTO _begin_record_id, _end_record_id, _begin_dtg, _end_dtg, _sboxid, _merge_time
  FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- find dtg low upper bounds to identify conflict sndboxes
  IF _end_record_id > _begin_record_id THEN
     SELECT reftime INTO _new_end_dtg
     FROM atcf.adeck
     WHERE id = _end_record_id;--
  ELSE
     SELECT _end_dtg INTO _new_end_dtg;--
  END IF;--

  -- conflictgroup
  SELECT _basin||_year||_cycloneNum||'ADECK' as conflictGroup
  INTO _conflictGroup;--

  -- delete newly add deck records
  IF _end_record_id > _begin_record_id THEN
      DELETE FROM atcf.adeck
      WHERE id BETWEEN _begin_record_id+1 AND _end_record_id
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--
  END IF;--

  -- restore overlapped records from backup
  IF (_begin_dtg IS NOT NULL AND _end_dtg IS NOT NULL AND _sboxid>0) THEN
      -- delete merged records in the overlapped area
      DELETE FROM atcf.adeck
      WHERE reftime BETWEEN _begin_dtg AND _end_dtg
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--

      -- restore from the backup sandbox
      INSERT INTO atcf.adeck(iid, reftime,
          basin, eyesize, fcsthour, clat, clon, closedp, cyclonenum,
          forecaster, gust, intensity, maxseas, maxwindrad,
          mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
          quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
          radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
          stormdrct, stormname, stormsped, subregion, technique, techniquenum,
          userdata, userdefined, windmax, year)
      SELECT adt.id, adt.reftime,
          adt.basin, adt.eyesize, adt.fcsthour, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
          adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
          adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
          adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
          adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
          adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
          adt.userdata, adt.userdefined, adt.windmax, adt.year
      FROM  atcf.sandbox_adeck adt
      WHERE adt.sandbox_id=_sboxid;--
  END IF;--

  -- identify and set conflicted sandboxes
  -- however, merge conflicted sandboxes can't be rollback
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND createddt > _merge_time
  AND scopecd = 'ADECK'
  AND sandboxtype = 'CHECKOUT'
  AND id IN (SELECT distinct sd.sandbox_id
             FROM   atcf.sandbox_dtg sd
             WHERE  sd.conflictgroup = _conflictGroup
             AND    sd.dtg BETWEEN _begin_dtg AND _new_end_dtg);--

  IF _sboxid > 0 THEN
      -- Clean sandbox contents
      DELETE FROM atcf.sandbox_adeck
      WHERE sandbox_id=_sboxid;--
      -- Clean sandbox
      DELETE FROM atcf.sandbox
      WHERE id=_sboxid;--
  END IF;--
  -- Clean deckmergelog records
  DELETE FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- return list of conflicted sandbox ids
  RETURN QUERY SELECT s1.id
               FROM atcf.sandbox s1
               INNER JOIN sandbox_dtg sd
                     ON s1.id = sd.sandbox_id
                     AND sd.conflictgroup = _conflictGroup
                     AND sd.dtg BETWEEN _begin_dtg AND _new_end_dtg
               WHERE s1.region=_region
               AND   s1.cyclonenum = _cycloneNum
               AND   s1.year = _year
               AND   s1.scopecd = 'ADECK'
               AND   s1.sandboxtype = 'CHECKOUT'
               AND   s1.validflag = 1
               AND   s1.createddt > _merge_time;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.rollback_merged_adeck(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.rollback_merged_adeck(INTEGER) TO awips;


-- rollback merged B deck

CREATE OR REPLACE FUNCTION
  atcf.rollback_merged_bdeck
   (
     _dmlog_id IN INTEGER
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _basin VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _begin_record_id INTEGER;--
  _end_record_id INTEGER;--
  _begin_dtg  TIMESTAMP without time zone;--
  _end_dtg TIMESTAMP without time zone;--
  _new_end_dtg TIMESTAMP without time zone;--
  _sboxid INTEGER;--
  _conflictGroup VARCHAR; --
  _merge_time TIMESTAMP without time zone;--
BEGIN
  -- Retrive storm key columns
  SELECT basin, cyclonenum, year
  INTO _basin, _cycloneNum, _year
  FROM atcf.deckmergelog WHERE id=_dmlog_id; --

  --Check if given sandbox exists
  IF(_basin is NULL) THEN
      RAISE EXCEPTION 'Given DeckMergeLog is not existing'; --
  END IF; --

  IF EXISTS ( SELECT 1 FROM atcf.deckmergelog
              WHERE id>_dmlog_id
              AND basin=_basin
              AND cyclonenum=_cycloneNum
              AND year=_year
              AND decktype='B') THEN
      RAISE EXCEPTION 'Only the latest deck merge could be rollback'; --
  END IF;--

  SELECT endrecordid, newendrecordid, begindtg, enddtg, sandboxid, mergetime
  INTO _begin_record_id, _end_record_id, _begin_dtg, _end_dtg, _sboxid, _merge_time
  FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- find dtg low upper bounds to identify conflict sndboxes
  IF _end_record_id > _begin_record_id THEN
     SELECT reftime INTO _new_end_dtg
     FROM atcf.bdeck
     WHERE id = _end_record_id;--
  ELSE
     SELECT _end_dtg INTO _new_end_dtg;--
  END IF;--

  -- conflictgroup
  SELECT _basin||_year||_cycloneNum||'BDECK' as conflictGroup
  INTO _conflictGroup;--

  -- delete newly add deck records
  IF _end_record_id > _begin_record_id THEN
      DELETE FROM atcf.bdeck
      WHERE id BETWEEN _begin_record_id+1 AND _end_record_id
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--
  END IF;--

  -- restore overlapped records from backup
  IF (_begin_dtg IS NOT NULL AND _end_dtg IS NOT NULL AND _sboxid>0) THEN
      -- delete merged records in the overlapped area
      DELETE FROM atcf.bdeck
      WHERE reftime BETWEEN _begin_dtg AND _end_dtg
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--

      -- restore from the backup sandbox
      INSERT INTO atcf.bdeck(id, reftime,
          basin, eyesize, fcsthour, clat, clon, closedp, cyclonenum,
          forecaster, gust, intensity, maxseas, maxwindrad,
          mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
          quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp,
          radwave, radwavequad, radwind, radwindquad, reporttype, stormdepth,
          stormdrct, stormname, stormsped, subregion, technique, techniquenum,
          userdata, userdefined, windmax, year)
      SELECT adt.id, adt.reftime,
          adt.basin, adt.eyesize, adt.fcsthour, adt.clat, adt.clon, adt.closedp, adt.cyclonenum,
          adt.forecaster, adt.gust, adt.intensity, adt.maxseas, adt.maxwindrad,
          adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad,
          adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
          adt.radwave, adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
          adt.stormdrct, adt.stormname, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
          adt.userdata, adt.userdefined, adt.windmax, adt.year
      FROM  atcf.sandbox_bdeck adt
      WHERE adt.sandbox_id=_sboxid;--
  END IF;--

  -- identify and set conflicted sandboxes
  -- however, merge conflicted sandboxes can't be rollback
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND createddt > _merge_time
  AND scopecd = 'BDECK'
  AND sandboxtype = 'CHECKOUT';--


  IF _sboxid > 0 THEN
      -- Clean sandbox contents
      DELETE FROM atcf.sandbox_bdeck
      WHERE sandbox_id=_sboxid;--
      -- Clean sandbox
      DELETE FROM atcf.sandbox
      WHERE id=_sboxid;--
  END IF;--
  -- Clean deckmergelog records
  DELETE FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- return list of conflicted sandbox ids
  RETURN QUERY SELECT id
               FROM atcf.sandbox
               WHERE region=_region
               AND   cyclonenum = _cycloneNum
               AND   year = _year
               AND   scopecd = 'BDECK'
               AND   sandboxtype = 'CHECKOUT'
               AND   submitted IS NULL
               AND   validflag = 1
               AND   createddt > _merge_time;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.rollback_merged_bdeck(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.rollback_merged_bdeck(INTEGER) TO awips;

-- rollback merged E deck

CREATE OR REPLACE FUNCTION
  atcf.rollback_merged_edeck
   (
     _dmlog_id IN INTEGER
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _basin VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _begin_record_id INTEGER;--
  _end_record_id INTEGER;--
  _begin_dtg  TIMESTAMP without time zone;--
  _end_dtg TIMESTAMP without time zone;--
  _new_end_dtg TIMESTAMP without time zone;--
  _sboxid INTEGER;--
  _conflictGroup VARCHAR; --
  _merge_time TIMESTAMP without time zone;--
BEGIN
  -- Retrive storm key columns
  SELECT basin, cyclonenum, year
  INTO _basin, _cycloneNum, _year
  FROM atcf.deckmergelog WHERE id=_dmlog_id; --

  --Check if given sandbox exists
  IF(_basin is NULL) THEN
      RAISE EXCEPTION 'Given DeckMergeLog is not existing'; --
  END IF; --

  IF EXISTS ( SELECT 1 FROM atcf.deckmergelog
              WHERE id>_dmlog_id
              AND basin=_basin
              AND cyclonenum=_cycloneNum
              AND year=_year
              AND decktype='E') THEN
      RAISE EXCEPTION 'Only the latest deck merge could be rollback'; --
  END IF;--

  SELECT endrecordid, newendrecordid, begindtg, enddtg, sandboxid, mergetime
  INTO _begin_record_id, _end_record_id, _begin_dtg, _end_dtg, _sboxid, _merge_time
  FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- find dtg low upper bounds to identify conflict sndboxes
  IF _end_record_id > _begin_record_id THEN
     SELECT reftime INTO _new_end_dtg
     FROM atcf.edeck
     WHERE id = _end_record_id;--
  ELSE
     SELECT _end_dtg INTO _new_end_dtg;--
  END IF;--

  -- conflictgroup
  SELECT _basin||_year||_cycloneNum||'EDECK' as conflictGroup
  INTO _conflictGroup;--

  -- delete newly add deck records
  IF _end_record_id > _begin_record_id THEN
      DELETE FROM atcf.edeck
      WHERE id BETWEEN _begin_record_id+1 AND _end_record_id
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--
  END IF;--

  -- restore overlapped records from backup
  IF (_begin_dtg IS NOT NULL AND _end_dtg IS NOT NULL AND _sboxid>0) THEN
      -- delete merged records in the overlapped area
      DELETE FROM atcf.edeck
      WHERE reftime BETWEEN _begin_dtg AND _end_dtg
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--

      -- restore from the backup sandbox
      INSERT INTO atcf.edeck(id,reftime,
          basin,
          clat,clon,cyclonenum,forecaster,reporttype,year,
          alongtrackbias,alongtrackradius,box1latns,box1lonew,
          box2latns,box2lonew,crosstrackbias,crosstrackdirection,
          crosstrackradius,developmentlevel,ellipseangle,
          ellipseralong,ellipsercross,eventdatetimegroup,
          fcsthour,genordis,genesisnum,halfrange,minutes,
          polygonpointstext,probformat,probability,probabilityitem,
          radwindquad,ristarttau,ristoptau,shapetype,stormid2,
          tcfamanopdtg,tcfamsgdtg,tcfaradius,tcfawtnum,technique,
          undefined,vfinal)
      SELECT id,reftime,
          basin,
          clat,clon,cyclonenum,forecaster,reporttype,year,
          alongtrackbias,alongtrackradius,box1latns,box1lonew,
          box2latns,box2lonew,crosstrackbias,crosstrackdirection,
          crosstrackradius,developmentlevel,ellipseangle,
          ellipseralong,ellipsercross,eventdatetimegroup,
          fcsthour,genordis,genesisnum,halfrange,minutes,
          polygonpointstext,probformat,probability,probabilityitem,
          radwindquad,ristarttau,ristoptau,shapetype,stormid2,
          tcfamanopdtg,tcfamsgdtg,tcfaradius,tcfawtnum,technique,
          undefined,vfinal
      FROM  atcf.sandbox_edeck
      WHERE sandbox_id=_sboxid;--
  END IF;--

  -- identify and set conflicted sandboxes
  -- however, merge conflicted sandboxes can't be rollback
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND createddt > _merge_time
  AND scopecd = 'EDECK'
  AND sandboxtype = 'CHECKOUT';--


  IF _sboxid > 0 THEN
      -- Clean sandbox contents
      DELETE FROM atcf.sandbox_edeck
      WHERE sandbox_id=_sboxid;--
      -- Clean sandbox
      DELETE FROM atcf.sandbox
      WHERE id=_sboxid;--
  END IF;--
  -- Clean deckmergelog records
  DELETE FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- return list of conflicted sandbox ids
  RETURN QUERY SELECT id
               FROM atcf.sandbox
               WHERE region=_region
               AND   cyclonenum = _cycloneNum
               AND   year = _year
               AND   scopecd = 'EDECK'
               AND   sandboxtype = 'CHECKOUT'
               AND   submitted IS NULL
               AND   validflag = 1
               AND   createddt > _merge_time;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.rollback_merged_edeck(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.rollback_merged_edeck(INTEGER) TO awips;

-- rollback merged F deck

CREATE OR REPLACE FUNCTION
  atcf.rollback_merged_fdeck
   (
     _dmlog_id IN INTEGER
   )
RETURNS TABLE(sid INTEGER) AS
$FUNC$
DECLARE
  _basin VARCHAR; --
  _year INTEGER; --
  _cycloneNum INTEGER; --
  _begin_record_id INTEGER;--
  _end_record_id INTEGER;--
  _begin_dtg  TIMESTAMP without time zone;--
  _end_dtg TIMESTAMP without time zone;--
  _new_end_dtg TIMESTAMP without time zone;--
  _sboxid INTEGER;--
  _conflictGroup VARCHAR; --
  _merge_time TIMESTAMP without time zone;--
BEGIN
  -- Retrive storm key columns
  SELECT basin, cyclonenum, year
  INTO _basin, _cycloneNum, _year
  FROM atcf.deckmergelog WHERE id=_dmlog_id; --

  --Check if given sandbox exists
  IF(_basin is NULL) THEN
      RAISE EXCEPTION 'Given DeckMergeLog is not existing'; --
  END IF; --

  IF EXISTS ( SELECT 1 FROM atcf.deckmergelog
              WHERE id>_dmlog_id
              AND basin=_basin
              AND cyclonenum=_cycloneNum
              AND year=_year
              AND decktype='F') THEN
      RAISE EXCEPTION 'Only the latest deck merge could be rollback'; --
  END IF;--

  SELECT endrecordid, newendrecordid, begindtg, enddtg, sandboxid, mergetime
  INTO _begin_record_id, _end_record_id, _begin_dtg, _end_dtg, _sboxid, _merge_time
  FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- find dtg low upper bounds to identify conflict sndboxes
  IF _end_record_id > _begin_record_id THEN
     SELECT reftime INTO _new_end_dtg
     FROM atcf.fdeck
     WHERE id = _end_record_id;--
  ELSE
     SELECT _end_dtg INTO _new_end_dtg;--
  END IF;--

  -- conflictgroup
  SELECT _basin||_year||_cycloneNum||'FDECK' as conflictGroup
  INTO _conflictGroup;--

  -- delete newly add deck records
  IF _end_record_id > _begin_record_id THEN
      DELETE FROM atcf.fdeck
      WHERE id BETWEEN _begin_record_id+1 AND _end_record_id
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--
  END IF;--

  -- restore overlapped records from backup
  IF (_begin_dtg IS NOT NULL AND _end_dtg IS NOT NULL AND _sboxid>0) THEN
      -- delete merged records in the overlapped area
      DELETE FROM atcf.fdeck
      WHERE reftime BETWEEN _begin_dtg AND _end_dtg
      AND basin=_basin
      AND cyclonenum=_cycloneNum
      AND year=_year;--

      -- restore from the backup sandbox
      INSERT INTO atcf.fdeck(id, reftime,
         basin, clat, clon, cyclonenum, forecaster, reporttype,
         year, accuracymeteorological, accuracynavigational, algorithm,
         analysisinitials, centerorintensity, centertype, ci24hourforecast,
         ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
         dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
         eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
         fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
         flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
         inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
         inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
         maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
         maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
         maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
         microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
         outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
         outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
         pressureconfidence, pressurederivation, process, quad1windrad,
         quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
         radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
         radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
         radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
         radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
         rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
         rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
         slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
         speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
         tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
         tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
         temppassivemicrowave, tropicalindicator, waveheight, windcode,
         windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
         windrad5, windrad6, windrad7, windrad8)
      SELECT id, reftime,
         basin, clat, clon, cyclonenum, forecaster, reporttype,
         year, accuracymeteorological, accuracynavigational, algorithm,
         analysisinitials, centerorintensity, centertype, ci24hourforecast,
         ciconfidence, cinum, comments, dewpointtemp, distancetonearestdatanm,
         dvorakcodelongtermtrend, dvorakcodeshorttermtrend, endtime, eyecharacterorwallcloudthickness,
         eyediameternm, eyeorientation, eyeshape, eyeshortaxis, eyesize,
         fixformat, fixsite, fixtype, flaggedindicator, flightlevel100feet,
         flightlevelmillibars, flightlevelminimumheightmeters, heightmidpointlowest150m,
         inboundmaxwindazimuth, inboundmaxwindelevationfeet, inboundmaxwindrangenm,
         inboundmaxwindspeed, initials, maxcloudheightfeet, maxflwindinboundbearing,
         maxflwindinbounddirection, maxflwindinboundintensity, maxflwindinboundrangenm,
         maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
         maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
         microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
         outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
         outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
         pressureconfidence, pressurederivation, process, quad1windrad,
         quad2windrad, quad3windrad, quad4windrad, radmod1, radmod2, radmod3,
         radmod4, radmod5, radmod6, radmod7, radmod8, radwind, radwindquad,
         radarformat, radarobservationcodecharacteristics, radarobservationcodemovement,
         radarsiteposlat, radarsiteposlon, radartype, radiiconfidence,
         radiusofmaximumwind, radiusofwindintensity, radobcode, rainaccumulationlat,
         rainaccumulationlon, rainaccumulationtimeinterval, rainflag,
         rainrate, satellitetype, scenetype, seasurfacetemp, sensortype,
         slpraw, slpretrieved, sondeenvironment, speedmeanwind0to500mkt,
         speedmeanwindlowest150mkt, spiraloverlaydegrees, starttime, subregion,
         tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
         tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
         temppassivemicrowave, tropicalindicator, waveheight, windcode,
         windmax, windmaxconfidence, windrad1, windrad2, windrad3, windrad4,
         windrad5, windrad6, windrad7, windrad8
      FROM  atcf.sandbox_fdeck
      WHERE sandbox_id=_sboxid;--
  END IF;--

  -- identify and set conflicted sandboxes
  -- however, merge conflicted sandboxes can't be rollback
  UPDATE atcf.sandbox
  SET validflag=1
  WHERE submitted IS NULL
  AND createddt > _merge_time
  AND scopecd = 'FDECK'
  AND sandboxtype = 'CHECKOUT';--


  IF _sboxid > 0 THEN
      -- Clean sandbox contents
      DELETE FROM atcf.sandbox_fdeck
      WHERE sandbox_id=_sboxid;--
      -- Clean sandbox
      DELETE FROM atcf.sandbox
      WHERE id=_sboxid;--
  END IF;--
  -- Clean deckmergelog records
  DELETE FROM atcf.deckmergelog WHERE id=_dmlog_id;--

  -- return list of conflicted sandbox ids
  RETURN QUERY SELECT id
               FROM atcf.sandbox
               WHERE region=_region
               AND   cyclonenum = _cycloneNum
               AND   year = _year
               AND   scopecd = 'FDECK'
               AND   sandboxtype = 'CHECKOUT'
               AND   submitted IS NULL
               AND   validflag = 1
               AND   createddt > _merge_time;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.rollback_merged_fdeck(INTEGER)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.rollback_merged_fdeck(INTEGER) TO awips;

-- manage deck merge logs

CREATE OR REPLACE FUNCTION
  atcf.manage_deckmerge_log
   (
     _basin VARCHAR,
     _year INTEGER,
     _cycloneNum INTEGER,
     _decktype VARCHAR
   )
RETURNS void AS
$FUNC$
DECLARE
  _scope_cd VARCHAR;--
  _dml_id INTEGER; --
  _sbox_id INTEGER; --
  _merge_time TIMESTAMP without time zone;--
  _count INTEGER;--
  _offset INTEGER;--
  i INTEGER;--
BEGIN

  -- initialize dml id and sandbix id
  SELECT -1 INTO _dml_id;--
  SELECT -1 INTO _sbox_id;--
  SELECT 0 INTO _offset;--
  SELECT 0 INTO i;--
  SELECT _decktype||'DECK' INTO _scope_cd;--

  SELECT count(*) INTO _count
  FROM atcf.deckmergelog
  WHERE basin= _basin AND year = _year AND cyclonenum=_cycloneNum AND decktype = _decktype;--

  IF _count = 0 THEN
      -- nothing need to be managed
      RETURN;--
  END IF;--
  WHILE i < _count LOOP
      SELECT id, sandboxid, mergetime INTO _dml_id, _sbox_id, _merge_time
      FROM atcf.deckmergelog
      WHERE basin=_basin AND year=_year AND cyclonenum=_cycloneNum AND decktype=_decktype
      ORDER BY mergetime DESC
      LIMIT 1 OFFSET _offset;--

      IF EXISTS (SELECT 1 FROM atcf.sandbox WHERE submitted > _merge_time
                 AND region= _basin AND year = _year AND cyclonenum=_cycloneNum
                 AND scopecd=_scope_cd AND sandboxtype='CHECKOUT') THEN
          -- Someone checked in, no longer abl to rollback
          IF _sbox_id > 0 THEN
              SELECT atcf.rm_backup_sandbox(_sbox_id, _decktype);--
          END IF;--
          DELETE FROM atcf.deckmergelog WHERE id=_dml_id;--
          SELECT _count-1 INTO _count;--
      ELSE
          IF i > 2 THEN
              -- only keep top 3 backup for rollback
              IF _sbox_id > 0 THEN
                  SELECT atcf.rm_backup_sandbox(_sbox_id, _decktype);--
              END IF;--
              DELETE FROM atcf.deckmergelog WHERE id=_dml_id;--
              SELECT _count-1 INTO _count;--
          ELSE
              SELECT _offset+1 INTO _offset;--
          END IF;--
      END IF;--
      SELECT i+1 INTO i;--
   END LOOP;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.manage_deckmerge_log(VARCHAR, INTEGER, INTEGER, VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.manage_deckmerge_log(VARCHAR, INTEGER, INTEGER, VARCHAR) TO awips;

-- remove backup sandbox

CREATE OR REPLACE FUNCTION
  atcf.rm_backup_sandbox
   (
     _sbox_id INTEGER,
     _decktype VARCHAR
   )
RETURNS INTEGER AS
$FUNC$
DECLARE
  _table_name VARCHAR; --
  _sbox_id INTEGER; --
BEGIN

    IF NOT EXISTS (SELECT 1 FROM atcf.sandbox WHERE id=_sbox_id AND sandboxtype='BACKUP') THEN
        -- No such backup sandbox
        RETURN -1;--
    END IF;--

    IF _deck_type = 'A' THEN
        SELECT 'atcf.sandbox_adeck' INTO _table_name;--
    ELSIF _deck_type = 'B' THEN
        SELECT 'atcf.sandbox_bdeck' INTO _table_name;--
    ELSIF _deck_type = 'E' THEN
        SELECT 'atcf.sandbox_edeck' INTO _table_name;--
    ELSIF _deck_type = 'F' THEN
        SELECT 'atcf.sandbox_fdeck' INTO _table_name;--
    END IF;--

    -- clean backup sandbox
    EXECUTE format('DELETE FROM %s WHERE sandbox_id=$1', _table_name)
    USING _sbox_id;--
    DELETE FROM atcf.sandbox WHERE id=_sbox_id;--

    RETURN _sbox_id;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.rm_backup_sandbox(INTEGER, VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.rm_backup_sandbox(INTEGER, VARCHAR) TO awips;

--copy storm

CREATE OR REPLACE FUNCTION
  atcf.copy_storm
  (
     _old_storm_id IN VARCHAR,
     _new_storm_basin IN VARCHAR,
     _new_storm_cyclonenum IN INTEGER,
     _new_storm_year IN INTEGER,
     _new_storm_name IN VARCHAR
  )
RETURNS VARCHAR AS
$FUNC$
DECLARE
  _old_basin VARCHAR; --
  _old_cyclonenum INTEGER;--
  _old_year INTEGER;--
  _new_storm_id VARCHAR; --
BEGIN

  -- Check if the source storm is existing
  IF NOT EXISTS (
     SELECT 1
     FROM  atcf.storm
     WHERE stormid=_old_storm_id
     ) THEN
     RETURN 'The source storm: ' || _old_storm_id || ' is not existing!'; --
  END IF; --

  SELECT trim(_new_storm_basin)||trim(to_char(_new_storm_cyclonenum, '09'))||_new_storm_year
  INTO _new_storm_id;--

  -- Check if the target storm copying to is existing
  IF EXISTS (
     SELECT 1
     FROM  atcf.storm
     WHERE stormid=_new_storm_id
     ) THEN
     RETURN 'The copying storm: ' || _new_storm_id || ' is already existing!'; --
  END IF; --

  SELECT region, cyclonenum, year
  INTO _old_basin, _old_cyclonenum, _old_year
  FROM atcf.storm
  WHERE stormid=_old_storm_id;--

  BEGIN
    -- copy the source storm record to a new storm
    INSERT INTO atcf.storm(stormid,stormname,region,subregion,region2,region3,region4,region5, cyclonenum,
      year,tchlevel,mover,startdtg,enddtg,size,genesisnum,par1,par2,priority,stormstate,wtnum)
    SELECT _new_storm_id,_new_storm_name,_new_storm_basin,subregion,region2,region3,region4,region5,_new_storm_cyclonenum,
      _new_storm_year,tchlevel,mover,startdtg,enddtg,size,genesisnum,par1,par2,priority,stormstate,wtnum
    FROM atcf.storm
    WHERE stormid=_old_storm_id;--

    -- copy decks
    PERFORM atcf.copy_adeck(_old_basin, _old_cyclonenum, _old_year, _new_storm_basin, _new_storm_cyclonenum, _new_storm_year, _new_storm_name);--
    PERFORM atcf.copy_bdeck(_old_basin, _old_cyclonenum, _old_year, _new_storm_basin, _new_storm_cyclonenum, _new_storm_year, _new_storm_name);--
    PERFORM atcf.copy_edeck(_old_basin, _old_cyclonenum, _old_year, _new_storm_basin, _new_storm_cyclonenum, _new_storm_year, _new_storm_name);--
    PERFORM atcf.copy_fdeck(_old_basin, _old_cyclonenum, _old_year, _new_storm_basin, _new_storm_cyclonenum, _new_storm_year, _new_storm_name);--

  EXCEPTION WHEN OTHERS THEN
      -- client need to check which storm id returned
      RETURN 'FAILED copy ' || _old_storm_id || ' to ' || _new_storm_id;--
  END;--
  RETURN  _new_storm_id;--
END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.copy_storm(VARCHAR, VARCHAR, INTEGER, INTEGER, VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.copy_storm(VARCHAR, VARCHAR, INTEGER, INTEGER, VARCHAR) TO awips;


CREATE OR REPLACE FUNCTION
  atcf.copy_adeck
  (
     _old_storm_basin IN VARCHAR,
     _old_storm_cyclonenum IN INTEGER,
     _old_storm_year IN INTEGER,
     _new_storm_basin IN VARCHAR,
     _new_storm_cyclonenum IN INTEGER,
     _new_storm_year IN INTEGER,
     _new_storm_name IN VARCHAR
  )
RETURNS void AS
$FUNC$
DECLARE
  _max_id INTEGER; --
BEGIN
  SELECT max(id) INTO _max_id
  FROM atcf.adeck;--

  IF (_max_id > 0) THEN
    PERFORM setval('atcf.adeckseq', _max_id);--
  END IF;--

  -- Insert new a_deck rows when change_cd=1
  INSERT INTO atcf.adeck(id, reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp, radwave,
        radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT nextval('atcf.adeckseq'), adt.reftime, _new_storm_basin, adt.clat, adt.clon, adt.closedp,
        _new_storm_cyclonenum,adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas,
        adt.maxwindrad,adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad, adt.radwave,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, _new_storm_name, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, _new_storm_year
  FROM atcf.adeck adt
  WHERE basin=_old_storm_basin
  AND   cyclonenum=_old_storm_cyclonenum
  AND   year=_old_storm_year;--


END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.copy_adeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.copy_adeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR) TO awips;

CREATE OR REPLACE FUNCTION
  atcf.copy_bdeck
  (
     _old_storm_basin IN VARCHAR,
     _old_storm_cyclonenum IN INTEGER,
     _old_storm_year IN INTEGER,
     _new_storm_basin IN VARCHAR,
     _new_storm_cyclonenum IN INTEGER,
     _new_storm_year IN INTEGER,
     _new_storm_name IN VARCHAR
  )
RETURNS void AS
$FUNC$
DECLARE
  _max_id INTEGER; --
BEGIN

  SELECT max(id) INTO _max_id
  FROM atcf.bdeck;--

  IF (_max_id > 0) THEN
    PERFORM setval('atcf.bdeckseq', _max_id);--
  END IF;--

  -- Insert new b_deck rows when change_cd=1
  INSERT INTO atcf.bdeck(id, reftime, basin, clat, clon, closedp, cyclonenum,
        eyesize, fcsthour, forecaster, gust, intensity, maxseas, maxwindrad,
        mslp, quad1waverad, quad1windrad, quad2waverad, quad2windrad,
        quad3waverad, quad3windrad, quad4waverad, quad4windrad, radclosedp, radwave,
        radwavequad, radwind, radwindquad, reporttype, stormdepth,
        stormdrct, stormname, stormsped, subregion, technique, techniquenum,
        userdata, userdefined, windmax, year)
  SELECT NEXTVAL('atcf.bdeckseq'), adt.reftime, _new_storm_basin, adt.clat, adt.clon, adt.closedp,
        _new_storm_cyclonenum,adt.eyesize, adt.fcsthour, adt.forecaster, adt.gust, adt.intensity, adt.maxseas,
        adt.maxwindrad,adt.mslp, adt.quad1waverad, adt.quad1windrad, adt.quad2waverad, adt.quad2windrad, adt.radwave,
        adt.quad3waverad, adt.quad3windrad, adt.quad4waverad, adt.quad4windrad, adt.radclosedp,
        adt.radwavequad, adt.radwind, adt.radwindquad, adt.reporttype, adt.stormdepth,
        adt.stormdrct, _new_storm_name, adt.stormsped, adt.subregion, adt.technique, adt.techniquenum,
        adt.userdata, adt.userdefined, adt.windmax, _new_storm_year
  FROM atcf.bdeck adt
  WHERE basin=_old_storm_basin
  AND   cyclonenum=_old_storm_cyclonenum
  AND   year=_old_storm_year;--


END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.copy_bdeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.copy_bdeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR) TO awips;


CREATE OR REPLACE FUNCTION
  atcf.copy_edeck
  (
     _old_storm_basin IN VARCHAR,
     _old_storm_cyclonenum IN INTEGER,
     _old_storm_year IN INTEGER,
     _new_storm_basin IN VARCHAR,
     _new_storm_cyclonenum IN INTEGER,
     _new_storm_year IN INTEGER,
     _new_storm_name IN VARCHAR
  )
RETURNS void AS
$FUNC$
DECLARE
  _max_id INTEGER; --
BEGIN

  SELECT max(id) INTO _max_id
  FROM atcf.edeck;--

  IF (_max_id > 0) THEN
    PERFORM setval('atcf.edeckseq', _max_id);--
  END IF;--
  -- Insert new storm e deck records
  INSERT INTO atcf.edeck(id, reftime,
             basin, clat, clon, cyclonenum, forecaster, reporttype,
             year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
             box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
             developmentlevel, ellipseangle, ellipseralong, ellipsercross,
             eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
             minutes, polygonpointstext, probformat, probability, probabilityitem,
             radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
             tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal)
  SELECT     NEXTVAL('atcf.edeckseq'), reftime,
             _new_storm_basin, clat, clon, _new_storm_cyclonenum, forecaster, reporttype,
             _new_storm_year, alongtrackbias, alongtrackradius, box1latns, box1lonew,
             box2latns, box2lonew, crosstrackbias, crosstrackdirection, crosstrackradius,
             developmentlevel, ellipseangle, ellipseralong, ellipsercross,
             eventdatetimegroup, fcsthour, genordis, genesisnum, halfrange,
             minutes, polygonpointstext, probformat, probability, probabilityitem,
             radwindquad, ristarttau, ristoptau, shapetype, stormid2, tcfamanopdtg,
             tcfamsgdtg, tcfaradius, tcfawtnum, technique, undefined, vfinal
  FROM atcf.edeck
  WHERE basin=_old_storm_basin
  AND   cyclonenum=_old_storm_cyclonenum
  AND   year=_old_storm_year;--

END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.copy_edeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.copy_edeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR) TO awips;


CREATE OR REPLACE FUNCTION
  atcf.copy_fdeck
  (
     _old_storm_basin IN VARCHAR,
     _old_storm_cyclonenum IN INTEGER,
     _old_storm_year IN INTEGER,
     _new_storm_basin IN VARCHAR,
     _new_storm_cyclonenum IN INTEGER,
     _new_storm_year IN INTEGER,
     _new_storm_name IN VARCHAR
  )
RETURNS void AS
$FUNC$
DECLARE
  _max_id INTEGER; --
BEGIN

  SELECT max(id) INTO _max_id
  FROM atcf.fdeck;--

  IF (_max_id > 0) THEN
    PERFORM setval('atcf.fdeckseq', _max_id);--
  END IF;--
  -- Copy source f deck records to the new storm
  INSERT INTO atcf.fdeck(id, reftime, basin, clat, clon,
              cyclonenum, forecaster, reporttype, year, accuracymeteorological,
              accuracynavigational, algorithm, analysisinitials, centerorintensity,
              centertype, ci24hourforecast, ciconfidence, cinum, comments, dewpointtemp,
              distancetonearestdatanm, dvorakcodelongtermtrend, dvorakcodeshorttermtrend,
              endtime, eyecharacterorwallcloudthickness, eyediameternm, eyeorientation,
              eyeshape, eyeshortaxis, eyesize, fixformat, fixsite, fixtype, flaggedindicator,
              flightlevel100feet, flightlevelmillibars, flightlevelminimumheightmeters,
              heightmidpointlowest150m, inboundmaxwindazimuth, inboundmaxwindelevationfeet,
              inboundmaxwindrangenm, inboundmaxwindspeed, initials, maxcloudheightfeet,
              maxflwindinboundbearing, maxflwindinbounddirection, maxflwindinboundintensity,
              maxflwindinboundrangenm, maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
              maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
              microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
              outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
              outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
              pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
              quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
              radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
              radarobservationcodecharacteristics, radarobservationcodemovement,
              radarsiteposlat, radarsiteposlon, radartype, radiiconfidence, radiusofmaximumwind,
              radiusofwindintensity, radobcode, rainaccumulationlat, rainaccumulationlon,
              rainaccumulationtimeinterval, rainflag, rainrate, satellitetype, scenetype,
              seasurfacetemp, sensortype, slpraw, slpretrieved, sondeenvironment,
              speedmeanwind0to500mkt, speedmeanwindlowest150mkt, spiraloverlaydegrees,
              starttime, subregion, tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
              tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
              temppassivemicrowave, tropicalindicator, waveheight, windcode, windmax,
              windmaxconfidence, windrad1, windrad2, windrad3, windrad4, windrad5,
              windrad6, windrad7, windrad8)
  SELECT      NEXTVAL('atcf.fdeckseq'), reftime, _new_storm_basin, clat, clon,
              _new_storm_cyclonenum, forecaster, reporttype, _new_storm_year, accuracymeteorological,
              accuracynavigational, algorithm, analysisinitials, centerorintensity,
              centertype, ci24hourforecast, ciconfidence, cinum, comments, dewpointtemp,
              distancetonearestdatanm, dvorakcodelongtermtrend, dvorakcodeshorttermtrend,
              endtime, eyecharacterorwallcloudthickness, eyediameternm, eyeorientation,
              eyeshape, eyeshortaxis, eyesize, fixformat, fixsite, fixtype, flaggedindicator,
              flightlevel100feet, flightlevelmillibars, flightlevelminimumheightmeters,
              heightmidpointlowest150m, inboundmaxwindazimuth, inboundmaxwindelevationfeet,
              inboundmaxwindrangenm, inboundmaxwindspeed, initials, maxcloudheightfeet,
              maxflwindinboundbearing, maxflwindinbounddirection, maxflwindinboundintensity,
              maxflwindinboundrangenm, maxrainaccumulation, maxseas, maxsurfacewindinboundlegbearing,
              maxsurfacewindinboundlegintensity, maxsurfacewindinboundlegrangenm,
              microwaveradiiconfidence, missionnumber, mslp, obheight, observationsources,
              outboundmaxwindazimuth, outboundmaxwindelevationfeet, outboundmaxwindrangenm,
              outboundmaxwindspeed, pcncode, percentofeyewallobserved, positionconfidence,
              pressureconfidence, pressurederivation, process, quad1windrad, quad2windrad,
              quad3windrad, quad4windrad, radmod1, radmod2, radmod3, radmod4, radmod5,
              radmod6, radmod7, radmod8, radwind, radwindquad, radarformat,
              radarobservationcodecharacteristics, radarobservationcodemovement,
              radarsiteposlat, radarsiteposlon, radartype, radiiconfidence, radiusofmaximumwind,
              radiusofwindintensity, radobcode, rainaccumulationlat, rainaccumulationlon,
              rainaccumulationtimeinterval, rainflag, rainrate, satellitetype, scenetype,
              seasurfacetemp, sensortype, slpraw, slpretrieved, sondeenvironment,
              speedmeanwind0to500mkt, speedmeanwindlowest150mkt, spiraloverlaydegrees,
              starttime, subregion, tnumaverage, tnumaveragingderivation, tnumaveragingtimeperiod,
              tnumraw, tempcloudsurroundingeye, tempeye, tempinsideeye, tempoutsideeye,
              temppassivemicrowave, tropicalindicator, waveheight, windcode, windmax,
              windmaxconfidence, windrad1, windrad2, windrad3, windrad4, windrad5,
              windrad6, windrad7, windrad8
  FROM atcf.fdeck
  WHERE basin=_old_storm_basin
  AND   cyclonenum=_old_storm_cyclonenum
  AND   year=_old_storm_year;--


END; --
$FUNC$
  LANGUAGE plpgsql VOLATILE;

ALTER FUNCTION atcf.copy_fdeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.copy_fdeck(VARCHAR,INTEGER,INTEGER,VARCHAR,INTEGER,INTEGER,VARCHAR) TO awips;


-- Update storm ( 6 modifiable parameters)

CREATE OR REPLACE FUNCTION
  atcf.update_storm
  (
     _old_storm_id VARCHAR,
     _new_region VARCHAR,
     _new_cyclonenum INTEGER,
     _new_storm_name VARCHAR,
     _new_storm_state VARCHAR,
     _new_sub_region VARCHAR,
     _new_mover VARCHAR,
     _new_wt_num INTEGER
   )
RETURNS VARCHAR AS
$BODY$
DECLARE
    _old_region VARCHAR;--
    _old_year Integer;--
    _old_cyclonenum Integer;--
    _old_storm_name VARCHAR;--
    _old_storm_state VARCHAR;--
    _old_sub_region VARCHAR;--
    _old_mover VARCHAR;--
    _old_wt_num INTEGER;--
    _update_deck_flag Integer;--
    _new_storm_id VARCHAR;--
BEGIN
    -- Retrive storm key columns
    SELECT region, cyclonenum, year, stormname, stormstate, subregion, mover, wtnum
    INTO _old_region, _old_cyclonenum, _old_year, _old_storm_name, _old_storm_state, _old_sub_region, _old_mover, _old_wt_num
    FROM atcf.storm WHERE stormid=_old_storm_id; --

    --Check if given sandbox exists
    IF(_old_region is NULL) THEN
      RAISE EXCEPTION 'Given old Storm is not existing'; --
    END IF; --

    -- initialize variables
    SELECT -1 INTO _update_deck_flag;--
    SELECT 'NaN' INTO _new_storm_id;--

    -- check if deck records need to be updated
    IF (_new_storm_name != 'NaN' OR _new_region != 'NaN' OR _new_cyclonenum > 0) THEN
      -- Any change on these three fields required deck level updates
      SELECT 1 INTO _update_deck_flag;--

      -- either region or cyclonenum change will change the stormid
      IF (_new_region != 'NaN' OR _new_cyclonenum > 0) THEN
        IF (_new_region = 'NaN') THEN
          SELECT _old_region INTO _new_region;--
        END IF;--
        IF (_new_cyclonenum <= 0) THEN
          SELECT _old_cyclonenum INTO _new_cyclonenum;--
        END IF;--

        -- create new storm ID
        SELECT trim(_new_region)||trim(to_char(_new_cyclonenum, '09'))||_old_year
        INTO _new_storm_id;--
      END IF;--
    END IF;--

    -- check if new storm id is already existing
    IF (_new_storm_id != 'NaN') THEN
      IF EXISTS (SELECT 1 FROM atcf.storm WHERE stormid=_new_storm_id) THEN
        RAISE EXCEPTION 'Updating region or cyclonenum are conflicted with a existing storm!'; --
      END IF; --
    END IF;--

    -- if any field is NaN or -1, set to old value
    IF (_new_region = 'NaN') THEN
      SELECT _old_region INTO _new_region;--
    END IF;--

    IF (_new_cyclonenum <= 0) THEN
      SELECT _old_cyclonenum INTO _new_cyclonenum;--
    END IF;--

    IF (_new_storm_name = 'NaN') THEN
      SELECT _old_storm_name INTO _new_storm_name;--
    END IF;--

    IF (_new_storm_state = 'NaN') THEN
      SELECT _old_storm_state INTO _new_storm_state;--
    END IF;--

    IF (_new_sub_region = 'NaN') THEN
      SELECT _old_sub_region INTO _new_sub_region;--
    END IF;--

    IF (_new_mover = 'NaN') THEN
      SELECT _old_mover INTO _new_mover;--
    END IF;--

    IF (_new_wt_num < 0) THEN
      SELECT _old_wt_num INTO _new_wt_num;--
    END IF;--

    IF (_update_deck_flag > 0) THEN

      --Update storm
      IF (_new_storm_id != 'NaN') THEN
        UPDATE atcf.storm
        SET stormid=_new_storm_id, region=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name, stormstate=_new_storm_state, subregion=_new_sub_region, mover=_new_mover, wtnum=_new_wt_num
        WHERE region=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--
      ELSE
        UPDATE atcf.storm
        SET stormname=_new_storm_name, stormstate=_new_storm_state, subregion=_new_sub_region, mover=_new_mover, wtnum=_new_wt_num
        WHERE region=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--
      END IF;--

      -- Update decks
      UPDATE atcf.adeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.bdeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.edeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.fdeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.fst
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox_adeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox_bdeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox_edeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox_fdeck
      SET basin=_new_region, cyclonenum=_new_cyclonenum
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox_fst
      SET basin=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE basin=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--

      UPDATE atcf.sandbox
      SET region=_new_region, cyclonenum=_new_cyclonenum, stormname=_new_storm_name
      WHERE region=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--
    ELSE
      -- none of stormname, region or cyclonenum being changed, only storm table need to be updated
      UPDATE atcf.storm
      SET stormname=_new_storm_name, stormstate=_new_storm_state, subregion=_new_sub_region, mover=_new_mover, wtnum=_new_wt_num
      WHERE region=_old_region AND cyclonenum=_old_cyclonenum AND year=_old_year;--
    END IF;--

    -- caller should check the return then notify accordingly
    IF (_new_storm_id = 'NaN') THEN
      RETURN _old_storm_id;--
    ELSE
      RETURN _new_storm_id;--
    END IF;--

END;--
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.update_storm(varchar, varchar, integer, varchar, varchar, varchar, varchar, integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.update_storm(varchar, varchar, integer, varchar, varchar, varchar, varchar, integer) TO awips;

-- delete storm
CREATE OR REPLACE FUNCTION
  atcf.delete_storm
  (
     _region VARCHAR,
     _cyclonenum INTEGER,
     _year INTEGER
   )
RETURNS void AS
$BODY$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atcf.storm WHERE region=_region AND cyclonenum=_cyclonenum AND year=_year) THEN
        RAISE EXCEPTION 'Given storm is not existing!'; --
    END IF;--

    -- delete decks
    DELETE FROM atcf.adeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.bdeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.edeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.fdeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.fst
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    -- delete sandbox records
    DELETE FROM atcf.sandbox_adeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.sandbox_bdeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.sandbox_edeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.sandbox_fdeck
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--

    DELETE FROM atcf.sandbox_fst
    WHERE basin=_region AND cyclonenum=_cyclonenum AND year=_year;--
 
    -- delete sandbox dtg
    DELETE FROM atcf.sandbox_dtg
    WHERE sandbox_id IN (SELECT id FROM atcf.sandbox WHERE region=_region AND cyclonenum=_cyclonenum AND year=_year);--

    --delete any sandbox related to the storm
    DELETE FROM atcf.sandbox
    WHERE region=_region AND cyclonenum=_cyclonenum AND year=_year;--

    --Delete storm record
    DELETE FROM atcf.storm
    WHERE region=_region AND cyclonenum=_cyclonenum AND year=_year;--

END;--
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION atcf.delete_storm(varchar, integer, integer)
  OWNER TO awipsadmin;
GRANT EXECUTE ON FUNCTION atcf.delete_storm(varchar, integer, integer) TO awips;


