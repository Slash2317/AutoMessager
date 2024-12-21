package com.slash.automessager.repository;

import com.slash.automessager.domain.PendingMessage;

import java.util.List;

public interface AutoMessageCommandRepository {

    List<PendingMessage> findAndUpdatePendingMessages(Integer botId);
}
