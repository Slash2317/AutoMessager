package com.slash.automessager.repository;

import com.slash.automessager.domain.Data;

public interface DataRepository {

    Data loadData();

    void saveData(Data data);
}
