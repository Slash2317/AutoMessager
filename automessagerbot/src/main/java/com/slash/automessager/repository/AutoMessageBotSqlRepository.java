package com.slash.automessager.repository;

import java.util.Map;

public interface AutoMessageBotSqlRepository {

    Map<Long, String> loadPrefixes(Integer botId);
}
