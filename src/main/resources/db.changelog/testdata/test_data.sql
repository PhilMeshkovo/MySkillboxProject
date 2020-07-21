
BEGIN;

ALTER TABLE posts MODIFY text TEXT;

INSERT INTO users(id, is_moderator, reg_time, name, email, password, code, photo) VALUES
(1, 1,  '2016-01-06','Bonita Boyer',  'tarakan@mail.ru', '$2y$12$tfeBz8zV72agV/JS/UJy4OHD7sEcPsyworySRx0jaTgpQljHRe4Jm', '1234', '1234'),
(2, 1,'2014-09-30', 'Ernie Adamson',  'WaltraudK.Pearson96@example.com', '44968843', '1234', '1234'),
(3, -1, '2000-02-16','Hosea Tolliver',  'dydqi814@example.com', '982937485', '1234', '1234'),
(4, 1, '1996-04-22','Maryanna Boykin',   'ElliottSkaggs@example.com', '989219271', '1234', '1234'),
(5, -1, '1988-01-04','Hank Mullin',   'HerschelKendall72@example.com', '80403776', '1234', '1234'),
(6, 1, '2015-07-21','Alpha Figueroa',   'SheriseLogue@example.com', '330900623', '1234', '1234'),
(7, -1, '1988-03-03','Elisha Schmitz',   'Melda.Mercier27@nowhere.com', '9433394', '1234', '1234'),
(8, 1,'1991-06-22','Clifford Tomlin',   'FossD3@nowhere.com', '8272165', '1234', '1234'),
(9, -1,'2009-03-09','Kenneth Rayburn',   'Copeland@example.com', '$2a$10$cAB8OjwxRpXpFX1hPGu3ZeM/clcv48hihWGOnclIbG0YU.vCHKrNq', '1234', '1234'),
(10, 1, '1987-07-09','Dominique Schneider',  'Meacham33@nowhere.com', '8223815', '1234', '1234'),
(11, 0, '2019-07-09','phil',  'gmeshkovo1977@gmail.com', '$2a$10$mrctKO/LYECQkj7vY9a5wuXhzrL./0Kc4NrzoNWjO9gPGzDg5x1AK', 'd6cb2c50-6d5c-4584-b5bc-13ed6b58a650', '1234');

INSERT INTO posts(id, moderation_status,moderator_id, view_count, time, user_id, title, text, is_active) VALUES
(1,'NEW',9,7, '1971-01-01 00:28:11', 9, 'Non nostrum dignissimos.', 'Focusing on the latest investigations, we can positively say that the example of the feedback system must stay true to an importance of the well-known practice.  ',1),
(2,'ACCEPTED',5,3, '1981-11-10 03:2:13', 5, 'Doloremque qui rerum.', 'The most common argument against this is that the initial progress in the specific decisions reinforces the argument for any contemporary or prominent approach.  ', -1),
(3,'DECLINED',4,2,'1983-12-09 07:15:10', 8, 'Nulla fuga exercitationem.', 'To straighten it out, the conventional notion of the arrangement of the system mechanism provides rich insights into the predictable behavior.  ', 1),
(4,'NEW',6,9, '1993-07-13 07:19:30', 1, 'Ad voluptate laudantium.', 'One cannot deny that a number of brand new approaches has been tested during the the improvement of the final draft.', 1),
(5, 'ACCEPTED',5,3,'1971-01-01 00:15:34', 7, 'Numquam et eveniet.', 'In respect that any technical requirements impacts fully on every grand strategy. In respect of the condition of the flexible production planning may share attitudes on the potential role models.', 1),
(6, 'DECLINED',7,1,'1971-01-01 00:00:05', 7, 'Qui inventore est.', 'Besides, components of an overview of the strategic planning poses problems and challenges for both the internal network and the basic reason of the closely developed techniques.', -1),
(7, 'NEW',6,9,'1971-01-01 00:00:02', 7, 'Veniam voluptas est.', 'On the contrary, the accurate predictions of the sources and influences of the overall scores may share attitudes on the benefits of data integrity. Thus a complete understanding is missing.', 1),
(8, 'ACCEPTED',1,3,'1990-09-01 13:25:49', 7, 'Rerum eos quam.', '<!DOCTYPE html> <html><head><title>Hyperlink Example</title>  </head><body> <p>Click following link</p></body> </html>', 1),
(9, 'DECLINED',7,8,'2003-04-05 03:35:01', 1, 'Sed quidem cupiditate.', 'The the layout of the commitment to quality assurance gives less satisfactory results.', 1),
(10, 'ACCEPTED',8,4,'1977-02-20 02:00:16', 8, 'Nihil quos aspernatur.', 'Nevertheless, one should accept that the big impact provides a glimpse at the strategic planning. Thus a complete understanding is missing.  ', -1),
(11, 'ACCEPTED',8,4,'2020-02-20 02:00:16', 8, 'tagName', 'Nevertheless, one should accept that the big impact provides a glimpse at the strategic planning. Thus a complete understanding is missing.  ', 1);

INSERT INTO post_comments(id, time, post_id, parent_id, user_id, text) VALUES
(1, '1971-01-01 00:00:07', 9, null, 10, 'By the way, with help of the skills leads us to a clear understanding of the conceptual design.  '),
(2, '2006-11-23 05:4:15', 9, 1, 4, 'In a word, each of the task analysis impacts typically on every market tendencies.'),
(3, '2006-12-23 23:20:32', 9, 2, 8, 'What is more, the condition of the treatment provides a strict control over the sufficient amount.'),
(4, '1974-05-31 10:02:51', 6, null, 7, 'By the way, any further consideration cannot be developed under such circumstances.'),
(5, '1971-01-01 00:00:08', 6, 4, 1, '1The the framework of the primary element gives less satisfactory results.'),
(6, '1971-01-01 00:00:08', 6, 5, 2, '2e the framework of the primary element gives less satisfactory results.'),
(7, '1971-01-01 00:00:08', 4, null, 3, 'T3he the framework of the primary element gives less satisfactory results.'),
(8, '1971-01-01 00:00:08', 4, 7, 5, '4The the framework of the primary element gives less satisfactory results.'),
(9, '1971-01-01 00:00:08', 4, 7, 6, 'T5he the framework of the primary element gives less satisfactory results.'),
(10, '1971-01-01 00:00:08', 4, 9, 9, 'T6he the framework of the primary element gives less satisfactory results.');

INSERT INTO captcha_codes(id, time, code, secret_code) VALUES
(1, '1971-01-01 01:20:00', '5232', '5212'),
(2, '2016-07-01 15:33:11', '7777', '7777'),
(3, '1971-01-01 00:00:23', '9999', '8889'),
(4, '1971-01-01 00:00:11', '9666', '9666'),
(5, '2000-09-22 13:08:01', '3333', '3333'),
(6, '1971-01-01 00:00:05', '3333', '3333'),
(7, '1983-09-01 16:45:08', '1111', '1111'),
(8, '2012-11-29 09:02:27', '1111', '1111'),
(9, '1971-01-01 01:29:01', '8888', '8888'),
(10, '1971-01-01 00:00:01', '1000', '1000');

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
(1, '1979-02-20 05:12:1', 8, 3, -1),
(2, '1978-08-19 10:18:20', 7, 2, 1),
(3, '1971-01-01 02:07:38', 2, 2, 1),
(4, '1995-12-10 04:17:25', 8, 3, -1),
(5, '1971-01-01 00:00:04', 5, 5, 1),
(6, '1980-04-14 02:06:48', 10, 5, -1),
(7, '2001-12-31 20:47:3', 9, 4, 1),
(8, '1974-08-22 10:06:19', 7, 2, 1),
(9, '1971-01-01 00:00:3', 9, 4, 1),
(10, '1984-10-31 07:25:26', 4, 4, 1);

INSERT INTO global_settings(id, code, name, value) VALUES
(1, 'MULTIUSER_MODE', 'Многопользовательский режим', false),
(2, 'POST_PREMODERATION', 'Премодерация постов', true),
(3, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', true);

INSERT INTO tag2post(post_id, tag_id) VALUES
(5, 5),
(7, 7),
(9, 10),
(9, 9),
(3, 3),
(3, 1),
(1, 2),
(1, 1),
(8, 8),
(10, 10);

COMMIT;

