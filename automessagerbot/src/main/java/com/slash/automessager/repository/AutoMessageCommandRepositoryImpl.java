package com.slash.automessager.repository;

import com.slash.automessager.Application;
import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.domain.PendingMessage;
import com.slash.automessager.utils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutoMessageCommandRepositoryImpl implements AutoMessageCommandRepository {

    @Override
    public List<PendingMessage> findAndUpdatePendingMessages(Integer botId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("""
                UPDATE auto_message_command
                SET last_run_date = DATE_TRUNC('MINUTE', CURRENT_TIMESTAMP)
                WHERE auto_message_bot_id = ?
                    AND last_run_date <= (CURRENT_TIMESTAMP - minutes * INTERVAL '1 minute')
                RETURNING guild_discord_id, channel_discord_id, content""");
            stmt.setInt(1, botId);
            rs = stmt.executeQuery();

            List<PendingMessage> pendingMessages = new ArrayList<>();
            while (rs.next()) {
                PendingMessage pendingMessage = new PendingMessage();
                pendingMessage.setGuildId(rs.getLong(1));
                pendingMessage.setChannelId(rs.getLong(2));
                pendingMessage.setContent(rs.getString(3));
                pendingMessages.add(pendingMessage);
            }
            return pendingMessages;
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
    public List<AutoMessageCommand> findAllByBotIdAndGuildDiscordId(Integer botId, Long guildDiscordId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("""
                SELECT auto_message_command_id, auto_message_guild_id, channel_discord_id, minutes, content, last_run_date
                FROM auto_message_command
                WHERE auto_message_bot_id = ?
                    AND guild_discord_id = ?""");
            stmt.setInt(1, botId);
            stmt.setLong(2, guildDiscordId);
            rs = stmt.executeQuery();

            List<AutoMessageCommand> commands = new ArrayList<>();
            while (rs.next()) {
                AutoMessageCommand command = new AutoMessageCommand();
                command.setId(rs.getInt(1));
                command.setBotId(botId);
                command.setGuildId(rs.getInt(2));
                command.setGuildDiscordId(guildDiscordId);
                command.setChannelDiscordId(rs.getLong(3));
                command.setMinutes(rs.getInt(4));
                command.setContent(rs.getString(5));
                command.setLastRunDate(rs.getTimestamp(6).toLocalDateTime());
                commands.add(command);
            }
            return commands;
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
    public void insertCommand(AutoMessageCommand command) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("INSERT INTO auto_message_command(auto_message_bot_id, auto_message_guild_id, guild_discord_id, channel_discord_id, minutes, content, last_run_date) VALUES(?, ?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, command.getBotId());
            stmt.setInt(2, command.getGuildId());
            stmt.setLong(3, command.getGuildDiscordId());
            stmt.setLong(4, command.getChannelDiscordId());
            stmt.setInt(5, command.getMinutes());
            stmt.setString(6, command.getContent());
            stmt.setTimestamp(7, Timestamp.valueOf(command.getLastRunDate()));
            stmt.execute();
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
    public void deleteAllByIdIn(List<Integer> commandIds) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement(String.format("DELETE FROM auto_message_command WHERE auto_message_command_id IN (%s)", commandIds.stream().map(Object::toString).collect(Collectors.joining(","))));
            stmt.execute();
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
    public int countByBotIdAndGuildDiscordIdAndChannelDiscordId(Integer botId, Long guildDiscordId, Long channelDiscordId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("""
                SELECT COUNT(auto_message_command_id)
                FROM auto_message_command
                WHERE auto_message_bot_id = ?
                    AND guild_discord_id = ?
                    AND channel_discord_id = ?""");
            stmt.setInt(1, botId);
            stmt.setLong(2, guildDiscordId);
            stmt.setLong(3, channelDiscordId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return 0;
            }
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
