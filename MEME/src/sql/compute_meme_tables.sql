
--
-- Load meme_tables (as MTH user)
--
truncate table meme_tables;
insert into meme_tables select table_name from user_tables;

--
-- Load meme_indexes, meme_ind_colums (assumes MEME_SYSTEM has been loaded)
--
exec MEME_SYSTEM.refresh_meme_indexes
