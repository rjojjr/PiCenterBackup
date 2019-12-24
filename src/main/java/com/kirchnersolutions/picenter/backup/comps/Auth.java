package com.kirchnersolutions.picenter.backup.comps;

import com.kirchnersolutions.utilities.ByteTools;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class Auth {

    private final File authDir = new File("PiCenter/Backup/Auth");
    private final File auth = new File(authDir, "/auth.auth");

    private String token;

    public Auth() throws Exception{
        if(!authDir.exists()){
            authDir.mkdirs();
        }
        if(!authDir.exists()){
            auth.createNewFile();
        }
    }

    @PostConstruct
    public void init() throws Exception{
        token = new String(ByteTools.readBytesFromFile(auth), "UTF-8");
    }

    public boolean validateToken(String token){
        return this.token.equals(token);
    }

}
