package xyz.peasfultown.dao;

import java.util.List;
import java.util.Set;

public interface GenericDAO<P> {
    Set<P> readAll() throws DAOException;
    P read(int id) throws DAOException;
    P read(String str) throws DAOException;

    void create(P object) throws DAOException;

    void update(P object) throws DAOException;

    void delete(int id) throws DAOException;
}
