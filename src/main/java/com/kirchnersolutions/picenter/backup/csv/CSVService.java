package com.kirchnersolutions.picenter.backup.csv;


import com.kirchnersolutions.picenter.backup.csv.parsers.CSVParserImpl;
import com.kirchnersolutions.picenter.backup.entites.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.kirchnersolutions.utilities.*;

@DependsOn({"threadPoolTaskExecutor"})
@Service
public class CSVService {

    ReadingRepository readingRepository;
    UserLogRepository userLogRepository;
    AppUserRepository appUserRepository;
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private File dir, appUserDir, userLogDir, readingDir, downloadDir, downloadFileTempDir, downloadFile, restoreDir;

    @Autowired
    public CSVService(AppUserRepository appUserRepository, UserLogRepository userLogRepository, ReadingRepository readingRepository, ThreadPoolTaskExecutor threadPoolTaskExecutor){
        this.appUserRepository = appUserRepository;
        this.readingRepository = readingRepository;
        this.userLogRepository = userLogRepository;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        dir = new File("PiCenter/Backup");
        appUserDir = new File(dir, "/AppUsers");
        userLogDir = new File(dir, "/UserLogs");
        readingDir = new File(dir, "/Readings");
        downloadDir = new File(dir, "/Download");
        downloadFileTempDir = new File(downloadDir, "/PiCenterBackup");
        downloadFile = new File(downloadDir, "/PiCenterBackup.zip");
        restoreDir = new File(dir, "/Restore");
        if(!dir.exists()){
            dir.mkdirs();
            appUserDir.mkdirs();
            userLogDir.mkdirs();
            readingDir.mkdirs();
            restoreDir.mkdirs();
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void backup(){
        try{
            if(makeCSVSwitch("all")){
                downloadFile = new File(downloadDir, "/PiCenterBackup_" + CalenderConverter.getMonthDayYear(System.currentTimeMillis(), "-" , "-") + ".zip");
                downloadFile.createNewFile();
                List<File> zipFiles = Arrays.asList(downloadFileTempDir.listFiles());
                if(ZipTools.zip(zipFiles, downloadFile.getPath())){
                    System.gc();
                    List<File> files = new ArrayList<>();
                    files.add(downloadFileTempDir);
                }
                System.gc();
                List<File> files = new ArrayList<>();
                files.add(downloadFileTempDir);
                files.add(downloadFile);
                DeleteTools.delete(files);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean generateDownload(String table){
        try{
            return GenerateDownload(table);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreCSV(){
        try{
            return restore();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean restore() throws Exception{
        for(File csv : restoreDir.listFiles()){
            List<DBItem> items = new ArrayList<>();
            String CSV = new String(ByteTools.readBytesFromFile(csv), "UTF-8");
            items = (new CSVParserImpl()).parseToListWithoutId(CSV);
            switch(items.get(0).getType()){
                case "AppUser":
                    for(DBItem item : items){
                        appUserRepository.saveAndFlush((AppUser)item);
                    }
                    break;
                case "Reading":
                    for(DBItem item : items){
                        readingRepository.saveAndFlush((Reading) item);
                    }
                    break;
                case "UserLog":
                    for(DBItem item : items){
                        userLogRepository.saveAndFlush((UserLog) item);
                    }
                    break;
            }
        }
        return DeleteTools.delete(restoreDir.listFiles());
    }

    private boolean GenerateDownload(String table) throws Exception{
        try{
            if(makeCSVSwitch(table)){
                downloadFile.createNewFile();
                List<File> zipFiles = Arrays.asList(downloadFileTempDir.listFiles());
                if(ZipTools.zip(zipFiles, downloadFile.getPath())){
                    List<File> files = new ArrayList<>();
                    files.add(downloadFileTempDir);
                    DeleteTools.delete(files);
                    return true;
                }
                List<File> files = new ArrayList<>();
                files.add(downloadFileTempDir);
                files.add(downloadFile);
                DeleteTools.delete(files);
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            List<File> files = new ArrayList<>();
            files.add(downloadFileTempDir);
            files.add(downloadFile);
            DeleteTools.delete(files);
            return false;
        }
        List<File> files = new ArrayList<>();
        files.add(downloadFileTempDir);
        files.add(downloadFile);
        DeleteTools.delete(files);
        return false;
    }

    private boolean makeCSVSwitch(String table) throws IOException, Exception {
        if(!downloadFileTempDir.exists()){
            downloadFileTempDir.mkdirs();
        }
        File out;
        boolean success = false;
        switch (table.toLowerCase()){
            case "all":
                success = true;
                Future<Boolean>[] futures = new Future[3];
                futures[0] = threadPoolTaskExecutor.submit(new CSVThread("users"));
                futures[1] = threadPoolTaskExecutor.submit(new CSVThread("readings"));
                futures[2] = threadPoolTaskExecutor.submit(new CSVThread("userlogs"));
                boolean temp = futures[0].get();
                if(!temp){
                    success = false;
                }
                temp = futures[1].get();
                if(!temp){
                    success = false;
                }
                temp = futures[2].get();
                if(!temp){
                    success = false;
                }
                break;
            case "users":
                out = new File(downloadFileTempDir, "/Users_" + CalenderConverter.getMonthDayYear(System.currentTimeMillis(), "-", "-") + ".csv");
                out.createNewFile();
                success = makeUserCSV(out);
                break;
            case "readings" :
                out = new File(downloadFileTempDir, "/Readings_" + CalenderConverter.getMonthDayYear(System.currentTimeMillis(), "-", "-") + ".csv");
                out.createNewFile();
                success = makeReadingCSV(out);
                break;
            case "userlogs" :
                out = new File(downloadFileTempDir, "/UserLogs_" + CalenderConverter.getMonthDayYear(System.currentTimeMillis(), "-", "-") + ".csv");
                out.createNewFile();
                success = makeUserLogCSV(out);
                break;
            default:
                break;
        }
        return success;
    }

    private class CSVThread implements Callable<Boolean>{

        private String table;

        CSVThread(String table){
            this.table = table;
        }

        public Boolean call() throws Exception{
            return makeCSVSwitch(table);
        }

    }

    private boolean makeUserCSV(File out){
        return makeCSV(out, new ArrayList<>(appUserRepository.getAll()));
    }

    private boolean makeReadingCSV(File out){
        return makeCSV(out, new ArrayList<>(readingRepository.getAll()));
    }

    private boolean makeUserLogCSV(File out){
        return makeCSV(out, new ArrayList<>(userLogRepository.getAll()));
    }

    private boolean makeCSV(File out, List<DBItem> items) {
        if(!out.exists()){
            return false;
        }
        String csv = parseItemsToCSV(items);
        try{
            File hash = new File(downloadFileTempDir, "/" + out.getName() + ".sha512");
            hash.createNewFile();
            ByteTools.writeBytesToFile(hash, hashCSV(csv));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            ByteTools.writeBytesToFile(out,
                            csv.getBytes("UTF-8")
                    );
            return true;
        }catch (Exception e){
            //Log here
           e.printStackTrace();
           return false;
        }
    }

    private String parseItemsToCSV(List<DBItem> dbItems){
        if(dbItems == null || dbItems.size() == 0){
            return null;
        }
        return new CSVParserImpl().parseToCSV(dbItems);
    }

    private List<DBItem> parseCSVToItemWithId(String CSV){
        return new CSVParserImpl().parseToList(CSV);
    }

    private List<DBItem> parseCSVToItemWithoutId(String CSV){
        return new CSVParserImpl().parseToListWithoutId(CSV);
    }

    private byte[] hashCSV(String csv) throws UnsupportedEncodingException, Exception {
        byte[] hash = new byte[1];
        for(String line : csv.split("\r\n")){
            hash = CryptTools.getSHA512(csv.getBytes("UTF-8"), hash);
        }
        return Base64.getEncoder().encode(hash);
    }

    private boolean compareHash(String hash1, String hash2){
        return hash1.equals(hash2);
    }

}
