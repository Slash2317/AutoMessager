package com.slash.automessager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.slash.automessager.domain.Data;
import org.springframework.stereotype.Repository;

import java.io.*;

@Repository
public class DataRepositoryImpl implements DataRepository {

    @Override
    public Data loadData() {
        File file = new File(getFilepath());
        if (!file.exists()) {
            return null;
        }
        else {
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String json = sb.toString();

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, Data.class);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void saveData(Data data) {
        File file = createDataFileIfNeeded();
        try (FileWriter fileWriter = new FileWriter(file)) {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            fileWriter.write(ow.writeValueAsString(data));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createDataFileIfNeeded() {
        File file = new File(getFilepath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private String getFilepath() {
        return System.getProperty("user.home") + File.separator + "AutoMessagerBot" + File.separator + "data.json";
    }
}
