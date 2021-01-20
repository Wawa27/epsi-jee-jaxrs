package fr.epsi.tp.persistance.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import fr.epsi.tp.persistance.ConnectionFactory;
import fr.epsi.tp.persistance.bean.Marque;

public class MarqueDAO implements IJdbcCrud<Marque, Long> {

    @Override
    public Marque findById(Long identifier) throws SQLException {
        String query = "SELECT * FROM marque WHERE id = ?";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, identifier);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Marque not found
            if (!resultSet.next()) return null;

            Marque marque = new Marque();
            marque.setIdentifier(identifier);
            marque.setLibelle(resultSet.getString("libelle"));
            return marque;
        }
    }

    @Override
    public Collection<Marque> findAll() throws SQLException {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT * FROM marque");

            ArrayList<Marque> marques = new ArrayList<>();

            while (resultSet.next()) {
                Marque marque = new Marque();
                marque.setIdentifier(resultSet.getLong("id"));
                marque.setLibelle(resultSet.getString("libelle"));
                marques.add(marque);
            }

            return marques;
        }
    }

    @Override
    public Marque create(Marque entity) throws SQLException {
        String query = "INSERT INTO marque (libelle) VALUES (?)";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, entity.getLibelle());

            if (preparedStatement.executeUpdate() != 1) return null;

            // Get last inserted marque's id
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (!resultSet.next()) return null;

            entity.setIdentifier(resultSet.getLong(1));

            return entity;
        }
    }

    @Override
    public Marque update(Marque entity) throws SQLException {
        String query = "UPDATE marque SET libelle = ? WHERE id = ?";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, entity.getLibelle());
            preparedStatement.setLong(2, entity.getIdentifier());
            preparedStatement.executeUpdate();

            return entity;
        }
    }

}
