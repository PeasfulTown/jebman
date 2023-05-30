package xyz.peasfultown.dao;

import java.util.Set;

/**
 * Joint tables have names in this format "firstcol_secondcol_link"
 * Example: books_authors_link
 * Where:
 *      - `books` is the first column and contains IDs of `books` table records
 *      - `authors` is the second column and contains IDs of `authors` table records
 *
 * To establish relationships between records of 2 different tables.
 */
public interface GenericJointTableDAO {
    Set<Integer> readFirstColIdsBySecondColIds(int id) throws DAOException;
    Set<Integer> readSecondColIdsByFirstColIds(int id) throws DAOException;
}
