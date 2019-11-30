package com.kirchnersolutions.picenter.backup.csv.parsers;

import com.kirchnersolutions.picenter.backup.entites.DBItem;

import java.util.List;

public interface CSVParser {

    public String parseToCSV(List<DBItem> items);

    public List<DBItem> parseToList(String CSV);

    public List<DBItem> parseToListWithoutId(String CSV);

}
