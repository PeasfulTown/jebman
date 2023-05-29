package xyz.peasfultown.dao;

import xyz.peasfultown.helpers.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

public interface GenericJointTableDAO {
    Set<Integer> readIdsOfMainObject(int id, String colName) throws DAOException;
}
