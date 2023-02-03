drop sequence if exists hibernate_sequence;
drop constraint if exists unique_title;
drop table if exists Todo;
create sequence hibernate_sequence start 1 increment 1;
create table Todo (
       id int8 not null,
       completed boolean not null,
       ordering int4,
       title varchar(255),
       url varchar(255),
       primary key (id)
    );
alter table if exists Todo
    add constraint unique_title unique (title);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('hibernate_sequence'), 'Introduction to Quarkus', true, 0, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('hibernate_sequence'), 'Hibernate with Panache', false, 1, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('hibernate_sequence'), 'Visit Quarkus web site', false, 2, 'https://quarkus.io');
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('hibernate_sequence'), 'Star Quarkus project', false, 3, 'https://github.com/quarkusio/quarkus/');
