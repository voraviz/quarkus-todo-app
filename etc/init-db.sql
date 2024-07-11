drop table if exists Todo;
drop sequence if exists todo_seq;
create sequence todo_seq start 1 increment 1;
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
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('todo_seq'), 'Introduction to Quarkus', true, 0, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('todo_seq'), 'Hibernate with Panache', false, 1, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('todo_seq'), 'Visit Quarkus web site', false, 2, 'https://quarkus.io');
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('todo_seq'), 'Star Quarkus project', false, 3, 'https://github.com/quarkusio/quarkus/');
