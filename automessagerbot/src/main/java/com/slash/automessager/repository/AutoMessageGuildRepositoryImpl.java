package com.slash.automessager.repository;

import com.slash.automessager.Application;
import com.slash.automessager.domain.AutoMessageGuild;
import com.slash.automessager.domain.BasicGuildInfo;
import com.slash.automessager.utils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoMessageGuildRepositoryImpl implements AutoMessageGuildRepository {

    @Override
    public BasicGuildInfo findGuildInfoByBotIdAndGuildDiscordId(Integer botId, Long guildDiscordId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("SELECT auto_message_guild_id, prefix FROM auto_message_guild WHERE auto_message_bot_id = ? AND guild_discord_id = ?");
            stmt.setInt(1, botId);
            stmt.setLong(2, guildDiscordId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }
            BasicGuildInfo guildInfo = new BasicGuildInfo();
            guildInfo.setGuildId(rs.getInt(1));
            guildInfo.setPrefix(rs.getString(2));
            return guildInfo;
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
    public void updateGuildPrefix(Integer guildId, String prefix) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("UPDATE auto_message_guild SET prefix = ? WHERE auto_message_guild_id = ?");
            stmt.setString(1, prefix);
            stmt.setInt(2, guildId);
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
    public Integer insertGuild(AutoMessageGuild guild) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Application.getDataSource().getConnection();
            stmt = conn.prepareStatement("INSERT INTO auto_message_guild(auto_message_bot_id, guild_discord_id, prefix) VALUES(?, ?, ?) RETURNING auto_message_guild_id");
            stmt.setLong(1, guild.getBotId());
            stmt.setLong(2, guild.getDiscordId());
            stmt.setString(3, guild.getPrefix());
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
