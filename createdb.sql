DROP TABLE IF EXISTS books_authors_link;
DROP TABLE IF EXISTS tags_books_link;
DROP TABLE IF EXISTS series_books_link;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS series;
DROP TABLE IF EXISTS publishers;

CREATE TABLE publishers (
  id INTEGER,
  name TEXT,
  CONSTRAINT pk_publishers PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE books (
  id INTEGER
    CONSTRAINT pk_books PRIMARY KEY AUTOINCREMENT,
  isbn TEXT
    COLLATE NOCASE
    CONSTRAINT df_books_i DEFAULT "",
  title TEXT 
    NOT NULL
    COLLATE NOCASE
    CONSTRAINT df_books_n DEFAULT "Unknown",
  series_index REAL
    NOT NULL
    CONSTRAINT df_books_sidx DEFAULT 1.0,
  publisher_id INTEGER 
    CONSTRAINT fk_books_p REFERENCES publishers (id),
  path TEXT 
    NOT NULL
    COLLATE NOCASE
    CONSTRAINT df_books_p DEFAULT "",
  date_added TEXT
    NOT NULL
    CONSTRAINT df_books_da DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
  date_modified TEXT
    NOT NULL
    CONSTRAINT df_books_dm DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
  date_published TEXT
    NOT NULL
    CONSTRAINT df_books_dp DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))
);

CREATE TABLE authors (
  id INTEGER
    CONSTRAINT pk_authors PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_authors UNIQUE
    CONSTRAINT df_authors DEFAULT "Unknown"
);

CREATE TABLE books_authors_link (
  id INTEGER
    CONSTRAINT pk_books_authors_relation PRIMARY KEY AUTOINCREMENT,
  book_id INTEGER
    CONSTRAINT fk_books_authors_bid REFERENCES books (id),
  author_id INTEGER
    CONSTRAINT fk_books_authors_aid REFERENCES authors (id)
);

INSERT INTO books (id,isbn,title) VALUES (1,"8392389283398","One Flew Over The Cuckoo's Nest");
INSERT INTO books (id,isbn,title) VALUES (2,"9780316323703","Rebecca");
INSERT INTO books (id,isbn,title) VALUES (3,"9780449237571","Menfreya in the Morning");
INSERT INTO books (id,isbn,title) VALUES (4,"9780134685991","Effective Java");

INSERT INTO books (id,title) VALUES (5,"Head First Design Patterns");

INSERT INTO books (id,title) VALUES (6,"A Little Hatred");
INSERT INTO books (id,title) VALUES (7,"The Trouble with Peace");
INSERT INTO books (id,title) VALUES (8,"The Wisdom of Crowds");

INSERT INTO authors (id,name) VALUES (1,"Ken Kersey");
INSERT INTO authors (id,name) VALUES (2,"Daphne du Maurier");
INSERT INTO authors (id,name) VALUES (3,"Victoria Holt");
INSERT INTO authors (id,name) VALUES (4,"Joshua Bloch");

INSERT INTO authors (id,name) VALUES (5,"Eric Freeman");
INSERT INTO authors (id,name) VALUES (6,"Elisabeth Robson");
INSERT INTO authors (id,name) VALUES (7,"Bert Bates");

INSERT INTO authors (id,name) VALUES (8,"Joe Abercrombie");

INSERT INTO books_authors_link (book_id, author_id) VALUES (1,1);
INSERT INTO books_authors_link (book_id, author_id) VALUES (2,2);
INSERT INTO books_authors_link (book_id, author_id) VALUES (3,3);
INSERT INTO books_authors_link (book_id, author_id) VALUES (4,4);

INSERT INTO books_authors_link (book_id, author_id) VALUES (5,5);
INSERT INTO books_authors_link (book_id, author_id) VALUES (5,6);
INSERT INTO books_authors_link (book_id, author_id) VALUES (5,7);

INSERT INTO books_authors_link (book_id, author_id) VALUES (6,8);
INSERT INTO books_authors_link (book_id, author_id) VALUES (7,8);
INSERT INTO books_authors_link (book_id, author_id) VALUES (8,8);

CREATE TABLE series (
  id INTEGER
    CONSTRAINT pk_series PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_series UNIQUE
);

INSERT INTO series (id, name) VALUES (1, "The Age of Madness");

CREATE TABLE series_books_link (
  id INTEGER
    CONSTRAINT pk_series_books PRIMARY KEY AUTOINCREMENT,
  book_id INTEGER
    CONSTRAINT fk_series_books_link_bid REFERENCES books (id)
    CONSTRAINT uq_series_books UNIQUE,
  series_id INTEGER
    CONSTRAINT fk_series_books_link_sid REFERENCES series (id)
);

INSERT INTO series_books_link (book_id, series_id) VALUES (6, 1);
INSERT INTO series_books_link (book_id, series_id) VALUES (7, 1);
INSERT INTO series_books_link (book_id, series_id) VALUES (8, 1);

CREATE TABLE tags (
  id INTEGER
    CONSTRAINT pk_tags PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_tags UNIQUE
);

INSERT INTO tags (id, name) VALUES (1, "series");
INSERT INTO tags (id, name) VALUES (2, "gothic");

CREATE TABLE tags_books_link (
  id INTEGER
    CONSTRAINT pk_tags_books_link PRIMARY KEY AUTOINCREMENT,
  tag_id INTEGER
    CONSTRAINT fk_tags_books_link_tid REFERENCES tags (id),
  book_id INTEGER
    CONSTRAINT fk_tags_books_link_bid REFERENCES books (id)
);

INSERT INTO tags_books_link (tag_id, book_id) VALUES (1,6);
INSERT INTO tags_books_link (tag_id, book_id) VALUES (1,7);
INSERT INTO tags_books_link (tag_id, book_id) VALUES (1,8);

INSERT INTO tags_books_link (tag_id, book_id) VALUES (2, 2);
INSERT INTO tags_books_link (tag_id, book_id) VALUES (2, 3);

