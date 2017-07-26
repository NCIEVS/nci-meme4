
EXEC DBMS_STATS.GATHER_SCHEMA_STATS(ownname => 'MTH', degree => 8, options => 'GATHER STALE', cascade => TRUE);
EXEC DBMS_STATS.GATHER_SCHEMA_STATS(ownname => 'MEOW', degree => 8, options => 'GATHER', cascade => TRUE);
EXEC DBMS_STATS.EXPORT_SCHEMA_STATS('MTH', 'MTHSTATS');
EXEC DBMS_STATS.EXPORT_SCHEMA_STATS('MEOW', 'MEOWSTATS');
