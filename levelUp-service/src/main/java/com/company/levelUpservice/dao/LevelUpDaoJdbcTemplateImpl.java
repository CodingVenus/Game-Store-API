package com.company.levelUpservice.dao;

import com.company.levelUpservice.dto.LevelUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class LevelUpDaoJdbcTemplateImpl implements LevelUpDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public LevelUpDaoJdbcTemplateImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static final String INSERT_LEVEL_UP_SQL =
            "INSERT INTO level_up (customer_id, member_date, points) values (?, ?, ?)";

    private static final String SELECT_LEVEL_UP_SQL =
            "SELECT * FROM level_up WHERE level_up_id = ?";

    private static final String SELECT_LEVEL_UP_BY_CUSTOMER_ID_SQL =
            "SELECT * FROM level_up WHERE customer_id = ?";

    private static final String SELECT_ALL_LEVEL_UPS_SQL =
            "SELECT * FROM level_up";

    private static final String DELETE_LEVEL_UP_SQL =
            "DELETE FROM level_up WHERE level_up_id = ?";

    private static final String UPDATE_LEVEL_UP_SQL =
            "UPDATE level_up SET customer_id = ?, member_date = ?, points = ? WHERE level_up_id = ?";


    //Mapper

    private LevelUp mapRowToLevelUp(ResultSet rs, int rowNum) throws SQLException {
        LevelUp levelUp = new LevelUp();
        levelUp.setLevelUpId(rs.getInt("level_up_id"));
        levelUp.setCustomerId(rs.getInt("customer_id"));
        levelUp.setMemberDate(rs.getDate("member_date").toLocalDate());
        levelUp.setPoints(rs.getInt("points"));
        return levelUp;
    }

    // Method Implementation

    @Override
    public LevelUp addLevelUp(LevelUp levelUp) {
        jdbcTemplate.update(INSERT_LEVEL_UP_SQL,
                levelUp.getCustomerId(),levelUp.getMemberDate(),levelUp.getPoints());

        int id = jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);

        levelUp.setLevelUpId(id);

        return levelUp;
    }

    @Override
    public LevelUp getLevelUp(int levelUpId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_LEVEL_UP_SQL, this::mapRowToLevelUp, levelUpId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LevelUp getLevelUpByCustomerId(int customerId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_LEVEL_UP_BY_CUSTOMER_ID_SQL, this::mapRowToLevelUp, customerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<LevelUp> getAllLevelUp() {
        return jdbcTemplate.query(SELECT_ALL_LEVEL_UPS_SQL, this::mapRowToLevelUp);
    }

    @Override
    public void updateLevelUp(LevelUp levelUp) {
        jdbcTemplate.update(UPDATE_LEVEL_UP_SQL,
                levelUp.getCustomerId(),levelUp.getMemberDate(),levelUp.getPoints(),
                levelUp.getLevelUpId());
    }

    @Override
    public void deleteLevelUp(int levelUpId) {
        jdbcTemplate.update(DELETE_LEVEL_UP_SQL, levelUpId);
    }
}
