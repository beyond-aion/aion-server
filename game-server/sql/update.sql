/*
 * DB changes since e7107d7f (02.02.2019)
 */

-- drop unused 'seller'-name column in broker
alter table broker drop column seller;