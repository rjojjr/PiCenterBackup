package com.kirchnersolutions.picenter.backup.csv.interfaces;


import com.kirchnersolutions.picenter.backup.entites.AppUser;

import java.util.ArrayList;
import java.util.List;

public class AppUserCSVImpl implements CSVParser{

    public String parseToCSV(List<DBItem> items){
        return getCSV(items);
    }

    public List<DBItem> parseToList(String CSV) {
        return getDbItems(CSV, true);
    }

    public List<DBItem> parseToListWithoutId(String CSV) {
        return getDbItems(CSV, false);
    }

    private String getCSV(List<DBItem> items) {
        StringBuilder CSV = new StringBuilder("id,username,first_name,last_name,password,admin,creation_time");
        for (DBItem item : items) {
            AppUser user = (AppUser) item;
            String admin = "false";
            if (user.isAdmin()) {
                admin = "true";
            }
            CSV.append("\r\r" +
                    user.getId() + "," +
                    user.getUserName() + "," +
                    user.getFirstName() + "," +
                    user.getLastName() + "," +
                    user.getPassword() + "," +
                    admin + "," +
                    user.getCreateTime());
        }
        return CSV.toString();
    }

    private AppUser toItem(String string, boolean withId){
        boolean admin = false;
        String[] columns = string.split(",");
        if(columns[5].contains("t")){
            admin = true;
        }
        if(withId){
            return new AppUser(
                    Long.parseLong(columns[0]),
                    Long.parseLong(columns[6]),
                    columns[1],
                    columns[2],
                    columns[3],
                    columns[5],
                    admin
            );
        }else{
            return new AppUser(
                    Long.parseLong(columns[6]),
                    columns[1],
                    columns[2],
                    columns[3],
                    columns[5],
                    admin
            );
        }

    }

    private List<DBItem> getDbItems(String CSV, boolean withId) {
        boolean first = true;
        List<DBItem> output = new ArrayList<>();
        String[] items = CSV.split("\r\n");
        for (String item : items) {
            if (first) {
                first = false;
            } else {
                output.add(toItem(item, withId));
            }
        }
        return output;
    }
}
