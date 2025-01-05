package com.slash.automessager.repository;

import com.slash.automessager.Application;
import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.utils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AutoMessageBotRepositoryImpl implements AutoMessageBotRepository {

    @Override
    public Optional<AutoMessageBot> findByDiscordId(Long discordId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("SELECT auto_message_bot_id, sent_messages_count FROM auto_message_bot WHERE bot_discord_id = ?");
            stmt.setLong(1, discordId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            AutoMessageBot bot = new AutoMessageBot();
            bot.setId(rs.getInt(1));
            bot.setDiscordId(discordId);
            bot.setSentMessagesCount(rs.getInt(2));
            return Optional.of(bot);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }

    @Override
    public void updateMessageCount(Integer botId, Integer sentMessagesCount) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("UPDATE auto_message_bot SET sent_messages_count = ? WHERE auto_message_bot_id = ?");
            stmt.setInt(1, sentMessagesCount);
            stmt.setInt(2, botId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, null);
        }
    }

    @Override
    public Map<Long, String> loadPrefixes(Integer botId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("SELECT guild_discord_id, prefix FROM auto_message_guild WHERE auto_message_bot_id = ? AND prefix IS NOT NULL");
            stmt.setInt(1, botId);
            rs = stmt.executeQuery();

            Map<Long, String> guildIdToPrefix = new HashMap<>();
            while (rs.next()) {
                guildIdToPrefix.put(rs.getLong(1), rs.getString(2));
            }
            return guildIdToPrefix;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }

    @Override
    public Integer insertBot(AutoMessageBot bot) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("INSERT INTO auto_message_bot(bot_discord_id, sent_messages_count) VALUES(?, ?) RETURNING auto_message_bot_id");
            stmt.setLong(1, bot.getDiscordId());
            stmt.setInt(2, bot.getSentMessagesCount());
            rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }
}
