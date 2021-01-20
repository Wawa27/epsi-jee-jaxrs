package fr.epsi.tp.persistance.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.epsi.tp.persistance.ConnectionFactory;
import fr.epsi.tp.persistance.bean.Commande;
import fr.epsi.tp.persistance.bean.CommandeLigne;
import fr.epsi.tp.persistance.bean.Marque;
import fr.epsi.tp.persistance.bean.Produit;

public class CommandeDAO implements IJdbcCrud<Commande, Long> {

    @Override
    public Commande findById(Long identifier) throws SQLException {
        String query = "SELECT * FROM commande WHERE id = ?";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, identifier);
            ResultSet resultSet = preparedStatement.executeQuery();

            return getCommandListFromResultSet(resultSet).get(0);
        }
    }

    @Override
    public Collection<Commande> findAll() throws SQLException {
        String query = "SELECT * FROM commande";

        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(query);

            return getCommandListFromResultSet(resultSet);
        }
    }

    @Override
    public Commande create(Commande entity) throws SQLException {
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("INSERT INTO commande (date_creation) VALUES (?)");
            preparedStatement.setDate(1, Date.valueOf(entity.getDateCreation()));
            preparedStatement.execute();

            // Get last inserted marque's id
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            if (!resultSet.next()) return null;

            entity.setIdentifier(resultSet.getLong(1));

            for (CommandeLigne commandeLigne : entity.getLignes()) {
                preparedStatement =
                        connection.prepareStatement("""
                                INSERT INTO comm_produit (commande_id, produit_id, quantite) 
                                VALUES (?, ?, ?)
                                """);
                preparedStatement.setLong(1, entity.getIdentifier());
                preparedStatement.setLong(2, commandeLigne.getProduit().getIdentifier());
                preparedStatement.setInt(3, commandeLigne.getQuantite());
                preparedStatement.execute();
            }

            return entity;
        }
    }

    @Override
    public Commande update(Commande entity) throws SQLException {
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("UPDATE commande SET date_creation = ? WHERE id = ?");
            preparedStatement.setDate(1, Date.valueOf(entity.getDateCreation()));
            preparedStatement.setLong(2, entity.getIdentifier());
            preparedStatement.executeUpdate();

            for (CommandeLigne commandeLigne : entity.getLignes()) {
                preparedStatement =
                        connection.prepareStatement("""
                                UPDATE comm_produit 
                                SET quantite = ? 
                                WHERE command_id = ? AND produit_id = ?
                                """);
                preparedStatement.setInt(1, commandeLigne.getQuantite());
                preparedStatement.setLong(2, entity.getIdentifier());
                preparedStatement.setLong(3, commandeLigne.getProduit().getIdentifier());
                preparedStatement.execute();
            }

            return entity;
        }
    }

    /**
     * Retrieve a list of commands from a result set
     * @param resultSet The command to retrieve commands from
     * @return A list of command
     * @throws SQLException if a database access error occurs
     */
    private ArrayList<Commande> getCommandListFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Commande> commandes = new ArrayList<>();

        while (resultSet.next()) {
            Commande commande = new Commande();
            commande.setIdentifier(resultSet.getLong("id"));
            commande.setDateCreation(resultSet.getDate("date_creation").toLocalDate());
            commande.setLignes(getCommandeLignes(commande));
            commandes.add(commande);
        }

        return commandes;
    }

    /**
     * Retrieve a list of command line from a command
     *
     * @param commande The command to retrieve command lines from
     * @return A list of command lines
     * @throws SQLException if a database access error occurs
     */
    private List<CommandeLigne> getCommandeLignes(Commande commande) throws SQLException {
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            ProduitDAO produitDAO = new ProduitDAO();

            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT * FROM comm_produit WHERE commande_id = ?
                    """);
            preparedStatement.setLong(1, commande.getIdentifier());
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<CommandeLigne> commandeLignes = new ArrayList<>();

            while (resultSet.next()) {
                CommandeLigne commandeLigne = new CommandeLigne();
                commandeLigne.setProduit(produitDAO.findById(resultSet.getLong("produit_id")));
                commandeLigne.setQuantite(resultSet.getInt("quantite"));
                commandeLignes.add(commandeLigne);
            }

            return commandeLignes;
        }
    }
}

