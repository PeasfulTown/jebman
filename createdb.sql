DROP TABLE IF EXISTS books_authors_link;
DROP TABLE IF EXISTS series;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;

CREATE TABLE books (
  id INTEGER,
  isbn TEXT,
  title TEXT,
  series_index REAL,
  CONSTRAINT pk_books PRIMARY KEY ("id" AUTOINCREMENT),
  CONSTRAINT df_books_series_index DEFAULT 1.0
);

CREATE TABLE authors (
  id INTEGER,
  name TEXT,
  CONSTRAINT pk_authors PRIMARY KEY ("id" AUTOINCREMENT),
  CONSTRAINT uq_authors UNIQUE (name),
  CONSTRAINT df_authors DEFAULT "Unknown"
);

CREATE TABLE books_authors_link (
  id INTEGER,
  book_id INTEGER,
  author_id INTEGER,
  CONSTRAINT pk_books_authors_relation PRIMARY KEY ("id" AUTOINCREMENT),
  CONSTRAINT fk_books_authors_books FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT fk_books_authors_authors FOREIGN KEY (author_id) REFERENCES authors (id)
);

INSERT INTO books (isbn, title) VALUES ("8392389283398", "One Flew Over The Cuckoo's Nest");
INSERT INTO books (isbn, title) VALUES ("9780316323703","Rebecca");
INSERT INTO books (isbn, title) VALUES ("9780449237571","Menfreya in the Morning");
INSERT INTO books (isbn, title) VALUES ("9780134685991","Effective Java");
INSERT INTO books (title) VALUES ("Head First Design Patterns");

INSERT INTO authors (name) VALUES ("Ken Kersey");
INSERT INTO authors (name) VALUES ("Daphne du Maurier");
INSERT INTO authors (name) VALUES ("Victoria Holt");
INSERT INTO authors (name) VALUES ("Joshua Bloch");

INSERT INTO authors (name) VALUES ("Eric Freeman");
INSERT INTO authors (name) VALUES ("Elisabeth Robson");
INSERT INTO authors (name) VALUES ("Bert Bates");

INSERT INTO books_authors_link (book_id, author_id) VALUES (3,3);
INSERT INTO books_authors_link (book_id, author_id) VALUES (4,4);

INSERT INTO books_authors_link (book_id, author_id) VALUES (5,5);
INSERT INTO books_authors_link (book_id, author_id) VALUES (5,6);
INSERT INTO books_authors_link (book_id, author_id) VALUES (5,7);

INSERT INTO books_authors_link (book_id, author_id) VALUES (1,1);
INSERT INTO books_authors_link (book_id, author_id) VALUES (2,2);

CREATE TABLE series (
  id INTEGER,
  name TEXT,
  CONSTRAINT pk_series PRIMARY KEY ("id" AUTOINCREMENT),
  CONSTRAINT uq_series UNIQUE (name)
)

CREATE TABLE series_books_link (
  id INTEGER,
  book_id INTEGER,
  series_id INTEGER,
  CONSTRAINT pk_series_books PRIMARY KEY (id AUTOINCREMENT),
  CONSTRAINT fk_series_books_bid FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT fk_series_books_sid FOREIGN KEY (series_id) REFERENCES series (id),
  CONSTRAINT uq_series_books UNIQUE (book_id)
)

CREATE TABLE tags (
  id INTEGER,
  name TEXT,
  CONSTRAINT pk_tags PRIMARY KEY (id AUTOINCREMENT),
  CONSTRAINT uq_tags UNIQUE (name)
)

CREATE TABLE tags_books_link (
  id INTEGER,
  book_id INTEGER,
  tag_id INTEGER,
  CONSTRAINT pk_tags_books_link PRIMARY KEY (id AUTOINCREMENT),
  CONSTRAINT fk_tags_books_b FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT fk_tags_books_t FOREIGN KEY (tag_id) REFERENCES tags (id)
)

