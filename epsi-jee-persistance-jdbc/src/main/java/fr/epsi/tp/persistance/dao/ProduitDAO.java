package fr.epsi.tp.persistance.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.epsi.tp.persistance.ConnectionFactory;
import fr.epsi.tp.persistance.bean.Produit;

public class ProduitDAO implements IJdbcCrud<Produit, Long> {
    private final MarqueDAO marqueDAO;

    public ProduitDAO() {
        this.marqueDAO = new MarqueDAO();
    }

    @Override
    public Produit findById(Long identifier) throws SQLException {
        String query = "SELECT * FROM produit WHERE id = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, identifier);
            ResultSet resultSet = preparedStatement.executeQuery();

            return createProductListFromResultSet(resultSet).get(0);
        }
    }

    @Override
    public Collection<Produit> findAll() throws SQLException {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            String query = "SELECT * FROM produit";
            ResultSet resultSet = statement.executeQuery(query);

            return createProductListFromResultSet(resultSet);
        }
    }

    @Override
    public Produit create(Produit entity) throws SQLException {
        String query = """
                INSERT INTO produit (libelle, description, prix, marque_id) 
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, entity.getLibelle());
            preparedStatement.setString(2, entity.getDescription());
            preparedStatement.setBigDecimal(3, entity.getPrix());
            preparedStatement.setLong(4, entity.getMarque().getIdentifier());
            preparedStatement.execute();

            // Get last inserted marque's id
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            if (!resultSet.next()) return null;

            entity.setIdentifier(resultSet.getLong(1));

            return entity;
        }
    }

    public Collection<Produit> findByLibelle(String libelle) throws SQLException {
        String query = "SELECT * FROM produit WHERE libelle = ?";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, libelle);
            ResultSet resultSet = preparedStatement.executeQuery();

            return createProductListFromResultSet(resultSet);
        }
    }

    @Override
    public Produit update(Produit entity) throws SQLException {
        String query = """
                UPDATE produit 
                SET libelle = ?, description = ?, prix = ?, marque_id = ?
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, entity.getLibelle());
            preparedStatement.setString(2, entity.getDescription());
            preparedStatement.setBigDecimal(3, entity.getPrix());
            preparedStatement.setLong(4, entity.getMarque().getIdentifier());
            preparedStatement.setLong(5, entity.getIdentifier());
            preparedStatement.executeUpdate();

            return entity;
        }
    }

    /**
     * Helper to create a list of products from a result set
     *
     * @param resultSet The result set to retrieve products from, must NOT be closed
     * @return A list of product, may be empty
     * @throws SQLException if a database access error occurs or the result set is closed
     */
    private List<Produit> createProductListFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Produit> produits = new ArrayList<>();

        while (resultSet.next()) {
            Produit produit = new Produit();
            produit.setIdentifier(resultSet.getLong("id"));
            produit.setLibelle(resultSet.getString("libelle"));
            produit.setDescription(resultSet.getString("description"));
            produit.setPrix(resultSet.getBigDecimal("prix"));
            produit.setMarque(this.marqueDAO.findById(resultSet.getLong("marque_id")));

            produits.add(produit);
        }

        return produits;
    }
}
