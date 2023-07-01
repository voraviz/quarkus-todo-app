INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('Todo_SEQ'), 'Introduction to Quarkus', true, 0, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('Todo_SEQ'), 'Hibernate with Panache', false, 1, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('Todo_SEQ'), 'Visit Quarkus web site', false, 2, 'https://quarkus.io');
INSERT INTO todo(id, title, completed, ordering, url) VALUES (nextval('Todo_SEQ'), 'Star Quarkus project', false, 3, 'https://github.com/quarkusio/quarkus/');