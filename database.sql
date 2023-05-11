CREATE TABLE IF NOT EXISTS authors (
  id INTEGER
    CONSTRAINT pk_authors PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_authors UNIQUE
    CONSTRAINT df_authors DEFAULT "Unknown"
);

CREATE TABLE IF NOT EXISTS publishers (
  id INTEGER
    CONSTRAINT pk_publishers PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_publishers_n UNIQUE
);

CREATE TABLE IF NOT EXISTS series (
  id INTEGER
    CONSTRAINT pk_series PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_series UNIQUE
);

CREATE TABLE IF NOT EXISTS tags (
  id INTEGER
    CONSTRAINT pk_tags PRIMARY KEY AUTOINCREMENT,
  name TEXT
    COLLATE NOCASE
    CONSTRAINT uq_tags UNIQUE
);

CREATE TABLE IF NOT EXISTS books (
  id INTEGER
    CONSTRAINT pk_books PRIMARY KEY AUTOINCREMENT,
  isbn TEXT
    COLLATE NOCASE
    CONSTRAINT df_books_isbn DEFAULT "",
  uuid TEXT
    COLLATE NOCASE
    CONSTRAINT df_books_uuid DEFAULT "",
  title TEXT 
    NOT NULL
    COLLATE NOCASE
    CONSTRAINT df_books_title DEFAULT "Unknown",
  series_id INTEGER
    CONSTRAINT fk_books_series_id REFERENCES series (id),
  series_number REAL
    NOT NULL
    CONSTRAINT df_books_series_number DEFAULT 1.0,
  publisher_id INTEGER 
    CONSTRAINT fk_books_publisher_id REFERENCES publishers (id),
  date_published TEXT
    NOT NULL
    CONSTRAINT df_books_dp DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
  date_added TEXT
    NOT NULL
    CONSTRAINT df_books_da DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
  date_modified TEXT
    NOT NULL
    CONSTRAINT df_books_dm DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
  path TEXT 
);

CREATE TABLE IF NOT EXISTS books_authors_link (
  id INTEGER
    CONSTRAINT pk_books_authors_relation PRIMARY KEY AUTOINCREMENT,
  book_id INTEGER
    CONSTRAINT fk_books_authors_bid REFERENCES books (id),
  author_id INTEGER
    CONSTRAINT fk_books_authors_aid REFERENCES authors (id)
);

CREATE TABLE IF NOT EXISTS books_tags_link (
  id INTEGER
    CONSTRAINT pk_tags_books_link PRIMARY KEY AUTOINCREMENT,
  book_id INTEGER
    CONSTRAINT fk_tags_books_link_bid REFERENCES books (id),
  tag_id INTEGER
    CONSTRAINT fk_tags_books_link_tid REFERENCES tags (id)
);

