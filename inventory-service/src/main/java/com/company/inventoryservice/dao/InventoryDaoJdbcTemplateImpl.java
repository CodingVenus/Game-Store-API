package com.company.inventoryservice.dao;

import com.company.inventoryservice.dto.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InventoryDaoJdbcTemplateImpl implements InventoryDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public InventoryDaoJdbcTemplateImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // Prepared Statements

    private static final String INSERT_INVENTORY_SQL =
            "INSERT INTO inventory (product_id, quantity) values (?, ?)";

    private static final String SELECT_INVENTORY_SQL =
            "SELECT * FROM inventory WHERE inventory_id = ?";

    private static final String SELECT_ALL_INVENTORYS_SQL =
            "SELECT * FROM inventory";

    private static final String DELETE_INVENTORY_SQL =
            "DELETE FROM inventory WHERE inventory_id = ?";

    private static final String UPDATE_INVENTORY_SQL =
            "UPDATE inventory set product_id = ?, quantity = ? WHERE inventory_id = ?";


    // Mapper
    private Inventory mapRowToInventory(ResultSet rs, int rowNum) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setInventoryID(rs.getInt("inventory_id"));
        inventory.setProductID(rs.getInt("product_id"));
        inventory.setQuantity(rs.getInt("quantity"));
        return inventory;
    }

    // Method Implementations


    @Override
    public Inventory addInventory(Inventory inventory) {
        jdbcTemplate.update(INSERT_INVENTORY_SQL,
                inventory.getProductID(),inventory.getQuantity());

        int id = jdbcTemplate.queryForObject("SELECT last_insert_id()", Integer.class);

        inventory.setInventoryID(id);

        return inventory;
    }

    @Override
    public Inventory getInventory(int id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_INVENTORY_SQL, this::mapRowToInventory, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Inventory> getAllInventory() {
        return jdbcTemplate.query(SELECT_ALL_INVENTORYS_SQL, this::mapRowToInventory);
    }

    @Override
    public void updateInventory(Inventory inventory) {
        jdbcTemplate.update(UPDATE_INVENTORY_SQL,
                inventory.getProductID(),inventory.getQuantity(),
                inventory.getInventoryID());
    }

    @Override
    public void deleteInventory(int id) {
        jdbcTemplate.update(DELETE_INVENTORY_SQL, id);
    }
}
