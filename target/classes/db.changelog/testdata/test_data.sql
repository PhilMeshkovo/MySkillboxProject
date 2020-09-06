
BEGIN;

INSERT INTO users(id, is_moderator, reg_time, name, email, password, code, photo) VALUES
(1, -1,  '2020-01-06','Bonita Boyer',  'tarakan@mail.ru', '$2y$12$tfeBz8zV72agV/JS/UJy4OHD7sEcPsyworySRx0jaTgpQljHRe4Jm', '1234', '/api/image/default-1.png'),
(2, 1,'2020-09-30', 'Philipp',  'meshkovo1977@mail.ru', '$2y$12$tfeBz8zV72agV/JS/UJy4OHD7sEcPsyworySRx0jaTgpQljHRe4Jm', '1234', '1234');

INSERT INTO posts(id, moderation_status,moderator_id, view_count, time, user_id, title, text, is_active) VALUES
(1,'ACCEPTED',2,7, '2020-01-01 00:28:11', 1, 'Non nostrum dignissimos.', 'Focusing on the latest investigations, we can positively say that the example of the feedback system must stay true to an importance of the well-known practice.  ',1),
(2,'ACCEPTED',1,3, '2020-01-01 00:28:11', 2, 'Java and spring boot', '2e the framework of the primary element gives less satisfactory results.', 1 );

INSERT INTO post_comments(id, time, post_id, parent_id, user_id, text) VALUES
(1, '2020-01-01 00:00:07', 1, null, 2, 'By the way, with help of the skills leads us to a clear understanding of the conceptual design.  '),
(2, '2020-07-23 05:4:15', 2, null, 1, 'In a word, each of the task analysis impacts typically on every market tendencies.');
--(3, '2006-12-23 23:20:32', 9, 2, 8, 'What is more, the condition of the treatment provides a strict control over the sufficient amount.'),
--(4, '1974-05-31 10:02:51', 6, null, 7, 'By the way, any further consideration cannot be developed under such circumstances.'),
--(5, '1971-01-01 00:00:08', 6, 4, 1, '1The the framework of the primary element gives less satisfactory results.'),
--(6, '1971-01-01 00:00:08', 6, 5, 2, '2e the framework of the primary element gives less satisfactory results.'),
--(7, '1971-01-01 00:00:08', 4, null, 3, 'T3he the framework of the primary element gives less satisfactory results.'),
--(8, '1971-01-01 00:00:08', 4, 7, 5, '4The the framework of the primary element gives less satisfactory results.'),
--(9, '1971-01-01 00:00:08', 4, 7, 6, 'T5he the framework of the primary element gives less satisfactory results.'),
--(10, '1971-01-01 00:00:08', 4, 9, 9, 'T6he the framework of the primary element gives less satisfactory results.');

--INSERT INTO captcha_codes(id, time, code, secret_code) VALUES
--(1, '1971-01-01 01:20:00', '5232', '5212'),
--(2, '2016-07-01 15:33:11', '7777', '7777'),
--(3, '1971-01-01 00:00:23', '9999', '8889'),
--(4, '1971-01-01 00:00:11', '9666', '9666'),
--(5, '2000-09-22 13:08:01', '3333', '3333'),
--(6, '1971-01-01 00:00:05', '3333', '3333'),
--(7, '1983-09-01 16:45:08', '1111', '1111'),
--(8, '2012-11-29 09:02:27', '1111', '1111'),
--(9, '1971-01-01 01:29:01', '8888', '8888'),
--(10, '1971-01-01 00:00:01', '1000', '1000');

INSERT INTO tags(id, name) VALUES
(1, 'java'),
(2, 'kotlin'),
(3, 'php'),
(4, 'python'),
(5, 'tagName'),
(6, 'spring'),
(7, 'sql'),
(8, 'postgres'),
(9, 'hibernate'),
(10, 'database');

INSERT INTO post_votes(id, time, user_id, post_id, value) VALUES
(1, '2020-08-20 05:12:1', 1, 2, 1),
(2, '2020-08-20 05:12:1', 2, 1, 1);

INSERT INTO global_settings(id, code, name, value) VALUES
(1, 'MULTIUSER_MODE', 'Многопользовательский режим', 'YES'),
(2, 'POST_PREMODERATION', 'Премодерация постов', 'YES'),
(3, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'YES');

INSERT INTO tag2post(post_id, tag_id) VALUES
(1, 1),
(2, 6);

COMMIT;

